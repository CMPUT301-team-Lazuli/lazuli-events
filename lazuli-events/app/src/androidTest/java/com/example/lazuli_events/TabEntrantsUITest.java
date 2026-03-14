package com.example.lazuli_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lazuli_events.home.TabEntrantsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI tests for the TabEntrantsFragment.
 * Verifies that the UI components are displayed correctly to the user.
 */
@RunWith(AndroidJUnit4.class)
public class TabEntrantsUITest {

    /**
     * Launches the fragment and checks if the main UI elements
     * (ListView, Delete Button, Select All Checkbox) are visible on the screen.
     */
    @Test
    public void testUIElementsAreDisplayed() {
        // launch the fragment directly in a testing container
        FragmentScenario.launchInContainer(TabEntrantsFragment.class, null, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        // checks if the entrant listview is visible
        onView(withId(R.id.entrant_tab_listView)).check(matches(isDisplayed()));

        // checks if the delete button is visible
        onView(withId(R.id.delete_entrants_button)).check(matches(isDisplayed()));

        // checks if the select all checkbox is visible
        onView(withId(R.id.select_all_checkbox)).check(matches(isDisplayed()));
    }
}