package com.aiotlabs.ifitpro.plugin.bluetooth;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;


@Dao
public interface MyDao{
    @Insert
    public void addUser(User user);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

}
