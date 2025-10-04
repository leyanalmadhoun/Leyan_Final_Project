package com.example.leyan_final_project;

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

import android.os.Bundle;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailsFragment extends Fragment {

    private AppDatabase db;
    private long orderId;
    private OrderItemsAdapter itemsAdapter;

    public OrderDetailsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_details, container, false);

        db = AppDatabase.get(requireContext());
        orderId = getArguments() != null ? getArguments().getLong("order_id", -1) : -1;

        TextView tvId = root.findViewById(R.id.tvOrderId);
        TextView tvStatus = root.findViewById(R.id.tvOrderStatus);
        TextView tvDate = root.findViewById(R.id.tvOrderDate);
        TextView tvTotal = root.findViewById(R.id.tvOrderTotal);

        RecyclerView rv = root.findViewById(R.id.rvItems);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsAdapter = new OrderItemsAdapter();
        rv.setAdapter(itemsAdapter);

        db.orderDao().getById(orderId).observe(getViewLifecycleOwner(), o -> {
            if (o == null) return;
            tvId.setText("#" + o.id);
            tvStatus.setText(o.status == null ? "Pending" : o.status);
            tvTotal.setText(String.format("$%.2f", o.total));
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault());
            tvDate.setText(fmt.format(new Date(o.createdAt)));
        });

        db.orderItemDao().getForOrder(orderId).observe(getViewLifecycleOwner(), items -> {
            itemsAdapter.submit(items);
        });

        Button btnAgain = root.findViewById(R.id.btnOrderAgain);
        btnAgain.setOnClickListener(v -> AppExecutors.getInstance().diskIO().execute(() -> {
            List<OrderItem> items = db.orderItemDao().getForOrderNow(orderId);
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
        }));

        Button btnMark = root.findViewById(R.id.btnMarkCompleted);
        boolean isAdmin = "admin".equalsIgnoreCase(
                requireActivity()
                        .getSharedPreferences("app_prefs", 0)
                        .getString("user_role", "user")
        );
        btnMark.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        btnMark.setOnClickListener(v ->
                AppExecutors.getInstance().diskIO().execute(() ->
                        db.orderDao().updateStatus(orderId, "Completed"))
        );

        return root;
    }

    static class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {
        private List<OrderItem> data;

        void submit(List<OrderItem> list) {
            this.data = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_item, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            OrderItem it = data.get(pos);
            if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
                Glide.with(h.itemView).load(it.imageUrl)
                        .placeholder(R.drawable.sample_food)
                        .error(R.drawable.sample_food)
                        .centerCrop().into(h.img);
            } else if (it.imageRes != 0) {
                h.img.setImageResource(it.imageRes);
            } else {
                h.img.setImageResource(R.drawable.sample_food);
            }
            h.name.setText(it.dishName);
            h.qtyPrice.setText(it.qty + " × " + String.format("$%.2f", it.priceEach));
            double line = it.qty * it.priceEach;
            h.lineTotal.setText(String.format("$%.2f", line));
        }

        @Override public int getItemCount() { return (data == null) ? 0 : data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            android.widget.ImageView img;
            TextView name, qtyPrice, lineTotal;
            VH(View v) {
                super(v);
                img = v.findViewById(R.id.img);
                name = v.findViewById(R.id.tvName);
                qtyPrice = v.findViewById(R.id.tvQtyPrice);
                lineTotal = v.findViewById(R.id.tvLineTotal);
            }
        }
    }
}
