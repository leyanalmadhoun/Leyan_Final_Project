package com.example.leyan_final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean logged = prefs.getBoolean("logged_in", false);
        isAdmin = prefs.getBoolean("is_admin", false);
        String role = prefs.getString("user_role", isAdmin ? "admin" : "user");
        isAdmin = isAdmin || "admin".equals(role);

        if (!logged) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_menu) {
                openFragment(new MenuFragment());
                return true;

            } else if (id == R.id.nav_cart) {
                openFragment(new CartFragment());
                return true;

            } else if (id == R.id.nav_orders) {
                openFragment(new OrdersFragment());
                return true;

            } else if (id == R.id.nav_map) {
                Intent i = new Intent(this, MapActivity.class);
                startActivity(i);
                return false;

            } else if (id == R.id.nav_profile) {
                openFragment(new ProfileFragment());
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_menu);
        }

        ColorStateList tint = AppCompatResources.getColorStateList(this, R.color.bottom_nav_tint);
        bottomNav.setItemIconTintList(tint);
        bottomNav.setItemTextColor(tint);
    }

    private void openFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
    }
}
