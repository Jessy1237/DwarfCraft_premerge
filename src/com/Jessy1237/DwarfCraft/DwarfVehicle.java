package com.Jessy1237.DwarfCraft;

import org.bukkit.entity.Vehicle;

public class DwarfVehicle
{
    private Vehicle vehicle;
    private boolean changedSpeed;

    public DwarfVehicle( Vehicle vehicle )
    {
        this.vehicle = vehicle;
        this.changedSpeed = false;
    }

    @Override
    public boolean equals( Object that )
    {
        if ( that instanceof Vehicle )
        {
            Vehicle vec = (Vehicle) that;
            if ( vec.getEntityId() == vehicle.getEntityId() )
                return true;
        }
        return false;
    }

    public Vehicle getVehicle()
    {
        return this.vehicle;
    }

    public boolean changedSpeed() {
        return this.changedSpeed;
    }
    
    public void speedChanged() {
        this.changedSpeed = true;
    }
}