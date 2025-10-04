package com.example.leyan_final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile, btnEditImage;
    private TextView tvName, tvEmail;
    private EditText etName, etPhone;
    private Button btnSave, btnLogout, btnDark, btnLang;

    private SharedPreferences prefs;
    private String email;
    private String imageUri;

    private boolean suppressWatcher = false;

    private final ActivityResultLauncher<String[]> openImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        requireActivity().getContentResolver()
                                .takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                    } catch (Exception ignore) {}

                    imageUri = uri.toString();
                    imgProfile.setImageURI(uri);

                    saveImageUri(imageUri);

                    AppExecutors.getInstance().diskIO().execute(() -> {
                        AppDatabase.get(requireContext())
                                .userDao()
                                .updateImage(email, imageUri);
                    });
                }
            });

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = requireActivity().getSharedPreferences("app_prefs", 0);
        imgProfile  = root.findViewById(R.id.imgProfile);
        btnEditImage = root.findViewById(R.id.btnEditImage);
        tvName = root.findViewById(R.id.tvUserName);
        tvEmail = root.findViewById(R.id.tvEmail);
        etName  = root.findViewById(R.id.etName);
        etPhone = root.findViewById(R.id.etPhone);
        btnSave = root.findViewById(R.id.btnSave);
        btnLogout = root.findViewById(R.id.btnLogout);
        btnDark = root.findViewById(R.id.btnDark);
        btnLang = root.findViewById(R.id.btnLang);

        View.OnClickListener pick = v -> openImageLauncher.launch(new String[]{"image/*"});
        btnEditImage.setOnClickListener(pick);
        imgProfile.setOnClickListener(pick);

        email    = prefs.getString("user_email", "guest@example.com");
        String nm = prefs.getString("user_name",  "");
        if (isGuestToken(nm)) nm = "";
        imageUri = prefs.getString("user_image", null);

        tvEmail.setText(email);
        tvName.setText(nm.isEmpty() ? getString(R.string.guest) : nm);
        etName.setText(nm);
        etPhone.setText(prefs.getString("user_phone", ""));

        if (imageUri != null) {
            try { imgProfile.setImageURI(Uri.parse(imageUri)); }
            catch (Exception ignored) { imgProfile.setImageResource(R.drawable.baseline_co_present_24); }
        } else {
            imgProfile.setImageResource(R.drawable.baseline_co_present_24);
        }
        applyPhoneLocalePresentation();

        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(android.text.Editable s) {
                if (!isArabicApp() || suppressWatcher) return;
                String en = toEnglishDigits(s.toString());
                String ar = toArabicDigits(en);
                if (!ar.contentEquals(s)) {
                    suppressWatcher = true;
                    int pos = ar.length();
                    etPhone.setText(ar);
                    etPhone.setSelection(Math.min(pos, ar.length()));
                    suppressWatcher = false;
                }
            }
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            UserEntity u = AppDatabase.get(requireContext()).userDao().findByEmailSync(email);
            if (u != null) {
                requireActivity().runOnUiThread(() -> {
                    String nm2 = (u.name == null ? "" : u.name.trim());
                    if (isGuestToken(nm2)) nm2 = "";

                    tvName.setText(nm2.isEmpty() ? getString(R.string.guest) : nm2);
                    etName.setText(nm2);

                    String ph = (u.phone == null ? "" : u.phone);
                    etPhone.setText(isArabicApp() ? toArabicDigits(ph) : ph);
                    applyPhoneLocalePresentation();

                    if (u.imageUri != null) {
                        imageUri = u.imageUri;
                        try { imgProfile.setImageURI(Uri.parse(u.imageUri)); }
                        catch (Exception ignored) {}
                        saveImageUri(u.imageUri);
                    }
                });
            }
        });

        btnSave.setOnClickListener(v -> {
            String newName  = etName.getText().toString().trim();
            String nameToStore = newName.isEmpty() ? "" : newName;
            String newPhone = toEnglishDigits(etPhone.getText().toString().trim());

            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase.get(requireContext())
                        .userDao()
                        .updateProfile(email, nameToStore, newPhone, imageUri);

                prefs.edit()
                        .putString("user_name",  nameToStore)
                        .putString("user_phone", newPhone)
                        .putString("user_image", imageUri)
                        .apply();

                requireActivity().runOnUiThread(() -> {
                    tvName.setText(nameToStore.isEmpty() ? getString(R.string.guest) : nameToStore);
                    etPhone.setText(isArabicApp() ? toArabicDigits(newPhone) : newPhone);
                    applyPhoneLocalePresentation();
                    Toast.makeText(getContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                });
            });
        });

        btnDark.setOnClickListener(v -> {
            int mode = AppCompatDelegate.getDefaultNightMode();
            AppCompatDelegate.setDefaultNightMode(
                    mode == AppCompatDelegate.MODE_NIGHT_YES
                            ? AppCompatDelegate.MODE_NIGHT_NO
                            : AppCompatDelegate.MODE_NIGHT_YES
            );
        });

        btnLang.setOnClickListener(v -> {
            LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
            String lang = (current == null || current.isEmpty())
                    ? java.util.Locale.getDefault().getLanguage()
                    : current.toLanguageTags();

            String next = (lang != null && lang.startsWith("ar")) ? "en" : "ar";
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next));
        });

        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dlg_logout_title)
                    .setMessage(R.string.dlg_logout_msg)
                    .setPositiveButton(R.string.dlg_yes, (dialog, which) -> {
                        prefs.edit().clear().apply();
                        Intent i = new Intent(requireContext(), AuthActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.dlg_no, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return root;
    }

    private boolean isGuestToken(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.equalsIgnoreCase("guest") || t.equals("ضيف");
    }

    private void applyPhoneLocalePresentation() {
        if (isArabicApp()) {
            etPhone.setTextDirection(View.TEXT_DIRECTION_RTL);
            etPhone.setGravity(Gravity.END);
            etPhone.setText(toArabicDigits(etPhone.getText().toString()));
        } else {
            etPhone.setTextDirection(View.TEXT_DIRECTION_LTR);
            etPhone.setGravity(Gravity.START);
            etPhone.setText(toEnglishDigits(etPhone.getText().toString()));
        }
        etPhone.setSelection(etPhone.getText().length());
    }

    private boolean isArabicApp() {
        LocaleListCompat loc = AppCompatDelegate.getApplicationLocales();
        Locale l = (loc == null || loc.isEmpty())
                ? Locale.getDefault()
                : Locale.forLanguageTag(loc.toLanguageTags());
        return l.getLanguage().startsWith("ar");
    }

    private String toArabicDigits(String s) {
        if (s == null) return "";
        char[] en = {'0','1','2','3','4','5','6','7','8','9'};
        char[] ar = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int idx = -1;
            for (int j = 0; j < 10; j++) if (c == en[j]) { idx = j; break; }
            out.append(idx >= 0 ? ar[idx] : c);
        }
        return out.toString();
    }

    private String toEnglishDigits(String s) {
        if (s == null) return "";
        char[] ar = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        char[] en = {'0','1','2','3','4','5','6','7','8','9'};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int idx = -1;
            for (int j = 0; j < 10; j++) if (c == ar[j]) { idx = j; break; }
            out.append(idx >= 0 ? en[idx] : c);
        }
        return out.toString();
    }

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private void saveImageUri(String uri) {
        prefs.edit().putString("user_image", uri).apply();
    }
}
