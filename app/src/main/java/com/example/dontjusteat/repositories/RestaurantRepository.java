package com.example.dontjusteat.repositories;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.dontjusteat.models.Restaurant;
import com.example.dontjusteat.models.RestaurantAvailability;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RestaurantRepository {

    private final FirebaseFirestore db;

    public RestaurantRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface OnAvailabilitySearchListener {
        void onSuccess(List<RestaurantAvailability> results);
        void onFailure(String error);
    }

    // search restaurants in specific location that have at least 1 table for guests
    // and return available slots after "requestedAfter".
    public void searchAvailableRestaurants(
            @NonNull String locationText,
            @NonNull Timestamp requestedAfter,
            int guests,
            int maxSlotsPerRestaurant,
            @NonNull OnAvailabilitySearchListener listener
    )

    {
         // used this for debugging
//        android.util.Log.d("REPO_DEBUG", "Search started: location=" + locationText + ", guests=" + guests + ", after=" + requestedAfter.toDate());
        // search through restaurants that are active
        db.collection("restaurants")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(qs -> {

                        // used this for debugging
//                    android.util.Log.d("REPO_DEBUG", "Found " + qs.size() + " active restaurants total");
                    // filter restaurants by location
                    List<Restaurant> candidates = new ArrayList<>();

                    // for every restaurant in the list
                    for (QueryDocumentSnapshot doc : qs) {
                        // get the restaurant object
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r == null) continue;
                        // set the id of the restaurant within the model
                        r.setId(doc.getId());

                        // match address contains city/location text
                        String addr = r.getAddress();

                        // used for debugging
//                        android.util.Log.d("REPO_DEBUG", "  Checking: " + r.getName() + " (address: " + addr + ")");

                        // check if the address contains the location text
                        if (containsIgnoreCase(addr, locationText)) {
                            candidates.add(r);
                            Log.d("REPO_DEBUG", "    ✓ Matched location");
                        } else {
                            Log.d("REPO_DEBUG", "    ✗ Location mismatch");
                        }
                    }
                    // used for debugging
//                    android.util.Log.d("REPO_DEBUG", "Location filter: " + candidates.size() + " restaurants match");

                    // create a list of tasks for each restaurant
                    List<Task<RestaurantAvailability>> tasks = new ArrayList<>();

                    for (Restaurant r : candidates) {
                        // check availability for each restaurant
                        tasks.add(computeAvailabilityForRestaurant(r, requestedAfter, guests, maxSlotsPerRestaurant));
                    }

                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(allObjects -> {
                                //list of restaurants with available slots
                                List<RestaurantAvailability> out = new ArrayList<>();
                                //
                                for (Object o : allObjects) {
                                    if (o instanceof RestaurantAvailability) {
                                        // cast the object to a RestaurantAvailability object
                                        RestaurantAvailability restaurantAvailability = (RestaurantAvailability) o;

                                        if (restaurantAvailability.slots != null && !restaurantAvailability.slots.isEmpty()) {
                                            out.add(restaurantAvailability);
                                            // used for debugging
                                            // android.util.Log.d("REPO_DEBUG", "  ✓ " + restaurantAvailability.restaurant.getName() + " has " + restaurantAvailability.slots.size() + " slots");
                                        } else {
                                             android.util.Log.d("REPO_DEBUG", "  ✗ " + restaurantAvailability.restaurant.getName() + " has no available slots");
                                        }
                                    }
                                }

                                //sort restaurants by earliest available slot
                                out.sort(Comparator.comparing(a -> a.slots.get(0).startTime.toDate()));
                                //used for debugging
                                //Log.d("REPO_DEBUG", "Final result: " + out.size() + " restaurants with availability");
                                listener.onSuccess(out);
                            })

                            .addOnFailureListener(e -> {
                                Log.e("REPO_DEBUG", "Availability computation failed", e);
                                listener.onFailure(msg(e, "Availability search failed"));
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("REPO_DEBUG", "Failed to load restaurants", e);
                    listener.onFailure(msg(e, "Failed to load restaurants"));
                });
    }

    //the main computation for one restaurant
    private Task<RestaurantAvailability> computeAvailabilityForRestaurant(
            @NonNull Restaurant r,
            @NonNull Timestamp requestedAfter,
            int guests,
            int maxSlots
    ) {

        TaskCompletionSource<RestaurantAvailability> tcs = new TaskCompletionSource<>();
        // set the default values
        int durationMinutes = safeInt(r.getDefaultDurationMinutes(), 90);
        int slotMinutes = safeInt(r.getSlotMinutes(), 15);
        // convert the timestamp to milliseconds
        long requestedMs = requestedAfter.toDate().getTime();
        // round up to the nearest slot
        long startMs = roundUpToSlot(requestedMs, slotMinutes);

        //until end of the same day (local device time)
        long endMs = endOfDayMillis(startMs);

        //Load tables that can fit guests
        db.collection("restaurants").document(r.getId())

                .collection("tables")
                .whereGreaterThanOrEqualTo("capacity", guests)
                .get()

                .addOnSuccessListener(tableSnap -> {
                    if (tableSnap.isEmpty()) {
                        // no tables found for this restaurant
                        tcs.setResult(new RestaurantAvailability(r, Collections.emptyList()));
                        return;
                    }
                    // lise of tables that can fit guests
                    List<TableRef> tables = new ArrayList<>();

                    // for each table document in the list
                    for (QueryDocumentSnapshot tdoc : tableSnap) {
                        // get the table capacity
                        Long cap = tdoc.getLong("capacity");
                        // check if the capacity is valid
                        if (cap == null) continue;
                        // add the table to the list
                        tables.add(new TableRef(tdoc.getId(), cap.intValue()));
                    }

                    // For each table, load locks in the window
                    List<Task<TableLocks>> lockTasks = new ArrayList<>();

                    for (TableRef tr : tables) {
                        // load locks for each table
                        // add the task to the list
                        lockTasks.add(loadLocksForTable(r.getId(), tr.tableId, startMs, endMs));
                    }

                })
                .addOnFailureListener(e -> tcs.setResult(new RestaurantAvailability(r, Collections.emptyList())));

        return tcs.getTask();
    }

    //load locks for one table
    private Task<TableLocks> loadLocksForTable(String restaurantId, String tableId, long startMs, long endMs) {
        TaskCompletionSource<TableLocks> tcs = new TaskCompletionSource<>();

        db.collection("restaurants").document(restaurantId)
                .collection("tables").document(tableId)
                .collection("lock")
                .whereEqualTo("status", "held")
                .whereGreaterThanOrEqualTo("startTime", new Timestamp(new Date(startMs)))
                .whereLessThan("startTime", new Timestamp(new Date(endMs)))
                .get()
                .addOnSuccessListener(qs -> {
                    // list of locked start times
                    Set<Long> lockedSlotStarts = new HashSet<>();
                    // for each document in the list
                    for (QueryDocumentSnapshot doc : qs) {
                        Timestamp ts = doc.getTimestamp("startTime");
                        if (ts != null) lockedSlotStarts.add(ts.toDate().getTime());
                    }
                    tcs.setResult(new TableLocks(tableId, lockedSlotStarts));
                })
                .addOnFailureListener(e -> tcs.setResult(new TableLocks(tableId, Collections.emptySet())));

        return tcs.getTask();
    }


    // small structures as helpers
    private static class TableRef {
        final String tableId;
        final int capacity;
        TableRef(String tableId, int capacity) { this.tableId = tableId; this.capacity = capacity; }
    }

    private static class TableLocks {
        final String tableId;
        final Set<Long> lockedSlotStarts;
        TableLocks(String tableId, Set<Long> lockedSlotStarts) {
            this.tableId = tableId;
            this.lockedSlotStarts = lockedSlotStarts;
        }
    }


    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (needle == null || needle.trim().isEmpty()) return true;
        if (haystack == null) return false;
        return haystack.toLowerCase().contains(needle.trim().toLowerCase());
    }

    private static int safeInt(Integer v, int def) {
        return v == null ? def : v;
    }

    private static long roundUpToSlot(long timeMs, int slotMinutes) {
        long slotMs = TimeUnit.MINUTES.toMillis(slotMinutes);
        long rem = timeMs % slotMs;
        return rem == 0 ? timeMs : (timeMs + (slotMs - rem));
    }

    private static long endOfDayMillis(long anyMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anyMs);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private static String msg(Exception e, String fallback) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : fallback;
    }
}
