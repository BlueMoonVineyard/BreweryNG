package com.dre.brewery.recipe;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

public interface BEffect {
	public void apply(int quality, Player player);
	public boolean isHidden();
	public void writeInto(PotionMeta meta, int quality);
}
