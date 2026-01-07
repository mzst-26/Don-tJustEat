package com.example.dontjusteat.viewMode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dontjusteat.models.RestaurantAvailability;
import com.example.dontjusteat.models.TableAvailability;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerBookingViewModel extends ViewModel {

    private final MutableLiveData<Double> lat = new MutableLiveData<>();
    private final MutableLiveData<Double> lng = new MutableLiveData<>();

    // Store booking search inputs as real values (not strings)
    private final MutableLiveData<Long> startTimeMillis = new MutableLiveData<>(); // chosen date+time
    private final MutableLiveData<Integer> guests = new MutableLiveData<>(2);

    public LiveData<Double> getLat() { return lat; }
    public LiveData<Double> getLng() { return lng; }
    public LiveData<Long> getStartTimeMillis() { return startTimeMillis; }
    public LiveData<Integer> getGuests() { return guests; }
    private final MutableLiveData<String> city = new MutableLiveData<>("");

    public LiveData<String> getCity() { return city; }
    public void setCity(String value) { city.setValue(value); }

    public void setUserLocation(double latVal, double lngVal) {
        lat.setValue(latVal);
        lng.setValue(lngVal);
    }

    public void setStartTimeMillis(long millis) {
        startTimeMillis.setValue(millis);
    }

    public void setGuests(int value) {
        guests.setValue(value);
    }

    public void setAvailabilityResults(List<RestaurantAvailability> results) {
        availabilityResults.setValue(results);
    }

    private final MutableLiveData<List<RestaurantAvailability>> availabilityResults = new MutableLiveData<>();
    public LiveData<List<RestaurantAvailability>> getAvailabilityResults() {
        return availabilityResults;
    }

    // booking states that are specific to the booking
    private final MutableLiveData<String> selectedRestaurantId = new MutableLiveData<>();
    private final MutableLiveData<List<TableAvailability>> tableAvailability = new MutableLiveData<>();


    // Map of tableId to selected Timestamp for that table
    private final Map<String, Timestamp> selectedTableTimes = new HashMap<>();

    public LiveData<String> getSelectedRestaurantId() { return selectedRestaurantId; }
    public LiveData<List<TableAvailability>> getTableAvailability() { return tableAvailability; }

    public void setSelectedRestaurantId(String restaurantId) {
        selectedRestaurantId.setValue(restaurantId);
    }

    public void setTableAvailability(List<TableAvailability> results) {
        tableAvailability.setValue(results);
    }

    public void selectTimeForTable(String tableId, Timestamp time) {
        selectedTableTimes.put(tableId, time);
    }

    public Timestamp getSelectedTimeForTable(String tableId) {
        return selectedTableTimes.get(tableId);
    }

    public Map<String, Timestamp> getSelectedTableTimes() {
        return new HashMap<>(selectedTableTimes);
    }

    public void clearTableSelections() {
        selectedTableTimes.clear();
    }

}
