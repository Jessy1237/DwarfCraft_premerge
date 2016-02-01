package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class DataManager {

	private List<DCPlayer> dwarves = new ArrayList<DCPlayer>();
	private List<DwarfVehicle> vehicleList = new ArrayList<DwarfVehicle>();
	public HashMap<Integer, DwarfTrainer> trainerList = new HashMap<Integer, DwarfTrainer>();
	private HashMap<String, GreeterMessage> greeterMessageList = new HashMap<String, GreeterMessage>();
	private final ConfigManager configManager;
	private final DwarfCraft plugin;
	private Connection mDBCon;

	protected DataManager(DwarfCraft plugin, ConfigManager cm) {
		this.plugin = plugin;
		this.configManager = cm;
	}

	public void addVehicle(DwarfVehicle v) {
		vehicleList.add(v);
	}

	/**
	 * this is untested and quite a lot of new code, it will probably fail
	 * several times. no way to bugfix currently. Just praying it works
	 * 
	 * @param oldVersion
	 */
	private void buildDB(int oldVersion) {
		try {
			Statement statement = mDBCon.createStatement();
			ResultSet rs = statement.executeQuery("select * from sqlite_master WHERE name = 'players';");
			if (!rs.next()) {
				statement.executeUpdate("create table players ( id INTEGER PRIMARY KEY, uuid, race );");
			}
			rs.close();

			rs = statement.executeQuery("select * from sqlite_master WHERE name = 'skills';");
			if (!rs.next()) {
				statement.executeUpdate("CREATE TABLE 'skills' " + "  ( " + "    'player' INT, " + "    'id' int, " + "    'level' INT DEFAULT 0, " + "    'deposit1' INT DEFAULT 0, " + "    'deposit2' INT DEFAULT 0, " + "    'deposit3' INT DEFAULT 0, " + "    PRIMARY KEY ('player','id') " + "  );");
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println("[SEVERE]DB not built successfully");
			e.printStackTrace();
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	public boolean checkTrainersInChunk(Chunk chunk) {
		for (Iterator<Map.Entry<Integer, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Integer, DwarfTrainer> pairs = i.next();
			DwarfTrainer d = (pairs.getValue());
			if (Math.abs(chunk.getX() - d.getLocation().getBlock().getChunk().getX()) > 1)
				continue;
			if (Math.abs(chunk.getZ() - d.getLocation().getBlock().getChunk().getZ()) > 1)
				continue;
			return true;
		}
		return false;
	}

	public DCPlayer createDwarf(Player player) {
		DCPlayer newDwarf = new DCPlayer(plugin, player);
		newDwarf.setRace(plugin.getConfigManager().getDefaultRace());
		newDwarf.setSkills(plugin.getConfigManager().getAllSkills());

		for (Skill skill : newDwarf.getSkills().values()) {
			skill.setLevel(0);
			skill.setDeposit1(0);
			skill.setDeposit2(0);
			skill.setDeposit3(0);
		}

		if (player != null)
			dwarves.add(newDwarf);
		return newDwarf;
	}

	@SuppressWarnings("deprecation")
	protected void dbInitialize() {
		try {
			Class.forName("org.sqlite.JDBC");
			mDBCon = DriverManager.getConnection("jdbc:sqlite:" + configManager.getDbPath());
			Statement statement = mDBCon.createStatement();
			ResultSet rs = statement.executeQuery("select * from sqlite_master WHERE name = 'players';");
			if (!rs.next()) {
				buildDB(0);
			}

			// check for update to skill deposits
			try {
				rs = statement.executeQuery("SELECT deposit1 FROM skills;");
			} catch (SQLException ex) {
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit1 NUMERIC DEFAULT 0;");
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit2 NUMERIC DEFAULT 0;");
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit3 NUMERIC DEFAULT 0;");
			}

			try {
				rs = statement.executeQuery("select uuid from players");
			} catch (Exception e) {
				System.out.println("[DwarfCraft] Converting Player DB (may lag a little wait for completion message).");
				mDBCon.setAutoCommit(false);
				HashMap<UUID, String> dcplayers = new HashMap<UUID, String>();
				HashMap<UUID, Integer> ids = new HashMap<UUID, Integer>();

				try {
					PreparedStatement prep = mDBCon.prepareStatement("SELECT * FROM players");
					rs = prep.executeQuery();

					while (rs.next()) {
						dcplayers.put(plugin.getServer().getOfflinePlayer(rs.getString("name")).getUniqueId(), rs.getString("race"));
						ids.put(plugin.getServer().getOfflinePlayer(rs.getString("name")).getUniqueId(), rs.getInt("id"));
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				statement.executeUpdate("DROP TABLE players");
				statement.executeUpdate("create table players ( id INTEGER PRIMARY KEY, uuid, race );");
				for (UUID uuid : dcplayers.keySet()) {
					if (uuid != null) {
						PreparedStatement prep = mDBCon.prepareStatement("insert into players(id, uuid, race) values(?,?,?);");
						prep.setInt(1, ids.get(uuid));
						prep.setString(2, uuid.toString());
						prep.setString(3, dcplayers.get(uuid));
						prep.execute();
						prep.close();
					}
				}
				System.out.println("[DwarfCraft] Finished Converting the Players DB.");
			}

			try {
				rs = statement.executeQuery("select * from sqlite_master WHERE name = 'trainers';");
				if (rs.next()) {
					System.out.println("[DwarfCraft] Transfering Trainer DB to citizens  (may lag a little wait for completion message).");

					rs = statement.executeQuery("select * from trainers;");

					while (rs.next()) {
						AbstractNPC npc1;
						if (rs.getString("type").equalsIgnoreCase("PLAYER")) {
							npc1 = (AbstractNPC) plugin.getNPCRegistry().createNPC(EntityType.PLAYER, UUID.randomUUID(), Integer.parseInt(rs.getString("uniqueId")), rs.getString("name"));
						} else {
							npc1 = (AbstractNPC) plugin.getNPCRegistry().createNPC(EntityType.fromName(rs.getString("type")), UUID.randomUUID(), Integer.parseInt(rs.getString("uniqueId")), rs.getString("name"));
						}
						npc1.spawn(new Location(plugin.getServer().getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
						npc1.addTrait(new DwarfTrainerTrait(plugin, Integer.parseInt(rs.getString("uniqueId")), rs.getInt("skill"), rs.getInt("maxSkill"), rs.getInt("minSkill"), rs.getBoolean("isGreeter"), rs.getString("messageId")));
						npc1.setProtected(true);
					}
				}
				statement.execute("DROP TABLE trainers");
				System.out.println("[DwarfCraft] Finished Transfering the Trainers DB.");
			} catch (Exception e) {

			}
			/*
			 * Old conversion for Trainer tables based on old trainer code try {
			 * rs = statement.executeQuery("select minSkill from trainers"); }
			 * catch (Exception e) { System.out.println(
			 * "[DwarfCraft] Converting Trainer DB (may lag a little wait for completion message)."
			 * ); mDBCon.setAutoCommit(false); ArrayList<DwarfTrainer> trainers
			 * = new ArrayList<DwarfTrainer>();
			 * 
			 * for (Iterator<World> i =
			 * plugin.getServer().getWorlds().iterator(); i.hasNext();) { try {
			 * final World world = i.next(); PreparedStatement prep =
			 * mDBCon.prepareStatement
			 * ("SELECT * FROM trainers WHERE world = ?;"); prep.setString(1,
			 * world.getName()); rs = prep.executeQuery();
			 * 
			 * while (rs.next()) { if
			 * (world.getName().equals(rs.getString("world"))) { if
			 * (DwarfCraft.debugMessagesThreshold < 5)
			 * System.out.println("DC5: trainer: " + rs.getString("name") +
			 * " in world: " + world.getName());
			 * 
			 * 
			 * DwarfTrainer trainer = new DwarfTrainer(plugin, new
			 * Location(world, rs.getDouble("x"), rs.getDouble("y"),
			 * rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")),
			 * rs.getInt("uniqueId"), rs.getString("name"), rs.getInt("skill"),
			 * rs.getInt("maxSkill"), -1, rs.getString("messageId"),
			 * rs.getBoolean("isGreeter"), false, 0);
			 * 
			 * trainers.add(trainer); } } prep.close();
			 * System.out.println("[DwarfCraft] Finished Converting the Trainer DB."
			 * ); } catch (Exception e1) { e1.printStackTrace(); } } }
			 * 
			 * try { rs = statement.executeQuery("select type from trainers"); }
			 * catch (Exception e) { System.out.println(
			 * "[DwarfCraft] Converting Trainer DB to include Trainer Types (may lag a little wait for completion message)."
			 * ); mDBCon.setAutoCommit(false); ArrayList<DwarfTrainer> trainers
			 * = new ArrayList<DwarfTrainer>();
			 * 
			 * for (Iterator<World> i =
			 * plugin.getServer().getWorlds().iterator(); i.hasNext();) { try {
			 * final World world = i.next(); PreparedStatement prep =
			 * mDBCon.prepareStatement
			 * ("SELECT * FROM trainers WHERE world = ?;"); prep.setString(1,
			 * world.getName()); rs = prep.executeQuery();
			 * 
			 * while (rs.next()) { if
			 * (world.getName().equals(rs.getString("world"))) { if
			 * (DwarfCraft.debugMessagesThreshold < 5)
			 * System.out.println("DC5: trainer: " + rs.getString("name") +
			 * " in world: " + world.getName());
			 * 
			 * DwarfTrainer trainer = new DwarfTrainer(plugin, new
			 * Location(world, rs.getDouble("x"), rs.getDouble("y"),
			 * rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")),
			 * rs.getInt("uniqueId"), rs.getString("name"), rs.getInt("skill"),
			 * rs.getInt("maxSkill"), -1, rs.getString("messageId"),
			 * rs.getBoolean("isGreeter"), false, 0, "PLAYER");
			 * 
			 * trainers.add(trainer); } } rs.close();
			 * mDBCon.setAutoCommit(true); prep.close(); } catch (Exception e1)
			 * { e1.printStackTrace(); } }
			 * statement.execute("DROP TABLE trainers");
			 * statement.executeUpdate("create table trainers " + "  (" +
			 * "    world, uniqueId, name, skill, maxSkill, minSkill, material, isGreeter, messageId, "
			 * + "    x, y, z, yaw, pitch" + ", type  );"); for (DwarfTrainer d
			 * : trainers) { if (d != null) { PreparedStatement prep =
			 * mDBCon.prepareStatement
			 * ("insert into trainers values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			 * prep.setString(1, d.getWorld().getName()); prep.setInt(2,
			 * d.getUniqueId()); prep.setString(3, d.getName()); prep.setInt(4,
			 * d.getSkillTrained()); prep.setInt(5, d.getMaxSkill());
			 * prep.setInt(6, d.getMinSkill()); prep.setInt(7, d.getMaterial());
			 * prep.setBoolean(8, d.isGreeter()); prep.setString(9,
			 * d.getMessage()); prep.setDouble(10, d.getLocation().getX());
			 * prep.setDouble(11, d.getLocation().getY()); prep.setDouble(12,
			 * d.getLocation().getZ()); prep.setFloat(13,
			 * d.getLocation().getYaw()); prep.setFloat(14,
			 * d.getLocation().getPitch()); prep.setString(15, d.getType());
			 * prep.execute(); prep.close(); } } trainers.clear();
			 * mDBCon.commit();
			 * System.out.println("[DwarfCraft] Finished converting Trainer table!"
			 * ); }
			 */
			rs.close();
			mDBCon.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void dbFinalize() {
		try {
			mDBCon.commit();
			mDBCon.close();
			mDBCon = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds a dwarf from the server's static list based on player's name
	 * 
	 * @param player
	 * @return dwarf or null
	 */
	public DCPlayer find(Player player) {
		for (DCPlayer d : dwarves) {
			if (d != null) {
				if (d.getPlayer() != null) {
					if (d.getPlayer().getName().equalsIgnoreCase(player.getName())) {
						d.setPlayer(player);
						return d;
					}
				}
			}
		}
		return null;
	}

	protected DCPlayer findOffline(UUID uuid) {
		DCPlayer dCPlayer = createDwarf(null);
		if (checkDwarfData(dCPlayer, uuid))
			return dCPlayer;
		else {
			// No dwarf or data found
			return null;
		}
	}

	public void createDwarfData(DCPlayer dCPlayer) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("insert into players(uuid, race) values(?,?);");
			prep.setString(1, dCPlayer.getPlayer().getUniqueId().toString());
			prep.setString(2, plugin.getConfigManager().getDefaultRace());
			prep.execute();
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean checkDwarfData(DCPlayer player) {
		return checkDwarfData(player, player.getPlayer().getUniqueId());
	}

	/**
	 * Used for creating and populating a dwarf with a null(off line) player
	 * 
	 * @param player
	 * @param name
	 */
	private boolean checkDwarfData(DCPlayer player, UUID uuid) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("select * from players WHERE uuid = ?;");
			prep.setString(1, uuid.toString());
			ResultSet rs = prep.executeQuery();

			if (!rs.next())
				return false;

			player.setRace(rs.getString("race"));

			int id = rs.getInt("id");
			rs.close();

			prep.close();
			prep = mDBCon.prepareStatement("select id, level, deposit1, deposit2, deposit3 " + "from skills WHERE player = ?;");
			prep.setInt(1, id);
			rs = prep.executeQuery();

			while (rs.next()) {
				int skillID = rs.getInt("id");
				int level = rs.getInt("level");
				Skill skill = player.getSkill(skillID);
				if (skill != null) {
					skill.setLevel(level);
					skill.setDeposit1(rs.getInt("deposit1"));
					skill.setDeposit2(rs.getInt("deposit2"));
					skill.setDeposit3(rs.getInt("deposit3"));
				}
			}
			rs.close();
			prep.close();

			if (!dwarves.contains(player))
				dwarves.add(player);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected GreeterMessage getGreeterMessage(String messageId) {
		return greeterMessageList.get(messageId);
	}

	public DwarfTrainer getTrainer(NPC npc) {
		for (Iterator<Map.Entry<Integer, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Integer, DwarfTrainer> pairs = i.next();
			DwarfTrainer trainer = (pairs.getValue());
			if (trainer.getEntity().getId() == npc.getId())
				return trainer;
		}
		return null;
	}

	public boolean isTrainer(Entity entity) {
		for (Iterator<Map.Entry<Integer, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Integer, DwarfTrainer> pairs = i.next();
			DwarfTrainer trainer = (pairs.getValue());
			if (trainer.getEntity().getId() == entity.getEntityId())
				return true;
		}
		return false;
	}

	protected DwarfTrainer getTrainer(String str) {
		return (trainerList.get(str)); // can return null
	}

	public DwarfVehicle getVehicle(Vehicle v) {
		for (DwarfVehicle i : vehicleList) {
			if (i.equals(v)) {
				return i;
			}
		}
		return null;
	}

	protected void insertGreeterMessage(String messageId, GreeterMessage greeterMessage) {
		try {
			greeterMessageList.put(messageId, greeterMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DwarfTrainer getTrainerByName(String name) {
		for (DwarfTrainer trainer : trainerList.values()) {
			if (trainer.getName().equalsIgnoreCase(name)) {
				return trainer;
			}
		}
		return null;
	}

	public void removeTrainer(NPC npc) {
		DwarfTrainer trainer = getTrainer(npc);

		plugin.despawnById(trainer.getUniqueId());
		plugin.getNPCRegistry().deregister(npc);
		trainerList.remove(trainer);
	}

	public void removeTrainerByName(String name) {
		DwarfTrainer trainer = getTrainerByName(name);

		plugin.despawnById(trainer.getUniqueId());
		plugin.getNPCRegistry().deregister(trainer.getEntity());
		trainerList.remove(trainer);
	}

	public void removeVehicle(Vehicle v) {
		for (DwarfVehicle i : vehicleList) {
			if (i.equals(v)) {
				plugin.getDataManager().vehicleList.remove(i);
				if (DwarfCraft.debugMessagesThreshold < 5)
					System.out.println("DC5:Removed DwarfVehicle from vehicleList");
			}
		}
	}

	private int getPlayerID(UUID uuid) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("select id from players WHERE uuid = ?;");
			prep.setString(1, uuid.toString());
			ResultSet rs = prep.executeQuery();

			if (!rs.next())
				return -1;

			int id = rs.getInt("id");
			rs.close();
			prep.close();
			return id;
		} catch (Exception e) {
			System.out.println("DC: Failed to get player ID: " + uuid.toString());
		}
		return -1;
	}

	public boolean saveDwarfData(DCPlayer dCPlayer, Skill[] skills) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("UPDATE players SET race=? WHERE uuid=?;");
			prep.setString(1, dCPlayer.getRace());
			prep.setString(2, dCPlayer.getPlayer().getUniqueId().toString());
			prep.execute();
			prep.close();

			prep = mDBCon.prepareStatement("REPLACE INTO skills(player, id, level, " + "deposit1, deposit2, deposit3) " + "values(?,?,?,?,?,?);");

			int id = getPlayerID(dCPlayer.getPlayer().getUniqueId());
			for (Skill skill : skills) {
				prep.setInt(1, id);
				prep.setInt(2, skill.getId());
				prep.setInt(3, skill.getLevel());
				prep.setInt(4, skill.getDeposit1());
				prep.setInt(5, skill.getDeposit2());
				prep.setInt(6, skill.getDeposit3());
				prep.addBatch();
			}
			prep.executeBatch();
			prep.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
