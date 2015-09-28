package com.Jessy1237.DwarfCraft.events;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
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
import com.Jessy1237.DwarfCraft.Util;

public class DCInventoryListener implements Listener {

	private DwarfCraft plugin;
	private HashMap<Location, BrewerInventory> stands = new HashMap<Location, BrewerInventory>();

	public DCInventoryListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onFurnaceExtractEvent(FurnaceExtractEvent event) {
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;
		
		DCPlayer player = plugin.getDataManager().find(event.getPlayer());
		HashMap<Integer, Skill> skills = player.getSkills();
		Material item = event.getItemType();
		final int amount = event.getItemAmount();

		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.SMELT && effect.checkInitiator(new ItemStack(item))) {
					item = effect.getOutput().getType();
					int newAmount = Util.randomAmount(amount * effect.getEffectAmount(player));
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

	@SuppressWarnings("deprecation")
	private void handleCrafting(InventoryClickEvent event) {
		HumanEntity player = event.getWhoClicked();
		ItemStack toCraft = event.getCurrentItem();
		ItemStack toStore = event.getCursor();

		// Make sure we are actually crafting anything
		if (player != null && hasItems(toCraft)) {

			// Make sure they aren't duping when repairing tools
			if (Util.isTool(toCraft.getTypeId())) {
				CraftingInventory ci = (CraftingInventory) event.getInventory();
				if (ci.getRecipe() instanceof ShapelessRecipe) {
					ShapelessRecipe r = (ShapelessRecipe) ci.getRecipe();
					for (ItemStack i : r.getIngredientList()) {
						if (Util.isTool(i.getTypeId()) && toCraft.getTypeId() == i.getTypeId()) {
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

							final ItemStack output = e.getOutput(dCPlayer, (byte) toCraft.getData().getData());

							float modifier = (float) output.getAmount() / (float) toCraft.getAmount();

							ItemStack check = null;

							if (toCraft.getTypeId() != output.getTypeId()) {
								check = toCraft;
								modifier = (float) (output.getAmount() + 1) / 1.0f;
							}

							player.setCanPickupItems(false);

							plugin.getServer().getScheduler().runTaskLater(plugin, new ShiftClickTask(dCPlayer, output, check, held, modifier, e), 5);

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

								final ItemStack output = e.getOutput(dCPlayer, (byte) toCraft.getData().getData());

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
				} else {
					event.setCancelled(true);
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

	// HotFix for when Result is ShiftClicked out of FurnaceExtractEvent until
	// spigot team fixes bug.
	@SuppressWarnings("deprecation")
	private void handleShiftClickFurnace(InventoryClickEvent event) {
		if (event.isShiftClick()) {
			Player player = (Player) event.getWhoClicked();
			DCPlayer dCPlayer = plugin.getDataManager().find((Player) event.getWhoClicked());
			HashMap<Integer, Skill> skills = dCPlayer.getSkills();
			ItemStack extract = event.getCurrentItem();

			for (Skill s : skills.values()) {
				for (Effect effect : s.getEffects()) {
					if (effect.getEffectType() == EffectType.SMELT && effect.checkInitiator(extract)) {

						int held = 0;
						for (ItemStack i : player.getInventory().all(extract.getType()).values()) {
							held += i.getAmount();
						}

						ItemStack output = effect.getOutput(dCPlayer, (byte) extract.getData().getData());

						// All Furnace recipes make 1 result item by default and
						// also item.getAmount() will be 0 due to spigot event
						// bug.
						float modifier = (float) (output.getAmount() + 1) / 1.0f;

						ItemStack check = null;
						if (extract.getTypeId() != output.getTypeId())
							check = extract;

						player.setCanPickupItems(false);

						plugin.getServer().getScheduler().runTaskLater(plugin, new ShiftClickTask(dCPlayer, output, check, held, modifier, effect), 5);
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

	@SuppressWarnings({ "deprecation", "incomplete-switch" })
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (!Util.isWorldAllowed(event.getWhoClicked().getWorld()))
			return;

		if (event.getInventory() != null && event.getSlotType() == SlotType.RESULT) {
			switch (event.getInventory().getType()) {
			case CRAFTING:
				handleCrafting(event);
				break;
			case WORKBENCH:
				handleCrafting(event);
				break;
			case FURNACE:
				handleShiftClickFurnace(event);
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

class ShiftClickTask implements Runnable {

	private DCPlayer p;
	private int init;
	private ItemStack item;
	private ItemStack check;
	private float modifier;
	private Effect e;

	public ShiftClickTask(DCPlayer p, final ItemStack item, ItemStack check, int init, float modifier, Effect e) {
		this.p = p;
		this.item = item;
		if (check == null) {
			this.check = item;
		} else {
			this.check = check;
		}
		this.init = init;
		this.modifier = modifier;
		this.e = e;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void run() {
		int held = 0;

		Collection<ItemStack> items = (Collection<ItemStack>) p.getPlayer().getInventory().all(check.getType()).values();
		// Check inventory count of the item
		for (ItemStack i : items) {
			held += i.getAmount();
		}

		// Checks if one of the effects has modified the amount of items in the
		// players inventory. We want to apply the modifier effects on the
		// Vanilla drops. We dont want them to stack with previous effect
		// modifiers.

		final int difference = held - init;
		if (modifier > 1) {
			final int amount = Util.randomAmount((modifier * difference - difference));

			// Added the amount from this effect into the limbo ItemStack

			// Adds the leftover items to the player
			for (int i = amount; i > 0; i -= item.getMaxStackSize()) {
				if (i > item.getMaxStackSize()) {
					p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), new ItemStack(item.getType(), item.getMaxStackSize(), item.getDurability()));
				} else {
					p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), new ItemStack(item.getType(), i, item.getDurability()));
				}
			}
			// Does nothing when the modifier is 0. Happens when extra items are
			// dropped from furnace events as its not the usual drop.
		} else if (modifier == 0) {

			// Takes away items from the inventory when the shift click crafts
			// more than it should do
		} else if (modifier < 1) {
			int amount = Util.randomAmount((difference - modifier * difference));
			p.getPlayer().getInventory().removeItem(new ItemStack(item.getType(), amount, item.getDurability()));
		}
		for (Skill s : p.getSkills().values()) {
			if (s.getEffects().contains(e)) {
				if (s.getEffects().indexOf(e) + 1 < s.getEffects().size()) {
					p.getPlayer().setCanPickupItems(true);
				}
			}
		}
	}
}
