package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DCCommandException.Type;
import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;

public class CommandEffectInfo extends Command
{
    private final DwarfCraft plugin;

    public CommandEffectInfo( final DwarfCraft plugin )
    {
        super( "EffectInfo" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
            System.out.println( "DC1: started command 'effectinfo'" );

        if ( args.length == 0 )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Usage.EFFECTINFO.getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Desc.EFFECTINFO.getDesc() );
        }
        else
        {
            try
            {
                CommandParser parser = new CommandParser( plugin, sender, args );
                List<Object> desiredArguments = new ArrayList<Object>();
                List<Object> outputList = null;

                DCPlayer dCPlayer = new DCPlayer( plugin, null );
                Effect effect = new Effect( null, plugin );
                desiredArguments.add( dCPlayer );
                desiredArguments.add( effect );
                try
                {
                    outputList = parser.parse( desiredArguments, false );
                    effect = ( Effect ) outputList.get( 1 );
                    dCPlayer = ( DCPlayer ) outputList.get( 0 );
                }
                catch ( DCCommandException dce )
                {
                    if ( dce.getType() == Type.PARSEDWARFFAIL || dce.getType() == Type.TOOFEWARGS )
                    {
                        desiredArguments.remove( 0 );
                        desiredArguments.add( dCPlayer );
                        outputList = parser.parse( desiredArguments, true );
                        effect = ( Effect ) outputList.get( 0 );
                        dCPlayer = ( DCPlayer ) outputList.get( 1 );
                    }
                    else
                        throw dce;
                }
                plugin.getOut().effectInfo( sender, dCPlayer, effect );
            }
            catch ( DCCommandException e )
            {
                e.describe( sender );
                sender.sendMessage( CommandInformation.Usage.EFFECTINFO.getUsage() );
                return false;
            }
        }
        return true;
    }
}
