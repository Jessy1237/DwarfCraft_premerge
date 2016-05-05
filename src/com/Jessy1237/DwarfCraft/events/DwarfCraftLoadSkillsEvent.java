package com.Jessy1237.DwarfCraft.events;

import java.util.HashMap;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.Jessy1237.DwarfCraft.Skill;

public class DwarfCraftLoadSkillsEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private HashMap<Integer, Skill> skills = new HashMap<Integer, Skill>();

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
     * The event for when DwarfCraft loads all the skills from the csv file but
     * before it is set into the plugins memory. So you can inject or remove
     * skills into the plugin via this event
     * 
     * @param skills
     *            the skills that were loaded by DwarfCraft from the csv file.
     *            The key is the skill ID, the value is the skill.
     */
    public DwarfCraftLoadSkillsEvent( HashMap<Integer, Skill> skills )
    {
        this.skills = skills;
    }

    /**
     * Gets the skills HashMap, the key is the skillID and the value is the
     * Skill
     * 
     * @return DCPlayer
     */
    @SuppressWarnings( "unchecked" )
    public HashMap<Integer, Skill> getSkills()
    {
        return ( HashMap<Integer, Skill> ) skills.clone();
    }

    /**
     * Sets the skills that will be stored in DwarfCrafts memory.
     * 
     * @param skills
     *            The skills HashMap, The key is the skillID, the value is the
     *            Skill.
     * 
     */
    public void setSkills( HashMap<Integer, Skill> skills )
    {
        this.skills = skills;
    }

    /**
     * Adds a skill to the Skill HashMap that is stored in the DwarfCraft
     * memory.
     * 
     * @param skill
     *            A skill to be added to the skills HashMap
     * 
     */
    public void addSkill( Skill skill )
    {
        skills.put( skill.getId(), skill );
    }

    /**
     * Removes a skill from the Skill HashMap that is stored in the DwarfCraft
     * memory.
     * 
     * @param skill
     *            A skill to be removed from the skills HashMap
     * 
     */
    public void removeSkill( Skill skill )
    {
        skills.remove( skill );
    }
}
