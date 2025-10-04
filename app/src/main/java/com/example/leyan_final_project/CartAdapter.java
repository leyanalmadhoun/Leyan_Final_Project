package com.example.leyan_final_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnDataChangedListener { void onChanged(); }
    private OnDataChangedListener onDataChanged;
    public void setOnDataChangedListener(OnDataChangedListener l) { this.onDataChanged = l; }
    private void fireChanged() { if (onDataChanged != null) onDataChanged.onChanged(); }

    public interface OnQtyChange { void onChange(CartItem item, int delta); }
    private OnQtyChange onQtyChange;
    public void setOnQtyChange(OnQtyChange l) { this.onQtyChange = l; }

    private final List<CartItem> items = new ArrayList<>();

    public CartAdapter(List<CartItem> initial) {
        if (initial != null) items.addAll(initial);
    }

    public void submit(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
        fireChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        CartItem it = items.get(i);

        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            Glide.with(h.itemView).load(it.imageUrl)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .centerCrop()
                    .into(h.img);
        } else if (it.imageRes != null && it.imageRes != 0) {
            h.img.setImageResource(it.imageRes);
        } else {
            h.img.setImageResource(R.drawable.sample_food);
        }

        h.title.setText(it.name);
        h.price.setText(String.format("$%.2f", it.price));
        h.qty.setText(String.valueOf(it.qty));

        h.plus.setOnClickListener(v -> {
            if (onQtyChange != null) onQtyChange.onChange(it, +1);
        });

        h.minus.setOnClickListener(v -> {
            if (onQtyChange != null) onQtyChange.onChange(it, -1);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, price, qty;
        View plus, minus;
        VH(View v) {
            super(v);
            img   = v.findViewById(R.id.img);
            title = v.findViewById(R.id.tvTitle);
            price = v.findViewById(R.id.tvPrice);
            qty   = v.findViewById(R.id.tvQty);
            plus  = v.findViewById(R.id.btnPlus);
            minus = v.findViewById(R.id.btnMinus);
        }
    }
}
