package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftBoat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.DwarfVehicle;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;

public class DCVehicleListener implements Listener {
	private final DwarfCraft plugin;

	public DCVehicleListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}
	
	/**
     * Called when a vehicle is destroyed
     *
     * @param event
     */
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getAttacker().getWorld() == w){
						return;
					}
				}
			}
		}
		
    	boolean dropChange = false;
    	
    	if (event.getVehicle() instanceof Boat &&
    		event.getAttacker() instanceof Player){
    		
    		Player player = (Player)event.getAttacker();
    		DCPlayer dcPlayer = plugin.getDataManager().find(player);
    		Location loc = event.getVehicle().getLocation();

    		for (Skill skill : dcPlayer.getSkills().values()) {
    			for (Effect effect : skill.getEffects()){
    				if (effect.getEffectType() == EffectType.VEHICLEDROP){
    					ItemStack drop = effect.getOutput(dcPlayer);
    					
    					if (DwarfCraft.debugMessagesThreshold < 6) 
                            System.out.println("Debug: dropped " + drop.toString());
    					
    					if (drop.getAmount() > 0)
    						loc.getWorld().dropItemNaturally(loc, drop);
    					
    					dropChange = true;
    				}
    			}
    		}
    	}
    	
    	if (dropChange){
    		event.getVehicle().remove();
    		event.setCancelled(true);
    	}
    }
	@EventHandler(priority = EventPriority.NORMAL)
	public void onVehicleEnter(VehicleEnterEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getVehicle().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if (!(event.getVehicle() instanceof CraftBoat)) return;
		plugin.getDataManager().addVehicle(new DwarfVehicle(event.getVehicle()));
		if (DwarfCraft.debugMessagesThreshold < 6)
			System.out.println("DC6:Added DwarfVehicle to vehicleList");
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onVehicleExit(VehicleExitEvent event) {
		if (!(event.getVehicle() instanceof CraftBoat)) return;
		plugin.getDataManager().removeVehicle(event.getVehicle());
	}

	/**
	 * Called when a vehicle moves.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleMove(VehicleMoveEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getVehicle().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if (event.getVehicle().getPassenger() == null) return;
		if (!(event.getVehicle() instanceof CraftBoat)) return;
		if (!(event.getVehicle().getPassenger() instanceof Player)) return;
		
		DCPlayer dCPlayer = plugin.getDataManager().find((Player)event.getVehicle().getPassenger()); 
		double effectAmount = 1.0;

		for (Skill s : dCPlayer.getSkills().values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.VEHICLEMOVE) {
					effectAmount = e.getEffectAmount(dCPlayer);
				}
			}
		}

		DwarfVehicle dv = plugin.getDataManager().getVehicle(event.getVehicle());
		if (dv != null) {
			Location oldLoc = event.getVehicle().getLocation();
			Vector vel = event.getVehicle().getVelocity().multiply(effectAmount);
			Location location = new Location(event.getVehicle().getWorld(), oldLoc.getX(), oldLoc.getY(), oldLoc.getZ());
			location.setX(location.getX() + vel.getX());
			location.setZ(location.getZ() + vel.getZ());
			event.getVehicle().teleport(location);
		}
	}
}
