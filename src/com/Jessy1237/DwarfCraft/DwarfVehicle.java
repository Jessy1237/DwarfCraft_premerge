package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.Location;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class DwarfVehicle {
	private Vehicle vehicle;
	private Vector velocity;
	private Location location;

	public DwarfVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
		this.velocity = vehicle.getVelocity().clone();
		this.location = vehicle.getLocation().clone();
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof Vehicle) {
			if (that == vehicle)
				return true;
		}
		return false;
	}

	protected Location getLocation() {
		return location;
	}

	protected Vehicle getVehicle() {
		return vehicle;
	}

	protected Vector getVelocity() {
		return velocity;
	}

}