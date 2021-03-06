package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.CommandInformation.Usage;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DCCommandException.Type;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandTutorial extends Command
{
    private final DwarfCraft plugin;

    public CommandTutorial( final DwarfCraft plugin )
    {
        super( "Tutorial" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        try
        {

            CommandParser parser = new CommandParser( plugin, sender, args );
            List<Object> desiredArguments = new ArrayList<Object>();
            List<Object> outputList = null;

            if ( DwarfCraft.debugMessagesThreshold < 1 )
                System.out.println( "DC1: started command 'tutorial'" );
            int page = 0;
            desiredArguments.add( page );

            try
            {
                outputList = parser.parse( desiredArguments, false );
                page = ( Integer ) outputList.get( 0 );
            }
            catch ( DCCommandException e )
            {
                if ( e.getType() == Type.TOOFEWARGS )
                    page = 1;
                else
                    throw e;
            }

            if ( page < 0 || page > 6 )
                throw new DCCommandException( plugin, Type.PAGENUMBERNOTFOUND );
            plugin.getOut().tutorial( sender, page );
            return true;

        }
        catch ( DCCommandException e )
        {
            e.describe( sender );
            sender.sendMessage( Usage.TUTORIAL.getUsage() );
            return false;
        }
    }
}
