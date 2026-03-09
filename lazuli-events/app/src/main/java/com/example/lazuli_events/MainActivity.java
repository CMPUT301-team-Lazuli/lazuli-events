package com.example.lazuli_events;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


// this is the entry point of the application, using a single activity model.
// NavGraphActivity == MainActivity

// TODO: set the session user (maybe as a singleton ... idek ...)
public class MainActivity extends AppCompatActivity {

    public NavController navController;
    NavHostFragment navHostFragment;
//    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nav_graph);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get the controller of the navhost. navhost is the UI view with navigation buttons
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host);
        navController = navHostFragment.getNavController();






        // set listeners for the bottom bar menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.menu_item_home) {
                    Log.d("clicked", String.valueOf(itemId));
//                    setCurrentFragment(R.id.action_userEventsFragment_to_userProfileFragment);
                    setCurrentFragment(R.id.userEventsFragment);  // these are the ids from the nav graph xml
                } else if (itemId == R.id.menu_item_profile) {
                    setCurrentFragment(R.id.userProfileFragment);
                } else if (itemId == R.id.menu_item_explore) {
                    setCurrentFragment(R.id.userExploreEventsFragment);
                } else if (itemId == R.id.menu_item_notifications) {
                    setCurrentFragment(R.id.userNotificationsFragment);
                }
                return true;
            }
        });
    }



    // this switches out the current fragment to be displayed
    // invoked when navigating with ui buttons
    private void setCurrentFragment(int resourceId) {
        navController.navigate(resourceId);
    }

    // getter
    public Fragment getCurrentFragment() {
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

    }
}