package com.example.leyan_final_project;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        EditText etEmail = root.findViewById(R.id.etEmail);
        EditText etNewPass = root.findViewById(R.id.etNewPass);
        EditText etConfirmPass = root.findViewById(R.id.etConfirmPass);
        Button btnReset = root.findViewById(R.id.btnReset);
        Button btnBack = root.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showFragment(new LoginFragment())
        );

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String p1    = etNewPass.getText().toString();
            String p2    = etConfirmPass.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email"); etEmail.requestFocus(); return;
            }
            if (p1.length() < 6) {
                etNewPass.setError("Password must be ≥ 6"); etNewPass.requestFocus(); return;
            }
            if (!p1.equals(p2)) {
                etConfirmPass.setError("Passwords do not match"); etConfirmPass.requestFocus(); return;
            }

            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase db = AppDatabase.get(requireContext());
                UserDao dao = db.userDao();
                UserEntity u = dao.findByEmailSync(email);
                requireActivity().runOnUiThread(() -> {
                    if (u == null) {
                        Toast.makeText(getContext(), "Email not found", Toast.LENGTH_SHORT).show();
                    } else {
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            dao.updatePassword(email, p1);
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                ((AuthActivity) requireActivity()).showFragment(new LoginFragment());
                            });
                        });
                    }
                });
            });
        });

        return root;
    }
}
