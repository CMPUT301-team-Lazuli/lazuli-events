package com.example.lazuli_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lazuli_events.home.ComposeNotificationFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI tests for the ComposeNotificationFragment.
 * Verifies that the notification composing interface is correctly displayed.
 */
@RunWith(AndroidJUnit4.class)
public class ComposeNotificationUITest {

    /**
     * Verifies that the message input, notification type spinner,
     * and confirmation button are visible on the screen.
     */
    @Test
    public void testComposeUIElementsAreDisplayed() {
        // launch fragment with material theme
        FragmentScenario.launchInContainer(ComposeNotificationFragment.class, null,
                com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        // check if the message input area is visible
        onView(withId(R.id.edit_address_textfield)).check(matches(isDisplayed()));

        // check if the notification type spinner is visible
        onView(withId(R.id.notification_type_spinner)).check(matches(isDisplayed()));

        // check if the "Confirm and Send" button is visible
        onView(withId(R.id.confirm_send_button)).check(matches(isDisplayed()));

        // check if the checkboxes for recipients are there
        onView(withId(R.id.registered_checkbox)).check(matches(isDisplayed()));
        onView(withId(R.id.waitlisted_checkbox)).check(matches(isDisplayed()));
    }
}