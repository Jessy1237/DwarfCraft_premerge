package com.Jessy1237.DwarfCraft;

public class CommandInformation {

	/**
	 * Authors: Jessy1237 & Curtis1509
	 */
	
	public enum Desc{
		
		DEBUG("Sets the debug message threshold in console, from -10(everthing) to +10(critical only)."),
		DCHELP("Displays general help information. Use /help <command> for command specific info."),
		INFO("Displays general information about the DwarfCraft plugin."),
		RULES("Displays server rules defined in the DwarfCraft messages config file."),
		TUTORIAL("Displays a series of guide messages to instruct in the basics of DwarfCraft."),
		DCCOMMANDS("Displays all DwarfCraft commands."),
		SKILLSHEET("Displays a list of skills and levels for a Dwarf."),
		SKILLINFO("Displays a description of a dwarf's skill and training costs."),
		EFFECTINFO("Displays a description of a dwarf's effect information."),
		RACE("Checks a players race information, or changes it."),
		SETSKILL("Admin command to change a players skill level manually."),
		CREATEGREETER("Creates a new greeter where you are standing."),
		CREATETRAINER("Creates a new trainer where you are standing."),
		LISTTRAINERS("Displays a list of trainers and greeters on the server."),
		RACES("Displays a list of the races with descriptions"),
		DMEM("Displays a list of what type, and how many entites are loaded on the server.");
		
		private String Desc;

		private Desc(String Desc) {
			this.Desc = Desc;
		}

		public String getDesc() {
			return Desc;
		}
	}
	
	public enum Usage{
		DEBUG("/<command> [debug level]\nExample: /<command> 2 - sets the console debug printing threshold to 2"),
		DCHELP("/<command> <command name> Displays general help information. Use /help <command> for command specific info."),
		INFO("/<command> Displays general information about the DwarfCraft plugin."),
		RULES("/rules Displays server rules defined in the DwarfCraft messages config file."),
		TUTORIAL(" Displays a series of guide messages to instruct in the basics of DwarfCraft\n/tutorial <page number>\nExample: /tutorial 2 - Prints the second tutorial section"),
		DCCOMMANDS("/dccommands Displays all DwarfCraft commands"),
		SKILLSHEET(" Displays a list of skills and levels for a Dwarf.\n/<command> <full/-f)> <Player Name or blank>\nExample: /skillsheet smartaleq - Prints smartaleq's skillsheet\nExample: /ss -f - Prints your complete skillsheet with level 0 Skills"),
		SKILLINFO("Displays a description of a dwarf's skill and training costs\n/<command> <player name> [Skill ID or Skill Name]\nExample: /skillinfo 11 - Prints details about Excavation skill\nExample: /skillinfo Dirt_Digging - Prints details about Dirt Digging skill\nExample: /skill smartaleq 3 - Prints details about Smartaleq's Axe use skill"),
		EFFECTINFO("Displays a description of a dwarf's effect information\n/<command> <player name> [EffectID]\nExample: /effect smartaleq 131 - Prints details about Smartaleq's effect 131"),
		RACE("/race <Race name> <confirm>\nExample: /race - Displays the player's current race information.\nExample: /race elf - Displays information about the elf race.\nExample: /race elf confirm - Changes the player's race to elf and resets their skills.\nAdmin: /race <Player> <Racename> <confirm> - Alters another player's race, use confirm. \n Admin: /race <player> - shows a players race."),
		SETSKILL("/<command> <player name> [Skill ID or Skill Name or All] [new skill level]"),
		CREATEGREETER("/<command> <id> <DisplayName> <MessageId>"),
		CREATETRAINER("/<command> <id> <DisplayName> <Skill ID or Skill Name> <Max Skill Level> <Min Skill Level> <EntityType>"),
		LISTTRAINERS("/<command> [PageNumber]"),
		RACES("/races"),
		DMEM("/dmem");
		
		private String Usage;

		private Usage(String Usage) {
			this.Usage = Usage;
		}

		public String getUsage() {
			return Usage;
		}
	}
}
