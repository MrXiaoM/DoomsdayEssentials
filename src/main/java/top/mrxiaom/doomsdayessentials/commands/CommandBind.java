package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import think.rpgitems.item.ItemManager;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.BindConfig;
import top.mrxiaom.doomsdayessentials.gui.GuiBindRecall;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.List;

public class CommandBind extends ICommand {
	public static final char[] ID_DICT = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'B', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	public CommandBind(Main plugin) {
		super(plugin, "bind", new String[] {});
	}
	public boolean isBindable(ItemStack item) {
		if (plugin.getSkillSelfAttack().canItemStackRunSkill(item)
				|| ItemManager.toRPGItem(item).orElse(null) != null)
			return true;
		if (item == null || item.getType() == Material.AIR)
			return false;
		if (item.getItemMeta() == null || item.getItemMeta().getLore() == null)
			return false;
		return false;
	}
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if (!player.hasPermission("doomsdayessentials.bind")) {
			return Util.noPerm(sender);
		}
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("confirm")) {
				if(plugin.getBindConfig().getPlayerBindCount(player.getName()) > 9999) {
					player.sendMessage("§7[§9末日社团§7] §c你的神器绑定数已达上限");
					return true;
				}
				ItemStack item = player.getInventory().getItemInMainHand();
				if (!this.isBindable(item)) {
					player.sendMessage("§7[§9末日社团§7] §c该物品无法绑定");
					return true;
				}
				double cost = plugin.getConfig().getDouble("bind-cost");
				if (plugin.getEcoApi().getBalance(player) < cost) {
					player.sendMessage("§7[§9末日社团§7] §c你所持有的新币不足， 需要 300 新币来绑定装备");
					return true;
				}
				plugin.getEcoApi().withdrawPlayer(player, cost);
				String code = plugin.getBindConfig().putBind(player.getName(), item);
				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.getLore();
				lore.add(BindConfig.PREFIX + code);
				lore.add("§a已绑定 " + player.getName());
				meta.setLore(lore);
				item.setItemMeta(meta);
				player.getInventory().setItemInMainHand(item);
				player.sendMessage("§7[§9末日社团§7] §6你成功绑定了 §c" + ItemStackUtil.getItemDisplayName(item));
				return true;
			}
			if (args[0].equalsIgnoreCase("relieve")) {
				ItemStack item = player.getInventory().getItemInMainHand();
				String code = plugin.getBindConfig().getCodeFromItemStack(item);
				if (code == null) {
					player.sendMessage("§7[§9末日社团§7] §c该物品无需解绑");
					return true;
				}
				String owner = plugin.getBindConfig().getOwner(code);
				if (owner == null) {
					player.sendMessage("§7[§9末日社团§7] §c错误: 未找到绑定对象");
					return true;
				}
				if (!player.getName().equalsIgnoreCase(owner)) {
					player.sendMessage("§7[§9末日社团§7] §c你不是该物品的主人，无法解绑");
					return true;
				}
				plugin.getBindConfig().removeBind(code);
				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.getLore();
				int size = lore.size();
				lore.remove(size-1);
				lore.remove(size-2);
				meta.setLore(lore);
				item.setItemMeta(meta);
				player.getInventory().setItemInMainHand(item);
				player.sendMessage("§7[§9末日社团§7] §6你成功解绑了 §c" + ItemStackUtil.getItemDisplayName(item));
				return true;
			}
			if (args[0].equalsIgnoreCase("recall")) {
				plugin.getGuiManager().openGui(new GuiBindRecall(plugin, player, 1));
				return true;
			}
		}
		sender.sendMessage("§7[§9末日社团§7] §6绑定系统\n" + "§7[§9末日社团§7] §a/bind confirm §7花费300新币绑定手中装备\n"
				+ "§7[§9末日社团§7] §a/bind relieve §7解绑手中装备\n" + "§7[§9末日社团§7] §a/bind recall §7打开装备召回界面");
		
		return true;
	}
}
