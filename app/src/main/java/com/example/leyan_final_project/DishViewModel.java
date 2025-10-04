package com.example.leyan_final_project;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class DishViewModel extends AndroidViewModel {
    private final DishRepository repo;
    private final LiveData<List<DishEntity>> allDishes;

    public DishViewModel(@NonNull Application app) {
        super(app);
        repo = new DishRepository(app);
        allDishes = repo.getAllDishes();
    }

    public LiveData<List<DishEntity>> getAllDishes() {
        return allDishes;
    }

    public void insert(DishEntity dish) {
        repo.insert(dish);
    }

    public void update(DishEntity dish) {
        repo.update(dish);
    }

    public void deleteById(int id) {
        repo.deleteById(id);
    }
}