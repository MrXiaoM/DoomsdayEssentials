package top.mrxiaom.doomsdayessentials.utils;

import com.google.gson.JsonElement;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NMSUtil {
	public static class NMSItemStack {
		final Object nmsItem;

		private NMSItemStack(Object nmsItem) {
			this.nmsItem = nmsItem;
		}

		public Object getNMSItem() {
			return nmsItem;
		}

		public void setDamage(int value) {
			try {
				Class<?> classItemStack = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ItemStack");
				Method setDamage = classItemStack.getDeclaredMethod("setDamage", int.class);
				setDamage.invoke(nmsItem, value);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		public void setNBTTagInt(String name, int value) {
			try {
				Class<?> classItemStack = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ItemStack");
				Class<?> classNBT = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".NBTTagCompound");
				Constructor<?> constNBT = classNBT.getDeclaredConstructor();
				Method hasTag = classItemStack.getDeclaredMethod("hasTag");
				Method getTag = classItemStack.getDeclaredMethod("getTag");
				Method setTag = classItemStack.getDeclaredMethod("setTag", classNBT);
				Method setInt = classNBT.getDeclaredMethod("setInt", String.class, int.class);
				boolean flag = (boolean) hasTag.invoke(nmsItem);
				Object nbt = flag ? getTag.invoke(nmsItem) : constNBT.newInstance();
				setInt.invoke(nbt, name, value);
				setTag.invoke(nmsItem, nbt);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		public int getNBTTagInt(String name, int nullValue) {
			try {
				if (nbtHasKeyOfType(name, 3)) {

					Class<?> classItemStack = Class
							.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ItemStack");
					Class<?> classNBT = Class
							.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".NBTTagCompound");

					Method getTag = classItemStack.getDeclaredMethod("getTag");
					Method getInt = classNBT.getDeclaredMethod("getInt", String.class);
					Object nbt = getTag.invoke(nmsItem);
					Object value = getInt.invoke(nbt, name);
					return (int) value;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return nullValue;
		}

		public boolean nbtHasKeyOfType(String name, int type) {
			try {
				Class<?> classItemStack = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ItemStack");
				Class<?> classNBT = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".NBTTagCompound");

				Method hasTag = classItemStack.getDeclaredMethod("hasTag");
				Method getTag = classItemStack.getDeclaredMethod("getTag");
				Method hasKeyOfType = classNBT.getDeclaredMethod("hasKeyOfType", String.class, int.class);

				boolean flag = (boolean) hasTag.invoke(nmsItem);
				if (!flag)
					return false;
				Object nbt = getTag.invoke(nmsItem);
				return (boolean) hasKeyOfType.invoke(nbt, name, type);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return false;
		}

		@Nullable
		public static NMSItemStack fromBukkitItemStack(ItemStack item) {
			try {
				Class<?> classCraftItemStack = Class
						.forName("org.bukkit.craftbukkit." + NMSUtil.getNMSVersion() + ".inventory.CraftItemStack");
				Method asNMSCopy = classCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
				Object nmsItem = asNMSCopy.invoke(null, item);
				return new NMSItemStack(nmsItem);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return null;
		}

		@Nullable
		public ItemStack nmsToBukkitItemStack(Object nmsItem) {
			try {
				Class<?> classItemStack = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ItemStack");
				Class<?> classCraftItemStack = Class
						.forName("org.bukkit.craftbukkit." + NMSUtil.getNMSVersion() + ".inventory.CraftItemStack");
				Method asBukkitCopy = classCraftItemStack.getDeclaredMethod("asBukkitCopy", classItemStack);
				Object bukkitItem = asBukkitCopy.invoke(null, nmsItem);
				return (ItemStack) bukkitItem;
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return null;

		}

		@Nullable
		public ItemStack toBukkitItemStack() {
			return nmsToBukkitItemStack(nmsItem);
		}
	}

	public static String getNMSVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().substring(23);
	}

	public static void breakBlock(Block block) {
		breakBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}

	public static void breakBlock(Location loc) {
		breakBlock(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static void breakBlock(World world, int x, int y, int z) {
		try {
			Class<?> classCraftWorld = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".CraftWorld");
			Class<?> classNMSWorld = Class.forName("net.minecraft.server." + getNMSVersion() + ".World");
			Class<?> classBlockPosition = Class.forName("net.minecraft.server." + getNMSVersion() + ".BlockPosition");
			Class<?> classIBlockData = Class.forName("net.minecraft.server." + getNMSVersion() + ".IBlockData");
			Class<?> classBlocks = Class.forName("net.minecraft.server." + getNMSVersion() + ".Blocks");
			Class<?> classBlock = Class.forName("net.minecraft.server." + getNMSVersion() + ".Block");
			Class<?> classIBlockAccess = Class.forName("net.minecraft.server." + getNMSVersion() + ".IBlockAccess");
			Class<?> classPacket = Class
					.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutBlockChange");
			Method getHandle = classCraftWorld.getDeclaredMethod("getHandle");
			Method setTypeAndData = classNMSWorld.getDeclaredMethod("setTypeAndData", classBlockPosition,
					classIBlockData, int.class);
			Method getBlockData = classBlock.getDeclaredMethod("getBlockData");
			Field air = classBlocks.getDeclaredField("AIR");
			Field fieldBlock = classPacket.getDeclaredField("block");
			Constructor<?> constPosition = classBlockPosition.getDeclaredConstructor(int.class, int.class, int.class);
			Constructor<?> constPacket = classPacket.getConstructor(classIBlockAccess, classBlockPosition);
			Object nmsWorld = getHandle.invoke(world);
			Object position = constPosition.newInstance(x, y, z);
			Object blockAIR = air.get(null);
			Object blockData = getBlockData.invoke(blockAIR);
			setTypeAndData.invoke(nmsWorld, position, blockData, 0);

			Object packet = constPacket.newInstance(nmsWorld, position);
			fieldBlock.set(packet, blockData);
			for (Player p : world.getPlayers()) {
				sendPacket(p, packet);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void sendActionMsg(Player player, String msg) {
		try {
			Class<?> classPacket = Class.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutChat");
			Class<?> classChatText = Class.forName("net.minecraft.server." + getNMSVersion() + ".ChatComponentText");
			Class<?> classIChatBase = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent");
			Constructor<?> constChatText = classChatText.getDeclaredConstructor(String.class);
			Constructor<?> constPacket = classPacket.getDeclaredConstructor(classIChatBase, byte.class);
			Object text = constChatText.newInstance(ChatColor.translateAlternateColorCodes('&', msg));
			Object packet = constPacket.newInstance(text, (byte) 2);
			sendPacket(player, packet);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Class<?> classCraftPlayer = Class
					.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".entity.CraftPlayer");
			Class<?> classPlayer = Class.forName("net.minecraft.server." + getNMSVersion() + ".EntityPlayer");
			Class<?> classConnection = Class.forName("net.minecraft.server." + getNMSVersion() + ".PlayerConnection");
			Class<?> classPacket = Class.forName("net.minecraft.server." + getNMSVersion() + ".Packet");
			Method getNMSPlayer = classCraftPlayer.getDeclaredMethod("getHandle");
			Object nmsPlayer = getNMSPlayer.invoke(player);
			Field fieldConnection = classPlayer.getDeclaredField("playerConnection");
			Object conn = fieldConnection.get(nmsPlayer);
			if (conn == null)
				return;
			Method sendPacket = classConnection.getDeclaredMethod("sendPacket", classPacket);
			sendPacket.invoke(conn, packet);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void respawnPlayer(Player player) {
		try {
			if (player != null && player.isDead()) {
				Class<?> classPacket = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".PacketPlayInClientCommand");
				Class<?> classEnum = classPacket.getDeclaredClasses()[0];
				Object enumRespawn = Util.valueOfForce(classEnum, "PERFORM_RESPAWN");
				Constructor<?> constPacket = classPacket.getDeclaredConstructor(classEnum);
				Object packet = constPacket.newInstance(enumRespawn);
				NMSUtil.sendPacket(player, packet);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void sendChatPacket(Player player, Object jsonOrPlainText) {
		try {
			String nmsVersion = NMSUtil.getNMSVersion();
			Class<?> classIChatBase = Class
					.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
			Class<?> classPacket = Class
					.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
			Constructor<?> constPacket = classPacket.getDeclaredConstructor(classIChatBase);
			Object packet;
			if (jsonOrPlainText instanceof JsonElement) {
				Class<?> classChatSerializer = classIChatBase.getDeclaredClasses()[0];
				Method tellrawFromJson = null;
				for (Method m : classChatSerializer.getDeclaredMethods()) {
					if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(JsonElement.class)
							&& m.getReturnType().equals(classIChatBase)) {
						tellrawFromJson = m;
						break;
					}
				}
				if(tellrawFromJson == null) throw new NoSuchMethodException("net.minecraft.server." + nmsVersion + ".IChatBaseComponent.ChatSerializer.<unknown>(JsonElement) IChatBaseComponent");
				Object chatMsg = tellrawFromJson.invoke(null, jsonOrPlainText);
				packet = constPacket.newInstance(chatMsg);
			} else {
				Class<?> classChatMessage = Class
						.forName("net.minecraft.server." + NMSUtil.getNMSVersion() + ".ChatMessage");
				Constructor<?> constChatMessage = classChatMessage.getDeclaredConstructor(String.class, Object[].class);
				Object chatMsg = constChatMessage.newInstance(jsonOrPlainText.toString(), new Object[0]);
				packet = constPacket.newInstance(chatMsg);
			}
			sendPacket(player, packet);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void setEntityPose(Entity entity, Pose pose) {
		setEntityPose(entity, pose.name());
	}

	public static void setEntityPose(Entity entity, String pose) {
		try {
			Class<?> classCraftEntity = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".entity.CraftEntity");
			Class<?> classNMSEntity = Class.forName("net.minecraft.server." + getNMSVersion() + ".Entity");
			Class<?> classNMSEntityPose = Class.forName("net.minecraft.server." + getNMSVersion() + ".EntityPose");
			Method getHandle = classCraftEntity.getDeclaredMethod("getHandle");
			Method setPose = classNMSEntity.getDeclaredMethod("setPose", classNMSEntityPose);
			setPose.setAccessible(true);
			Object nmsEntity = getHandle.invoke(entity);
			Object enumPose = Util.valueOfForce(classNMSEntityPose, pose, classNMSEntityPose.getEnumConstants()[0]);
			setPose.invoke(nmsEntity, enumPose);
		}catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static ItemMeta getMetaFormMaterial(Material material) {
		try {
			Class<?> classCraftItemFactory = Class
					.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".inventory.CraftItemFactory");
			Method getInstance = classCraftItemFactory.getDeclaredMethod("instance");
			Method getItemMeta = classCraftItemFactory.getDeclaredMethod("getItemMeta", Material.class);
			Object itemFactory = getInstance.invoke(null);
			return (ItemMeta) getItemMeta.invoke(itemFactory, material);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public static BlockData newBlockData(Material material) {
		return newBlockData(material, "");
	}

	public static BlockData newBlockData(Material material, String data) {
		try {
			Class<?> classCraftBlockData = Class
					.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".block.data.CraftBlockData");
			Method newData = classCraftBlockData.getDeclaredMethod("newData", Material.class, String.class);
			return (BlockData) newData.invoke(null, material, data);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
}
