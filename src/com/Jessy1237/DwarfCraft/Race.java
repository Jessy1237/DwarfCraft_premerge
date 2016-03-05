package com.Jessy1237.DwarfCraft;

import java.util.ArrayList;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

public class Race
{
	
	private final String		mName;
	private ArrayList<Integer>	skills;
	private String				Desc;
	
	public Race(String name)
	{
		this.mName = name;
	}
	
	public Race(String name, final ArrayList<Integer> skills, String Desc)
	{
		this.mName = name;
		this.Desc = Desc;
		this.skills = skills;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public ArrayList<Integer> getSkills()
	{
		return this.skills;
	}
	
	public String getDesc()
	{
		return this.Desc;
	}
	
	public void setSkills(ArrayList<Integer> skills)
	{
		this.skills = skills;
	}
	
	public void setDesc(String Desc)
	{
		this.Desc = Desc;
	}
}
