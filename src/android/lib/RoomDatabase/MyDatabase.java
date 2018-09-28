package com.aiotlabs.ifitpro.plugin.bluetooth;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(entities ={User.class},version =2,exportSchema = false)
public abstract class MyDatabase extends RoomDatabase{
    public abstract MyDao myDao();

}
