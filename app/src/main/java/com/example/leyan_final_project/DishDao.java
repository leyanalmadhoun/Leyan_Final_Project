package com.example.leyan_final_project;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DishDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(DishEntity dish);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<DishEntity> dishes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DishEntity dish);

    @Update
    int update(DishEntity dish);

    @Delete
    int delete(DishEntity dish);

    @Query("DELETE FROM dishes WHERE id = :id")
    int deleteById(int id);

    @Query("SELECT * FROM dishes ORDER BY id ASC")
    LiveData<List<DishEntity>> getAllLive();

    @Query("SELECT * FROM dishes ORDER BY id ASC")
    List<DishEntity> getAllNow();

    @Query("SELECT COUNT(*) FROM dishes")
    int count();

    @Query("SELECT * FROM dishes WHERE name = :name LIMIT 1")
    DishEntity findByName(String name);
}
