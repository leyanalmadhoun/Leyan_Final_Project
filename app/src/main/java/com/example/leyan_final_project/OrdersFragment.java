package com.example.leyan_final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {
    private static final String STATUS_PENDING   = "Pending";
    private static final String STATUS_COMPLETED = "Completed";

    private AppDatabase db;
    private OrdersAdapter adapter;
    private List<OrderEntity> full = new ArrayList<>();
    private TabLayout tabs;
    private TextView tvEmpty;

    public OrdersFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_orders, container, false);

        db = AppDatabase.get(requireContext());

        tabs = root.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText(getString(R.string.orders_pending)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.orders_completed)));

        tvEmpty = root.findViewById(R.id.tvEmptyOrders);

        RecyclerView rv = root.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        boolean isAdmin = "admin".equalsIgnoreCase(
                requireActivity()
                        .getSharedPreferences("app_prefs", 0)
                        .getString("user_role", "user")
        );

        adapter = new OrdersAdapter(new OrdersAdapter.OnOrderClick() {
            @Override public void onClick(OrderEntity order) {
                Bundle b = new Bundle();
                b.putLong("order_id", order.id);
                Fragment f = new OrderDetailsFragment();
                f.setArguments(b);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, f)
                        .addToBackStack(null)
                        .commit();
            }

            @Override public void onOrderAgain(OrderEntity order) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    List<OrderItem> items = db.orderItemDao().getForOrderNow(order.id);
                    for (OrderItem it : items) {
                        CartItem exist = db.cartDao().findById(it.dishId);
                        if (exist == null) {
                            db.cartDao().upsert(new CartItem(
                                    it.dishId,
                                    it.dishName,
                                    it.priceEach,
                                    (it.imageRes == 0 ? null : it.imageRes),
                                    (it.imageUrl == null || it.imageUrl.isEmpty()) ? null : it.imageUrl,
                                    it.qty
                            ));
                        } else {
                            db.cartDao().updateQty(it.dishId, exist.qty + it.qty);
                        }
                    }
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(
                                getContext(),
                                getString(R.string.added_order_to_cart_fmt, String.valueOf(order.id)),
                                Toast.LENGTH_SHORT
                        ).show();

                        BottomNavigationView bottom = requireActivity().findViewById(R.id.bottom_nav);
                        if (bottom != null) bottom.setSelectedItemId(R.id.nav_cart);
                    });
                });
            }

            @Override public void onMarkCompleted(OrderEntity order) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    db.orderDao().updateStatus(order.id, STATUS_COMPLETED);
                    requireActivity().runOnUiThread(() -> {
                        if (tabs != null && tabs.getTabCount() > 1) {
                            tabs.selectTab(tabs.getTabAt(1));
                        }
                        Toast.makeText(getContext(), R.string.orders_completed, Toast.LENGTH_SHORT).show();
                    });
                });
            }
        }, isAdmin);

        rv.setAdapter(adapter);

        LiveData<List<OrderEntity>> live = db.orderDao().getAll();
        live.observe(getViewLifecycleOwner(), orders -> {
            full = (orders == null) ? new ArrayList<>() : orders;
            applyFilter();
        });

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { applyFilter(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { applyFilter(); }
        });

        return root;
    }

    private void applyFilter() {
        String want = (tabs.getSelectedTabPosition() == 1) ? STATUS_COMPLETED : STATUS_PENDING;
        List<OrderEntity> filtered = new ArrayList<>();
        for (OrderEntity o : full) {
            String st = (o.status == null ? STATUS_PENDING : o.status);
            if (st.equalsIgnoreCase(want)) filtered.add(o);
        }
        adapter.submitList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
