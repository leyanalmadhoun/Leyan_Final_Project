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

public class AdminMenuFragment extends Fragment {
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_admin_menu, container, false);

        db = AppDatabase.get(requireContext());

        EditText etName  = root.findViewById(R.id.etDishName);
        EditText etPrice = root.findViewById(R.id.etDishPrice);
        Button   btnAdd  = root.findViewById(R.id.btnAddDish);

        btnAdd.setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(getContext(), "Enter name and price", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException ex) {
                etPrice.setError("Invalid price");
                etPrice.requestFocus();
                return;
            }

            DishEntity entity = new DishEntity(
                    name,
                    price,
                    R.drawable.sample_food,
                    null,
                    ""
            );

            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase.get(requireContext()).dishDao().insert(entity);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Dish added!", Toast.LENGTH_SHORT).show()
                );
            });
        });

        return root;
    }
}
