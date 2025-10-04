package com.example.leyan_final_project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InMemoryCart {

    private static InMemoryCart instance;
    private final List<CartItem> items = new ArrayList<>();

    private InMemoryCart() {}

    public static InMemoryCart get() {
        if (instance == null) {
            instance = new InMemoryCart();
        }
        return instance;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void add(Dish d, int qty) {
        if (qty <= 0) return;

        for (CartItem it : items) {
            if (it.dishId == d.id) {
                it.qty += qty;
                return;
            }
        }

        items.add(new CartItem(
                d.id,
                d.name,
                d.price,
                (d.imageRes == null ? 0 : d.imageRes),
                d.imageUrl,
                qty
        ));
    }

    public void inc(int dishId) {
        for (CartItem it : items) {
            if (it.dishId == dishId) {
                it.qty++;
                return;
            }
        }
    }

    public void dec(int dishId) {
        Iterator<CartItem> it = items.iterator();
        while (it.hasNext()) {
            CartItem ci = it.next();
            if (ci.dishId == dishId) {
                ci.qty--;
                if (ci.qty <= 0) it.remove();
                return;
            }
        }
    }
    public void remove(int dishId) {
        items.removeIf(it -> it.dishId == dishId);
    }

    public void clear() {
        items.clear();
    }
}
