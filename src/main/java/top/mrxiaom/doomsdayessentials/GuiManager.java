package top.mrxiaom.doomsdayessentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.mrxiaom.doomsdayessentials.api.IGui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener{
	final Map<UUID, IGui> playersGui = new HashMap<>();
	final Main plugin;
	public GuiManager(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public void openGui(IGui gui) {
		Player player = gui.getPlayer();
		if(player == null) return;
		player.closeInventory();
		playersGui.remove(player.getUniqueId());
		playersGui.put(player.getUniqueId(), gui);
		player.openInventory(gui.newInventory());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player) event.getWhoClicked();
		if(playersGui.containsKey(player.getUniqueId())) {
			playersGui.get(player.getUniqueId()).onClick(event.getAction(), event.getClick(), event.getSlotType(),
					event.getRawSlot(), event.getCurrentItem(), event.getCursor(), event.getView(), event);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!(event.getPlayer() instanceof Player)) return;
		Player player = (Player) event.getPlayer();
		if(playersGui.containsKey(player.getUniqueId())) {
			playersGui.get(player.getUniqueId()).onClose(event.getView());
			playersGui.remove(player.getUniqueId());
		}
	}
}
