package com.example.leyan_final_project;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.EditText;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private AppDatabase db;

    public CheckoutFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_checkout, container, false);
        db = AppDatabase.get(requireContext());

        RadioGroup rg       = root.findViewById(R.id.rgPayment);
        View btnConfirm     = root.findViewById(R.id.btnConfirm);
        EditText etAddress  = root.findViewById(R.id.etAddress);
        EditText etPhone    = root.findViewById(R.id.etPhone);

        btnConfirm.setOnClickListener(v -> {
            AppExecutors.getInstance().diskIO().execute(() -> {
                List<CartItem> current = db.cartDao().getAllSync();
                requireActivity().runOnUiThread(() -> {
                    if (current.isEmpty()) {
                        Toast.makeText(getContext(), R.string.cart_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(etAddress.getText())) {
                        etAddress.setError(getString(R.string.required_field));
                        return;
                    }
                    if (TextUtils.isEmpty(etPhone.getText())) {
                        etPhone.setError(getString(R.string.required_field));
                        return;
                    }
                    if (rg.getCheckedRadioButtonId() == -1) {
                        Toast.makeText(getContext(), R.string.select_payment_method, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double total = 0.0;
                    for (CartItem it : current) total += it.qty * it.price;
                    showConfirmPlaceOrderDialog(total);
                });
            });
        });

        return root;
    }

    private void showConfirmPlaceOrderDialog(double total) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dlg_place_order_title)
                .setMessage(R.string.dlg_place_order_msg)
                .setPositiveButton(R.string.dlg_yes, (d, w) -> placeOrder(total))
                .setNegativeButton(R.string.dlg_no, (d, w) -> d.dismiss())
                .show();
    }

    private void placeOrder(double total) {
        OrderEntity order = new OrderEntity();
        order.total = total;
        order.status = "Pending";
        order.createdAt = System.currentTimeMillis();

        AppExecutors.getInstance().diskIO().execute(() -> {
            long orderId = db.orderDao().insert(order);

            List<CartItem> cartItems = db.cartDao().getAllSync();
            List<OrderItem> items = new ArrayList<>();
            for (CartItem it : cartItems) {
                items.add(new OrderItem(
                        orderId,
                        it.dishId,
                        it.name,
                        (it.imageRes == null ? 0 : it.imageRes),
                        (it.imageUrl == null || it.imageUrl.isEmpty()) ? null : it.imageUrl,
                        it.qty,
                        it.price
                ));
            }
            db.orderItemDao().insertAll(items);
            db.cartDao().clear();

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), R.string.order_placed_toast, Toast.LENGTH_SHORT).show();
                NotificationUtils.showOrderPlaced(requireContext(), orderId);
                BottomNavigationView bottom = requireActivity().findViewById(R.id.bottom_nav);
                if (bottom != null) bottom.setSelectedItemId(R.id.nav_orders);
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        });
    }
}
