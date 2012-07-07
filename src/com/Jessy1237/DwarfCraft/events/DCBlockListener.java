package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;

public class DCBlockListener implements Listener {
	private final DwarfCraft plugin;

	public DCBlockListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getPlayer().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if (event.isCancelled())
			return;
		
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		
		DCPlayer player = plugin.getDataManager().find(event.getPlayer());
		HashMap<Integer, Skill> skills = player.getSkills();
		
		ItemStack tool    = player.getPlayer().getItemInHand();		
		Location  loc     = event.getBlock().getLocation();
		int       blockID = event.getBlock().getTypeId();
		byte      meta    = event.getBlock().getData();

		boolean blockDropChange = false;
		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.BLOCKDROP && effect.checkInitiator(blockID, meta)) {
					
					// Crops special line:
					if (effect.getInitiatorId() == 59){
						if (meta != 7)
							return;
					}
					
					if (effect.checkTool(tool)) {
						ItemStack item = effect.getOutput(player, meta);
						ItemStack i = new ItemStack(item.getTypeId());
						
						if(item.getTypeId() != 351){
							item.setDurability(i.getDurability());
							item.setData(i.getData());
						}
							
						//Checks for Silktouch & and allows for Silktouch items to override default
						if(tool.containsEnchantment(Enchantment.SILK_TOUCH)){
							if(item.getType() == Material.COBBLESTONE){
								item.setType(Material.STONE);
							} else if(item.getType() == Material.GOLD_INGOT){
								item.setType(Material.GOLD_ORE);
							} else if(item.getType() == Material.DIAMOND){
								item.setType(Material.DIAMOND_ORE);
							} else if(item.getType() == Material.COAL){
								item.setType(Material.COAL_ORE);
							} else if(item.getType() == Material.IRON_INGOT){
								item.setType(Material.IRON_ORE);
							} else if(item.getType() == Material.REDSTONE){
								item.setType(Material.REDSTONE_ORE);
							} else if(event.getBlock().getType() == Material.GRASS){
								item.setType(Material.GRASS);
							}
						}
						
						if (DwarfCraft.debugMessagesThreshold < 6)
							System.out.println("Debug: dropped " + item.toString());
						
						if (item.getAmount() > 0)
							loc.getWorld().dropItemNaturally(loc, item);
						
						blockDropChange = true;
					}
				}
			}
		}
		
		if (tool != null && tool.getType().getMaxDurability() > 0){
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.SWORDDURABILITY && e.checkTool(tool))
						e.damageTool(player, 2, tool, !blockDropChange);

					if (e.getEffectType() == EffectType.TOOLDURABILITY && e.checkTool(tool))
						e.damageTool(player, 1, tool, !blockDropChange);
				}
			}
		}

		if (blockDropChange) {
			event.getBlock().setTypeId(0);
			event.setCancelled(true);
		}
	}

	/**
	 * onBlockDamage used to accelerate how quickly blocks are destroyed.
	 * setDamage() not implemented yet
	 */
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDamage(BlockDamageEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getPlayer().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		DCPlayer dCPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dCPlayer.getSkills();

		// Effect Specific information
		ItemStack tool = player.getItemInHand();
		int materialId = event.getBlock().getTypeId();
		byte data      = event.getBlock().getData();
		
		//if (event.getDamageLevel() != BlockDamageLevel.STARTED)
		//	return;
				
		for (Skill s : skills.values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.DIGTIME && e.checkInitiator(materialId, data) && e.checkTool(tool)) {
					if (DwarfCraft.debugMessagesThreshold < 2)
						System.out.println("DC2: started instamine check");
					
					if (Util.randomAmount(e.getEffectAmount(dCPlayer)) == 0)
						return;
					
					if (DwarfCraft.debugMessagesThreshold < 3)
						System.out.println("DC3: Insta-mine occured. Block: " + materialId);
					
					event.setInstaBreak(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPhysics(BlockPhysicsEvent event){
		if (event.getBlock().getType() == Material.CACTUS && plugin.getConfigManager().disableCacti){
			
			if(plugin.getConfigManager().worldBlacklist){
				for (World w : plugin.getConfigManager().worlds){
					if(w != null){
						if(event.getBlock().getWorld() == w){
							return;
						}
					}
				}
			}
			
			World world = event.getBlock().getWorld();
			Location loc = event.getBlock().getLocation();
			//this is a re-implementation of BlockCactus's doPhysics event, minus the spawning of a droped item.
			if (!(checkCacti(world, loc))){
				event.getBlock().setTypeId(0, true);
				event.setCancelled(true);
				
				Material base = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType();
				if ((base != Material.CACTUS) && (base != Material.SAND))
					world.dropItemNaturally(loc, new ItemStack(Material.CACTUS, 1));
					
			}
		}
	}

	private boolean checkCacti(World world, Location loc){
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
	    if (isBuildable(world.getBlockAt(x - 1, y, z    ).getType())) return false;
	    if (isBuildable(world.getBlockAt(x + 1, y, z    ).getType())) return false;
	    if (isBuildable(world.getBlockAt(x,     y, z - 1).getType())) return false;
	    if (isBuildable(world.getBlockAt(x,     y, z + 1).getType())) return false;
	    
		Material base = world.getBlockAt(x, y - 1, z).getType();

	    return (base == Material.CACTUS) || (base == Material.SAND);
	}
	
	//Bukkit really needs to implement access to Material.isBuildable()
	private boolean isBuildable(Material block){
		switch(block){
			case AIR:              
			case WATER:            
			case STATIONARY_WATER: 
			case LAVA:             
			case STATIONARY_LAVA:  
			case YELLOW_FLOWER:    
			case RED_ROSE:         
			case BROWN_MUSHROOM:   
			case RED_MUSHROOM:     
			case SAPLING:          
			case SUGAR_CANE:       
			case FIRE:             
			case STONE_BUTTON:     
			case DIODE_BLOCK_OFF:  
			case DIODE_BLOCK_ON:
			case LADDER:
			case LEVER:
			case RAILS:
			case REDSTONE_WIRE:
			case TORCH:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
			case SNOW:	
			case POWERED_RAIL:
			case DETECTOR_RAIL:
				return false;
			default:
				return true;
		}
	}
	

}
