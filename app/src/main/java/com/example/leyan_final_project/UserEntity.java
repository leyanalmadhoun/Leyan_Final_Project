package com.example.leyan_final_project;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String email;
    public String password;
    public String name;
    public String phone;
    public String imageUri;
    public UserEntity() {}

    @Ignore
    public UserEntity(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }

    @Ignore
    public UserEntity(String email, String password, String name, String phone, String imageUri) {
        this(email, password, name, phone);
        this.imageUri = imageUri;
    }
}
