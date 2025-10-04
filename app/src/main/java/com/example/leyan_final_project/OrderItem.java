package com.example.leyan_final_project;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_items")
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderId;

    public int dishId;
    public String dishName;
    public int imageRes;
    @Nullable public String imageUrl;

    public int qty;
    public double priceEach;

    public OrderItem(long orderId, int dishId, String dishName, int imageRes,
                     @Nullable String imageUrl, int qty, double priceEach) {
        this.orderId = orderId;
        this.dishId = dishId;
        this.dishName = dishName;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
        this.qty = qty;
        this.priceEach = priceEach;
    }
}
