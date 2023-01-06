package com.dre.brewery.recipe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import com.dre.brewery.P;

public class BSequenceEffect implements BEffect {
	private Duration pause;
	private List<BEffect> effects;

	public BSequenceEffect(int duration, List<BEffect> inner) {
		pause = Duration.ofSeconds(duration);
		effects = new ArrayList<>(inner);
	}

	@Override
	public void apply(int quality, Player player) {
		Duration delay = Duration.ofSeconds(0);
		for (BEffect bEffect : effects) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(P.p, () -> {
				if (!player.isOnline())
					return;
				bEffect.apply(quality, player);
			}, delay.toSeconds() * 20);
			delay = delay.plus(pause);
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
