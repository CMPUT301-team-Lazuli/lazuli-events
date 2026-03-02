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
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;

public class NavGraphActivity extends AppCompatActivity {

    NavController navController;
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
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host);
        navController = navHostFragment.getNavController();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
//        NavigationBarView bottomNavigationView = (NavigationBarView) findViewById(R.id.bottom_navigation_view);
//        bottomNavigationView.bringToFront();
//
//        bottomNavigationView.OnItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.menu_item_home) {
                    Log.d("clicked", String.valueOf(itemId));
//                    setCurrentFragment(R.id.action_userEventsFragment_to_userProfileFragment);  // these are the ids from the nav graph xml
                    setCurrentFragment(R.id.userEventsFragment);
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
}