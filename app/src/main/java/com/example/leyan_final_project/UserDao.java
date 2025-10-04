package com.example.leyan_final_project;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmailSync(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity loginSync(String email, String password);

    @Query("UPDATE users SET name = :name, phone = :phone WHERE email = :email")
    void updateProfile(String email, String name, String phone);

    @Query("UPDATE users SET name = :name, phone = :phone, imageUri = :imageUri WHERE email = :email")
    void updateProfile(String email, String name, String phone, String imageUri);

    @Query("UPDATE users SET imageUri = :imageUri WHERE email = :email")
    void updateImage(String email, String imageUri);
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);


}
