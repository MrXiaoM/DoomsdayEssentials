package top.mrxiaom.doomsdayessentials.api;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandProcessEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private final CommandSender sender;
	private final String label;
	private String[] args;

	public CommandProcessEvent(CommandSender sender, String label, String[] args) {
		this.sender = sender;
		this.label = label;
		this.args = args;
	}

	public CommandSender getSender() {
		return sender;
	}

	public String getLabel() {
		return this.label;
	}

	public String[] getArgs() {
		return this.args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	boolean cancelled = false;

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		this.cancelled = value;
	}
}