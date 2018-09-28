package com.aiotlabs.ifitpro.plugin.bluetooth;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "users")
public class User{

    @PrimaryKey(autoGenerate = true)
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

    public String toString(){
        return "{id: " + String.valueOf(getId()) + ", steps: " + String.valueOf(getSteps()) + "}";

    }

}
