package com.dre.brewery.filedata;


import java.io.File;
import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;

import com.dre.brewery.P;
import com.dre.brewery.utility.BTask;

/**
 * Writes the collected Data to file in Async Thread
 */
public class WriteData implements Consumer<BTask> {

	private FileConfiguration data;
	private FileConfiguration worldData;

	public WriteData(FileConfiguration data, FileConfiguration worldData) {
		this.data = data;
		this.worldData = worldData;
	}

	@Override
	public void accept(BTask task) {
		File datafile = new File(P.p.getDataFolder(), "data.yml");
		File worlddatafile = new File(P.p.getDataFolder(), "worlddata.yml");

		try {
			data.save(datafile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			worldData.save(worlddatafile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataSave.lastSave = 1;
		BData.dataMutex.set(0);
	}
}
