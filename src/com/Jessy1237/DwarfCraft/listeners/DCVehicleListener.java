package com.Jessy1237.DwarfCraft.listeners;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.DwarfVehicle;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.events.DwarfCraftEffectEvent;

public class DCVehicleListener implements Listener
{
    private final DwarfCraft plugin;

    public DCVehicleListener( final DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    /**
     * Called when a vehicle is destroyed
     * 
     * @param event
     */
    @EventHandler( priority = EventPriority.HIGHEST )
    public void onVehicleDestroy( VehicleDestroyEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getAttacker().getWorld() ) )
            return;

        boolean dropChange = false;

        if ( event.getVehicle() instanceof Boat && event.getAttacker() instanceof Player )
        {

            Player player = ( Player ) event.getAttacker();
            DCPlayer dcPlayer = plugin.getDataManager().find( player );
            Location loc = event.getVehicle().getLocation();

            for ( Skill skill : dcPlayer.getSkills().values() )
            {
                for ( Effect effect : skill.getEffects() )
                {
                    if ( effect.getEffectType() == EffectType.VEHICLEDROP )
                    {
                        ItemStack drop = effect.getOutput( dcPlayer );

                        DwarfCraftEffectEvent ev = new DwarfCraftEffectEvent( dcPlayer, effect, new ItemStack[] { new ItemStack( Material.BOAT, 1 ) }, new ItemStack[] { drop }, null, null, null, null,
                                event.getVehicle().getVehicle(), null, null );
                        plugin.getServer().getPluginManager().callEvent( ev );

                        if ( ev.isCancelled() )
                            return;

                        if ( DwarfCraft.debugMessagesThreshold < 6 )
                            System.out.println( "Debug: dropped " + drop.toString() );

                        for ( ItemStack i : ev.getAlteredItems() )
                        {
                            if ( i != null )
                            {
                                if ( i.getAmount() > 0 )
                                {
                                    loc.getWorld().dropItemNaturally( loc, i );
                                }
                            }
                        }

                        dropChange = true;
                    }
                }
            }
        }

        if ( dropChange )
        {
            event.getVehicle().remove();
            event.setCancelled( true );
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onVehicleEnter( VehicleEnterEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getVehicle().getWorld() ) )
            return;

        if ( !( event.getVehicle() instanceof Boat ) )
            return;
        plugin.getDataManager().addVehicle( new DwarfVehicle( event.getVehicle() ) );
        if ( DwarfCraft.debugMessagesThreshold < 6 )
            System.out.println( "DC6:Added DwarfVehicle to vehicleList" );
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onVehicleExit( VehicleExitEvent event )
    {
        plugin.getDataManager().removeVehicle( event.getVehicle() );
    }

    /**
     * Called when a vehicle moves.
     * 
     * @param event
     */
    @SuppressWarnings( "deprecation" )
    @EventHandler( priority = EventPriority.LOWEST )
    public void onVehicleMove( VehicleMoveEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getVehicle().getWorld() ) )
            return;

        if ( event.getVehicle().getPassenger() == null )
            return;
        if ( !( event.getVehicle() instanceof Boat ) )
            return;
        if ( !( event.getVehicle().getPassenger() instanceof Player ) )
            return;

        DCPlayer dCPlayer = plugin.getDataManager().find( ( Player ) event.getVehicle().getPassenger() );
        double effectAmount = 1.0;
        Effect effect = null;
        for ( Skill s : dCPlayer.getSkills().values() )
        {
            for ( Effect e : s.getEffects() )
            {
                if ( e.getEffectType() == EffectType.VEHICLEMOVE )
                {
                    effect = e;
                    effectAmount = e.getEffectAmount( dCPlayer );
                }
            }
        }

        DwarfVehicle dv = plugin.getDataManager().getVehicle( event.getVehicle() );
        if ( dv != null )
        {
            if ( !dv.changedSpeed() )
            {
                Boat boat = ( Boat ) event.getVehicle();

                //The original boat speed and altered boat speed are assigned to the damage variables
                DwarfCraftEffectEvent e = new DwarfCraftEffectEvent( dCPlayer, effect, null, null, null, null, boat.getMaxSpeed(), boat.getMaxSpeed() * effectAmount, event.getVehicle(), null, null );
                plugin.getServer().getPluginManager().callEvent( e );
                if ( !e.isCancelled() )
                {
                    boat.setMaxSpeed( e.getAlteredDamage() );
                    dv.speedChanged();
                }
            }
        }
    }
}
