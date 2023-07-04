package com.dre.brewery.utility;

import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public interface BTask {
	void cancel();

	public static BTask from(BukkitTask task) {
		return new BBukkitTask(task);
	}
	public static BTask from(ScheduledTask task) {
		return new BFoliaTask(task);
	}

	public record BBukkitTask(BukkitTask task) implements BTask {
		@Override
		public void cancel() {
			task.cancel();
		}
	}
	public record BFoliaTask(ScheduledTask task) implements BTask {
		@Override
		public void cancel() {
			task.cancel();
		}
	}
}
