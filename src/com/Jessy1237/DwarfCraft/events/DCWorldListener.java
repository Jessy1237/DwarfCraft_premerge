package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.Chunk;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class DCWorldListener implements Listener {
	private final DwarfCraft plugin;

	public DCWorldListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		event.setCancelled(plugin.getDataManager().checkTrainersInChunk(chunk));
	}
}