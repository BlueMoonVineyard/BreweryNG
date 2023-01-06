package com.dre.brewery.recipe;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import com.dre.brewery.P;

public class BRepeatEffect implements BEffect {
	private Duration pauseBetweenDuration;
	private BEffect inner;
	private int repeatNumTimes;

	public BRepeatEffect(int times, int pauseBetween, BEffect effect) {
		repeatNumTimes = times;
		pauseBetweenDuration = Duration.ofSeconds(pauseBetween);
		inner = effect;
	}

	@Override
	public void apply(int quality, Player player) {
		for (int i = 0; i < repeatNumTimes; i++) {
			long ticks = i * pauseBetweenDuration.toSeconds() * 20;
			Bukkit.getScheduler().scheduleSyncDelayedTask(P.p, () -> {
				if (!player.isOnline())
					return;
				inner.apply(quality, player);
			}, ticks);
		}
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public void writeInto(PotionMeta meta, int quality) {
	}
}
