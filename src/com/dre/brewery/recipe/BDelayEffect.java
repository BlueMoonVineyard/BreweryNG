package com.dre.brewery.recipe;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import com.dre.brewery.P;

public class BDelayEffect implements BEffect {
	private Duration duration;
	private BEffect inner;

	public BDelayEffect(int seconds, BEffect effect) {
		duration = Duration.ofSeconds(seconds);
		inner = effect;
	}

	@Override
	public void apply(int quality, Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(P.p, () -> {
			if (!player.isOnline())
				return;
			inner.apply(quality, player);
		}, duration.toSeconds() * 20);
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public void writeInto(PotionMeta meta, int quality) {
	}
}
