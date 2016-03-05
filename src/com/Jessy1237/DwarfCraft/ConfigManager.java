package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jbls.LexManos.CSV.CSVReader;
import org.jbls.LexManos.CSV.CSVRecord;

public final class ConfigManager
{
	
	private final DwarfCraft					plugin;
	
	private final String						configDirectory;
	
	private String								configMainFileName;
	private String								configSkillsFileName;
	private int									configSkillsVersion;
	private String								configEffectsFileName;
	private int									configEffectsVersion;
	private String								configMessagesFileName;
	private String								configWorldFileName;
	private String								cfgGreeterFile;
	private String								cfgRaceFile;
	private String								cfgBlockGroupsFile;
	private String								dbpath;
	private Integer								trainDelay;
	private Integer								announcementInterval;
	private String								announcementMessage;
	private Integer								maxLevel;
	private Integer								raceLevelLimit;
	private String								vanillaRace;
	private String								prefixStr;
	
	private HashMap<Integer, Skill>				skillsArray		= new HashMap<Integer, Skill>();
	public ArrayList<World>						worlds			= new ArrayList<World>();
	private HashMap<String, ArrayList<Integer>>	blockgroups		= new HashMap<String, ArrayList<Integer>>();
	
	private ArrayList<Race>						raceList		= new ArrayList<Race>();
	private String								defaultRace;
	
	public boolean								sendGreeting	= false;
	public boolean								disableCacti	= true;
	public boolean								worldBlacklist	= false;
	public boolean								silkTouch		= true;
	public boolean								vanilla			= true;
	public boolean								buildingblocks	= true;
	public boolean								prefix			= false;
	public boolean								announce		= false;
	
	protected ConfigManager(DwarfCraft plugin, String directory,
			String paramsFileName)
	{
		this.plugin = plugin;
		if (!directory.endsWith(File.separator))
			directory += File.separator;
		configDirectory = directory;
		configMainFileName = paramsFileName;
		checkFiles(configDirectory);
		
		try
		{
			if (!readConfigFile() || !readSkillsFile() || !readEffectsFile()
					|| !readMessagesFile() || !readWorldFile()
					|| !readRacesFile() || !readBlockGroupsFile())
			{
				System.out
						.println("[SEVERE] Failed to Enable DwarfCraft Skills and Effects)");
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out
					.println("[SEVERE] Failed to Enable DwarfCraft Skills and Effects)");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
		
	}
	
	public HashMap<Integer, Skill> getAllSkills()
	{
		HashMap<Integer, Skill> newSkillsArray = new HashMap<Integer, Skill>();
		for (Skill s : skillsArray.values())
		{
			if (newSkillsArray.containsKey(s.getId()))
				continue;
			newSkillsArray.put(s.getId(), s.clone());
		}
		return newSkillsArray;
	}
	
	public Race getRace(String Race)
	{
		for (Race r : raceList)
		{
			if (r != null)
			{
				if (r.getName().equalsIgnoreCase(Race))
				{
					return r;
				}
			}
		}
		return null;
	}
	
	public ArrayList<Integer> getAllSkills(String Race)
	{
		return getRace(Race).getSkills();
	}
	
	protected Skill getGenericSkill(int skillId)
	{
		
		for (Skill s : skillsArray.values())
		{
			if (s.getId() == skillId)
				return s.clone();
			
		}
		return null;
	}
	
	protected int getConfigSkillsVersion()
	{
		return configSkillsVersion;
	}
	
	protected String getDbPath()
	{
		return configDirectory + dbpath;
	}
	
	private void getDefaultValues()
	{
		if (configSkillsVersion == 0)
			configSkillsVersion = 100;
		if (configEffectsVersion == 0)
			configEffectsVersion = 100;
		if (configSkillsFileName == null)
			configSkillsFileName = "skills.csv";
		if (configEffectsFileName == null)
			configEffectsFileName = "effects.csv";
		if (configMessagesFileName == null)
			configMessagesFileName = "messages.config";
		if (configWorldFileName == null)
			configWorldFileName = "world-blacklist.config";
		if (cfgGreeterFile == null)
			cfgGreeterFile = "greeters.config";
		if (dbpath == null)
			dbpath = "dwarfcraft.db";
		if (cfgRaceFile == null)
			cfgRaceFile = "races.config";
		if (cfgBlockGroupsFile == null)
			cfgBlockGroupsFile = "block-groups.config";
		if (defaultRace == null)
			defaultRace = "NULL";
		if (trainDelay == null)
			trainDelay = 2;
		if (maxLevel == null)
			maxLevel = 30;
		if (raceLevelLimit == null)
			raceLevelLimit = 5;
		if (announcementInterval == null)
			announcementInterval = 5;
		if (prefixStr == null)
			prefixStr = "[%racename%]";
		if (announcementMessage == null)
			announcementMessage = "%playername% has just leveled %skillname% to level %level%!";
	}
	
	private void checkFiles(String path)
	{
		File root = new File(path);
		if (!root.exists())
			root.mkdirs();
		try
		{
			File file = new File(root, "DwarfCraft.config");
			if (!file.exists())
			{
				file.createNewFile();
				CopyFile("/default_files/DwarfCraft.config", file);
			}
			
			readConfigFile();
			getDefaultValues();
			
			String[][] mfiles = { { configSkillsFileName, "skills.csv" },
					{ configEffectsFileName, "effects.csv" },
					{ configMessagesFileName, "messages.config" },
					{ dbpath, "dwarfcraft.db" },
					{ cfgGreeterFile, "greeters.config" },
					{ configWorldFileName, "world-blacklist.config" },
					{ cfgRaceFile, "races.config" },
					{ cfgBlockGroupsFile, "block-groups.config" } };
			for (String[] mfile : mfiles)
			{
				file = new File(root, mfile[0]);
				if (!file.exists())
				{
					file.createNewFile();
					CopyFile("/default_files/" + mfile[1], file);
				}
			}
		} catch (Exception e)
		{
			System.out.println("DC: ERROR: Could not verify files: "
					+ e.toString());
			e.printStackTrace();
		}
	}
	
	private void CopyFile(String name, File toFile) throws Exception
	{
		InputStream ins = ConfigManager.class.getResourceAsStream(name);
		OutputStream out = new FileOutputStream(toFile);
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = ins.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		out.flush();
		ins.close();
		out.close();
	}
	
	@SuppressWarnings("resource")
	private boolean readConfigFile()
	{
		try
		{
			System.out.println("[DwarfCraft] Reading Config File: "
					+ configDirectory + configMainFileName);
			getDefaultValues();
			FileReader fr = new FileReader(configDirectory
					+ configMainFileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null)
			{
				if (line.length() == 0)
				{
					line = br.readLine();
					continue;
				}
				if (line.charAt(0) == '#')
				{
					line = br.readLine();
					continue;
				}
				String[] theline = line.split(":");
				if (theline.length != 2)
				{
					line = br.readLine();
					continue;
				}
				if (theline[0].equalsIgnoreCase("Skills File Name"))
					configSkillsFileName = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Effects File Name"))
					configEffectsFileName = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Messages File Name"))
					configMessagesFileName = theline[1].trim();
				if (theline[0].equalsIgnoreCase("World Blacklist File Name"))
					configWorldFileName = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Greeter Messages File Name"))
					cfgGreeterFile = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Races File Name"))
					cfgRaceFile = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Database File Name"))
					dbpath = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Debug Level"))
					DwarfCraft.debugMessagesThreshold = Integer
							.parseInt(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Send Login Greet"))
					sendGreeting = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Disable Farm Exploits"))
					disableCacti = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("World Blacklist"))
					worldBlacklist = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Train Delay"))
					trainDelay = Integer.parseInt(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Silk Touch"))
					silkTouch = Boolean.parseBoolean(theline[1].trim());
				if (theline[0]
						.equalsIgnoreCase("Group Equivalent Building Blocks"))
					buildingblocks = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Default Race"))
					defaultRace = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Vanilla Race Enabled"))
					vanilla = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Vanilla Race"))
					vanillaRace = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Prefix Enabled"))
					prefix = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Prefix"))
					prefixStr = theline[1].trim();
				if (theline[0].equalsIgnoreCase("Max Skill Level"))
					maxLevel = Integer.parseInt(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Race Level Limit"))
					raceLevelLimit = Integer.parseInt(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Announce Level Up"))
					announce = Boolean.parseBoolean(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Announcement Interval"))
					announcementInterval = Integer
							.parseInt(theline[1].trim());
				if (theline[0].equalsIgnoreCase("Announcement Message"))
					announcementMessage = theline[1].trim();
				
				line = br.readLine();
			}
			
			if (vanilla)
			{
				if (getRace(vanillaRace) == null)
				{
					raceList.add(new Race(vanillaRace,
							new ArrayList<Integer>(),
							"The all round balanced race (vanilla)."));
					System.out.println("[DwarfCraft] Loaded vanilla race: "
							+ vanillaRace);
				}
			}
		} catch (FileNotFoundException fN)
		{
			fN.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	@SuppressWarnings("resource")
	private boolean readWorldFile()
	{
		System.out.println("[DwarfCraft] Reading world blacklist file: "
				+ configDirectory + configWorldFileName);
		
		FileReader fr;
		try
		{
			fr = new FileReader(configDirectory + configWorldFileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null)
			{
				if (line.length() == 0)
				{
					line = br.readLine();
					continue;
				}
				if (line.charAt(0) == '#')
				{
					line = br.readLine();
					continue;
				}
				String[] theline = line.split("-");
				if (theline.length > 2)
				{
					line = br.readLine();
					continue;
				}
				
				if (theline[0].equalsIgnoreCase(" "))
					worlds.add(Bukkit.getServer().getWorld(theline[1].trim()));
				
				line = br.readLine();
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean readEffectsFile()
	{
		System.out.println("[DwarfCraft] Reading effects file: "
				+ configDirectory + configEffectsFileName);
		try
		{
			CSVReader csv = new CSVReader(configDirectory
					+ configEffectsFileName);
			Iterator<CSVRecord> records = csv.getRecords();
			while (records.hasNext())
			{
				CSVRecord item = records.next();
				Effect effect = new Effect(item, plugin);
				Skill skill = skillsArray.get(effect.getId() / 10);
				if (skill != null)
				{
					skill.getEffects().add(effect);
				}
			}
			return true;
		} catch (FileNotFoundException fN)
		{
			fN.printStackTrace();
			return false;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("resource")
	protected boolean readRacesFile()
	{
		System.out.println("[DwarfCraft] Reading races file: "
				+ configDirectory + cfgRaceFile);
		try
		{
			FileReader fr = new FileReader(configDirectory + cfgRaceFile);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			boolean name = false;
			boolean desc = false;
			boolean skills = false;
			Race race = null;
			while (line != null)
			{
				if (line.length() == 0)
				{
					line = br.readLine();
					continue;
				}
				if (line.charAt(0) == '#')
				{
					line = br.readLine();
					continue;
				}
				String[] theline = line.split(":");
				if (theline.length > 2)
				{
					line = br.readLine();
					continue;
				}
				if (theline[0].equalsIgnoreCase("Name"))
				{
					race = new Race(theline[1].trim());
					name = true;
					line = br.readLine();
				}
				if (theline[0].equalsIgnoreCase("SkillIDs"))
				{
					String ids[] = theline[1].trim().split(",");
					race.setSkills(new ArrayList<Integer>());
					for (int i = 0; i < ids.length; i++)
					{
						race.getSkills().add(Integer.parseInt(ids[i].trim()));
					}
					
					skills = true;
					line = br.readLine();
				}
				if (theline[0].equalsIgnoreCase("Description"))
				{
					race.setDesc(theline[1].trim());
					
					desc = true;
					line = br.readLine();
				}
				if (name && desc && skills)
				{
					raceList.add(race);
					name = false;
					desc = false;
					skills = false;
					System.out.println("[DwarfCraft] Loaded race: "
							+ race.getName());
					continue;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (defaultRace == null)
		{
			defaultRace = "NULL";
		} else
		{
			if (!checkRace(defaultRace))
				defaultRace = "NULL";
		}
		return true;
	}
	
	@SuppressWarnings("resource")
	protected boolean readGreeterMessagesfile()
	{
		System.out.println("[DwarfCraft] Reading greeter messages file: "
				+ configDirectory + cfgGreeterFile);
		try
		{
			getDefaultValues();
			FileReader fr = new FileReader(configDirectory + cfgGreeterFile);
			BufferedReader br = new BufferedReader(fr);
			String messageId = br.readLine();
			while (messageId != null)
			{
				messageId = messageId.trim();
				String leftClick, rightClick;
				if (messageId.length() == 0)
				{
					messageId = br.readLine();
					continue;
				}
				if (messageId.charAt(0) == '#')
				{
					messageId = br.readLine();
					continue;
				}
				leftClick = br.readLine().trim();
				rightClick = br.readLine().trim();
				
				plugin.getDataManager().insertGreeterMessage(messageId,
						new GreeterMessage(leftClick, rightClick));
				messageId = br.readLine();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
		
	}
	
	@SuppressWarnings("resource")
	private boolean readMessagesFile()
	{
		System.out.println("[DwarfCraft] Reading general messages file: "
				+ configDirectory + configMessagesFileName);
		try
		{
			getDefaultValues();
			FileReader fr = new FileReader(configDirectory
					+ configMessagesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.length() == 0)
					continue;
				if (line.charAt(0) == '#')
					continue;
				
				if (line.indexOf(":") <= 0)
					continue;
				
				String name = line.substring(0, line.indexOf(":"));
				String message = line.substring(name.length() + 1);
				
				if (name.equalsIgnoreCase("Server Rules"))
					Messages.ServerRules = message;
				
				line = br.readLine();
			}
			
		} catch (FileNotFoundException fN)
		{
			fN.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			// Default to enum values if not found
			if (Messages.ServerRules == null)
				Messages.ServerRules = Messages.Fixed.SERVERRULESMESSAGE
						.getMessage();
		}
		return true;
	}
	
	private boolean readSkillsFile()
	{
		System.out.println("[DwarfCraft] Reading skills file: "
				+ configDirectory + configSkillsFileName);
		try
		{
			CSVReader csv = new CSVReader(configDirectory
					+ configSkillsFileName);
			configSkillsVersion = csv.getVersion();
			Iterator<CSVRecord> records = csv.getRecords();
			while (records.hasNext())
			{
				CSVRecord item = records.next();
				
				@SuppressWarnings("deprecation")
				Skill skill = new Skill(item.getInt("ID"),
						item.getString("Name"), 0, new ArrayList<Effect>(),
						new TrainingItem(plugin.getUtil().parseItem(
								item.getString("Item1")), item
								.getDouble("Item1Base"), item
								.getInt("Item1Max")), new TrainingItem(
								plugin.getUtil().parseItem(
										item.getString("Item2")),
								item.getDouble("Item2Base"),
								item.getInt("Item2Max")), new TrainingItem(
								plugin.getUtil().parseItem(
										item.getString("Item3")),
								item.getDouble("Item3Base"),
								item.getInt("Item3Max")),
						Material.getMaterial(item.getInt("Held")));
				
				skillsArray.put(skill.getId(), skill);
				
			}
			return true;
		} catch (FileNotFoundException fN)
		{
			fN.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	@SuppressWarnings("resource")
	private boolean readBlockGroupsFile()
	{
		System.out.println("[DwarfCraft] Reading Block Groups file: "
				+ configDirectory + cfgBlockGroupsFile);
		
		try
		{
			FileReader fr = new FileReader(configDirectory
					+ cfgBlockGroupsFile);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			
			while (line != null)
			{
				if (line.length() == 0)
				{
					line = br.readLine();
					continue;
				}
				if (line.charAt(0) == '#')
				{
					line = br.readLine();
					continue;
				}
				if (line.indexOf(':') <= 0)
				{
					line = br.readLine();
					continue;
				}
				
				String[] split = line.split(":");
				
				if (split.length > 2 || split.length == 0 || split == null)
				{
					line = br.readLine();
					continue;
				}
				
				if (split[0] == null || split[0] == "")
				{
					line = br.readLine();
					continue;
				}
				
				String[] ints = split[1].split(",");
				ArrayList<Integer> blocks = new ArrayList<Integer>();
				
				if (ints.length == 0 || ints == null)
				{
					line = br.readLine();
					continue;
				}
				
				for (int i = 0; i < ints.length; i++)
				{
					blocks.add(Integer.parseInt(ints[i].trim()));
				}
				
				blockgroups.put(split[0].trim(), blocks);
				line = br.readLine();
			}
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String getDefaultRace()
	{
		return defaultRace;
	}
	
	public ArrayList<Race> getRaceList()
	{
		return raceList;
	}
	
	public boolean checkRace(String name)
	{
		for (Race r : raceList)
		{
			if (r != null)
			{
				if (r.getName().equalsIgnoreCase(name))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public String getPrefix()
	{
		return prefixStr;
	}
	
	public int getTrainDelay()
	{
		return trainDelay;
	}
	
	public int getMaxSkillLevel()
	{
		return maxLevel;
	}
	
	public int getRaceLevelLimit()
	{
		return raceLevelLimit;
	}
	
	public int getAnnouncementInterval()
	{
		return announcementInterval;
	}
	
	public String getAnnouncementMessage()
	{
		return announcementMessage;
	}
	
	public HashMap<String, ArrayList<Integer>> getBlockGroups()
	{
		return blockgroups;
	}
	
}
