package com.example.leyan_final_project;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean logged = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("logged_in", false);
        if (logged) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            showFragment(new LoginFragment());
        }
    }

    public void showFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, f)
                .commit();
    }
}
