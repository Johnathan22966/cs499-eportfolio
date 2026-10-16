package com.example.nexventory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class UserRecord {
    @PrimaryKey(autoGenerate = true) public int id;
    @NonNull public String username = "";
    @NonNull public String password = "";
}
