package com.Jessy1237.DwarfCraft.events;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;

public class DCInventoryListener implements Listener {

	private DwarfCraft plugin;
	private HashMap<Location, BrewerInventory> stands = new HashMap<Location, BrewerInventory>();

	public DCInventoryListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onFurnaceExtractEvent(FurnaceExtractEvent event) {
		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getPlayer().getWorld() == w) {
						return;
					}
				}
			}
		}

		DCPlayer player = plugin.getDataManager().find(event.getPlayer());
		HashMap<Integer, Skill> skills = player.getSkills();
		Material item = event.getItemType();
		final int amount = event.getItemAmount();

		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.SMELT && effect.checkInitiator(new ItemStack(item))) {
					item = effect.getOutput().getType();
					int newAmount = (int) (amount * effect.getEffectAmount(player));
					int i = 0;
					while (i != newAmount) {
						ItemStack itemstack;
						if ((newAmount - i) < 64) {
							itemstack = new ItemStack(item, (newAmount - i));
							i = newAmount;
						} else {
							itemstack = new ItemStack(item, 64);
							i = i + 64;
						}
						player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), itemstack);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCraftItemEvent(CraftItemEvent event) {
		DCPlayer dCPlayer = plugin.getDataManager().find((Player)event.getWhoClicked());
		ItemStack outputStack = event.getCurrentItem();
		
		for (Skill s : dCPlayer.getSkills().values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.CRAFT && e.checkInitiator(outputStack.getTypeId(), (byte)outputStack.getData().getData())){

					org.bukkit.inventory.ItemStack output = e.getOutput(dCPlayer, (byte)outputStack.getData().getData());

					if (output.getAmount() == 0)
						outputStack = null;
					else{
						outputStack.setAmount(output.getAmount());
						if (output.getData() != null)
							outputStack.getData().setData(output.getData().getData());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBrewEvent(BrewEvent event) {
		if (!stands.containsKey(event.getBlock())) {
			stands.put(event.getBlock().getLocation(), event.getContents());
		} else {
			stands.remove(event.getBlock().getLocation());
			stands.put(event.getBlock().getLocation(), event.getContents());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getWhoClicked().getWorld() == w) {
						return;
					}
				}
			}
		}
		if (event.getSlotType() == SlotType.CRAFTING && (event.isLeftClick() || event.isShiftClick()) && event.getInventory().getHolder() instanceof BrewingStand) {
			DCPlayer player = plugin.getDataManager().find((Player) event.getWhoClicked());
			HashMap<Integer, Skill> skills = player.getSkills();
			ItemStack item = event.getCurrentItem();
			final int amount = item.getAmount();
			System.out.println("1");
			BrewingStand block = (BrewingStand) event.getInventory().getHolder();
			BrewerInventory inv = check(block.getLocation());
			ItemStack[] stack = inv.getContents();
			if (stack != null) {
				System.out.println("2");
				if (sameInv(stack, block.getInventory())) {
					System.out.println("3");
					for (Skill s : skills.values()) {
						for (Effect effect : s.getEffects()) {
							if (effect.getEffectType() == EffectType.BREW && effect.checkInitiator(item)) {
								System.out.println("4");
								int newAmount = (int) (amount * effect.getEffectAmount(player));
								for (int n = 0; n != stack.length; n++) {
									ItemStack it = stack[n];
									if (it != null) {
										int i = 1;
										if (inv.getItem(3).getTypeId() != it.getTypeId()) {
											while (i != newAmount) {
												ItemStack itemstack;
												if ((newAmount - i) < 64) {
													itemstack = new ItemStack(it.getType(), (newAmount - i), it.getData().getData());
													itemstack.setData(new MaterialData(it.getTypeId(), it.getData().getData()));
													i = newAmount;
												} else {
													itemstack = new ItemStack(item.getType(), 64, item.getData().getData());
													itemstack.setData(new MaterialData(it.getTypeId(), it.getData().getData()));
													i = i + 64;
												}
												player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), itemstack);
											}
										}
									}
								}
								stands.remove(block.getLocation());
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean sameInv(ItemStack[] orig, BrewerInventory new1) {
		for (int n = 0; n != orig.length; n++) {
			ItemStack i = orig[n];
			if (i != null) {
				if (new1.contains(i) && i.getTypeId() == 373) {
					if (new1.getItem(n).getData().getData() == i.getData().getData()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public BrewerInventory check(Location l) {
		for (Location l1 : stands.keySet()) {
			if (l1 != null) {
				if (l1.getX() == l.getX() && l1.getY() == l.getY() && l1.getZ() == l.getZ()) {
					return stands.get(l1);
				}
			}
		}
		return null;
	}
}
