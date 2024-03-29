package com.dre.brewery.filedata;


import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.dre.brewery.P;
import com.dre.brewery.utility.BTask;

import java.util.function.Consumer;

public class ReadOldData implements Consumer<BTask> {

	private FileConfiguration data;
	private Consumer<FileConfiguration> followUpConsumer;

	public ReadOldData(Consumer<FileConfiguration> followUpConsumer) {
		this.followUpConsumer = followUpConsumer;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public void accept(BTask it) {
		int wait = 0;
		// Set the Data Mutex to -1 if it is 0=Free
		while (!BData.dataMutex.compareAndSet(0, -1)) {
			if (wait > 300) {
				P.p.errorLog("Loading Process active for too long while trying to save! Mutex: " + BData.dataMutex.get());
				return;
			}
			wait++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}


		File worldDataFile = new File(P.p.getDataFolder(), "worlddata.yml");
		if (BData.worldData == null) {
			if (!worldDataFile.exists()) {
				data = new YamlConfiguration();
				followUpConsumer.accept(data);
				return;
			}

			data = YamlConfiguration.loadConfiguration(worldDataFile);
		} else {
			data = BData.worldData;
		}

		if (DataSave.lastBackup > 10) {
			worldDataFile.renameTo(new File(P.p.getDataFolder(), "worlddataBackup.yml"));
			DataSave.lastBackup = 0;
		} else {
			DataSave.lastBackup++;
		}

		followUpConsumer.accept(data);
	}

	public FileConfiguration getData() {
		return data;
	}

}
