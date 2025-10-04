package com.example.leyan_final_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddEditDishDialog extends BottomSheetDialogFragment {
    private static final String ARG_ID    = "id";
    private static final String ARG_NAME  = "name";
    private static final String ARG_PRICE = "price";
    private static final String ARG_URI   = "imageUrl";
    private static final String ARG_CAT   = "category";
    private AppDatabase db;
    private ImageView imgPreview;
    private EditText etName, etPrice, etUrl;
    private Spinner spCategory;
    private String pickedUri;
    private final List<Pair<String,String>> categories = new ArrayList<>();
    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        requireActivity().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignore) { }
                    pickedUri = uri.toString();
                    showImage(pickedUri);
                }
            });

    public static AddEditDishDialog newAdd() {
        AddEditDishDialog d = new AddEditDishDialog();
        Bundle b = new Bundle();
        b.putBoolean("is_edit", false);
        d.setArguments(b);
        return d;
    }

    public static AddEditDishDialog newEdit(int dishId, String name, double price, String imageUrl, String category) {
        AddEditDishDialog d = new AddEditDishDialog();
        Bundle b = new Bundle();
        b.putBoolean("is_edit", true);
        b.putInt(ARG_ID, dishId);
        b.putString(ARG_NAME, name);
        b.putDouble(ARG_PRICE, price);
        if (imageUrl != null) b.putString(ARG_URI, imageUrl);
        if (category != null) b.putString(ARG_CAT, category);
        d.setArguments(b);
        return d;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_edit_dish, container, false);
        db = AppDatabase.get(requireContext());

        imgPreview = v.findViewById(R.id.imgPreview);
        etName = v.findViewById(R.id.etDishName);
        etPrice = v.findViewById(R.id.etDishPrice);
        etUrl = v.findViewById(R.id.etImageUrl);
        spCategory = v.findViewById(R.id.spCategory);

        prepareCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labelsFromPairs(categories)
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        Bundle args = getArguments();
        final boolean isEdit = (args != null && args.getBoolean("is_edit", false));
        final int editId = (args != null) ? args.getInt(ARG_ID, -1) : -1;

        if (isEdit) {
            etName.setText(args.getString(ARG_NAME, ""));
            etPrice.setText(String.valueOf(args.getDouble(ARG_PRICE, 0)));
            pickedUri = args.getString(ARG_URI, null);
            if (pickedUri != null) showImage(pickedUri);
            String catKey = args.getString(ARG_CAT, "FAST");
            setSpinnerToKey(catKey);
        } else {
            setSpinnerToKey("FAST");
        }

        v.findViewById(R.id.btnPickGallery).setOnClickListener(b ->
                picker.launch(new String[]{"image/*"}));

        v.findViewById(R.id.btnLoadUrl).setOnClickListener(b -> {
            String url = etUrl.getText().toString().trim();
            if (!TextUtils.isEmpty(url)) {
                pickedUri = url;
                showImage(pickedUri);
            }
        });

        v.findViewById(R.id.btnSave).setOnClickListener(b -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(getContext(), "Name & price required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price");
                etPrice.requestFocus();
                return;
            }

            final String imageUrlToSave = pickedUri;
            final String categoryKey = getSelectedCategoryKey();

            AppExecutors.getInstance().diskIO().execute(() -> {
                DishDao dao = AppDatabase.get(requireContext()).dishDao();
                if (isEdit && editId != -1) {
                    DishEntity d = new DishEntity(name, price, null, imageUrlToSave, categoryKey);
                    d.id = editId;
                    dao.update(d);
                } else {
                    DishEntity d = new DishEntity(name, price, null, imageUrlToSave, categoryKey);
                    dao.insert(d);
                }
                requireActivity().runOnUiThread(this::dismissAllowingStateLoss);
            });
        });

        return v;
    }

    private void prepareCategories() {
        categories.clear();
        categories.add(new Pair<>("FAST",    getString(R.string.cat_fast)));
        categories.add(new Pair<>("HEAVY",   getString(R.string.cat_heavy)));
        categories.add(new Pair<>("STARTER", getString(R.string.cat_starter)));
        categories.add(new Pair<>("DRINKS",  getString(R.string.cat_drinks)));
        categories.add(new Pair<>("DESSERT", getString(R.string.cat_dessert)));
    }

    private List<String> labelsFromPairs(List<Pair<String,String>> pairs) {
        List<String> labels = new ArrayList<>();
        for (Pair<String,String> p : pairs) labels.add(p.second);
        return labels;
    }

    private void setSpinnerToKey(String key) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).first.equalsIgnoreCase(key)) {
                spCategory.setSelection(i);
                return;
            }
        }
        spCategory.setSelection(0);
    }

    private String getSelectedCategoryKey() {
        int pos = spCategory.getSelectedItemPosition();
        if (pos < 0 || pos >= categories.size()) return "FAST";
        return categories.get(pos).first;
    }

    private void showImage(String uri) {
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.sample_food)
                .error(R.drawable.sample_food)
                .centerCrop()
                .into(imgPreview);
    }
}
