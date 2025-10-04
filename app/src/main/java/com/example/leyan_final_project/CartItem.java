package com.example.leyan_final_project;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items")
public class CartItem {

    @PrimaryKey
    public int dishId;

    public String name;
    public double price;

    @Nullable public Integer imageRes;
    @Nullable public String imageUrl;
    public int qty;

    public CartItem() {}

    public CartItem(int dishId, String name, double price,
                    @Nullable Integer imageRes,
                    @Nullable String imageUrl,
                    int qty) {
        this.dishId = dishId;
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
        this.qty = qty;
    }


    @Ignore
    public CartItem(int dishId, String name, double price,
                    @Nullable Integer imageRes,
                    int qty) {
        this(dishId, name, price, imageRes, null, qty);
    }

    @Ignore
    public CartItem(int dishId, String name, double price,
                    @Nullable String imageUrl,
                    int qty) {
        this(dishId, name, price, null, imageUrl, qty);
    }

    @Override
    public String toString() {
        return "CartItem{dishId=" + dishId + ", name=" + name + ", qty=" + qty + "}";
    }
}
