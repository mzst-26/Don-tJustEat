package com.example.dontjusteat.repositories;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.dontjusteat.models.Restaurant;
import com.example.dontjusteat.models.RestaurantAvailability;
import com.example.dontjusteat.models.Table;
import com.example.dontjusteat.models.TableAvailability;
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

    public interface OnAllRestaurantsListener {
        void onSuccess(List<RestaurantAvailability> results);
        void onFailure(String error);
    }

    public interface OnRestaurantFetchListener {
        void onSuccess(Restaurant restaurant);
        void onFailure(String error);
    }

    public interface OnMenuItemsListener {
        void onSuccess(List<com.example.dontjusteat.models.MenuItem> items);
        void onFailure(String error);
    }

    public interface OnTablesListener {
        void onSuccess(List<Table> tables);
        void onFailure(String error);
    }

    public interface OnTableAvailabilityListener {
        void onSuccess(List<TableAvailability> results);
        void onFailure(String error);
    }

    // get a single restaurant by ID
    public void getRestaurantById(String restaurantId, OnRestaurantFetchListener listener) {
        db.collection("restaurants").document(restaurantId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            listener.onSuccess(r);
                        } else {
                            listener.onFailure("Failed to parse restaurant data");
                        }
                    } else {
                        listener.onFailure("Restaurant not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    //get all the restaurants
    public void getAllRestaurants(@NonNull OnAllRestaurantsListener listener) {
        db.collection("restaurants")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(qs -> {
                    List<com.example.dontjusteat.models.Slot> emptySlots = Collections.emptyList();
                    List<RestaurantAvailability> results = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : qs) {
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            results.add(new RestaurantAvailability(r, emptySlots));
                        }
                    }

                    android.util.Log.d("REPO_DEBUG", "Loaded " + results.size() + " restaurants");
                    listener.onSuccess(results);
                })
                .addOnFailureListener(e -> listener.onFailure(msg(e, "Failed to load restaurants")));
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
                    android.util.Log.d("REPO_DEBUG", "Found " + qs.size() + " active restaurants total");
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
                        android.util.Log.d("REPO_DEBUG", "  Checking: " + r.getName() + " (address: " + addr + ")");

                        // check if the address contains the location text
                        if (containsIgnoreCase(addr, locationText)) {
                            candidates.add(r);
                            Log.d("REPO_DEBUG", "    ✓ Matched location");
                        } else {
                            Log.d("REPO_DEBUG", "    ✗ Location mismatch");
                        }
                    }
                    // used for debugging
                    android.util.Log.d("REPO_DEBUG", "Location filter: " + candidates.size() + " restaurants match");

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

                    Tasks.whenAllSuccess(lockTasks)
                            .addOnSuccessListener(lockObjects -> {
                                List<com.example.dontjusteat.models.Slot> slots =
                                        buildAvailableSlots(startMs, endMs, slotMinutes, durationMinutes, maxSlots, tables, lockObjects);

                                tcs.setResult(new RestaurantAvailability(r, slots));
                            })
                            .addOnFailureListener(e -> tcs.setResult(new RestaurantAvailability(r, Collections.emptyList())));

                })
                .addOnFailureListener(e -> tcs.setResult(new RestaurantAvailability(r, Collections.emptyList())));

        return tcs.getTask();
    }

    //load locks for one table
    private Task<TableLocks> loadLocksForTable(String restaurantId, String tableId, long startMs, long endMs) {
        TaskCompletionSource<TableLocks> tcs = new TaskCompletionSource<>();

        db.collection("restaurants").document(restaurantId)
                .collection("tables").document(tableId)
                .collection("locks")
                .whereGreaterThanOrEqualTo("startTime", new Timestamp(new Date(startMs)))
                .whereLessThan("startTime", new Timestamp(new Date(endMs)))
                .get()
                .addOnSuccessListener(qs -> {
                    Set<Long> lockedSlotStarts = new HashSet<>();
                    // for each document in the list
                    for (QueryDocumentSnapshot doc : qs) {
                        String status = doc.getString("status");
                        // Treat anything that is not OPEN as locked (held/booked)
                        if (status != null && !status.equalsIgnoreCase("OPEN")) {
                            Timestamp ts = doc.getTimestamp("startTime");
                            if (ts != null) lockedSlotStarts.add(ts.toDate().getTime());
                        }
                    }
                    tcs.setResult(new TableLocks(tableId, lockedSlotStarts));
                })
                .addOnFailureListener(e -> tcs.setResult(new TableLocks(tableId, Collections.emptySet())));

        return tcs.getTask();
    }

    // fetch menu items for a specific restaurant
    public void getMenuItemsByRestaurantId(String restaurantId, OnMenuItemsListener listener) {
        db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .addOnSuccessListener(qs -> {
                    List<com.example.dontjusteat.models.MenuItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        com.example.dontjusteat.models.MenuItem item = doc.toObject(com.example.dontjusteat.models.MenuItem.class);
                        if (item != null) {
                            item.setItemId(doc.getId());
                            items.add(item);
                        }
                    }
                    listener.onSuccess(items);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // fetch all active tables for a restaurant, optionally filtered by capacity
    public void getTablesForRestaurant(String restaurantId, OnTablesListener listener) {

        db.collection("restaurants").document(restaurantId)
                .collection("tables")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Table> tables = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : qs)
                    {
                        Long cap = doc.getLong("capacity");
                        Boolean active = doc.getBoolean("active");
                        if (cap != null && active != null && active) {
                            Table t = new Table(doc.getId(), cap.intValue(), true);
                            tables.add(t);
                        }

                    }

                    listener.onSuccess(tables);

                })
                .addOnFailureListener(e -> listener.onFailure(msg(e, "Failed to load tables")));
    }

    // fetch availability for specific tables (not merged)
    // each table gets its own list of available slots
    public void getTableAvailability(
            String restaurantId,
            List<String> tableIds,
            Timestamp requestedAfter,
            int defaultDurationMinutes,
            int defaultSlotMinutes,
            OnTableAvailabilityListener listener
    )
    {
        if (tableIds == null || tableIds.isEmpty()) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        long requestedMs = requestedAfter.toDate().getTime();
        long startMs = roundUpToSlot(requestedMs, defaultSlotMinutes);
        long endMs = endOfDayMillis(startMs);
        long durationMs = TimeUnit.MINUTES.toMillis(defaultDurationMinutes);
        long slotMs = TimeUnit.MINUTES.toMillis(defaultSlotMinutes);



        // fetch all tables first
        db.collection("restaurants").document(restaurantId)
                .collection("tables")
                .whereIn(FieldPath.documentId(), tableIds)
                .get()
                .addOnSuccessListener(tableSnap -> {
                    Map<String, Table> tableMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : tableSnap) {
                        Long cap = doc.getLong("capacity");
                        Boolean active = doc.getBoolean("active");
                        if (cap != null && active != null) {
                            Table t = new Table(doc.getId(), cap.intValue(), active);
                            tableMap.put(doc.getId(), t);
                        }
                    }

                    // load locks for each table
                    List<Task<TableLocks>> lockTasks = new ArrayList<>();
                    for (String tableId : tableIds) {
                        lockTasks.add(loadLocksForTable(restaurantId, tableId, startMs, endMs));
                    }

                    Tasks.whenAllSuccess(lockTasks)
                            .addOnSuccessListener(lockObjects -> {
                                List<TableAvailability> results = new ArrayList<>();

                                // compute availability per table
                                for (String tableId : tableIds) {
                                    Table t = tableMap.get(tableId);
                                    if (t == null) continue;



                                    Set<Long> locked = Collections.emptySet();
                                    for (Object o : lockObjects) {
                                        if (o instanceof TableLocks) {
                                            TableLocks tl = (TableLocks) o;
                                            if (tl.tableId.equals(tableId)) {
                                                locked = (tl.lockedSlotStarts != null) ? tl.lockedSlotStarts : Collections.emptySet();
                                                break;
                                            }
                                        }
                                    }



                                    // build slots for this table only
                                    List<com.example.dontjusteat.models.Slot> tableSlots = new ArrayList<>();
                                    for (long time = startMs; time + durationMs <= endMs; time += slotMs) {
                                        if (!isLockedForWindow(locked, time, durationMs, slotMs)) {
                                            com.example.dontjusteat.models.Slot s = new com.example.dontjusteat.models.Slot(
                                                    new Timestamp(new Date(time)),
                                                    new Timestamp(new Date(time + durationMs))
                                            );
                                            tableSlots.add(s);
                                        }
                                    }

                                    results.add(new TableAvailability(t, tableSlots));
                                }

                                listener.onSuccess(results);
                            })
                            .addOnFailureListener(e -> listener.onFailure(msg(e, "Failed to load locks")));
                })

                .addOnFailureListener(e -> listener.onFailure(msg(e, "Failed to load tables")));
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


    private static List<com.example.dontjusteat.models.Slot> buildAvailableSlots(
            long startMs,
            long endMs,
            int slotMinutes,
            int durationMinutes,
            int maxSlots,
            List<TableRef> tables,
            List<Object> lockObjects
    ) {
        Map<String, Set<Long>> lockedByTable = new HashMap<>();

        for (Object o : lockObjects) {
            if (o instanceof TableLocks) {
                TableLocks tl = (TableLocks) o;
                lockedByTable.put(
                        tl.tableId,
                        (tl.lockedSlotStarts != null) ? tl.lockedSlotStarts : Collections.emptySet()
                );
            }
        }

        long slotMs = TimeUnit.MINUTES.toMillis(slotMinutes);
        long durationMs = TimeUnit.MINUTES.toMillis(durationMinutes);

        int limit = (maxSlots <= 0) ? Integer.MAX_VALUE : maxSlots;

        List<com.example.dontjusteat.models.Slot> out = new ArrayList<>();

        for (long t = startMs; t + durationMs <= endMs; t += slotMs) {

            boolean hasAnyTable = false;

            for (TableRef tr : tables) {
                Set<Long> locked = lockedByTable.get(tr.tableId);
                if (locked == null) locked = Collections.emptySet();

                if (!isLockedForWindow(locked, t, durationMs, slotMs)) {
                    Timestamp st = new Timestamp(new Date(t));
                    Timestamp en = new Timestamp(new Date(t + durationMs));

                    com.example.dontjusteat.models.Slot s = new com.example.dontjusteat.models.Slot(st, en);
                    trySetField(s, "tableId", tr.tableId);

                    out.add(s);
                    hasAnyTable = true;
                    break;
                }

            }

            if (hasAnyTable && out.size() >= limit) break;
        }

        return out;
    }

    private static boolean isLockedForWindow(Set<Long> lockedSlotStarts, long startMs, long durationMs, long slotMs) {
        long endExclusive = startMs + durationMs;
        for (long t = startMs; t < endExclusive; t += slotMs) {
            if (lockedSlotStarts.contains(t)) return true;
        }
        return false;
    }

    private static void trySetField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ignored) {
        }
    }

}
