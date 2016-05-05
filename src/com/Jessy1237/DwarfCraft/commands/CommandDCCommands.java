package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandDCCommands extends Command
{
    @SuppressWarnings( "unused" )
    private final DwarfCraft plugin;

    public CommandDCCommands( final DwarfCraft plugin )
    {
        super( "DCCommands" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
        {
            System.out.println( "DC1: started command 'dchelp'" );
        }
        sender.sendMessage( "DwarfCraft commands: dcdebug, dchelp, dcinfo, dcrules, tutorial, " + "dccommands, skillsheet, skillinfo, effectinfo, "
                + "race, races, setskill, creategreeter, createtrainer, listtrainers, dmem," );
        return true;
    }
}
