package top.mrxiaom.doomsdayessentials.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class BlockCounter implements RegionFunction {
	private final Extent extent;
	private boolean separateStates;
	private final Set<Location> locations = new HashSet<>();
	private final World world;

	public BlockCounter(Extent extent, World world) {
		this.extent = extent;
		this.world = world;
	}

	public boolean apply(BlockVector3 position) {
		BlockState blk = this.extent.getBlock(position);
		if (!this.separateStates) {
			blk = blk.getBlockType().getDefaultState();
		}
		locations.add(BukkitAdapter.adapt(world, position));

		return true;
	}

	public Set<Location> getLocs() {
		return this.locations;
	}
}
