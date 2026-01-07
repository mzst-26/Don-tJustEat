package com.example.dontjusteat.repositories;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;




public class ReviewRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OnReviewCheckListener {
        void hasReview(boolean reviewed);
    }



    public interface OnReviewSubmitListener {
        void onSuccess();
        void onFailure(String error);
    }




    // check if user already reviewed this booking
    public void checkIfReviewed(String restaurantId, String bookingId, OnReviewCheckListener listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            listener.hasReview(false);
            return;
        }


        db.collection("restaurants").document(restaurantId)
                .collection("reviews")
                .whereEqualTo("userId", uid)
                .whereEqualTo("bookingId", bookingId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    listener.hasReview(snapshot != null && !snapshot.isEmpty());
                })
                .addOnFailureListener(e -> listener.hasReview(false));
    }




    // submit review to firestore
    public void submitReview(String restaurantId, String bookingId, int rating, String description, OnReviewSubmitListener listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            listener.onFailure("Not logged in");
            return;
        }


        // create review doc
        java.util.Map<String, Object> review = new java.util.HashMap<>();
        review.put("userId", uid);
        review.put("bookingId", bookingId);
        review.put("rating", rating);
        review.put("description", description);
        review.put("createdAt", Timestamp.now());



        db.collection("restaurants").document(restaurantId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }



}

