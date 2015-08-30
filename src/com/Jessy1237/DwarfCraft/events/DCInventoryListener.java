package com.Jessy1237.DwarfCraft.events;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
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

	// Checks the itemID to see if it is a tool. Excludes fishing rod and, flint
	// and steel.
	private boolean isTool(int i) {
		if ((i >= 256 && i <= 258) || (i >= 267 && i <= 279) || (i >= 283 && i <= 286) || (i >= 290 && i <= 294)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void handleCrafting(InventoryClickEvent event) {

		HumanEntity player = event.getWhoClicked();
		ItemStack toCraft = event.getCurrentItem();
		ItemStack toStore = event.getCursor();

		// Make sure we are actually crafting anything
		if (player != null && hasItems(toCraft)) {

			// Make sure they aren't duping when repairing tools
			if (isTool(toCraft.getTypeId())) {
				CraftingInventory ci = (CraftingInventory) event.getInventory();
				if (ci.getRecipe() instanceof ShapelessRecipe) {
					ShapelessRecipe r = (ShapelessRecipe) ci.getRecipe();
					for (ItemStack i : r.getIngredientList()) {
						if (isTool(i.getTypeId()) && toCraft.getTypeId() == i.getTypeId()) {
							return;
						}
					}
				}
			}

			if (event.isShiftClick()) {
				DCPlayer dCPlayer = plugin.getDataManager().find((Player) player);
				for (Skill s : dCPlayer.getSkills().values()) {
					for (Effect e : s.getEffects()) {
						if (e.getEffectType() == EffectType.CRAFT && e.checkInitiator(toCraft.getTypeId(), (byte) toCraft.getData().getData())) {
							// Shift Click HotFix, checks inv for result item
							// before and then compares to after to modify the
							// amount of crafted items.
							int held = 0;
							for (ItemStack i : player.getInventory().all(toCraft.getType()).values()) {
								held += i.getAmount();
							}

							ItemStack output = e.getOutput(dCPlayer, (byte) toCraft.getData().getData());

							float modifier = (float) output.getAmount() / (float) toCraft.getAmount();

							plugin.getServer().getScheduler().runTaskLater(plugin, new ShiftCraftTask((Player) player, toCraft, held, modifier), 30);

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

								ItemStack output = e.getOutput(dCPlayer, (byte) toCraft.getData().getData());

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
			BrewingStand block = (BrewingStand) event.getInventory().getHolder();
			BrewerInventory inv = check(block.getLocation());
			ItemStack[] stack = inv.getContents();
			if (stack != null) {
				if (sameInv(stack, block.getInventory())) {
					for (Skill s : skills.values()) {
						for (Effect effect : s.getEffects()) {
							if (effect.getEffectType() == EffectType.BREW && effect.checkInitiator(item)) {
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

class ShiftCraftTask implements Runnable {

	Player p;
	int init;
	ItemStack item;
	float modifier;

	public ShiftCraftTask(Player p, final ItemStack item, int init, float modifier) {
		this.p = p;
		this.item = item;
		this.init = init;
		this.modifier = modifier;
	}

	@Override
	public void run() {
		int held = 0;
		for (ItemStack i : p.getInventory().all(item.getType()).values()) {
			held += i.getAmount();
		}
		final int difference = held - init;
		if (modifier > 1) {
			// Adds the leftover items to the player
			for (int i = Math.round((modifier * difference - difference)); i > 0; i -= item.getMaxStackSize()) {
				if (i > item.getMaxStackSize()) {
					p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(item.getType(), item.getMaxStackSize(), item.getDurability()));
				} else {
					p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(item.getType(), i, item.getDurability()));
				}
			}
			// Takes away items from the inventory when the shift click crafts
			// more than it should do
		} else if (modifier < 1) {
			int amount = Math.round((difference - modifier * difference));
			boolean run = true;
			while (run) {
				for (ItemStack i : p.getInventory().all(item.getType()).values()) {
					if (amount > 0) {
						if (i.getAmount() > amount) {
							i.setAmount(i.getAmount() - amount);
							amount = 0;
							run = false;
						} else {
							amount -= (i.getAmount() - 1);
							i.setAmount(1);
						}
					}
				}
			}
		}
	}
}
