package com.example.leyan_final_project;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dishes")
public class DishEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double price;
    public Integer imageRes;
    public String imageUrl;
    public String category;

    public DishEntity(String name, double price, Integer imageRes, String imageUrl, String category) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
