package com.example.dontjusteat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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


}
