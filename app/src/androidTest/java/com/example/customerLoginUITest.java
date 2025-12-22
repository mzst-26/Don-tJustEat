package com.example;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dontjusteat.R;
import com.example.dontjusteat.customer_login;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class customerLoginUITest {


    @Test
    public void backButton_closesActivity() {
        ActivityScenario.launch(customer_login.class);

        onView(withId(R.id.back_button)).perform(click());

        onView(withId(R.id.rootView)).check(doesNotExist());
    }
}
