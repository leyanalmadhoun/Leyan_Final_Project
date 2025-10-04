package com.example.leyan_final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private static final String ADMIN_EMAIL = "admin@foodify.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        EditText etEmail  = root.findViewById(R.id.etEmail);
        EditText etPassword = root.findViewById(R.id.etPassword);
        Button   btnSignIn  = root.findViewById(R.id.btnSignIn);
        TextView tvGoSignUp = root.findViewById(R.id.tvGoSignUp);
        TextView tvForgot = root.findViewById(R.id.tvForgot);
        Button   btnFacebook = root.findViewById(R.id.btnFacebook);
        CheckBox cbRemember = root.findViewById(R.id.cbRemember);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", 0);
        String savedEmail = prefs.getString("remember_email", "");
        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
            if (cbRemember != null) cbRemember.setChecked(true);
        }

        if (tvForgot != null) {
            tvForgot.setOnClickListener(v ->
                    ((AuthActivity) requireActivity()).showFragment(new ForgotPasswordFragment())
            );
        }

        if (btnFacebook != null) {
            btnFacebook.setOnClickListener(v -> {
                try {
                    Intent app = new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/login.php"));
                    app.setPackage("com.facebook.katana");
                    startActivity(app);
                } catch (Exception e) {
                    Intent web = new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.facebook.com/login.php"));
                    startActivity(web);
                }
            });
        }

        tvGoSignUp.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showFragment(new RegisterFragment())
        );

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }
            if (pass.length() < 6) {
                etPassword.setError("Password must be ≥ 6");
                etPassword.requestFocus();
                return;
            }

            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase db   = AppDatabase.get(requireContext());
                UserDao userDao  = db.userDao();
                UserEntity user  = userDao.loginSync(email, pass);

                requireActivity().runOnUiThread(() -> {
                    if (user == null) {
                        Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isAdmin = ADMIN_EMAIL.equalsIgnoreCase(email);
                    String role = isAdmin ? "admin" : "user";

                    prefs.edit()
                            .putBoolean("logged_in", true)
                            .putString("user_email", email)
                            .putString("user_role", role)
                            .putBoolean("is_admin", isAdmin)
                            .putString("remember_email", (cbRemember != null && cbRemember.isChecked()) ? email : "")
                            .apply();

                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();
                });
            });
        });

        return root;
    }
}