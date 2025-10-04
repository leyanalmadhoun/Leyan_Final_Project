package com.example.leyan_final_project;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface OrderDao {

    @Insert
    long insert(OrderEntity order);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<OrderEntity>> getAll();

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<OrderEntity>> getByStatus(String status);

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    void updateStatus(long orderId, String status);

    @Query("DELETE FROM orders")
    void clearAll();

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    LiveData<OrderEntity> getById(long id);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    OrderEntity getByIdNow(long id);
}
