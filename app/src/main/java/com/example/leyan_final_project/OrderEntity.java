package com.example.leyan_final_project;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @Ignore public String  previewImageUrl;
    @Ignore public Integer previewImageRes;

    public double total;
    public String status;
    public long   createdAt;

    @Ignore public List<OrderItem> items;
}
