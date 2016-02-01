package com.Jessy1237.DwarfCraft.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.Effect;

public class DwarfCraftEffectEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private DCPlayer player;
	private Effect effect;
	private ItemStack[] orig;
	private ItemStack[] altered;
	private Integer origHunger;
	private Integer newHunger;
	private Double origDmg;
	private Double newDmg;
	private Entity entity;
	private Block block;
	private ItemStack itemInHand;

	/**
	 * The event for when an Effect is fired. This event is fired after the
	 * effect is fired but before any of the stats are applied to the player/game.
	 * 
	 * @param player
	 *            The player that fired the effect
	 * @param Effect
	 *            The effect that was fired
	 * @param orig
	 *            The original itemstack contains the original drops/smelted/crafted/etc
	 * @param altered
	 *            The altered itemstack contains all items that the effect
	 *            alters/adds
	 * @param origHunger
	 *            The original hunger added to the player before the effect. Put null
	 *            if the players hunger is not altered
	 * @param newHunger
	 *            The altered hunger added to the player. Put null if the players
	 *            hunger is not altered
	 * @param orgDmg
	 *            The original Damgage taken by or given by the player before
	 *            the event, can also be damage done to a tool. Put null if the damage is not altered
	 * @param newDmg
	 *            The altered damage taken by or given by the player, can also be damage done to a tool. Put null
	 *            if the damage is not altered
	 * @param entity
	 *            The Entity that is involved with the effect. Put null if no
	 *            entity other than the player is involved.
	 * @param block
	 *            The block that is involved with the effect. i.e the block
	 *            broken or the block that contains the inventory used. Put null
	 *            if no block is involved in the event.
	 * @param itemInHand
	 *            The item that was in the hand of the player and that also
	 *            allowed the effect to fire. i.e. a sword when killing an
	 *            entity.
	 */
	public DwarfCraftEffectEvent(DCPlayer player, Effect effect, ItemStack[] orig, final ItemStack[] altered, Integer origHunger, Integer newHunger, Double origDmg, Double newDmg, Entity entity, Block block, ItemStack itemInHand) {
		this.player = player;
		this.effect = effect;
		this.orig = orig;
		this.altered = altered;
		this.origHunger = origHunger;
		this.newHunger = newHunger;
		this.origDmg = origDmg;
		this.newDmg = newDmg;
		this.entity = entity;
		this.block = block;
		this.itemInHand = itemInHand;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	/**
	 * Gets the DCPlayer that leveled up a skill.
	 * 
	 * @return DCPlayer
	 */
	public DCPlayer getDCPlayer() {
		return player;
	}

	/**
	 * Gets the Effect that was fired.
	 * 
	 * @return Effect
	 */
	public Effect getEffect() {
		return effect;
	}

	/**
	 * Gets an ItemStack Array containing the original items that wouldve been dropped/smelter/etc or null
	 * if no items were altered.
	 * 
	 * @return ItemStack[]
	 */
	public ItemStack[] getOriginalItems() {
		return orig;
	}

	/**
	 * Gets an ItemStack Array containing the new Items altered by DwarfCraft.
	 * i.e. new Mob drops, block drops, craft drops, etc. Will return null if
	 * there were no altered items.
	 * 
	 * @return ItemStack[]
	 */
	public ItemStack[] getAlteredItems() {
		return altered;
	}

	/**
	 * Gets the players added original hunger before the effect was fired. Will return
	 * null if the effect doesn't alter the players hunger.
	 * 
	 * @return Integer
	 */
	public Integer getOriginalHunger() {
		return origHunger;
	}

	/**
	 * Gets the players added altered hunger after the effect was fired. Will return
	 * null if the effect doesn't alter the players hunger.
	 * 
	 * @return Integer
	 */
	public Integer getAlteredHunger() {
		return newHunger;
	}

	/**
	 * Sets the players altered hunger. If the effect does change the players
	 * hunger then changing this value with do nothing.
	 * 
	 * @param newHunger
	 *            The altered hunger of the player after the effect takes place.
	 * 
	 */
	public void setAlteredHunger(int newHunger) {
		this.newHunger = newHunger;
	}

	/**
	 * Gets the original taken by or given by the player, can also be the damage to a tool. Will return null if
	 * the effect doesn't alter damage.
	 * 
	 * @return Integer
	 */
	public Double getOriginalDamage() {
		return origDmg;
	}

	/**
	 * Gets the altered taken by or given by the player, can also be the damage to a tool. Will return null if the
	 * effect doesn't alter damage.
	 * 
	 * @return Integer
	 */
	public Double getAlteredDamage() {
		return newDmg;
	}

	/**
	 * Sets the altered damage taken by or given by the player, can also be the damage to a tool. If the effect
	 * doesn't altered damage then changing this value will do nothing.
	 * 
	 * @param newDmg
	 *            The altered dmg taken by or given by the player after the
	 *            effect takes place.
	 * 
	 */
	public void setAlteredDamage(double newDmg) {
		this.newDmg = newDmg;
	}

	/**
	 * Gets the entity that is involved with the effect. If there is no entity
	 * involved with the event other then the player then this will return null
	 * 
	 * @return Entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the block involved with the effect, i.e. the block broken or the
	 * block that contains the inventory used (furnace, workbench, etc.). Will
	 * return null if no block is involved.
	 * 
	 * @return Block
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * Gets the item in the hand slot of the player. Will return null if the
	 * item in hand didnt help fire the effect of if the player didn't have an
	 * item in hand
	 * 
	 * @return ItemStack
	 */
	public ItemStack getItemInHand() {
		return itemInHand;
	}
}
