package com.example.leyan_final_project;

public class Dish {
    public int id;
    public String name;
    public double price;

    public Integer imageRes;
    public String imageUrl;
    public String category;

    public Dish(int id, String name, double price, Integer imageRes, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public Dish(int id, String name, double price, int imageRes, String category) {
        this(id, name, price, Integer.valueOf(imageRes), null, category);
    }

    public Dish(int id, String name, double price, String imageUrl, String category) {
        this(id, name, price, null, imageUrl, category);
    }
}
