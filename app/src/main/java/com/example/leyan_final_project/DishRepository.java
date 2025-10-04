package com.example.leyan_final_project;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;

public class DishRepository {
    private final DishDao dao;

    public DishRepository(Context context) {
        AppDatabase db = AppDatabase.get(context);
        dao = db.dishDao();
    }

    public LiveData<List<DishEntity>> getAllDishes() {
        return dao.getAllLive();
    }

    public void insert(DishEntity dish) {
        AppExecutors.getInstance().diskIO().execute(() -> dao.insert(dish));
    }

    public void update(DishEntity dish) {
        AppExecutors.getInstance().diskIO().execute(() -> dao.update(dish));
    }

    public void deleteById(int id) {
        AppExecutors.getInstance().diskIO().execute(() -> dao.deleteById(id));
    }
}

