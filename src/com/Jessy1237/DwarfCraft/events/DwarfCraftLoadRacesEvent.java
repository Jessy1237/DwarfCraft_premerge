package com.Jessy1237.DwarfCraft.events;

import java.util.ArrayList;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.Jessy1237.DwarfCraft.Race;

public class DwarfCraftLoadRacesEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private ArrayList<Race> races = new ArrayList<Race>();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * The event for when DwarfCraft loads all the races from the config file
     * but before it is set into the plugins memory. So you can inject, remove
     * or edit races into the plugin via this event
     * 
     * @param races
     *            The races that are going to be loaded into the DwarfCraft
     *            memory
     */
    public DwarfCraftLoadRacesEvent( ArrayList<Race> races )
    {
        this.races = races;
    }

    /**
     * Gets the races ArrayList
     * 
     * @return DCPlayer
     */
    @SuppressWarnings( "unchecked" )
    public ArrayList<Race> getRaces()
    {
        return ( ArrayList<Race> ) races.clone();
    }

    /**
     * Sets the races that will be stored in DwarfCraft memory.
     * 
     * @param races
     *            The races ArrayList to be loaded into the DwarfCraft memory
     * 
     */
    public void setRaces( ArrayList<Race> races )
    {
        this.races = races;
    }

    /**
     * Adds a races to the race ArrayList that is stored in the DwarfCraft
     * memory.
     * 
     * @param race
     *            A race to be added to the races ArrayList
     * 
     */
    public void addSkill( Race race )
    {
        
    }

    /**
     * Removes a race from the race ArrayList that is stored in the DwarfCraft
     * memory.
     * 
     * @param race
     *            A race to be removed from the races ArrayList
     * 
     */
    public void removeSkill( Race race )
    {
        races.remove( race );
    }
}
