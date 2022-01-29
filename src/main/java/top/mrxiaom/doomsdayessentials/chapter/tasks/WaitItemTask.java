package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class WaitItemTask implements IChapterTask<IChapterTask.NoEvent>{
	String itemName;
	Material material;
	List<String> itemLore;
	int itemCount;
	boolean isTake;
	List<String> waitingPlayer = new ArrayList<>();
	public WaitItemTask(Material material, String itemName, List<String> itemLore, int itemCount, boolean isTake) {
		this.material = material;
		this.itemName = itemName;
		this.itemLore = itemLore;
		this.itemCount = itemCount;
		this.isTake = isTake;
	}
	@Override
	public String toString(){
		StringBuilder lore = new StringBuilder();
		itemLore.forEach(s -> lore.append(",").append(s));
		return "waititem:" + material.name().toUpperCase() + "," + itemName + lore + "," + itemCount + "," + isTake;
	}
	@Override
	public void start(Player player) {
		waitingPlayer.add(player.getName());
		check(player);
	}

	private void check(Player player){
		// 每秒检查一次背包
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
			if (!waitingPlayer.contains(player.getName())) return;
			int count = 0;
			for(int i = 0; i < player.getInventory().getSize(); i++) {
				ItemStack item = player.getInventory().getItem(i);
				if(item != null && item.getType().equals(material)) {
					boolean nameAccess = itemName == null || ItemStackUtil.getItemDisplayName(item).equalsIgnoreCase(itemName);
					boolean loreAccess = itemLore == null;
					if (!loreAccess) {
						List<String> lore = ItemStackUtil.getItemLore(item);
						if (lore.size() < itemLore.size()) continue;
						loreAccess = true;
						for (int j = 0; j < itemLore.size(); j++) {
							if (!lore.get(j).equalsIgnoreCase(itemLore.get(j))) {
								loreAccess = false;
								break;
							}
						}
					}
					if (nameAccess && loreAccess) count += item.getAmount();
				}
			}
			if(count >= itemCount) {
				next(player);
			}
			else check(player);
		}, 20L);
	}

	@Override
	public void end(Player player) { waitingPlayer.remove(player.getName()); }

	@Override
	public String display() {
		return "";
	}
}
