package com.aiotlabs.ifitpro.plugin.bluetooth;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Dao;


@Entity
public class User{

    @PrimaryKey
    private int id ;

     private int steps;



    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSteps(){
        return steps;
    }

    public void setSteps(int steps){
        this.steps =steps;
    }

}
