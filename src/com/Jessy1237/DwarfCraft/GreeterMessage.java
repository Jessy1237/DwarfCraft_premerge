package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

public class GreeterMessage
{
	private final String	leftClick;
	private final String	rightClick;
	
	GreeterMessage(String newLeftClick, String newRightClick)
	{
		this.leftClick = newLeftClick;
		this.rightClick = newRightClick;
	}
	
	protected String getLeftClickMessage()
	{
		return leftClick;
	}
	
	protected String getRightClickMessage()
	{
		return rightClick;
	}
}