package com.example.leyan_final_project;

import android.content.Intent;
import android.os.Bundle;
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

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        EditText etEmail = root.findViewById(R.id.etRegEmail);
        EditText etPass  = root.findViewById(R.id.etRegPassword);
        EditText etMobile = root.findViewById(R.id.etRegMobile);
        CheckBox cbTerms = root.findViewById(R.id.cbTerms);
        Button btnSignUp = root.findViewById(R.id.btnSignUp);
        TextView tvGoSignIn= root.findViewById(R.id.tvGoSignIn);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString();
            String mob = etMobile.getText().toString().trim();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email"); etEmail.requestFocus(); return;
            }
            if (pass.length() < 6) {
                etPass.setError("Password must be ≥ 6"); etPass.requestFocus(); return;
            }
            if (mob.isEmpty()) {
                etMobile.setError("Required"); etMobile.requestFocus(); return;
            }
            if (cbTerms != null && !cbTerms.isChecked()) {
                Toast.makeText(getContext(), "You must accept terms", Toast.LENGTH_SHORT).show();
                return;
            }

            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase db = AppDatabase.get(requireContext());
                UserDao userDao = db.userDao();

                UserEntity exists = userDao.findByEmailSync(email);
                if (exists != null) {
                    requireActivity().runOnUiThread(() -> {
                        etEmail.setError("Email already exists");
                        etEmail.requestFocus();
                    });
                    return;
                }

                userDao.insert(new UserEntity(email, pass, "", mob, null));

                requireActivity().runOnUiThread(() -> {
                    requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE)
                            .edit()
                            .putBoolean("logged_in", true)
                            .putString("user_email", email)
                            .putString("user_name",  "Guest")
                            .putString("user_phone", mob)
                            .putString("user_image", null)
                            .apply();

                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();
                });
            });
        });

        tvGoSignIn.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showFragment(new LoginFragment()));

        return root;
    }
}
