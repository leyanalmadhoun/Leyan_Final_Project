package com.example.leyan_final_project;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface OrderItemDao {
    @Insert
    void insertAll(List<OrderItem> items);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    LiveData<List<OrderItem>> getForOrder(long orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    List<OrderItem> getForOrderNow(long orderId);
}
