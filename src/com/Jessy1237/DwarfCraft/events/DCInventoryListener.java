package com.Jessy1237.DwarfCraft.events;

import java.util.HashMap;

import net.minecraft.util.com.google.common.base.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

	private boolean hasItems(ItemStack stack) {
		return stack != null && stack.getAmount() > 0;
	}

	private void handleCrafting(InventoryClickEvent event) {

		HumanEntity player = event.getWhoClicked();
		ItemStack toCraft = event.getCurrentItem();
		ItemStack toStore = event.getCursor();
		final int origAmount = toCraft.getAmount();

		// Make sure we are actually crafting anything
		if (player != null && hasItems(toCraft)) {

			if (event.isShiftClick()) {
				DCPlayer dCPlayer = plugin.getDataManager().find((Player) player);
				for (Skill s : dCPlayer.getSkills().values()) {
					for (Effect e : s.getEffects()) {
						if (e.getEffectType() == EffectType.CRAFT && e.checkInitiator(toCraft.getTypeId(), (byte) toCraft.getData().getData())) {
							event.setCancelled(true);
						}
					}
				}
			} else {
				// The items are stored in the cursor. Make sure there's enough
				// space.
				if (isStackSumLegal(toCraft, toStore)) {

					DCPlayer dCPlayer = plugin.getDataManager().find((Player) event.getWhoClicked());

					for (Skill s : dCPlayer.getSkills().values()) {
						for (Effect e : s.getEffects()) {
							if (e.getEffectType() == EffectType.CRAFT && e.checkInitiator(toCraft.getTypeId(), (byte) toCraft.getData().getData())) {

								org.bukkit.inventory.ItemStack output = e.getOutput(dCPlayer, (byte) toCraft.getData().getData());

								if (output.getAmount() == 0)
									toCraft = null;
								else {
									toCraft.setAmount(output.getAmount());
									if (output.getData() != null)
										toCraft.getData().setData(output.getData().getData());
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean hasSameItem(ItemStack a, ItemStack b) {
		if (a == null)
			return b == null;
		else if (b == null)
			return a == null;

		return a.getTypeId() == b.getTypeId() && a.getDurability() == b.getDurability() && Objects.equal(a.getData(), b.getData()) && Objects.equal(a.getEnchantments(), b.getEnchantments());
	}

	private boolean isStackSumLegal(ItemStack a, ItemStack b) {
		// See if we can create a new item stack with the combined elements of a
		// and b
		if (a == null || b == null)
			return true; // Treat null as an empty stack
		else
			return a.getAmount() + b.getAmount() <= a.getType().getMaxStackSize();
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

	@SuppressWarnings({ "deprecation", "incomplete-switch" })
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

		if (event.getInventory() != null && event.getSlotType() == SlotType.RESULT) {

			switch (event.getInventory().getType()) {
			case CRAFTING:
				handleCrafting(event);
				break;
			case WORKBENCH:
				handleCrafting(event);
				break;
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
