package top.mrxiaom.doomsdayessentials.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SpaceUtil {

	public static Location getLocation(String world, double x, double y, double z) {
		World w = Bukkit.getWorld(world);
		if (w == null)
			return null;
		return new Location(w, x, y, z);
	}

	public static Location rotateLocation(Location loc, double length, double angle) {
		return rotateLocation(loc, 0, 0, 0, length, angle);
	}

	public static Location rotateLocation(Location loc, double xOffset, double yOffset, double zOffset, double length,
			double angle) {
		World world = loc.getWorld();
		double x = loc.getX() + xOffset;
		double y = loc.getY() + yOffset;
		double z = loc.getZ() + zOffset;
		float pitch = loc.getPitch();
		float yaw = loc.getYaw();
		System.out.println(loc.getYaw());
		double a = angle - loc.getYaw();
		while (a < 0) {
			a += 360.0D;
		}
		System.out.println(a);
		Double newAngle = (a) % 360.0D;
		double radians = Math.toRadians(newAngle % 90);
		// 特殊角
		if (newAngle.equals(0.0D))
			return new Location(world, x, y, z + length, yaw, pitch);
		if (newAngle.equals(90.0D))
			return new Location(world, x + length, y, z, yaw, pitch);
		if (newAngle.equals(180.0D))
			return new Location(world, x, y, z - length, yaw, pitch);
		if (newAngle.equals(270.0D))
			return new Location(world, x - length, y, z, yaw, pitch);
		double sinL = length * Math.sin(radians);
		double cosL = length * Math.cos(radians);
		if (newAngle > 0.0D && newAngle < 90.0D)
			return new Location(world, x + sinL, y, z + cosL, yaw, pitch);
		if (newAngle > 90.0D && newAngle < 180.0D)
			return new Location(world, x + cosL, y, z - sinL, yaw, pitch);
		if (newAngle > 180.0D && newAngle < 270.0D)
			return new Location(world, x - sinL, y, z - cosL, yaw, pitch);
		if (newAngle > 270.0D && newAngle < 360.0D)
			return new Location(world, x - cosL, y, z + sinL, yaw, pitch);
		return loc;
	}

	public static List<Block> getSightLineBlocks(Player player, int length) {
		Location loc = player.getLocation().clone();
		Vector v = loc.getDirection().clone();
		Vector unit = v.clone();
		List<Block> result = new ArrayList<>();
		for (int i = 0; i <= length; i++) {
			Block b = loc.getWorld().getBlockAt(loc.getBlockX() + v.getBlockX(), loc.getBlockY() + v.getBlockY() + 1,
					loc.getBlockZ() + v.getBlockZ());
			result.add(b);
			v.add(unit);
		}
		return result;
	}

	public static boolean signLineEquals(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).equals(content);
	}

	public static boolean signLineEqualsIgnoreCase(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).equalsIgnoreCase(content);
	}

	public static boolean signLineContains(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).contains(content);
	}

	public static boolean signLineContainsIgnoreCase(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).toLowerCase().contains(content.toLowerCase());
	}

	public static boolean signLineStartsWith(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).startsWith(content);
	}

	public static boolean signLineStartsWithIgnoreCase(Block block, int line, String content) {
		if (line < 0 || line > 3 || block == null || !(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		return sign.getLine(line).toLowerCase().startsWith(content.toLowerCase());
	}

	public static boolean signContains(Block block, String content) {
		if (!(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		for (String s : sign.getLines()) {
			if (s != null && s.contains(content))
				return true;
		}
		return false;
	}

	public static boolean signContainsIgnoreCase(Block block, String content) {
		if (!(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		for (String s : sign.getLines()) {
			if (s != null && s.toLowerCase().contains(content.toLowerCase()))
				return true;
		}
		return false;
	}

	public static boolean signStartsWith(Block block, String content) {
		if (!(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		for (String s : sign.getLines()) {
			if (s != null && s.startsWith(content))
				return true;
		}
		return false;
	}

	public static boolean signStartsWithIgnoreCase(Block block, String content) {
		if (!(block.getState() instanceof Sign))
			return false;
		Sign sign = (Sign) block.getState();
		for (String s : sign.getLines()) {
			if (s != null && s.toLowerCase().startsWith(content.toLowerCase()))
				return true;
		}
		return false;
	}
}
