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

import com.example.lazuli_events.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

// this is the entry point of the application, using a single activity model.
// NavGraphActivity == MainActivity

// TODO: replace dummy session user with real logged-in profile
public class MainActivity extends AppCompatActivity {

    public NavController navController;
    public FirebaseDB firebaseDB;
    public Profile sessionProfile;
    private final String profileBundleKey = "sessionProfile";
    NavHostFragment navHostFragment;

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

        firebaseDB = new FirebaseDB();

        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_nav_host);

        if (navHostFragment == null) {
            return;
        }

        navController = navHostFragment.getNavController();

        ArrayList<String> dummyProfileEventStrings = new ArrayList<>();
        dummyProfileEventStrings.add("event1");
        dummyProfileEventStrings.add("event2");

        sessionProfile = new Profile(
                "dummy",
                "dummy@gmail.com",
                "123-4567",
                "deviceId",
                "all",
                dummyProfileEventStrings
        );

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.menu_item_home) {
                    Log.d("clicked", String.valueOf(itemId));
                    setCurrentFragment(R.id.userEventsFragment);
                } else if (itemId == R.id.menu_item_profile) {
                    Bundle profileBundle = new Bundle();
                    profileBundle.putSerializable(profileBundleKey, sessionProfile);
                    setCurrentFragmentWithBundle(R.id.userProfileFragment, profileBundle);
                } else if (itemId == R.id.menu_item_explore) {
                    setCurrentFragment(R.id.userExploreEventsFragment);
                } else if (itemId == R.id.menu_item_notifications) {
                    setCurrentFragment(R.id.userNotificationsFragment);
                }
                return true;
            }
        });
    }

    private void setCurrentFragment(int resourceId) {
        navController.navigate(resourceId);
    }

    private void setCurrentFragmentWithBundle(int resourceId, Bundle bundle) {
        navController.navigate(resourceId, bundle);
    }

    public Fragment getCurrentFragment() {
        return navHostFragment == null
                ? null
                : navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
    }
}