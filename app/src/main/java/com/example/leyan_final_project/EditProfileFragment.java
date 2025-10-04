package com.example.leyan_final_project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EditProfileFragment extends Fragment {

    private EditText etName, etEmail, etPhone;
    private Button btnSave;

    public EditProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        etName  = root.findViewById(R.id.etName);
        etEmail = root.findViewById(R.id.etEmail);
        etPhone = root.findViewById(R.id.etPhone);
        btnSave = root.findViewById(R.id.btnSave);

        final var prefs = requireActivity().getSharedPreferences("app_prefs", 0);
        final String email = prefs.getString("user_email", null);

        if (email != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                UserDao dao = AppDatabase.get(requireContext()).userDao();
                UserEntity u = dao.findByEmailSync(email);

                requireActivity().runOnUiThread(() -> {
                    if (u != null) {
                        etName.setText(u.name == null ? "" : u.name);
                        etEmail.setText(u.email);
                        etPhone.setText(u.phone == null ? "" : u.phone);
                    } else {
                        etEmail.setText(email);
                    }
                });
            });
        }

        btnSave.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String image = prefs.getString("user_image", null);

            if (TextUtils.isEmpty(name))  { etName.setError("Required");  etName.requestFocus();  return; }
            if (TextUtils.isEmpty(phone)) { etPhone.setError("Required"); etPhone.requestFocus(); return; }

            AppExecutors.getInstance().diskIO().execute(() -> {
                UserDao dao = AppDatabase.get(requireContext()).userDao();

                dao.updateProfile(email, name, phone, image);

                prefs.edit()
                        .putString("user_name",  name)
                        .putString("user_phone", phone)
                        .apply();

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            });
        });

        return root;
    }
}
