package com.aiotlabs.ifitpro.plugin.bluetooth;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;


@Dao
public interface MyDao{
    @Insert
    public void addUser( User user);


}
