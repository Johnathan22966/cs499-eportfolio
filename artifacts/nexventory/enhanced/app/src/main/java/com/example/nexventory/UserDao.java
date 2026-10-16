package com.example.nexventory;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert long insert(UserRecord user);
    @Query("SELECT password FROM users WHERE username = :username LIMIT 1")
    String passwordFor(String username);
}
