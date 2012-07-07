package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import java.util.List;

import org.bukkit.World;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.*;
import com.topcat.npclib.nms.NPCNetHandler;
import com.topcat.npclib.nms.NpcEntityTargetEvent;
import com.topcat.npclib.nms.NpcEntityTargetEvent.NpcTargetReason;

public class DCEntityListener implements Listener {
	private final DwarfCraft plugin;
	private HashMap<Entity, DCPlayer> killMap;

	public DCEntityListener(DwarfCraft plugin) {
		this.plugin = plugin;
		killMap = new HashMap<Entity, DCPlayer>();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getEntity().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if (event instanceof EntityDamageByEntityEvent) {
			if ((event.getEntity() instanceof HumanEntity) && plugin.getDataManager().isTrainer(event.getEntity())) {
				EntityDamageByEntityEvent nevent = (EntityDamageByEntityEvent) event;
				if (!(nevent.getDamager() instanceof Arrow)) {
					if (checkTrainerLeftClick((EntityDamageByEntityEvent) event)) {
						event.setCancelled(true);
						return;
					}
				}
			} else {
				return;
			}
		}
		
		if(event.isCancelled())
			return;

		if ((event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.FALL || event.getCause() == DamageCause.SUFFOCATION || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK
				|| event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.DROWNING)) {

			if (DwarfCraft.debugMessagesThreshold < -1 && !event.isCancelled()) {
				System.out.println("DC-1: Damage Event: " + event.getCause());
			}
				onEntityDamagedByEnvirons(event);

		} else if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent nevent = (EntityDamageByEntityEvent) event;
			if (!(nevent.getDamager() instanceof Arrow)) {
				if (DwarfCraft.debugMessagesThreshold < 2)
				onEntityDamageByProjectile(nevent);
			} else if (DwarfCraft.debugMessagesThreshold < 2) {
				onEntityAttack(nevent);
			}
		}
	}

	private boolean checkTrainerLeftClick(EntityDamageByEntityEvent event) {
		DwarfTrainer trainer = plugin.getDataManager().getTrainer(event.getEntity());
		if (trainer != null) {
			if (event.getDamager() instanceof Player) {
				if (plugin.getDataManager().getTrainerRemove().contains(event.getDamager())) {
					plugin.getDataManager().removeTrainer(trainer.getUniqueId());
					plugin.getDataManager().getTrainerRemove().remove(event.getDamager());
				} else if (plugin.getDataManager().getTrainerLookAt().contains(event.getDamager())) {
					trainer.lookAt((Player)event.getDamager(), trainer);
					plugin.getDataManager().getTrainerLookAt().remove(event.getDamager());
				}else{
					// in business, left click
					if (trainer.isGreeter()) {
						trainer.printLeftClick((Player) (event.getDamager()));
					} else {
						trainer.lookAt(event.getDamager());
						Player player = (Player) event.getDamager();
						DCPlayer dCPlayer = plugin.getDataManager().find(player);
						Skill skill = dCPlayer.getSkill(trainer.getSkillTrained());
						plugin.getOut().printSkillInfo(player, skill, dCPlayer, trainer.getMaxSkill());
					}
				}
			}
			return true;
		}
		return false;
	}

	private boolean checkDwarfTrainer(NpcEntityTargetEvent event) {
		try {
			DCPlayer dCPlayer = plugin.getDataManager().find(((Player) event.getTarget()));
			DwarfTrainer trainer = plugin.getDataManager().getTrainer(event.getEntity());
			if (trainer != null) {
				if (event.getTarget() instanceof Player) {
					if (event.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
					} else if (event.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
						if (trainer.isGreeter()) {
							trainer.printRightClick((Player) (event.getTarget()));
						} else {
							trainer.lookAt(event.getTarget());
							trainer.getEntity().animateArmSwing();
							trainer.trainSkill(dCPlayer);
						}
					} else if (event.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
						// player collided with mob
						// doesn't seem to work
					}
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getDamager().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if ((plugin.getDataManager().isTrainer(event.getEntity())) && event.getEntity() instanceof HumanEntity) {
			event.setDamage(0);
			return;
		}
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		
		int Origdamage = event.getDamage();
		Entity damager = event.getDamager();
		LivingEntity victim;

		if (event.getEntity() instanceof LivingEntity) {
			victim = (LivingEntity) event.getEntity();
			if (DwarfCraft.debugMessagesThreshold < 0)
				System.out.println("DC0: victim is living ");
		} else {
			if (DwarfCraft.debugMessagesThreshold < 0)
				System.out.println("DC0: victim is unliving ");
			return;
		}

		boolean isPVP = false;
		DCPlayer attacker = null;

		if (victim instanceof Player) {
			isPVP = true;
			if (DwarfCraft.debugMessagesThreshold < 1)
				System.out.println("DC1: EDBE is PVP");
		}

		int damage = event.getDamage();
		int hp = victim.getHealth();
		if (damager instanceof Player) {
			attacker = plugin.getDataManager().find((Player) damager);
			assert ((Player) event.getDamager() == attacker.getPlayer());
			assert (attacker != null);
		} else {// EvP no effects, EvE no effects
			if (DwarfCraft.debugMessagesThreshold < 4)
				System.out.println(String.format("DC4: EVP %s attacked %s for %d of %d\r\n", damager.getClass().getSimpleName(), victim.getClass().getSimpleName(), damage, hp));
			if(!(event.getEntity() instanceof Player)){
				event.setDamage(Origdamage);
			}
			return;
		}

		ItemStack tool = attacker.getPlayer().getItemInHand();
		HashMap<Integer, Skill> skills = attacker.getSkills();

		for (Skill s : skills.values()) {
			for (Effect e : s.getEffects()) {
				if (tool != null && tool.getType().getMaxDurability() > 0) {
					if (e.getEffectType() == EffectType.SWORDDURABILITY && e.checkTool(tool))
						e.damageTool(attacker, 1, tool);

					if (e.getEffectType() == EffectType.TOOLDURABILITY && e.checkTool(tool))
						e.damageTool(attacker, 2, tool);
				}

				if (e.getEffectType() == EffectType.PVEDAMAGE && !isPVP && e.checkTool(tool)) {
					if (hp <= 0) {
						event.setCancelled(true);
						return;
					}
					damage = Util.randomAmount((e.getEffectAmount(attacker)) * damage);
					if (damage >= hp && !killMap.containsKey(victim)) {
						killMap.put(victim, attacker);
					}
					event.setDamage(damage);
					if (DwarfCraft.debugMessagesThreshold < 6) {
						System.out.println(String.format("DC6: PVE %s attacked %s for %.2f of %d doing %d dmg of %d hp" + " effect called: %d", attacker.getPlayer().getName(), victim.getClass().getSimpleName(), e.getEffectAmount(attacker), event.getDamage(), damage, hp, e.getId()));
					}
				}

				if (e.getEffectType() == EffectType.PVPDAMAGE && isPVP && e.checkTool(tool)) {
					damage = Util.randomAmount((e.getEffectAmount(attacker)) * damage);
					event.setDamage(damage);
					if (DwarfCraft.debugMessagesThreshold < 6) {
						System.out.println(String.format("DC6: PVP %s attacked %s for %.2f of %d doing %d dmg of %d hp" + " effect called: %d", attacker.getPlayer().getName(), ((Player) victim).getName(), e.getEffectAmount(attacker), event.getDamage(), damage, hp, e.getId()));
					}
				}
			}
		}
	}

	public void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getDamager().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if ((plugin.getDataManager().isTrainer(event.getEntity())) && event.getEntity() instanceof HumanEntity) {
			event.setDamage(0);
			return;
		}

		LivingEntity attacker = (LivingEntity) event.getDamager();
		LivingEntity hitThing = (LivingEntity) event.getEntity();

		int hp = hitThing.getHealth();
		if (hp <= 0) {
			event.setCancelled(true);
			return;
		}
		double damage = event.getDamage();
		double mitigation = 1;

		DCPlayer attackDwarf = null;

		if (attacker instanceof Player) {
			attackDwarf = plugin.getDataManager().find((Player) attacker);
			for (Skill skill : attackDwarf.getSkills().values()) {
				for (Effect effect : skill.getEffects()) {
					if (effect.getEffectType() == EffectType.BOWATTACK)
						damage = effect.getEffectAmount(attackDwarf);
				}
			}
		}

		damage = Util.randomAmount(damage * mitigation);
		event.setDamage((int) damage);
		if(attacker instanceof Player)
		((Player)attacker).sendMessage(""+(int)damage);
		if (damage >= hp && attacker instanceof Player && !killMap.containsKey(hitThing) && !(hitThing instanceof Player)) {
			killMap.put(hitThing, attackDwarf);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamagedByEnvirons(EntityDamageEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getEntity().getWorld() == w){
						return;
					}
				}
			}
		}
		
		if ((plugin.getDataManager().isTrainer(event.getEntity())) && event.getEntity() instanceof HumanEntity) {
			event.setDamage(0);
			event.setCancelled(true);
			return;
		}
		if ((event.getEntity() instanceof Player)) {
			DCPlayer dCPlayer = plugin.getDataManager().find((Player) event.getEntity());
			double damage = event.getDamage();
			for (Skill s : dCPlayer.getSkills().values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.FALLDAMAGE && event.getCause() == DamageCause.FALL)
						damage = Util.randomAmount(e.getEffectAmount(dCPlayer) * damage);
					else if (e.getEffectType() == EffectType.FIREDAMAGE && event.getCause() == DamageCause.FIRE)
						damage = Util.randomAmount(e.getEffectAmount(dCPlayer) * damage);
					else if (e.getEffectType() == EffectType.FIREDAMAGE && event.getCause() == DamageCause.FIRE_TICK)
						damage = Util.randomAmount(e.getEffectAmount(dCPlayer) * damage);
					else if (e.getEffectType() == EffectType.EXPLOSIONDAMAGE && event.getCause() == DamageCause.ENTITY_EXPLOSION)
						damage = Util.randomAmount(e.getEffectAmount(dCPlayer) * damage);
					else if (e.getEffectType() == EffectType.EXPLOSIONDAMAGE && event.getCause() == DamageCause.BLOCK_EXPLOSION)
						damage = Util.randomAmount(e.getEffectAmount(dCPlayer) * damage);

					if (e.getEffectType() == EffectType.FALLTHRESHOLD && event.getCause() == DamageCause.FALL) {
						if (event.getDamage() <= e.getEffectAmount(dCPlayer)) {
							if (DwarfCraft.debugMessagesThreshold < 1)
								System.out.println("DC1: Damage less than fall threshold");
							event.setCancelled(true);
						}
					}
				}
			}
			if (DwarfCraft.debugMessagesThreshold < 1) {
				System.out.println(String.format("DC1: environment damage type: %s base damage: %d new damage: %.2f\r\n", event.getCause(), event.getDamage(), damage));
			}
			event.setDamage((int) damage);
			if (damage == 0)
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDeath(EntityDeathEvent event) {
		
		if(plugin.getConfigManager().worldBlacklist){
			for (World w : plugin.getConfigManager().worlds){
				if(w != null){
					if(event.getEntity().getWorld() == w){
						return;
					}
				}
			}
		}
		
		Entity deadThing = event.getEntity();
		if (deadThing instanceof Player)
			return;

		boolean changed = false;

		List<ItemStack> items = event.getDrops();

		ItemStack[] normal = new ItemStack[items.size()];
		items.toArray(normal);

		items.clear();

		if (killMap.containsKey(event.getEntity())) {
			DCPlayer killer = killMap.get(deadThing);
			for (Skill skill : killer.getSkills().values()) {
				for (Effect effect : skill.getEffects()) {
					if (effect.getEffectType() == EffectType.MOBDROP) {
						if (effect.checkMob(deadThing)) {
							ItemStack output = effect.getOutput(killer);

							if (deadThing instanceof CraftSheep)
								output.setDurability((short) ((CraftSheep) deadThing).getColor().ordinal());

							if (DwarfCraft.debugMessagesThreshold < 5) {
								System.out.println(String.format("DC5: killed a %s effect called: %d created %d of %s\r\n", deadThing.getClass().getSimpleName(), effect.getId(), output.getAmount(), output.getType().name()));
							}

							changed = true;
							if (output.getAmount() > 0)
								items.add(output);
						}
					}
				}
			}
			if (!changed) { // If there was no skill for this type of entity,
							// just give the normal drop.
				for (ItemStack i : normal)
					items.add(i);
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event instanceof NpcEntityTargetEvent) {
			checkDwarfTrainer((NpcEntityTargetEvent) event);
			event.setCancelled(true);
		}
		if (event.getReason() == TargetReason.CLOSEST_PLAYER) {
			if (event.getTarget() instanceof CraftPlayer) {
				if (((CraftPlayer) event.getTarget()).getHandle().netServerHandler instanceof NPCNetHandler) {
					event.setCancelled(true);
				}
			}
		}
	}
}
