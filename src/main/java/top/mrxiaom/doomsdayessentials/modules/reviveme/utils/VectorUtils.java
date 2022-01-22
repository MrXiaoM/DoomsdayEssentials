package top.mrxiaom.doomsdayessentials.modules.reviveme.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class VectorUtils {
    public static final BlockFace[] axis;
    // $FF: synthetic field
    private static volatile int[] $SWITCH_TABLE$org$bukkit$block$BlockFace;

    static {
        axis = new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    }

    public static Block getDirBlock(Location plocation) {
        Block cur = plocation.getBlock();
        BlockFace face = yawToFace(plocation.getYaw());
        return face == null ? null : cur.getRelative(face);
    }

    public static BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90.0F) & 3];
    }

    public static List<Player> getNear(double radius, Player player) {
        List<Player> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().stream()
                .filter((other) -> player.getWorld().equals(other.getWorld()))
                .filter((other) -> player.getLocation().distance(other.getLocation()) <= radius)
                .forEach(players::add);
        return players;
    }

    public static float faceToYaw(BlockFace face) {
        switch ($SWITCH_TABLE$org$bukkit$block$BlockFace()[face.ordinal()]) {
            case 1:
                return 50.0F;
            case 2:
                return 132.0F;
            case 3:
                return 290.0F;
            case 4:
                return 0.0F;
            default:
                return 0.0F;
        }
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$org$bukkit$block$BlockFace() {
        int[] var10000 = $SWITCH_TABLE$org$bukkit$block$BlockFace;
        if (var10000 != null) {
            return var10000;
        } else {
            int[] var0 = new int[BlockFace.values().length];

            try {
                var0[BlockFace.DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.EAST.ordinal()] = 2;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.EAST_NORTH_EAST.ordinal()] = 14;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.EAST_SOUTH_EAST.ordinal()] = 15;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.NORTH.ordinal()] = 1;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.NORTH_EAST.ordinal()] = 7;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.NORTH_NORTH_EAST.ordinal()] = 13;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.NORTH_NORTH_WEST.ordinal()] = 12;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.NORTH_WEST.ordinal()] = 8;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SELF.ordinal()] = 19;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SOUTH.ordinal()] = 3;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SOUTH_EAST.ordinal()] = 9;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = 16;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = 17;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.SOUTH_WEST.ordinal()] = 10;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.UP.ordinal()] = 5;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.WEST.ordinal()] = 4;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.WEST_NORTH_WEST.ordinal()] = 11;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[BlockFace.WEST_SOUTH_WEST.ordinal()] = 18;
            } catch (NoSuchFieldError ignored) {
            }

            $SWITCH_TABLE$org$bukkit$block$BlockFace = var0;
            return var0;
        }
    }
}
