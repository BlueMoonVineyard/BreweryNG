package com.dre.brewery.filedata;


import com.dre.brewery.*;
import com.dre.brewery.utility.BTask;
import com.dre.brewery.utility.BUtil;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataSave implements Consumer<FileConfiguration> {

	public static int lastBackup = 0;
	public static int lastSave = 1;
	public static int autosave = 3;
	final public static String dataVersion = "1.2";
	public static List<World> unloadingWorlds = new CopyOnWriteArrayList<>();

	private final long time;
	private final List<World> loadedWorlds;
	public boolean collected = false;

	// Not Thread-Safe! Needs to be run in main thread but uses async Read/Write
	public DataSave() {
		time = System.currentTimeMillis();
		loadedWorlds = P.p.getServer().getWorlds();
	}

	@Override
	public void accept(FileConfiguration oldWorldData) {
		try {
			long saveTime = System.nanoTime();
			BData.worldData = null;

			FileConfiguration data = new YamlConfiguration();
			FileConfiguration worldData = new YamlConfiguration();

			data.set("installTime", Brew.installTime);
			data.set("MCBarrelTime", MCBarrel.mcBarrelTime);

			Brew.writePrevSeeds(data);

			List<Integer> brewsCreated = new ArrayList<>(7);
			brewsCreated.add(P.p.stats.brewsCreated);
			brewsCreated.add(P.p.stats.brewsCreatedCmd);
			brewsCreated.add(P.p.stats.exc);
			brewsCreated.add(P.p.stats.good);
			brewsCreated.add(P.p.stats.norm);
			brewsCreated.add(P.p.stats.bad);
			brewsCreated.add(P.p.stats.terr);
			data.set("brewsCreated", brewsCreated);
			data.set("brewsCreatedH", brewsCreated.hashCode());

			if (!Brew.legacyPotions.isEmpty()) {
				Brew.saveLegacy(data.createSection("Brew"));
			}

			if (!BPlayer.isEmpty()) {
				BPlayer.save(data.createSection("Player"));
			}

			if (!BCauldron.bcauldrons.isEmpty() || oldWorldData.contains("BCauldron")) {
				BCauldron.save(worldData.createSection("BCauldron"), oldWorldData.getConfigurationSection("BCauldron"));
			}

			if (!Barrel.barrels.isEmpty() || oldWorldData.contains("Barrel")) {
				Barrel.save(worldData.createSection("Barrel"), oldWorldData.getConfigurationSection("Barrel"));
			}

			if (!Wakeup.wakeups.isEmpty() || oldWorldData.contains("Wakeup")) {
				Wakeup.save(worldData.createSection("Wakeup"), oldWorldData.getConfigurationSection("Wakeup"));
			}

			CraftedBrewTracker.saveTo(data.createSection("CraftedBrews"));

			saveWorldNames(worldData, oldWorldData.getConfigurationSection("Worlds"));

			data.set("Version", dataVersion);

			collected = true;

			if (!unloadingWorlds.isEmpty()) {
				try {
					for (World world : unloadingWorlds) {
						// In the very most cases, it is just one world, so just looping like this is fine
						Barrel.onUnload(world);
						BCauldron.onUnload(world);
						Wakeup.onUnload(world);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				unloadingWorlds.clear();
			}

			P.p.debugLog("saving: " + ((System.nanoTime() - saveTime) / 1000000.0) + "ms");

			if (P.p.isEnabled()) {
				P.p.scheduler.runAsyncTaskLater(new WriteData(data, worldData), 0);
			} else {
				new WriteData(data, worldData).accept(null);
			}
			// Mutex will be released in WriteData
		} catch (Exception e) {
			e.printStackTrace();
			BData.dataMutex.set(0);
		}
	}

	public void saveWorldNames(FileConfiguration root, ConfigurationSection old) {
		if (old != null) {
			root.set("Worlds", old);
		}
		for (World world : loadedWorlds) {
			String worldName = world.getName();
			if (worldName.startsWith("DXL_")) {
				worldName = BUtil.getDxlName(worldName);
				root.set("Worlds." + worldName, 0);
			} else {
				worldName = world.getUID().toString();
				root.set("Worlds." + worldName, world.getName());
			}
		}
	}

	// Save all data. Takes a boolean whether all data should be collected in instantly
	public static void save(boolean collectInstant) {
		ReadOldData read = new ReadOldData(new DataSave());
		if (collectInstant) {
			read.accept(null);
		} else {
			P.p.scheduler.runAsyncTaskLater(read, 0);
		}
	}

	public static void autoSave() {
		if (lastSave >= autosave) {
			save(false);// save all data
		} else {
			lastSave++;
		}
	}
}
