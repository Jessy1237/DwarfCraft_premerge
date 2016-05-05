package com.Jessy1237.DwarfCraft.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfTrainer;
import com.Jessy1237.DwarfCraft.Skill;

public class DwarfCraftLevelUpEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private DCPlayer player;
    private DwarfTrainer trainer;
    private Skill skill;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled( boolean cancel )
    {
        cancelled = cancel;
    }

    /**
     * The event for when a DCPlayer levels up their skill. This event is fired
     * after the skill is levelled but before the data is saved.
     * 
     * @param player
     *            the player that levelled up a skill
     * @param trainer
     *            the trainer that was used to level up the skill
     * @param skill
     *            the skill that was levelled up
     */
    public DwarfCraftLevelUpEvent( DCPlayer player, DwarfTrainer trainer, Skill skill )
    {
        this.player = player;
        this.trainer = trainer;
        this.skill = skill;
    }

    /**
     * Gets the DCPlayer that levelled up a skill.
     * 
     * @return DCPlayer
     */
    public DCPlayer getDCPlayer()
    {
        return player;
    }

    /**
     * Gets the trainer that was used to level up the skill.
     * 
     * @return Trainer
     */
    public DwarfTrainer getTrainer()
    {
        return trainer;
    }

    /**
     * Gets the skill that was levelled up.
     * 
     * @return Skill
     */
    public Skill getSkill()
    {
        return skill;
    }
}
