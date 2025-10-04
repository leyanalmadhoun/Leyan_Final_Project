package com.example.leyan_final_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
public class OrdersAdapter extends ListAdapter<OrderEntity, OrdersAdapter.VH> {

    public interface OnOrderClick {
        void onClick(OrderEntity order);
        void onOrderAgain(OrderEntity order);
        void onMarkCompleted(OrderEntity order);
    }

    private final OnOrderClick onOrderClick;
    private final boolean isAdmin;

    public OrdersAdapter(OnOrderClick onOrderClick, boolean isAdmin) {
        super(DIFF);
        this.onOrderClick = onOrderClick;
        this.isAdmin = isAdmin;
    }

    public OrdersAdapter(OnOrderClick onOrderClick) {
        this(onOrderClick, false);
    }
    private static final DiffUtil.ItemCallback<OrderEntity> DIFF =
            new DiffUtil.ItemCallback<OrderEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull OrderEntity oldItem, @NonNull OrderEntity newItem) {
                    return oldItem.id == newItem.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull OrderEntity oldItem, @NonNull OrderEntity newItem) {
                    boolean sameTotal = Double.compare(oldItem.total, newItem.total) == 0;
                    boolean sameStatus = (oldItem.status == null && newItem.status == null)
                            || (oldItem.status != null && oldItem.status.equals(newItem.status));
                    return sameTotal && sameStatus;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderEntity o = getItem(position);

        String status = (o.status == null ? "Pending" : o.status);

        h.tvOrderTitle.setText("#" + o.id);
        h.tvOrderPrice.setText(String.format("$%.2f", o.total));
        h.tvOrderSub.setText(status);

        h.imgThumb.setImageResource(R.drawable.sample_food);

        boolean isCompleted = "Completed".equalsIgnoreCase(status);

        if (!isAdmin) {
            h.tvMarkCompleted.setVisibility(View.GONE);
        } else {
            h.tvMarkCompleted.setVisibility(View.VISIBLE);
            h.tvMarkCompleted.setEnabled(!isCompleted);
            h.tvMarkCompleted.setAlpha(isCompleted ? 0.4f : 1f);
            h.tvMarkCompleted.setOnClickListener(v -> {
                if (!isCompleted && onOrderClick != null) onOrderClick.onMarkCompleted(o);
            });
        }

        h.tvOrderAgain.setOnClickListener(v -> {
            if (onOrderClick != null) onOrderClick.onOrderAgain(o);
        });

        h.itemView.setOnClickListener(v -> {
            if (onOrderClick != null) onOrderClick.onClick(o);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvOrderTitle, tvOrderSub, tvOrderPrice, tvOrderAgain, tvMarkCompleted;

        VH(@NonNull View itemView) {
            super(itemView);
            imgThumb        = itemView.findViewById(R.id.imgThumb);
            tvOrderTitle    = itemView.findViewById(R.id.tvOrderTitle);
            tvOrderSub      = itemView.findViewById(R.id.tvOrderSub);
            tvOrderPrice    = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderAgain    = itemView.findViewById(R.id.tvOrderAgain);
            tvMarkCompleted = itemView.findViewById(R.id.tvMarkCompleted);
        }
    }
}
