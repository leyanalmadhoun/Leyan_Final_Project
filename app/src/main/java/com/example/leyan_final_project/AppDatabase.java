package com.example.leyan_final_project;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                OrderEntity.class,
                OrderItem.class,
                UserEntity.class,
                DishEntity.class,
                CartItem.class
        },
        version = 5,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract UserDao userDao();
    public abstract DishDao dishDao();
    public abstract CartDao cartDao();

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "app.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    AppExecutors.getInstance().diskIO().execute(() -> {
                                        UserDao userDao = get(ctx).userDao();
                                        UserEntity admin = new UserEntity();
                                        admin.email = "admin@foodify.com";
                                        admin.password = "123123";
                                        admin.name = "Administrator";
                                        admin.phone = "0591231234";
                                        admin.imageUri = null;
                                        userDao.insert(admin);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
