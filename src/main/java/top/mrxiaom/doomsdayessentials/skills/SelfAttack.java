package top.mrxiaom.doomsdayessentials.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

public class SelfAttack implements ISkill {
	final Main plugin;

	public SelfAttack(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean canItemStackRunSkill(ItemStack item) {
		for (String s : plugin.getConfig().getStringList("self-attack-lore")) {
			if (ItemStackUtil.hasLore(item, s))
				return true;
		}
		return false;
	}

	@Override
	public void runSkill(Object data) {
		Player damager = (Player) ((EntityDamageByEntityEvent) data).getDamager();
		if (((EntityDamageByEntityEvent) data).getEntity() instanceof Player
				&& ((EntityDamageByEntityEvent) data).getEntity().getName().equals(damager.getName()))
			return;
		damager.damage(((EntityDamageByEntityEvent) data).getDamage(), damager);
		((EntityDamageByEntityEvent) data).setCancelled(true);
	}
}
