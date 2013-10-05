package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import net.minecraft.server.v1_6_R3.ContainerPlayer;
import net.minecraft.server.v1_6_R3.ContainerWorkbench;
import net.minecraft.server.v1_6_R3.CraftingManager;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.ItemStack;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
public class DCCraftSchedule implements Runnable {
	private final DCPlayer dCPlayer;
	private final DwarfCraft plugin;
	private final EntityPlayer entityPlayer;
	private int taskID;

	public DCCraftSchedule(DwarfCraft newPlugin, DCPlayer newDwarf) {
		this.dCPlayer = newDwarf;
		this.plugin = newPlugin;
		this.entityPlayer = ((CraftPlayer) (dCPlayer.getPlayer())).getHandle();
	}

	public void setID(int id){
		taskID = id;
	}

	@Override
	public void run() {
		// in this task we need to check to see if they are still using a
		// craftbench. if so, continue the task.

		if (entityPlayer == null || entityPlayer.activeContainer == entityPlayer.defaultContainer){
			kill();
			return;
		}
		ItemStack outputStack = null;
		if (entityPlayer.activeContainer instanceof ContainerPlayer){
			ContainerPlayer player = (ContainerPlayer)(entityPlayer.activeContainer);
			outputStack = CraftingManager.getInstance().craft(player.craftInventory, entityPlayer.world); //Change here
		}else if (entityPlayer.activeContainer instanceof ContainerWorkbench){
			ContainerWorkbench workBench = (ContainerWorkbench)(entityPlayer.activeContainer);
			outputStack = CraftingManager.getInstance().craft(workBench.craftInventory, entityPlayer.world); //And here
		}else {
			kill();
			return;
		}
		if (outputStack != null) {
			int materialId = outputStack.id;
			int damage = outputStack.c;
			for (Skill s : dCPlayer.getSkills().values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.CRAFT && e.checkInitiator(materialId, (byte)damage)){

						org.bukkit.inventory.ItemStack output = e.getOutput(dCPlayer, (byte)damage);

						if (output.getAmount() == 0)
							outputStack = null;
						else{
							outputStack.count = output.getAmount();
							if (output.getData() != null)
								outputStack.c = output.getData().getData();
						}
						// TODO: need code to check max stack size and if amount
						// created > max stack size drop all count above 1 to
						// ground/inventory.
						// I'm not sure what the server ItemStack method is for
						// is.getMaxStackSize()
					}
				}
				if (entityPlayer.activeContainer instanceof ContainerPlayer){
					ContainerPlayer player = (ContainerPlayer)(entityPlayer.activeContainer);
					player.resultInventory.setItem(0, outputStack); //and here
				}else if (entityPlayer.activeContainer instanceof ContainerWorkbench){
					ContainerWorkbench workBench = (ContainerWorkbench)(entityPlayer.activeContainer);
					workBench.resultInventory.setItem(0, outputStack); //and finally here
				}
			}
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DCCraftSchedule(plugin, dCPlayer), 2);
	}
	public void kill() {
		plugin.getServer().getScheduler().cancelTask(taskID);
	}
}