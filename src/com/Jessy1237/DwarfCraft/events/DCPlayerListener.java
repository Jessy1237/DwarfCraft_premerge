package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

import com.Jessy1237.DwarfCraft.DCCraftSchedule;
import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DataManager;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;

@SuppressWarnings("deprecation")
public class DCPlayerListener implements Listener {
	private final DwarfCraft plugin;

	public DCPlayerListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event){}

	/**
	 * When a player joins the server this initialized their data from the
	 * database or creates new info for them.
	 * 
	 * also broadcasts a welcome "player" message
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		DataManager dm     = plugin.getDataManager();
		Player      player = event.getPlayer();
		DCPlayer    data   = dm.find(player);
		
		if (data == null)           data = dm.createDwarf(player);
		if (!dm.getDwarfData(data)) dm.createDwarfData(data);
		if(!plugin.getConfigManager().sendGreeting)
			return;
		
		plugin.getOut().welcome(plugin.getServer(), data);
	}
	
    /**	
	 * Called when a player interacts	
     *	
	 * @param event Relevant event details	
     */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		//Crafting changes
		
		int origHealth = event.getPlayer().getFoodLevel();
		int origItemAmount = event.getPlayer().getItemInHand().getAmount();
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WORKBENCH){
			DCCraftSchedule sched = new DCCraftSchedule(plugin, plugin.getDataManager().find(event.getPlayer()));
			int id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, sched, 0, 2);
			sched.setID(id);
		}
		
		Player   player = event.getPlayer();
		DCPlayer dcPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dcPlayer.getSkills();
		
		ItemStack item = player.getItemInHand();
				
		//EffectType.PLOWDURABILITY
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK){			
			Block    block    = event.getClickedBlock();
			Material material = block.getType();
			
			if (material == Material.DIRT || material == Material.GRASS){			
				for (Skill s : skills.values()) {
					for (Effect effect : s.getEffects()) {
						if (effect.getEffectType() == EffectType.PLOWDURABILITY && effect.checkTool(item)) {
							effect.damageTool(dcPlayer, 1, item);
							//block.setTypeId(60);
						}
					}
				}
			}
		}

		//EffectType.EAT
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){	
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.EAT && e.checkInitiator(item)) {
						int health = Util.randomAmount((e.getEffectAmount(dcPlayer)));
						
						if (DwarfCraft.debugMessagesThreshold < 8)
							System.out.println(String.format("DC8: Are Food: \"%s\" for %d health", Util.getCleanName(item), health));
						
						Block block = event.getClickedBlock();
						
						onPlayerEat(event, item, health, origHealth, origItemAmount, block);
						
					}
				}
			}
		}
	}
	/**
     * Called when a player opens an inventory
     *
     * @param event Relevant event details
     */
	
	public void onPlayerEat(PlayerInteractEvent event, ItemStack item, int health, int origHealth, int origItemAmount, Block block){
		
		int id = item.getTypeId();
		int bId = block.getTypeId();
		int itemFoodLevel = 0;
		
		//Raw Fish
		if(id == 349){
			itemFoodLevel = 2;
		
		//Mushroom Soup
		}else if(id == 282){
			itemFoodLevel = 8;
		
		//Raw Pork
		}else if(id == 319){
			itemFoodLevel = 3;
		}else if(id == 320){
			itemFoodLevel = 9;
		}else if(bId == 2){
			return;
		}
		
		event.getPlayer().setFoodLevel((origHealth - itemFoodLevel) + health);
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryOpen(PlayerInventoryEvent event) {
		DCCraftSchedule sched = new DCCraftSchedule(plugin, plugin.getDataManager().find(event.getPlayer()));
		int id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, sched, 0, 2);
		sched.setID(id);
    }
}