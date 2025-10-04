package com.example.leyan_final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private AppDatabase db;
    private CartAdapter adapter;
    private TextView tvTotal, tvEmpty;

    public CartFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        db = AppDatabase.get(requireContext());

        RecyclerView rv = root.findViewById(R.id.rvCart);
        tvTotal  = root.findViewById(R.id.tvTotal);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        Button btnCheckout = root.findViewById(R.id.btnCheckout);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        adapter.setOnQtyChange((item, delta) -> {
            AppExecutors.getInstance().diskIO().execute(() -> {
                int newQty = Math.max(0, item.qty + delta);
                if (newQty == 0) {
                    db.cartDao().deleteById(item.dishId);
                } else {
                    db.cartDao().updateQty(item.dishId, newQty);
                }
            });
        });

        db.cartDao().getAllLive().observe(getViewLifecycleOwner(), items -> {
            adapter.submit(items);
            updateTotal(items);
        });

        btnCheckout.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CheckoutFragment())
                    .addToBackStack("cart")
                    .commit();
        });

        return root;
    }

    private void updateTotal(List<CartItem> items) {
        double sum = 0.0;
        if (items != null) {
            for (CartItem it : items) sum += it.qty * it.price;
        }
        tvTotal.setText(String.format("$%.2f", sum));
        tvEmpty.setVisibility((items == null || items.isEmpty()) ? View.VISIBLE : View.GONE);
    }
}
