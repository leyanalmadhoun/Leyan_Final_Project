package com.example.leyan_final_project;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface CartDao {

    @Upsert
    void upsert(CartItem item);

    @Query("SELECT * FROM cart_items")
    LiveData<List<CartItem>> getAllLive();

    @Query("SELECT * FROM cart_items")
    List<CartItem> getAllSync();

    @Query("SELECT * FROM cart_items WHERE dishId = :dishId LIMIT 1")
    CartItem findById(int dishId);

    @Query("UPDATE cart_items SET qty = :newQty WHERE dishId = :dishId")
    void updateQty(int dishId, int newQty);

    @Query("DELETE FROM cart_items WHERE dishId = :dishId")
    void deleteById(int dishId);

    @Query("DELETE FROM cart_items")
    void clear();
}

