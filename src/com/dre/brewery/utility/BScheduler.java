package com.dre.brewery.utility;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.function.Consumer;

public interface BScheduler {
	BTask runTaskAt(Location location, Consumer<BTask> task, long delay);
	BTask runTaskFor(Entity entity, Consumer<BTask> task, long delay);
	BTask runAsyncTaskOnTimer(Consumer<BTask> task, long initialDelayTicks, long intervalTicks);
	BTask runAsyncTaskLater(Consumer<BTask> task, long delay);

	public static BScheduler getScheduler(Plugin p) {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return new FoliaScheduler(p);
		} catch (ClassNotFoundException e) {
			return new BukkitScheduler(p);
		}
	}

	public record FoliaScheduler(Plugin p) implements BScheduler {
		@Override
		public BTask runTaskAt(Location location, Consumer<BTask> task, long delay) {
			if (delay <= 0) {
				var ret = p.getServer().getRegionScheduler().run(p, location, sched -> task.accept(BTask.from(sched)));
				return BTask.from(ret);
			} else {
				var ret = p.getServer().getRegionScheduler().runDelayed(p, location, sched -> task.accept(BTask.from(sched)), delay);
				return BTask.from(ret);
			}
		}

		@Override
		public BTask runTaskFor(Entity entity, Consumer<BTask> task, long delay) {
			if (delay <= 0) {
				var ret = entity.getScheduler().run(p, sched -> task.accept(BTask.from(sched)), null);
				return BTask.from(ret);
			} else {
				var ret = entity.getScheduler().runDelayed(p, sched -> task.accept(BTask.from(sched)), null, delay);
				return BTask.from(ret);
			}
		}

		@Override
		public BTask runAsyncTaskOnTimer(Consumer<BTask> task, long initialDelayTicks, long intervalTicks) {
			var ret = p.getServer().getAsyncScheduler().runAtFixedRate(p, sched -> task.accept(BTask.from(sched)), initialDelayTicks * 50, intervalTicks * 50, TimeUnit.MILLISECONDS);
			return BTask.from(ret);
		}

		@Override
		public BTask runAsyncTaskLater(Consumer<BTask> task, long delay) {
			var ret = p.getServer().getAsyncScheduler().runDelayed(p, sched -> task.accept(BTask.from(sched)), delay * 50, TimeUnit.MILLISECONDS);
			return BTask.from(ret);
		}
	}

	public record BukkitScheduler(Plugin p) implements BScheduler {
		private class Runner extends BukkitRunnable {
			Consumer<BTask> task;
			public Runner(Consumer<BTask> task) {
				this.task = task;
			}
			@Override
			public void run() {
				try {
					var field = getClass().getSuperclass().getDeclaredField("task");
					field.setAccessible(true);
					var task = (BukkitTask)field.get(this);

					this.task.accept(BTask.from(task));
				} catch (Throwable it) {
					forciblyThrow(it);
				}
			}
			@SuppressWarnings("unchecked")
			private static <T extends Throwable> T forciblyThrow(Throwable t) throws T {
				throw (T)t;
			}
		}

		@Override
		public BTask runTaskAt(Location location, Consumer<BTask> task, long delay) {
			var ret = new Runner(task).runTaskLater(p, delay);
			return BTask.from(ret);
		}


		@Override
		public BTask runTaskFor(Entity entity, Consumer<BTask> task, long delay) {
			var ret = new Runner(task).runTaskLater(p, delay);
			return BTask.from(ret);
		}

		@Override
		public BTask runAsyncTaskOnTimer(Consumer<BTask> task, long initialDelayTicks, long intervalTicks) {
			var ret = new Runner(task).runTaskTimer(p, initialDelayTicks, intervalTicks);
			return BTask.from(ret);
		}

		@Override
		public BTask runAsyncTaskLater(Consumer<BTask> task, long delay) {
			var ret = new Runner(task).runTaskLaterAsynchronously(p, delay);
			return BTask.from(ret);
		}
	}
}
