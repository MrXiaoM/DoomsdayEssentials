package top.mrxiaom.doomsdayessentials.skills;

import org.bukkit.inventory.ItemStack;

public interface ISkill {
	boolean canItemStackRunSkill(ItemStack item);

	void runSkill(Object data);
}
