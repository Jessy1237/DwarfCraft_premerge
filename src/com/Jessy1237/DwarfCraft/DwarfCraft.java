package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.Jessy1237.DwarfCraft.commands.CommandCreateGreeter;
import com.Jessy1237.DwarfCraft.commands.CommandCreateTrainer;
import com.Jessy1237.DwarfCraft.commands.CommandDCCommands;
import com.Jessy1237.DwarfCraft.commands.CommandDMem;
import com.Jessy1237.DwarfCraft.commands.CommandDebug;
import com.Jessy1237.DwarfCraft.commands.CommandEffectInfo;
import com.Jessy1237.DwarfCraft.commands.CommandHelp;
import com.Jessy1237.DwarfCraft.commands.CommandInfo;
import com.Jessy1237.DwarfCraft.commands.CommandListTrainers;
import com.Jessy1237.DwarfCraft.commands.CommandRace;
import com.Jessy1237.DwarfCraft.commands.CommandRaces;
import com.Jessy1237.DwarfCraft.commands.CommandRules;
import com.Jessy1237.DwarfCraft.commands.CommandSetSkill;
import com.Jessy1237.DwarfCraft.commands.CommandSkillInfo;
import com.Jessy1237.DwarfCraft.commands.CommandSkillSheet;
import com.Jessy1237.DwarfCraft.commands.CommandTutorial;
import com.Jessy1237.DwarfCraft.listeners.DCBlockListener;
import com.Jessy1237.DwarfCraft.listeners.DCEntityListener;
import com.Jessy1237.DwarfCraft.listeners.DCInventoryListener;
import com.Jessy1237.DwarfCraft.listeners.DCListener;
import com.Jessy1237.DwarfCraft.listeners.DCPlayerListener;
import com.Jessy1237.DwarfCraft.listeners.DCVehicleListener;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

/**
 * 
 * DwarfCraft is a RPG-like plugin for minecraft (via Spigot) that allows
 * players to improve their characters. Players may pay materials to a trainer
 * to improve a skill level, which will provide benefits such as increased
 * weapon damage, decreased tool durability drop, increased drops from blocks or
 * mobs, etc.
 * 
 * Data used for this plugin comes from two places: On each load, a list of
 * skills and effects is pulled from flatfiles. Dwarf's skill levels (currently
 * supports only sqlite)
 * 
 * @OriganlAuthor smartaleq
 * @OriginalAuthor RCarretta
 * @OriginalAuthor LexManos
 * 
 * @CurrentAuthor Jessy1237
 * 
 */
public class DwarfCraft extends JavaPlugin
{

    private final DCBlockListener blockListener = new DCBlockListener( this );
    private final DCPlayerListener playerListener = new DCPlayerListener( this );
    private final DCEntityListener entityListener = new DCEntityListener( this );
    private final DCVehicleListener vehicleListener = new DCVehicleListener( this );
    private final DCInventoryListener inventoryListener = new DCInventoryListener( this );
    private final DCListener dcListener = new DCListener( this );
    private NPCRegistry npcr;
    private ConfigManager cm;
    private DataManager dm;
    private Out out;
    private Consumer consumer = null;
    private Util util;
    private Permission perms = null;
    private Chat chat = null;

    public static int debugMessagesThreshold = 10;

    public NPCRegistry getNPCRegistry()
    {
        return npcr;
    }

    public ConfigManager getConfigManager()
    {
        return cm;
    }

    public DataManager getDataManager()
    {
        return dm;
    }

    public Out getOut()
    {
        return out;
    }

    public Consumer getConsumer()
    {
        return consumer;
    }

    public Util getUtil()
    {
        return util;
    }

    public DCEntityListener getDCEntityListener()
    {
        return entityListener;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration( Permission.class );
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean isPermissionEnabled()
    {
        return perms != null;
    }

    public Permission getPermission()
    {
        return perms;
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration( Chat.class );
        chat = rsp.getProvider();
        return chat != null;
    }

    public boolean isChatEnabled()
    {
        return chat != null;
    }

    public Chat getChat()
    {
        return chat;
    }

    private boolean checkPermission( CommandSender sender, String name, String type )
    {

        if ( perms == null )
            return false;

        if ( sender instanceof Player )
        {
            if ( type.equals( "op" ) )
            {
                return perms.has( ( Player ) sender, ( "DwarfCraft.op." + name ).toLowerCase() );
            }
            else if ( type.equals( "norm" ) )
            {
                return perms.has( ( Player ) sender, ( "DwarfCraft.norm." + name ).toLowerCase() );
            }
            else if ( type.equals( "all" ) )
            {
                return perms.has( ( Player ) sender, "DwarfCraft.*".toLowerCase() );
            }
        }

        return true;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String commandLabel, String[] args )
    {
        Command cmd = null;
        String name = command.getName();
        boolean hasNorm = checkPermission( sender, name, "norm" );
        boolean hasOp = checkPermission( sender, name, "op" );
        boolean hasAll = checkPermission( sender, name, "all" );
        boolean isCmd = true;

        if ( name.equalsIgnoreCase( "DCHelp" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandHelp( this );
            }
        }
        else if ( name.equalsIgnoreCase( "SkillSheet" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandSkillSheet( this );
            }
        }
        else if ( name.equalsIgnoreCase( "Tutorial" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandTutorial( this );
            }
        }
        else if ( name.equalsIgnoreCase( "DCInfo" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandInfo( this );
            }
        }
        else if ( name.equalsIgnoreCase( "DCRules" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandRules( this );
            }
        }
        else if ( name.equalsIgnoreCase( "DCCommands" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandDCCommands( this );
            }
        }
        else if ( name.equalsIgnoreCase( "SkillInfo" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandSkillInfo( this );
            }
        }
        else if ( name.equalsIgnoreCase( "Race" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandRace( this );
            }
        }
        else if ( name.equalsIgnoreCase( "EffectInfo" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandEffectInfo( this );
            }
        }
        else if ( name.equalsIgnoreCase( "DCDebug" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandDebug( this );
            }
        }
        else if ( name.equalsIgnoreCase( "ListTrainers" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandListTrainers( this );
            }
        }
        else if ( name.equalsIgnoreCase( "SetSkill" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandSetSkill( this );
            }
        }
        else if ( name.equalsIgnoreCase( "CreateGreeter" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandCreateGreeter( this );
            }
        }
        else if ( name.equalsIgnoreCase( "CreateTrainer" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandCreateTrainer( this );
            }
        }
        else if ( name.equalsIgnoreCase( "DMem" ) )
        {
            if ( hasOp || hasAll )
            {
                cmd = new CommandDMem( this );
            }
        }
        else if ( name.equalsIgnoreCase( "Races" ) )
        {
            if ( hasNorm || hasAll )
            {
                cmd = new CommandRaces( this );
            }
        }
        else
        {
            isCmd = false;
        }

        if ( cmd == null )
        {
            if ( isCmd == false )
            {
                return false;
            }
            else
            {
                if ( hasNorm == false || hasOp == false )
                {
                    sender.sendMessage( "§4You do not have permission to do that." );
                }
                return true;
            }
        }
        else
        {
            return cmd.execute( sender, commandLabel, args );
        }
    }

    /**
     * Called upon disabling the plugin.
     */
    @Override
    public void onDisable()
    {
    }

    /**
     * Called upon enabling the plugin
     */
    @Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();

        if ( pm.getPlugin( "Vault" ) == null || pm.getPlugin( "Vault" ).isEnabled() == false )
        {
            System.out.println( "[DwarfCraft] Couldn't find Vault!" );
            System.out.println( "[DwarfCraft] DwarfCraft now disabiling..." );
            pm.disablePlugin( this );
            return;
        }

        try
        {
            setupPermissions();
            setupChat();
        }
        catch ( Exception e )
        {
            System.out.println( "[DwarfCraft] Unable to find a permissions plugin." );
            pm.disablePlugin( this );
            return;
        }

        if ( !isPermissionEnabled() )
        {
            System.out.println( "[DwarfCraft] Unable to find a permissions plugin." );
            pm.disablePlugin( this );
            return;
        }

        pm.registerEvents( playerListener, this );

        pm.registerEvents( entityListener, this );

        pm.registerEvents( blockListener, this );

        pm.registerEvents( vehicleListener, this );

        pm.registerEvents( inventoryListener, this );

        pm.registerEvents( dcListener, this );

        if ( pm.getPlugin( "Citizens" ) == null || pm.getPlugin( "Citizens" ).isEnabled() == false )
        {
            System.out.println( "[DwarfCraft] Couldn't find Citizens!" );
            System.out.println( "[DwarfCraft] DwarfCraft now disabiling..." );
            pm.disablePlugin( this );
            return;
        }
        System.out.println( "[DwarfCraft] Hooked into Citizens!" );

        CitizensAPI.getTraitFactory().registerTrait( TraitInfo.create( DwarfTrainerTrait.class ).withName( "DwarfTrainer" ) );

        npcr = CitizensAPI.getNPCRegistry();
        util = new Util( this );
        cm = new ConfigManager( this, getDataFolder().getAbsolutePath(), "DwarfCraft.config" );
        dm = new DataManager( this, cm );
        new Messages( this );

        dm.dbInitialize();

        out = new Out( this );

        // readGreeterMessagesfile() depends on datamanager existing, so this
        // has to go here
        if ( !getConfigManager().readGreeterMessagesfile() )
        {
            System.out.println( "[SEVERE] Failed to read DwarfCraft Greeter Messages)" );
            getServer().getPluginManager().disablePlugin( this );
        }

        for ( Player player : getServer().getOnlinePlayers() )
        {
            DCPlayer dCPlayer = getDataManager().find( player );
            if ( dCPlayer == null )
                dCPlayer = getDataManager().createDwarf( player );
            getDataManager().checkDwarfData( dCPlayer );
        }

        if ( pm.getPlugin( "LogBlock" ) != null )
        {
            consumer = ( ( LogBlock ) pm.getPlugin( "LogBlock" ) ).getConsumer();
            System.out.println( "[DwarfCraft] Hooked into LogBlock!" );
        }
        else
        {
            System.out.println( "[DwarfCraft] Couldn't find LogBlock!" );
        }

        System.out.println( "[DwarfCraft]" + getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!" );
    }

    public void despawnById( int ID )
    {
        NPC npc = getNPCRegistry().getById( ID );
        npc.despawn( DespawnReason.REMOVAL );
        getNPCRegistry().deregister( npc );
    }
}
