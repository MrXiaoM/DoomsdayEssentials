package top.mrxiaom.doomsdayessentials.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BeamBreakBlockEvent extends BlockBreakEvent{
	public BeamBreakBlockEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}
}
