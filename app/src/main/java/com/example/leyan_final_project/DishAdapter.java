package com.example.leyan_final_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.VH> {

    public interface OnDishAction {
        void onAddToCart(Dish dish);
        void onEdit(Dish dish);
        void onDelete(Dish dish);
        void onClick(Dish dish);
    }

    private final List<Dish> data;
    private final boolean isAdmin;
    private final OnDishAction listener;

    public DishAdapter(List<Dish> data, boolean isAdmin, OnDishAction listener) {
        this.data = data;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public DishAdapter(List<Dish> data) {
        this(data, false, null);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Dish d = data.get(position);

        h.name.setText(d.name);
        h.price.setText(String.format("$%.2f", d.price));
        h.rating.setRating(4.5f);

        if (d.imageUrl != null && !d.imageUrl.isEmpty()) {
            Glide.with(h.itemView).load(d.imageUrl)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .centerCrop().into(h.img);
        } else if (d.imageRes != null && d.imageRes != 0) {
            Glide.with(h.itemView).load(d.imageRes)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .centerCrop().into(h.img);
        } else {
            h.img.setImageResource(R.drawable.sample_food);
        }

        h.rowAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        h.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(d);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(d);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(d);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(d);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, price;
        RatingBar rating;
        Button btnAdd;
        View rowAdmin;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.img);
            name = v.findViewById(R.id.tvName);
            price = v.findViewById(R.id.tvPrice);
            rating = v.findViewById(R.id.ratingBar);
            btnAdd = v.findViewById(R.id.btnAdd);
            rowAdmin = v.findViewById(R.id.rowAdmin);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
