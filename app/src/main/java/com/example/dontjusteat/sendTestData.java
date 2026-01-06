package com.example.dontjusteat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.GeoPoint;

import java.time.*;
import java.util.*;

public class sendTestData {

    public static Task<Void> seedPlymouthStable(FirebaseFirestore db) {
        final String restaurantId = "Sfs5ORbFFb7NSkSNaJS";

        final int slotMinutes = 15;
        final int defaultDurationMinutes = 90;

        ZoneId zone = ZoneId.of("Europe/London");
        LocalDate day = LocalDate.now(zone);

        /* ---------------- RESTAURANT ---------------- */

        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("name", "The Plymouth Stable");
        restaurant.put("address", "90 Little Vauxhall Quay, Vauxhall Street, The Barbican, Plymouth PL4 0EY");
        restaurant.put("phone", "01752 228 069");
        restaurant.put("isActive", true);
        restaurant.put("defaultDurationMinutes", defaultDurationMinutes);
        restaurant.put("slotMinutes", slotMinutes);
        restaurant.put("location", new GeoPoint(50.36866698688542, -4.134821852026594));
        restaurant.put("locationURL",
                "https://www.google.com/maps/place/The+Plymouth+Stable/@50.3685695,-4.1361396,17.56z/");

        // Restaurant image (stable placeholder for tests)
        restaurant.put("imageUrl", "https://picsum.photos/seed/plymouth-stable-restaurant/1200/800");

        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);

        WriteBatch batch = db.batch();
        batch.set(restaurantRef, restaurant);

        /* ---------------- TABLES (20) ---------------- */

        CollectionReference tablesCol = restaurantRef.collection("tables");
        List<String> tableIds = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            String tableId = String.format(Locale.UK, "t%02d", i);
            tableIds.add(tableId);

            int capacity;
            if (i <= 8) capacity = 2;
            else if (i <= 16) capacity = 4;
            else capacity = 6;

            Map<String, Object> table = new HashMap<>();
            table.put("active", true);
            table.put("capacity", capacity);

            batch.set(tablesCol.document(tableId), table);
        }

        /* ---------------- TABLE LOCKS (11:30–22:00 every 15 mins) ---------------- */

        LocalTime start = LocalTime.of(11, 30);
        LocalTime end = LocalTime.of(22, 0);

        for (String tableId : tableIds) {
            CollectionReference locksCol = tablesCol.document(tableId).collection("locks");

            LocalTime t = start;
            while (t.isBefore(end)) {
                Timestamp ts = tsAt(zone, day, t);

                Map<String, Object> lock = new HashMap<>();
                lock.put("startTime", ts);
                lock.put("status", "OPEN"); // change to your expected values if needed

                String lockDocId = String.format(Locale.UK, "%04d%02d%02d_%02d%02d",
                        day.getYear(), day.getMonthValue(), day.getDayOfMonth(),
                        t.getHour(), t.getMinute());

                batch.set(locksCol.document(lockDocId), lock);
                t = t.plusMinutes(slotMinutes);
            }
        }

        /* ---------------- REVIEWS (7 total, some with photos) ---------------- */

        CollectionReference reviewsCol = restaurantRef.collection("reviews");

        batch.set(reviewsCol.document("r01"), makeReview(
                "user_anna", "", "Great pizza and cider selection. Nice views over the water.",
                "5", "https://picsum.photos/seed/rev-anna/900/600"));

        batch.set(reviewsCol.document("r02"), makeReview(
                "user_mobin", "", "Good atmosphere, service was quick. Would come again.",
                "4", ""));

        batch.set(reviewsCol.document("r03"), makeReview(
                "user_sam", "", "Tasty food, a bit busy at peak times but worth it.",
                "4", "https://picsum.photos/seed/rev-sam/900/600"));

        batch.set(reviewsCol.document("r04"), makeReview(
                "user_leila", "", "Cider options are excellent. Pizza base was spot on.",
                "5", ""));

        batch.set(reviewsCol.document("r05"), makeReview(
                "user_james", "", "Nice location by the harbour. Food was solid.",
                "4", ""));

        batch.set(reviewsCol.document("r06"), makeReview(
                "user_sara", "", "Loved the cider selection. Pizza was very good.",
                "5", "https://picsum.photos/seed/rev-sara/900/600"));

        batch.set(reviewsCol.document("r07"), makeReview(
                "user_tom", "", "Good value and friendly staff.",
                "4", ""));

        /* ---------------- MENU (10 items with REAL images) ---------------- */

        CollectionReference menuCol = restaurantRef.collection("menu");

        batch.set(menuCol.document("m01"), makeMenuItem(
                "Margherita Pizza",
                "Classic tomato sauce, mozzarella, fresh basil",
                11.50,
                "https://upload.wikimedia.org/wikipedia/commons/4/4b/Margherita_Originale.JPG"
        ));

        batch.set(menuCol.document("m02"), makeMenuItem(
                "Pepperoni Pizza",
                "Tomato base, mozzarella, spicy pepperoni",
                13.00,
                "https://upload.wikimedia.org/wikipedia/commons/d/d1/Pepperoni_pizza.jpg"
        ));

        batch.set(menuCol.document("m03"), makeMenuItem(
                "BBQ Chicken Pizza",
                "BBQ sauce, chicken, red onion, mozzarella",
                14.00,
                "https://upload.wikimedia.org/wikipedia/commons/3/3e/BBQ_chicken_pizza.jpg"
        ));

        batch.set(menuCol.document("m04"), makeMenuItem(
                "Vegan Veggie Pizza",
                "Roasted vegetables, tomato sauce, vegan cheese",
                13.50,
                "https://upload.wikimedia.org/wikipedia/commons/9/91/Vegan_pizza.jpg"
        ));

        batch.set(menuCol.document("m05"), makeMenuItem(
                "Garlic Bread",
                "Stone-baked garlic bread with herbs",
                5.50,
                "https://upload.wikimedia.org/wikipedia/commons/3/3a/Garlic_bread.jpg"
        ));

        batch.set(menuCol.document("m06"), makeMenuItem(
                "Loaded Fries",
                "Skin-on fries with cheese and house sauce",
                6.50,
                "https://upload.wikimedia.org/wikipedia/commons/6/67/Cheese_fries.jpg"
        ));

        batch.set(menuCol.document("m07"), makeMenuItem(
                "Caesar Salad",
                "Romaine, parmesan, croutons, Caesar dressing",
                8.00,
                "https://upload.wikimedia.org/wikipedia/commons/2/23/Caesar_salad_%282%29.jpg"
        ));

        batch.set(menuCol.document("m08"), makeMenuItem(
                "Halloumi Bites",
                "Crispy fried halloumi with chilli jam",
                7.00,
                "https://upload.wikimedia.org/wikipedia/commons/7/7f/Fried_halloumi_cheese.jpg"
        ));

        batch.set(menuCol.document("m09"), makeMenuItem(
                "Chocolate Brownie",
                "Warm chocolate brownie with vanilla ice cream",
                6.00,
                "https://upload.wikimedia.org/wikipedia/commons/6/68/Chocolatebrownie.JPG"
        ));

        batch.set(menuCol.document("m10"), makeMenuItem(
                "Classic Cider",
                "Dry West Country cider (pint)",
                5.80,
                "https://upload.wikimedia.org/wikipedia/commons/3/36/Glass_of_cider.jpg"
        ));


        /* ---------------- BOOKINGS (5) ---------------- */

        CollectionReference bookingsCol = restaurantRef.collection("bookings");

        // Choose tables with enough capacity and non-overlapping-ish times
        // (Your app can still validate conflicts; this is just realistic test data)
        batch.set(bookingsCol.document("b01"), makeBooking(
                day, zone,
                LocalTime.of(12, 0),
                defaultDurationMinutes,
                2,
                "CONFIRMED",
                "t01",
                "user_anna"
        ));

        batch.set(bookingsCol.document("b02"), makeBooking(
                day, zone,
                LocalTime.of(13, 30),
                defaultDurationMinutes,
                4,
                "CONFIRMED",
                "t10",
                "user_mobin"
        ));

        batch.set(bookingsCol.document("b03"), makeBooking(
                day, zone,
                LocalTime.of(15, 0),
                defaultDurationMinutes,
                2,
                "CONFIRMED",
                "t02",
                "user_sara"
        ));

        batch.set(bookingsCol.document("b04"), makeBooking(
                day, zone,
                LocalTime.of(18, 0),
                defaultDurationMinutes,
                6,
                "CONFIRMED",
                "t18",
                "user_sam"
        ));

        batch.set(bookingsCol.document("b05"), makeBooking(
                day, zone,
                LocalTime.of(20, 0),
                defaultDurationMinutes,
                4,
                "CONFIRMED",
                "t12",
                "user_leila"
        ));

        return batch.commit();
    }

    /* ---------------- HELPERS ---------------- */

    private static Timestamp tsAt(ZoneId zone, LocalDate day, LocalTime time) {
        ZonedDateTime zdt = ZonedDateTime.of(day, time, zone);
        return new Timestamp(Date.from(zdt.toInstant()));
    }

    private static Map<String, Object> makeReview(
            String userId,
            String bookingId,
            String body,
            String rating,
            String photoUrl
    ) {
        Map<String, Object> r = new HashMap<>();
        r.put("userId", userId);
        r.put("bookingId", bookingId);
        r.put("body", body);
        r.put("rating", rating);   // schema: string
        r.put("photoUrl", photoUrl == null ? "" : photoUrl);
        r.put("createdAt", Timestamp.now());
        return r;
    }

    private static Map<String, Object> makeMenuItem(
            String name,
            String description,
            double price,
            String imageURL
    ) {
        Map<String, Object> m = new HashMap<>();
        m.put("itemName", name);
        m.put("itemDes", description);
        m.put("price", price);
        m.put("imageURL", imageURL == null ? "" : imageURL);
        return m;
    }

    private static Map<String, Object> makeBooking(
            LocalDate day,
            ZoneId zone,
            LocalTime startTime,
            int durationMinutes,
            int partySize,
            String status,
            String tableId,
            String userId
    ) {
        Timestamp start = tsAt(zone, day, startTime);
        Timestamp end = tsAt(zone, day, startTime.plusMinutes(durationMinutes));

        Map<String, Object> b = new HashMap<>();
        b.put("createdAt", Timestamp.now());
        b.put("durationMinutes", durationMinutes);
        b.put("startTime", start);
        b.put("endTime", end);
        b.put("partySize", partySize);
        b.put("status", status);
        b.put("tableId", tableId);
        b.put("userId", userId);
        return b;
    }
}
