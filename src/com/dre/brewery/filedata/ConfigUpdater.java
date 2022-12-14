package com.dre.brewery.filedata;

import com.dre.brewery.P;
import com.dre.brewery.utility.LegacyUtil;
import com.dre.brewery.utility.Tuple;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.mini2Dx.gettext.GetText;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigUpdater {

	private ArrayList<String> config = new ArrayList<>();
	private File file;

	public ConfigUpdater(File file) {
		this.file = file;
		getConfigString();
	}

	// Returns the index of the line that starts with 'lineStart', returns -1 if not found;
	public int indexOfStart(String lineStart) {
		for (int i = 0; i < config.size(); i++) {
			if (config.get(i).startsWith(lineStart)) {
				return i;
			}
		}
		return -1;
	}

	// Adds some lines to the end
	public void appendLines(String... lines) {
		config.addAll(Arrays.asList(lines));
	}

	// Replaces the line at the index with the new Line
	public void setLine(int index, String newLine) {
		config.set(index, newLine);
	}

	// adds some Lines at the index
	// Will push all lines including the one at index down
	public void addLines(int index, String... newLines) {
		config.addAll(index, Arrays.asList(newLines));
	}

	public void removeLine(int index) {
		config.remove(index);
	}

	public void addLinesAt(String[] search, int offset, String... newLines) {
		addLinesAt(search, offset, true, newLines);
	}

	public void addLinesAt(String[] search, int offset, boolean appendIfNotFound, String... newLines) {
		int index = indexOfStart(search[0]);
		int s = 1;
		while (index == -1 && s < search.length) {
			index = indexOfStart(search[s]);
			s++;
		}

		if (index != -1) {
			addLines(index + offset, newLines);
		} else if (appendIfNotFound) {
			appendLines(newLines);
		}
	}

	public void saveConfig() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String line : config) {
			stringBuilder.append(line).append("\n");
		}
		String configString = stringBuilder.toString().trim();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(configString);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getConfigString() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				config.add(currentLine);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	// ---- Updating Scramble Seed ----

	public void setEncodeKey(long key) {
		int index = indexOfStart("encodeKey:");
		if (index != -1) {
			setLine(index, "encodeKey: " + key);
			return;
		}

		// Old key not present
		index = indexOfStart("enableEncode:");
		if (index == -1) {
			index = indexOfStart("# So enable this if you want to make recipe cheating harder");
		}
		if (index == -1) {
			index = indexOfStart("version:");
		}
		if (index != -1) {
			addLines(index + 1, "encodeKey: " + key);
		} else {
			addLines(1, "encodeKey: " + key);
		}

	}

	// ---- Updating to newer Versions ----

	// Update from a specified Config version and language to the newest version
	public void update(String fromVersion, boolean oldMat, String lang, FileConfiguration yml) {
		if (fromVersion.equals("0.5")) {
			// Version 0.5 was only released for de, but with en as setting, so default to de
			if (!lang.equals("de")) {
				lang = "de";
			}
		}
		boolean de = lang.equals("de");

		if (fromVersion.equals("0.5") || fromVersion.equals("1.0")) {
			if (de) {
				update05de();
			} else {
				update10en();
			}
			fromVersion = "1.1";
		}
		if (fromVersion.equals("1.1") || fromVersion.equals("1.1.1")) {
			if (de) {
				update11de();
			} else {
				update11en();
			}
			fromVersion = "1.2";
		}

		if (fromVersion.equals("1.2")) {
			if (de) {
				update12de();
			} else {
				update12en();
			}
			fromVersion = "1.3";
		}

		if (fromVersion.equals("1.3")) {
			if (de) {
				update13de();
			} else {
				update13en();
			}
			fromVersion = "1.3.1";
		}

		if (fromVersion.equals("1.3.1")) {
			if (de) {
				update131de();
			} else {
				update131en();
			}
			fromVersion = "1.4";
		}

		if (fromVersion.equals("1.4")) {
			if (de) {
				update14de();
			} else {
				update14en();
			}
			fromVersion = "1.5";
		}

		if (fromVersion.equals("1.5") || fromVersion.equals("1.6")) {
			update15(P.use1_13, de);
			fromVersion = "1.7";
			oldMat = false;
		}

		if (fromVersion.equals("1.7")) {
			if (de) {
				update17de();
			} else {
				update17en();
			}
			fromVersion = "1.8";
		}

		if (fromVersion.equals("1.8")) {
			if (de) {
				update18de(yml);
			} else if (lang.equals("fr")) {
				update18fr(yml);
			} else {
				update18en(yml);
			}
			fromVersion = "2.0";
		}

		if (fromVersion.equals("2.0")) {
			if (de) {
				update20de();
			} else if (lang.equals("fr")) {
				update20fr();
			} else {
				update20en();
			}
			fromVersion = "2.1";
		}

		if (fromVersion.equals("2.1")) {
			if (de) {
				update21de();
			} else if (lang.equals("fr")) {
				update21fr();
			} else {
				update21en();
			}
			fromVersion = "2.1.1";
		}
		if (fromVersion.equals("2.1.1")) {
			update30CauldronParticles();
			if (de) {
				update30de();
			} else {
				update30en();
			}
			fromVersion = "3.0";
		}
		if (fromVersion.equals("3.0")) {
			if (de) {
				update31de();
			} else {
				update31en();
			}
			updateVersion(BConfig.configVersion);
			fromVersion = "3.1";
		}

		if (P.use1_13 && oldMat) {
			updateMaterials(true);
			updateMaterialDescriptions(de);
		}

		if (!fromVersion.equals(BConfig.configVersion)) {
			P.p.log(GetText.tr("Unknown Brewery config version: v{0}, config was not updated!", fromVersion));
			return;
		}
		saveConfig();
	}

	// Update the Version String
	private void updateVersion(String to) {
		int index = indexOfStart("version");
		String line = "version: '" + to + "'";
		if (index != -1) {
			setLine(index, line);
		} else {
			index = indexOfStart("# Config Version");
			if (index == -1) {
				index = indexOfStart("autosave");
			}
			if (index == -1) {
				appendLines(line);
			} else {
				addLines(index, line);
			}
		}
	}

	// Updates de from 0.5 to 1.1
	private void update05de() {
		updateVersion("1.1");

		// Default language to de
		int index = indexOfStart("language: en");
		if (index != -1) {
			setLine(index, "language: de");
		}

		// Add the new entries for the Word Distortion above the words section
		String[] entries = {
			"# -- Chat Ver??nderungs Einstellungen --",
			"",
			"# Text nach den angegebenen Kommandos wird bei Trunkenheit ebenfalls Ver??ndert (Liste) [- /gl]",
			"distortCommands:",
			"- /gl",
			"- /global",
			"- /fl",
			"- /s",
			"- /letter",
			"",
			"# Geschriebenen Text auf Schildern bei Trunkenheit ver??ndern [false]",
			"distortSignText: false",
			"",
			"# Text, der zwischen diesen Buchstaben steht, wird nicht ver??ndert (\",\" als Trennung verwenden) (Liste) [- '[,]']",
			"distortBypass:",
			"- '*,*'",
			"- '[,]'",
			""
			};
		index = indexOfStart("# words");
		if (index == -1) {
			index = indexOfStart("# Diese werden von oben");
		}
		if (index == -1) {
			index = indexOfStart("# replace");
		}
		if (index == -1) {
			index = indexOfStart("words:");
		}
		if (index == -1) {
			appendLines(entries);
		} else {
			addLines(index, entries);
		}

		// Add some new separators for overview
		String line = "# -- Verschiedene Einstellungen --";
		index = indexOfStart("# Verschiedene Einstellungen");
		if (index != -1) {
			setLine(index, line);
		}

		line = "# -- Rezepte f??r Getr??nke --";
		index = indexOfStart("# Rezepte f??r Getr??nke");
		if (index != -1) {
			setLine(index, line);
		}
	}

	// Updates en from 1.0 to 1.1
	private void update10en() {
		// Update version String
		updateVersion("1.1");

		// Add the new entries for the Word Distortion above the words section
		String[] entries = {
			"# -- Chat Distortion Settings --",
			"",
			"# Text after specified commands will be distorted when drunk (list) [- /gl]",
			"distortCommands:",
			"- /gl",
			"- /global",
			"- /fl",
			"- /s",
			"- /letter",
			"",
			"# Distort the Text written on a Sign while drunk [false]",
			"distortSignText: false",
			"",
			"# Enclose a text with these Letters to bypass Chat Distortion (Use \",\" as Separator) (list) [- '[,]']",
			"distortBypass:",
			"- '*,*'",
			"- '[,]'",
			""
			};
		int index = indexOfStart("# words");
		if (index == -1) {
			index = indexOfStart("# Will be processed");
		}
		if (index == -1) {
			index = indexOfStart("# replace");
		}
		if (index == -1) {
			index = indexOfStart("words:");
		}
		if (index == -1) {
			appendLines(entries);
		} else {
			addLines(index, entries);
		}

		// Add some new separators for overview
		String line = "# -- Settings --";
		index = indexOfStart("# Settings");
		if (index != -1) {
			setLine(index, line);
		}

		line = "# -- Recipes for Potions --";
		index = indexOfStart("# Recipes for Potions");
		if (index != -1) {
			setLine(index, line);
		}
	}

	// Updates de from 1.1 to 1.2
	private void update11de() {
		updateVersion("1.2");

		int index = indexOfStart("# Das Item kann nicht aufgesammelt werden");
		if (index != -1) {
			setLine(index, "# Das Item kann nicht aufgesammelt werden und bleibt bis zum Despawnen liegen. (Achtung: Kann nach Serverrestart aufgesammelt werden!)");
		}

		// Add the BarrelAccess Setting
		String[] lines = {
				"# Ob gro??e F??sser an jedem Block ge??ffnet werden k??nnen, nicht nur an Zapfhahn und Schild. Bei kleinen F??ssern geht dies immer. [true]",
				"openLargeBarrelEverywhere: true",
				""
		};
		index = indexOfStart("colorInBrewer") + 2;
		if (index == 1) {
			index = indexOfStart("colorInBarrels") + 2;
		}
		if (index == 1) {
			index = indexOfStart("# Autosave");
		}
		if (index == -1) {
			index = indexOfStart("language") + 2;
		}
		if (index == 1) {
			addLines(3, lines);
		} else {
			addLines(index, lines);
		}

		// Add Plugin Support Settings
		lines = new String[] {
				"",
				"# -- Plugin Kompatiblit??t --",
				"",
				"# Andere Plugins (wenn installiert) nach Rechten zum ??ffnen von F??ssern checken [true]",
				"useWorldGuard: true",
				"useLWC: true",
				"useGriefPrevention: true",
				"",
				"# ??nderungen an Fassinventaren mit LogBlock aufzeichen [true]",
				"useLogBlock: true",
				"",
				""
		};
		index = indexOfStart("# -- Chat Ver??nderungs Einstellungen");
		if (index == -1) {
			index = indexOfStart("# words");
		}
		if (index == -1) {
			index = indexOfStart("distortCommands");
			if (index > 4) {
				index -= 4;
			}
		}
		if (index != -1) {
			addLines(index, lines);
		} else {
			appendLines(lines);
		}
	}

	// Updates en from 1.1 to 1.2
	private void update11en() {
		updateVersion("1.2");

		int index = indexOfStart("# The item can not be collected");
		if (index != -1) {
			setLine(index, "# The item can not be collected and stays on the ground until it despawns. (Warning: Can be collected after Server restart!)");
		}

		// Add the BarrelAccess Setting
		String[] lines = {
				"# If a Large Barrel can be opened by clicking on any of its blocks, not just Spigot or Sign. This is always true for Small Barrels. [true]",
				"openLargeBarrelEverywhere: true",
				""
		};
		index = indexOfStart("colorInBrewer") + 2;
		if (index == 1) {
			index = indexOfStart("colorInBarrels") + 2;
		}
		if (index == 1) {
			index = indexOfStart("# Autosave");
		}
		if (index == -1) {
			index = indexOfStart("language") + 2;
		}
		if (index == 1) {
			addLines(3, lines);
		} else {
			addLines(index, lines);
		}

		// Add Plugin Support Settings
		lines = new String[] {
				"",
				"# -- Plugin Compatibility --",
				"",
				"# Enable checking of other Plugins (if installed) for Barrel Permissions [true]",
				"useWorldGuard: true",
				"useLWC: true",
				"useGriefPrevention: true",
				"",
				"# Enable the Logging of Barrel Inventories to LogBlock [true]",
				"useLogBlock: true",
				"",
				""
		};
		index = indexOfStart("# -- Chat Distortion Settings");
		if (index == -1) {
			index = indexOfStart("# words");
		}
		if (index == -1) {
			index = indexOfStart("distortCommands");
			if (index > 4) {
				index -= 4;
			}
		}
		if (index != -1) {
			addLines(index, lines);
		} else {
			appendLines(lines);
		}
	}

	// Update de from 1.2 to 1.3
	private void update12de() {
		updateVersion("1.3");

		// Add the new Wood Types to the Description
		int index = indexOfStart("# wood:");
		if (index != -1) {
			setLine(index, "# wood: Holz des Fasses 0=alle Holzsorten 1=Birke 2=Eiche 3=Jungel 4=Fichte 5=Akazie 6=Schwarzeiche");
		}

		// Add the Example to the Cooked Section
		index = indexOfStart("# cooked:");
		if (index != -1) {
			addLines(index + 1, "# [Beispiel] MATERIAL_oder_id: Name nach G??hren");
		}

		// Add new ingredients description
		String replacedLine = "# ingredients: Auflistung von 'Material oder ID,Data/Anzahl'";
		String[] lines = new String[] {
				"#   (Item-ids anstatt Material werden von Bukkit nicht mehr unterst??tzt und funktionieren m??glicherweise in Zukunft nicht mehr!)",
				"#   Eine Liste von allen Materialien kann hier gefunden werden: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html",
				"#   Es kann ein Data-Wert angegeben werden, weglassen ignoriert diesen beim hinzuf??gen einer Zutat"
		};
		index = indexOfStart("# ingredients:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# name:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			} else {
				index = indexOfStart("# -- Rezepte f??r Getr??nke --");
				if (index != -1) {
					addLines(index + 2, lines);
					addLines(index + 2, "", replacedLine);
				}
			}
		}

		// Split the Color explanation into two lines
		replacedLine = "# color: Farbe des Getr??nks nach destillieren/reifen.";
		lines = new String[] {
				"#   Benutzbare Farben: DARK_RED, RED, BRIGHT_RED, ORANGE, PINK, BLUE, CYAN, WATER, GREEN, BLACK, GREY, BRIGHT_GREY"
		};

		index = indexOfStart("# color:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# age:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			}
		}

		// Add all the new info to the effects description
		replacedLine = "# effects: Auflistung Effekt/Level/Dauer  Besonderere Trank-Effekte beim Trinken, Dauer in sek.";
		lines = new String[] {
				"#   Ein 'X' an den Namen anh??ngen, um ihn zu verbergen. Bsp: 'POISONX/2/10' (WEAKNESS, INCREASE_DAMAGE, SLOW und SPEED sind immer verborgen.)",
				"#   M??gliche Effekte: http://jd.bukkit.org/rb/apidocs/org/bukkit/potion/PotionEffectType.html",
				"#   Minimale und Maximale Level/Dauer k??nnen durch \"-\" festgelegt werden, Bsp: 'SPEED/1-2/30-40' = Level 1 und 30 sek minimal, Level 2 und 40 sek maximal",
				"#   Diese Bereiche funktionieren auch umgekehrt, Bsp: 'POISON/3-1/20-5' f??r abschw??chende Effekte bei guter Qualit??t",
				"#   L??ngste m??gliche Effektdauer: 1638 sek. Es muss keine Dauer f??r Effekte mit sofortiger Wirkung angegeben werden."
		};

		index = indexOfStart("# effects:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# alcohol:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			} else {
				index = indexOfStart("# -- Rezepte f??r Getr??nke --");
				if (index != -1) {
					addLines(index + 2, lines);
					addLines(index + 2, "", replacedLine);
				}
			}
		}
		if (index != -1) {
			index = indexOfStart("#   (WEAKNESS, INCREASE_DAMAGE, SLOW und SPEED sind immer verborgen.)  M??gliche Effekte:");
			if (index != -1) {
				removeLine(index);
			}
		}
		index = indexOfStart("#   Bei Effekten mit sofortiger Wirkung ");
		if (index != -1) {
			removeLine(index);
		}

	}

	// Update en from 1.2 to 1.3
	private void update12en() {
		updateVersion("1.3");

		// Add the new Wood Types to the Description
		int index = indexOfStart("# wood:");
		if (index != -1) {
			setLine(index, "# wood: Wood of the barrel 0=any 1=Birch 2=Oak 3=Jungle 4=Spruce 5=Acacia 6=Dark Oak");
		}

		// Add the Example to the Cooked Section
		index = indexOfStart("# cooked:");
		if (index != -1) {
			addLines(index + 1, "# [Example] MATERIAL_or_id: Name after cooking");
		}

		// Add new ingredients description
		String replacedLine = "# ingredients: List of 'material or id,data/amount'";
		String[] lines = new String[] {
				"#   (Item-ids instead of material are deprecated by bukkit and may not work in the future!)",
				"#   A list of materials can be found here: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html",
				"#   You can specify a data value, omitting it will ignore the data value of the added ingredient"
		};
		index = indexOfStart("# ingredients:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# name:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			} else {
				index = indexOfStart("# -- Recipes for Potions --");
				if (index != -1) {
					addLines(index + 2, lines);
					addLines(index + 2, "", replacedLine);
				}
			}
		}

		// Split the Color explanation into two lines
		replacedLine = "# color: Color of the potion after distilling/aging.";
		lines = new String[] {
				"#   Usable Colors: DARK_RED, RED, BRIGHT_RED, ORANGE, PINK, BLUE, CYAN, WATER, GREEN, BLACK, GREY, BRIGHT_GREY"
		};

		index = indexOfStart("# color:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# age:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			}
		}

		// Add all the new info to the effects description
		replacedLine = "# effects: List of effect/level/duration  Special potion-effect when drinking, duration in sek.";
		lines = new String[] {
				"#   Suffix name with 'X' to hide effect from label. Sample: 'POISONX/2/10' (WEAKNESS, INCREASE_DAMAGE, SLOW and SPEED are always hidden.)",
				"#   Possible Effects: http://jd.bukkit.org/rb/apidocs/org/bukkit/potion/PotionEffectType.html",
				"#   Level or Duration ranges may be specified with a \"-\", ex. 'SPEED/1-2/30-40' = lvl 1 and 30 sec at worst and lvl 2 and 40 sec at best",
				"#   Ranges also work high-low, ex. 'POISON/3-1/20-5' for weaker effects at good quality.",
				"#   Highest possible Duration: 1638 sec. Instant Effects dont need any duration specified."
		};

		index = indexOfStart("# effects:");
		if (index != -1) {
			setLine(index, replacedLine);
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("# alcohol:");
			if (index != -1) {
				addLines(index + 1, lines);
				addLines(index + 1, replacedLine);
			} else {
				index = indexOfStart("# -- Recipes for Potions --");
				if (index != -1) {
					addLines(index + 2, lines);
					addLines(index + 2, "", replacedLine);
				}
			}
		}
		if (index != -1) {
			index = indexOfStart("#   (WEAKNESS, INCREASE_DAMAGE, SLOW and SPEED are always hidden.)  Possible Effects:");
			if (index != -1) {
				removeLine(index);
			}
		}
		index = indexOfStart("#   instant effects ");
		if (index != -1) {
			removeLine(index);
		}

	}

	// Update de from 1.3 to 1.3.1
	private void update13de() {
		updateVersion("1.3.1");

		int index = indexOfStart("# Autosave");
		String[] lines = new String[] { "# Aktiviert das Suchen nach Updates f??r Brewery mit der curseforge api [true]",
				"# Wenn ein Update gefunden wurde, wird dies bei Serverstart im log angezeigt, sowie ops benachrichtigt",
				"updateCheck: true",
				"" };

		if (index == -1) {
			index = indexOfStart("autosave:");
			if (index == -1) {
				index = indexOfStart("# Sprachedatei");
				if (index == -1) {
					index = indexOfStart("language:");
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index, lines);
		}
	}

	// Update en from 1.3 to 1.3.1
	private void update13en() {
		updateVersion("1.3.1");

		int index = indexOfStart("# Autosave");
		String[] lines = new String[] { "# Enable checking for Updates, Checks the curseforge api for updates to Brewery [true]",
				"# If an Update is found a Message is logged on Server-start and displayed to ops joining the game",
				"updateCheck: true",
				"" };

		if (index == -1) {
			index = indexOfStart("autosave:");
			if (index == -1) {
				index = indexOfStart("# Languagefile");
				if (index == -1) {
					index = indexOfStart("language:");
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index, lines);
		}
	}

	// Update de from 1.3.1 to 1.4
	private void update131de() {
		updateVersion("1.4");

		int index = indexOfStart("# SamplePlugin = installiertes home plugin. Unterst??tzt: ManagerXL.");
		if (index != -1) {
			removeLine(index);
		}

		index = indexOfStart("# Ob der Spieler nach etwas k??rzerem Ausloggen an einem zuf??lligen Ort \"aufwacht\" (diese m??ssen durch '/br Wakeup add");
		if (index != -1) {
			setLine(index, "# Ob der Spieler nach etwas k??rzerem Ausloggen an einem zuf??lligen Ort \"aufwacht\" (diese m??ssen durch '/brew Wakeup add' von einem Admin festgelegt werden)");
		}

		index = indexOfStart("# Ob der Spieler sich bei gro??er Trunkenheit teilweise nicht einloggen kann und kurz warten muss, da sein Charakter nicht reagiert");
		if (index != -1) {
			setLine(index, "# Ob der Spieler bei gro??er Trunkenheit mehrmals probieren muss sich einzuloggen, da sein Charakter kurz nicht reagiert [true]");
		}

		index = indexOfStart("# Ob der Spieler sich ??bertrinken kann und dann in Ohnmacht f??llt (gekickt wird)");
		if (index != -1) {
			setLine(index, "# Ob der Spieler kurz in Ohnmacht f??llt (vom Server gekickt wird) wenn er die maximale Trunkenheit erreicht [false]");
		}

		index = indexOfStart("# Das Item kann nicht aufgesammelt werden und bleibt bis zum Despawnen liegen. (Achtung:");
		if (index != -1) {
			setLine(index, "# Das Item kann nicht aufgesammelt werden und bleibt bis zum Despawnen liegen.");
		}

		String[] lines = new String[] { "",
				"# Zeit in Sekunden bis die pukeitems despawnen, (mc standard w??re 300 = 5 min) [60]",
				"# Wurde die item Despawnzeit in der spigot.yml ver??ndert, ver??ndert sich auch die pukeDespawnzeit in Abh??ngigkeit.",
				"pukeDespawntime: 60" };

		index = indexOfStart("pukeItem:");
		if (index == -1) {
			index = indexOfStart("enablePuke:");
			if (index == -1) {
				index = indexOfStart("# Konsumierbares Item") - 1;
				if (index == -2) {
					index = indexOfStart("enableKickOnOverdrink:");
					if (index == -1) {
						index = indexOfStart("language:");
					}
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index + 1, lines);
		}

		index = indexOfStart("# F??rben der Iteminformationen je nach Qualit??t w??hrend sie sich 1. im Fass und/oder 2. im Braustand befinden [true, false]");
		if (index != -1) {
			setLine(index, "# F??rben der Iteminformationen je nach Qualit??t w??hrend sie sich 1. im Fass und/oder 2. im Braustand befinden [true, true]");
		}

		index = indexOfStart("# Wenn ein Update gefunden wurde, wird dies bei Serverstart im log angezeigt, sowie ops benachrichtigt");
		if (index != -1) {
			setLine(index, "# Wenn ein Update gefunden wurde, wird dies bei Serverstart im log angezeigt, sowie OPs benachrichtigt");
		}

		index = indexOfStart("#   Eine Liste von allen Materialien kann hier gefunden werden: http://jd.bukkit.org");
		if (index != -1) {
			setLine(index, "#   Eine Liste von allen Materialien kann hier gefunden werden: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
		}

		lines = new String[] { "#   Wenn Vault installiert ist k??nnen normale englische Item Namen verwendet werden, anstatt Material, ID und Data!",
				"#   Vault erkennt Namen wie \"Jungle Leaves\" anstatt \"LEAVES,3\". Dies macht es viel einfacher!" };

		index = indexOfStart("#   Es kann ein Data-Wert angegeben werden, weglassen");
		if (index != -1) {
			setLine(index, "#   Es kann ein Data-Wert (durability) angegeben werden, weglassen ignoriert diesen beim hinzuf??gen einer Zutat");
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("#   Eine Liste von allen Materialien kann hier");
			if (index == -1) {
				index = indexOfStart("# cookingtime: ") - 1;
				if (index == -2) {
					index = indexOfStart("# ingredients: Auflistung von");
					if (index == -1) {
						index = indexOfStart("# -- Rezepte f??r Getr??nke --") + 1;
						if (index == 0) {
							index = indexOfStart("# -- Verschiedene Einstellungen --");
						}
					}
				}
			}
			if (index == -1) {
				appendLines(lines);
			} else {
				addLines(index + 1, lines);
			}
		}

		lines = new String[] { "#   Effekte sind ab der 1.9 immer verborgen, wegen ??nderungen an den Tr??nken." };
		index = indexOfStart("#   M??gliche Effekte: http://jd.bukkit.org");
		if (index != -1) {
			setLine(index, "#   M??gliche Effekte: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
			addLines(index, lines);
		} else {
			index = indexOfStart("#   Ein 'X' an den Namen anh??ngen, um");
			if (index == -1) {
				index = indexOfStart("# effects: ");
				if (index == -1) {
					index = indexOfStart("# -- Rezepte f??r Getr??nke --") + 1;
				}
			}
			if (index == 0) {
				appendLines(lines);
			} else {
				addLines(index + 1, lines);
			}
		}

		index = indexOfStart("# Text, der zwischen diesen Buchstaben");
		if (index != -1) {
			setLine(index, "# Im Chat geschriebener Text, der zwischen diesen Buchstaben steht, wird nicht ver??ndert (\",\" als Trennung verwenden) (Liste) [- '[,]']");
		}
	}

	// Update en from 1.3.1 to 1.4
	private void update131en() {
		updateVersion("1.4");

		int index = indexOfStart("# SamplePlugin = installed home plugin. Supports: ManagerXL.");
		if (index != -1) {
			removeLine(index);
		}

		index = indexOfStart("# If the player \"wakes up\" at a random place when offline for some time while drinking (the places have to be defined with '/br Wakeup add'");
		if (index != -1) {
			setLine(index, "# If the player \"wakes up\" at a random place when offline for some time while drinking (the places have to be defined with '/brew Wakeup add' through an admin)");
		}

		index = indexOfStart("# If the Player may get some logins denied, when his character is drunk");
		if (index != -1) {
			setLine(index, "# If the Player may have to try multiple times when logging in while extremely drunk [true]");
		}

		index = indexOfStart("# If the Player faints (gets kicked) for some minutes if he overdrinks");
		if (index != -1) {
			setLine(index, "# If the Player faints shortly (gets kicked from the server) if he drinks the max amount of alcohol possible [false]");
		}

		index = indexOfStart("# The item can not be collected and stays on the ground until it despawns. (Warning:");
		if (index != -1) {
			setLine(index, "# The item can not be collected and stays on the ground until it despawns.");
		}

		String[] lines = new String[] { "",
				"# Time in seconds until the pukeitems despawn, (mc default is 300 = 5 min) [60]",
				"# If the item despawn time was changed in the spigot.yml, the pukeDespawntime changes as well.",
				"pukeDespawntime: 60" };

		index = indexOfStart("pukeItem:");
		if (index == -1) {
			index = indexOfStart("enablePuke:");
			if (index == -1) {
				index = indexOfStart("# Consumable Item") - 1;
				if (index == -2) {
					index = indexOfStart("enableKickOnOverdrink:");
					if (index == -1) {
						index = indexOfStart("language:");
					}
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index + 1, lines);
		}

		index = indexOfStart("# Color the Item information (lore) depending on quality while it is 1. in a barrel and/or 2. in a brewing stand [true, false]");
		if (index != -1) {
			setLine(index, "# Color the Item information (lore) depending on quality while it is 1. in a barrel and/or 2. in a brewing stand [true, true]");
		}

		index = indexOfStart("# If an Update is found a Message is logged on Server-start and displayed to ops joining the game");
		if (index != -1) {
			setLine(index, "# If an Update is found a Message is logged on Server-start and displayed to OPs joining the game");
		}

		index = indexOfStart("#   A list of materials can be found here: http://jd.bukkit.org");
		if (index != -1) {
			setLine(index, "#   A list of materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
		}

		lines = new String[] { "#   If Vault is installed normal names can be used instead of material or id, so using Vault is highly recommended.",
				"#   Vault will recognize things like \"Jungle Leaves\" instead of \"LEAVES,3\"" };

		index = indexOfStart("#   You can specify a data value, omitting");
		if (index != -1) {
			setLine(index, "#   You can specify a data (durability) value, omitting it will ignore the data value of the added ingredient");
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("#   A list of materials can be found");
			if (index == -1) {
				index = indexOfStart("# cookingtime: Time in real minutes") - 1;
				if (index == -2) {
					index = indexOfStart("# ingredients: ");
					if (index == -1) {
						index = indexOfStart("# -- Recipes for Potions --") + 1;
						if (index == 0) {
							index = indexOfStart("# -- Settings --");
						}
					}
				}
			}
			if (index == -1) {
				appendLines(lines);
			} else {
				addLines(index + 1, lines);
			}
		}

		lines = new String[] { "#   Effects are always hidden in 1.9 and newer, because of changes in the potion mechanics." };
		index = indexOfStart("#   Possible Effects: http://jd.bukkit.org");
		if (index != -1) {
			setLine(index, "#   Possible Effects: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
			addLines(index, lines);
		} else {
			index = indexOfStart("#   Suffix name with");
			if (index == -1) {
				index = indexOfStart("# effects: ");
				if (index == -1) {
					index = indexOfStart("# -- Recipes for Potions --") + 1;
				}
			}
			if (index == 0) {
				appendLines(lines);
			} else {
				addLines(index + 1, lines);
			}
		}

		index = indexOfStart("# Enclose a text with these Letters to bypass Chat Distortion");
		if (index != -1) {
			setLine(index, "# Enclose a Chat text with these Letters to bypass Chat Distortion (Use \",\" as Separator) (list) [- '[,]']");
		}

	}

	// Update de from 1.4 to 1.5
	private void update14de() {
		updateVersion("1.5");

		String[] lines = new String[] {"",
				"# Ob geschriebener Chat bei gro??er Trunkenheit abgef??lscht werden soll,",
				"# so dass es etwas betrunken aussieht was geschrieben wird.",
				"# Wie stark der Chat ver??ndert wird h??ngt davon ab wie betrunken der Spieler ist",
				"# Unten kann noch eingestellt werden wie und was ver??ndert wird",
				"enableChatDistortion: true"};

		int index = indexOfStart("# -- Chat") + 2;
		if (index == 1) {
			index = indexOfStart("distortCommands:") - 1;
			if (index == -2) {
				index = indexOfStart("distortSignText:") - 1;
				if (index == -2) {
					index = indexOfStart("# words:");
					if (index == -1) {
						index = indexOfStart("words:");
					}
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index - 1, lines);
		}

		lines = new String[] {"# Also zum Beispiel im Chat: Hallo ich bin betrunken *Ich teste Brewery*"};

		index = indexOfStart("# Im Chat geschriebener Text, der zwischen");
		if (index != -1) {
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("distortBypass:");
			if (index != -1) {
				addLines(index, lines);
			}
		}

		lines = new String[] {"# distilltime: Wie lange (in sekunden) ein Destillations-Durchlauf braucht (0=Standard Zeit von 40 sek) MC Standard w??re 20 sek"};

		index = indexOfStart("# distillruns:");
		if (index == -1) {
			index = indexOfStart("# wood:") - 1;
			if (index == -2) {
				index = indexOfStart("# -- Rezepte") + 1;
				if (index == 0) {
					index = -1;
				}
			}
		}
		if (index != -1) {
			addLines(index + 1, lines);
		}

		index = indexOfStart("      name: Schlechtes Beispiel/Beispiel/Gutes Beispiel");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 60");
		}
		index = indexOfStart("      name: Bitterer Rum/W??rziger Rum/&6Goldener Rum");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 30");
		}
		index = indexOfStart("      name: minderwertiger Absinth/Absinth/Starker Absinth");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 80");
		}
	}

	// Update de from 1.4 to 1.5
	private void update14en() {
		updateVersion("1.5");

		String[] lines = new String[] {"",
				"# If written Chat is distorted when the Player is Drunk,",
				"# so that it looks like drunk writing",
				"# How much the chat is distorted depends on how drunk the Player is",
				"# Below are settings for what and how changes in chat occur",
				"enableChatDistortion: true"};

		int index = indexOfStart("# -- Chat") + 2;
		if (index == 1) {
			index = indexOfStart("distortCommands:") - 1;
			if (index == -2) {
				index = indexOfStart("distortSignText:") - 1;
				if (index == -2) {
					index = indexOfStart("# words:");
					if (index == -1) {
						index = indexOfStart("words:");
					}
				}
			}
		}
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index - 1, lines);
		}

		lines = new String[] {"# Chat Example: Hello i am drunk *I am testing Brewery*"};

		index = indexOfStart("# Enclose a Chat text with these Letters");
		if (index != -1) {
			addLines(index + 1, lines);
		} else {
			index = indexOfStart("distortBypass:");
			if (index != -1) {
				addLines(index, lines);
			}
		}

		lines = new String[] {"# distilltime: How long (in seconds) one distill-run takes (0=Default time of 40 sec) MC Default would be 20 sec"};

		index = indexOfStart("# distillruns:");
		if (index == -1) {
			index = indexOfStart("# wood:") - 1;
			if (index == -2) {
				index = indexOfStart("# -- Recipes") + 1;
				if (index == 0) {
					index = -1;
				}
			}
		}
		if (index != -1) {
			addLines(index + 1, lines);
		}

		index = indexOfStart("      name: Bad Example/Example/Good Example");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 60");
		}
		index = indexOfStart("      name: Bitter Rum/Spicy Rum/&6Golden Rum");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 30");
		}
		index = indexOfStart("      name: Poor Absinthe/Absinthe/Strong Absinthe");
		if (index != -1) {
			addLines(index + 1, "      distilltime: 80");
		}
	}


	//update from 1.5 to 1.7/mc 1.13
	private void update15(boolean mc113, boolean langDE) {
		updateVersion("1.7");
		updateMaterials(mc113);

		if (langDE) {

			int index = indexOfStart("# ingredients: Auflistung von 'Material oder ID");
			if (index != -1) {
				setLine(index, "# ingredients: Auflistung von 'Material,Data/Anzahl'");
			}

			index = indexOfStart("#   (Item-ids anstatt Material");
			if (index != -1) {
				setLine(index, "#   (Item-ids anstatt Material k??nnen in Bukkit nicht mehr benutzt werden)");
			}

			index = indexOfStart("# [Beispiel] MATERIAL_oder_id: Name");
			if (index != -1) {
				setLine(index, "# [Beispiel] MATERIAL: Name nach G??hren");
			}

		} else {

			int index = indexOfStart("# ingredients: List of 'material or id");
			if (index != -1) {
				setLine(index, "# ingredients: List of 'material,data/amount'");
			}

			index = indexOfStart("#   (Item-ids instead of material are deprecated");
			if (index != -1) {
				setLine(index, "#   (Item-ids instead of material are not supported by bukkit anymore and will not work)");
			}

			index = indexOfStart("# [Example] MATERIAL_or_id: Name");
			if (index != -1) {
				setLine(index, "# [Example] MATERIAL: Name after cooking");
			}

		}
	}

	// Update de from 1.7 to 1.8
	private void update17de() {
		updateVersion("1.8");

		int index = indexOfStart("openLargeBarrelEverywhere");
		if (index == -1) {
			index = indexOfStart("colorInBrewer");
			if (index == -1) {
				index = indexOfStart("colorInBarrels");
				if (index == -1) {
					index = indexOfStart("hangoverDays");
					if (index == -1) {
						index = indexOfStart("language");
					}
				}
			}
		}
		String[] lines = {"",
			"# Wie viele Brewery Getr??nke in die Minecraft F??sser getan werden k??nnen [6]",
			"maxBrewsInMCBarrels: 6"};
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index + 1, lines);
		}

		index = indexOfStart("#   Benutzbare Farben");
		if (index == -1) {
			index = indexOfStart("# color:");
		}
		if (index != -1) {
			addLines(index + 1, "#   Oder RGB Farben (Hex: also zB '99FF33') (Ohne #) (mit '') (Einfach nach \"HTML color\" im Internet suchen)");
		}

		index = indexOfStart("# ingredients:");
		if (index == -1) {
			index = indexOfStart("#   Eine Liste von allen Materialien");
			if (index == -1) {
				index = indexOfStart("# -- Rezepte");
			}
		}
		if (index != -1) {
			addLines(index + 1, "#   Halte ein Item in der Hand und benutze /brew ItemName um dessen Material herauszufinden und f??r ein Rezept zu benutzen");
		}
		index = indexOfStart("# wood: Holz des Fasses");
		if (index != -1) {
			addLines(index + 1, "#   Das Minecraft Fass besteht aus Eiche");
		}
		if (P.use1_13) updateMaterialDescriptions(true);
	}

	// Update en from 1.7 to 1.8
	private void update17en() {
		updateVersion("1.8");

		int index = indexOfStart("openLargeBarrelEverywhere");
		if (index == -1) {
			index = indexOfStart("colorInBrewer");
			if (index == -1) {
				index = indexOfStart("colorInBarrels");
				if (index == -1) {
					index = indexOfStart("hangoverDays");
					if (index == -1) {
						index = indexOfStart("language");
					}
				}
			}
		}
		String[] lines = {"",
			"# How many Brewery drinks can be put into the Minecraft barrels [6]",
			"maxBrewsInMCBarrels: 6"};
		if (index == -1) {
			appendLines(lines);
		} else {
			addLines(index + 1, lines);
		}

		index = indexOfStart("#   Usable Colors");
		if (index == -1) {
			index = indexOfStart("# color:");
		}
		if (index != -1) {
			addLines(index + 1, "#   Or RGB colors (hex: for example '99FF33') (with '') (search for \"HTML color\" on the internet)");
		}

		index = indexOfStart("# ingredients:");
		if (index == -1) {
			index = indexOfStart("#   A list of materials");
			if (index == -1) {
				index = indexOfStart("# -- Recipes");
			}
		}
		if (index != -1) {
			addLines(index + 1, "#   With an item in your hand, use /brew ItemName to get its material for use in a recipe");
		}
		index = indexOfStart("# wood: Wood of the barrel");
		if (index != -1) {
			addLines(index + 1, "#   The Minecraft barrel is made of oak");
		}
		if (P.use1_13) updateMaterialDescriptions(false);
	}

	private void update18de(FileConfiguration yml) {
		int index = indexOfStart("# L??schen einzelner Einstellungen");
		if (index != -1) {
			removeLine(index);
		}

		addLinesAt(new String[] {"colorInBrewer:", "colorInBarrels:", "hangoverDays:", "language:"}, 1, "",
				"# Ob in den Iteminformationen immer 1-5 Sterne f??r die Qualit??t angezeigt werden sollen, oder nur beim brauen [true]",
				"alwaysShowQuality: true",
				"",
				"# Ob in den Iteminformationen immer der Alkoholgehalt angezeigt weden soll, oder nur im Braustand [false]",
				"alwaysShowAlc: false");

		addLinesAt(new String[] {"maxBrewsInMCBarrels:", "openLargeBarrelEverywhere:", "language:"}, 1, "",
			"# Benutzte Zutaten und andere Brau-Daten werden in allen Brewery Tr??nken gespeichert. Um zu verhindern,",
				"# dass gehackte clients diese Daten auslesen um Rezepte herauszufinden, k??nnen diese encodiert werden.",
				"# Einziger Nachteil: Tr??nke k??nnen nur auf Servern mit dem gleichen encodeKey benutzt werden.",
				"# Dies kann also aktiviert werden um Rezept-cheating schwerer zu machen, aber keine Tr??nke per World Download, Schematic, o.??. geteilt werden. [false]",
				"enableEncode: false",
				"encodeKey: 0");

		if (indexOfStart("debug:") == -1) {
			addLinesAt(new String[]{"autosave:", "version:"}, 1, "",
				"# Debug Nachrichten im Log anzeigen [false]",
				"debug: false");
		}

		index = indexOfStart("oldMat:") + 1;
		if (index == 0) {
			index = indexOfStart("version:") + 1;
			if (index == 0) {
				index = 2;
			}
		}
		// Custom Items and start of cauldron section
		applyPatch("config/patches/de18.txt", index);

		index = indexOfStart("%%%%MAT1%%%%");
		if (index != -1) {
			if (P.use1_13) {
				setLine(index, "    material: Barrier");
			} else {
				setLine(index, "    material: BEDROCK");
			}
		}
		index = indexOfStart("%%%%MAT2%%%%");
		if (index != -1) {
			removeLine(index);
			if (P.use1_13) {
				addLines(index, "    material:",
					"      - Acacia_Door",
					"      - Oak_Door",
					"      - Spruce_Door");
			} else {
				addLines(index, "    material:",
					"      - WOODEN_DOOR",
					"      - IRON_DOOR");
			}
		}

		index = indexOfStart("# -- Eine Zutat:");
		if (index == -1) {
			index = indexOfStart("cauldron:");
			if (index == -1) {
				// Patch failed
				index = indexOfStart("version:");
				if (index == -1) {
					index = 0;
				}
				addLines(index + 1, "cauldron:");
				index ++;
			}
		}
		convertCookedSection(yml, index + 1);

		addLinesAt(new String[]{"#   Eine Liste von allen Materialien", "# ingredients:"}, 1,
			"#   Plugin Items mit 'Plugin:Id' (Im Moment ExoticGarden, Slimefun, MMOItems, Brewery)",
				"#   Oder ein oben definiertes Custom Item");

		addLinesAt(new String[]{"# alcohol:", "# difficulty:", "# ingredients:", "# -- Rezepte"}, 1,
			"# lore: Auflistung von zus??tzlichem Text auf dem fertigen Trank. (Farbcodes m??glich: z.b. &6)",
				"#   Lore nur f??r bestimmte Qualit??t m??glich mit + Schlecht, ++ Mittel, +++ Gut, vorne anh??ngen.",
				"# servercommands: Liste von Befehlen ausgef??hrt vom Server wenn der Trank getrunken wird",
				"# playercommands: Liste von Befehlen ausgef??hrt vom Spieler wenn der Trank getrunken wird",
				"# drinkmessage: Nachricht im Chat beim trinken des Trankes",
				"# drinktitle: Nachricht als Titel auf dem Bildschirm an den Spieler beim trinken des Trankes");

		addLinesAt(new String[]{"useGriefPrevention:", "useWorldGuard:", "# -- Plugin Kompatiblit"}, 1, "useGMInventories: true");

		index = indexOfStart("# cooked:");
		if (index != -1) {
			removeLine(index);
		}
		index = indexOfStart("# [Beispiel] MATERIAL:");
		if (index != -1) {
			removeLine(index);
		}

	}

	private void update18fr(FileConfiguration yml) {
		int index = indexOfStart("# Supprimer un para");
		if (index != -1) {
			removeLine(index);
		}

		addLinesAt(new String[] {"colorInBrewer:", "colorInBarrels:", "hangoverDays:", "language:"}, 1, "\n" +
			"# Toujours montrer les 1-5 ??toiles sur les objets en fonction de leur qualit??. S'ils sont faux, ils n'appara??tront que lors de l'infusion. [true]",
			"alwaysShowQuality: true",
			"",
			"# Toujours indiquer la teneur en alcool sur les objets. S'il est false, il n'appara??tra que dans le stand de brassage. [false]",
			"alwaysShowAlc: false");

		addLinesAt(new String[] {"maxBrewsInMCBarrels:", "openLargeBarrelEverywhere:", "language:"}, 1, "",
			"# Les ingr??dients et autres donn??es de brassage utilis??s sont sauvegard??s dans tous les articles de brasserie. [false]",
			"# Pour emp??cher les clients pirat??s de lire exactement ce qui a ??t?? utilis?? pour infuser un ??l??ment, les donn??es peuvent ??tre encod??es/brouill??es.",
			"# Il s'agit d'un processus rapide pour emp??cher les joueurs de pirater des recettes, une fois qu'ils mettent la main sur une bi??re.",
			"# Seul inconv??nient: Les boissons brassicoles ne peuvent ??tre utilis??s que sur un autre serveur avec la m??me cl?? de chiffrement.",
			"# Activez cette option si vous voulez rendre la tricherie des recettes plus difficile, mais ne partagez pas les infusions par t??l??chargement mondial, sch??mas ou autres moyens.",
			"enableEncode: false",
			"encodeKey: 0");

		if (indexOfStart("debug:") == -1) {
			addLinesAt(new String[]{"autosave:", "version:"}, 1, "",
				"# Show debug messages in log [false]",
				"debug: false");
		}

		index = indexOfStart("oldMat:") + 1;
		if (index == 0) {
			index = indexOfStart("version:") + 1;
			if (index == 0) {
				index = 2;
			}
		}
		// Custom Items and start of cauldron section
		applyPatch("config/patches/fr18.txt", index);

		index = indexOfStart("%%%%MAT1%%%%");
		if (index != -1) {
			if (P.use1_13) {
				setLine(index, "    material: Barrier");
			} else {
				setLine(index, "    material: BEDROCK");
			}
		}
		index = indexOfStart("%%%%MAT2%%%%");
		if (index != -1) {
			removeLine(index);
			if (P.use1_13) {
				addLines(index, "    material:",
					"      - Acacia_Door",
					"      - Oak_Door",
					"      - Spruce_Door");
			} else {
				addLines(index, "    material:",
					"      - WOODEN_DOOR",
					"      - IRON_DOOR");
			}
		}

		index = indexOfStart("  # -- Un ingr");
		if (index == -1) {
			index = indexOfStart("cauldron:");
			if (index == -1) {
				// Patch failed
				index = indexOfStart("version:");
				if (index == -1) {
					index = 0;
				}
				addLines(index + 1, "cauldron:");
				index ++;
			}
		}
		convertCookedSection(yml, index + 1);

		addLinesAt(new String[]{"#   Une liste des mat", "# ingredients:"}, 1,
			"#   Plugin items avec 'plugin:id' (Actuellement support?? ExoticGarden, Slimefun, MMOItems, Brewery)",
				"#   Ou un ??l??ment personnalis?? d??fini ci-dessus");

		addLinesAt(new String[]{"# alcohol:", "# difficulty:", "# ingredients:", "# -- Recette "}, 1,
			"# lore: Liste des textes suppl??mentaires sur le breuvage fini. (Codes de formatage possibles : tels que &6)",
				"#   Texte sp??cifique de qualit?? possible, en utilisant + mauvais, ++ normal, +++ bon, ajout?? ?? l'avant de la ligne.",
				"# servercommands: Liste des commandes ex??cut??es par le serveur lors de la consommation de la potion",
				"# playercommands: Liste des commandes ex??cut??es par le joueur lors de la consommation de la potion",
				"# drinkmessage: Chat-message au joueur lorsqu'il boit la potion",
				"# drinktitle: Titre ?? l'??cran du joueur lorsqu'il boit la potion");

		addLinesAt(new String[]{"useGriefPrevention:", "useWorldGuard:", "# -- Plugin Compatibility"}, 1, "useGMInventories: true");

		index = indexOfStart("# cooked:");
		if (index != -1) {
			removeLine(index);
		}
		index = indexOfStart("# [Exemple] MATERIEL");
		if (index != -1) {
			removeLine(index);
		}

	}

	private void update18en(FileConfiguration yml) {
		int index = indexOfStart("# Deleting of single settings");
		if (index != -1) {
			removeLine(index);
		}

		addLinesAt(new String[] {"colorInBrewer:", "colorInBarrels:", "hangoverDays:", "language:"}, 1, "",
			"# Always show the 1-5 stars on the item depending on the quality. If false, they will only appear when brewing [true]",
			"alwaysShowQuality: true",
			"",
			"# Always show the alcohol content on the item. If false, it will only show in the brewing stand [false]",
			"alwaysShowAlc: false");

		addLinesAt(new String[] {"maxBrewsInMCBarrels:", "openLargeBarrelEverywhere:", "language:"}, 1, "",
			"# The used Ingredients and other brewing-data is saved to all Brewery Items. To prevent",
			"# hacked clients from reading what exactly was used to brew an item, the data can be encoded/scrambled.",
			"# This is a fast process to stop players from hacking out recipes, once they get hold of a brew.",
			"# Only drawback: brew items can only be used on another server with the same encodeKey.",
			"# So enable this if you want to make recipe cheating harder, but don't share any brews by world download, schematics, or other means. [false]",
			"enableEncode: false",
			"encodeKey: 0");

		if (indexOfStart("debug:") == -1) {
			addLinesAt(new String[]{"autosave:", "version:"}, 1, "",
				"# Show debug messages in log [false]",
				"debug: false");
		}

		index = indexOfStart("oldMat:") + 1;
		if (index == 0) {
			index = indexOfStart("version:") + 1;
			if (index == 0) {
				index = 2;
			}
		}
		// Custom Items and start of cauldron section
		applyPatch("config/patches/en18.txt", index);

		index = indexOfStart("%%%%MAT1%%%%");
		if (index != -1) {
			if (P.use1_13) {
				setLine(index, "    material: Barrier");
			} else {
				setLine(index, "    material: BEDROCK");
			}
		}
		index = indexOfStart("%%%%MAT2%%%%");
		if (index != -1) {
			removeLine(index);
			if (P.use1_13) {
				addLines(index, "    material:",
					"      - Acacia_Door",
					"      - Oak_Door",
					"      - Spruce_Door");
			} else {
				addLines(index, "    material:",
					"      - WOODEN_DOOR",
					"      - IRON_DOOR");
			}
		}

		index = indexOfStart("  # -- One Ingredient");
		if (index == -1) {
			index = indexOfStart("cauldron:");
			if (index == -1) {
				// Patch failed
				index = indexOfStart("version:");
				if (index == -1) {
					index = 0;
				}
				addLines(index + 1, "cauldron:");
				index ++;
			}
		}
		convertCookedSection(yml, index + 1);

		addLinesAt(new String[]{"#   A list of materials", "# ingredients:"}, 1,
			"#   Plugin items with 'plugin:id' (Currently supporting ExoticGarden, Slimefun, MMOItems, Brewery)",
			"#   Or a custom item defined above");

		addLinesAt(new String[]{"# alcohol:", "# difficulty:", "# ingredients:", "# -- Recipes"}, 1,
			"# lore: List of additional text on the finished brew. (Formatting codes possible: such as &6)",
			"#   Specific lore for quality possible, using + bad, ++ normal, +++ good, added to the front of the line.",
			"# servercommands: List of Commands executed by the Server when drinking the brew",
			"# playercommands: List of Commands executed by the Player when drinking the brew",
			"# drinkmessage: Chat-message to the Player when drinking the Brew",
			"# drinktitle: Title on Screen to the Player when drinking the Brew");

		addLinesAt(new String[]{"useGriefPrevention:", "useWorldGuard:", "# -- Plugin Compatibility"}, 1, "useGMInventories: true");

		index = indexOfStart("# cooked:");
		if (index != -1) {
			removeLine(index);
		}
		index = indexOfStart("# [Example] MATERIAL:");
		if (index != -1) {
			removeLine(index);
		}

	}

	private void update20de() {
		addLinesAt(new String[]{"hangoverDays", "colorInBrewer", "encodeKey"}, 1, "",
			"# Ob das craften und das benutzen des Trank-Versiegelungs-Tisches aktiviert ist (2 Flaschen ??ber 4 Holz) [true, true]",
			"craftSealingTable: true",
			"enableSealingTable: true");

		addLinesAt(new String[]{"useLogBlock", "useGMInventories", "# -- Plugin Kompatibli", "# Es gibt noch viele Minecraft Items", "version"}, 1, "", "",
			"# -- MultiServer/BungeeCord --",
			"# Wenn Brewery auf mehreren Servern l??uft und diese zB mit BungeeCord verbunden sind,",
			"# sollte hier eine gemeinsame Datenbank eingetragen werden.",
			"# Dann wird Betrunkenheit auf den Servern synchronisiert und encodierte Tr??nke k??nnen auf allen Servern benutzt werden.",
			"",
			"multiServerDB:",
			"  # Soll die Datenbank-Synchronisation aktiviert sein",
			"  enabled: false",
			"  # Soll die Betrunkenheit von Spielern synchronisiert werden",
			"  syncDrunkeness: true",
			"  host: localhost",
			"  port: '3306'",
			"  user: minec",
			"  password: xyz",
			"  database: base",
			"",
			"",
			"# -- Verschiedene weitere Einstellungen --",
			"",
			"# Ob Items in der Zweithand auch in den Kessel geworfen werden sollen [false]",
			"useOffhandForCauldron: false");
	}

	private void update20fr() {
		addLinesAt(new String[]{"hangoverDays", "colorInBrewer", "encodeKey"}, 1, "",
			"# If crafting and using of the Brew Sealing Table is enabled (2 Bottles over 4 Planks) [true, true]",
			"craftSealingTable: true",
			"enableSealingTable: true");

		addLinesAt(new String[]{"useLogBlock", "useGMInventories", "# -- Compatibilit", "# There are a lot of items in Minecraft ", "version"}, 1, "", "",
			"# -- MultiServer/BungeeCord --",
			"# Si Brewery est ex??cut?? sur plusieurs serveurs connect??s (via BungeeCord), une base de donn??es partag??e peut ??tre utilis??e",
			"# ici pour synchroniser l'ivresse et pour pouvoir utiliser des boissons cod??es entre elles.",
			"",
			"multiServerDB:",
			"  # Si l'utilisation de la base de donn??es est activ??e",
			"  enabled: false",
			"  # Si l'ivresse des joueurs devait ??tre synchronis??e entre les serveurs",
			"  syncDrunkeness: true",
			"  host: localhost",
			"  port: '3306'",
			"  user: minec",
			"  password: xyz",
			"  database: base",
			"",
			"",
			"# -- Divers autres param??tres --",
			"",
			"# If items in Offhand should be added to the cauldron as well [false]",
			"useOffhandForCauldron: false");
	}

	private void update20en() {
		addLinesAt(new String[]{"hangoverDays", "colorInBrewer", "encodeKey"}, 1, "",
			"# If crafting and using of the Brew Sealing Table is enabled (2 Bottles over 4 Planks) [true, true]",
			"craftSealingTable: true",
			"enableSealingTable: true");

		addLinesAt(new String[]{"useLogBlock", "useGMInventories", "# -- Plugin Compatibility", "# # There are a lot of items in Minecraft", "version"}, 1, "", "",
			"# -- MultiServer/BungeeCord --",
			"# If Brewery is running on multiple connected Servers (via BungeeCord)",
			"# a shared Database can be used here to synchronise drunkeness and to be able to use encoded brews between them.",
			"",
			"multiServerDB:",
			"  # If using the Database is enabled",
			"  enabled: false",
			"  # If the drunkeness of players should be synchronised between Servers",
			"  syncDrunkeness: true",
			"  host: localhost",
			"  port: '3306'",
			"  user: minec",
			"  password: xyz",
			"  database: base",
			"",
			"",
			"# -- Various Other Settings --",
			"",
			"# If items in Offhand should be added to the cauldron as well [false]",
			"useOffhandForCauldron: false");
	}

	private void update21de() {
		int index = indexOfStart("# Wie viele Brewery Getr??nke in die Minecraft F??sser getan werden k??nnen");
		if (index != -1) {
			setLine(index, "# Ob das reifen in -Minecraft- F??ssern aktiviert ist und wie viele Brewery Getr??nke in die diese getan werden k??nnen [6]");
		}
		String add = "ageInMCBarrels: true";
		index = indexOfStart("maxBrewsInMCBarrels:");
		if (index != -1) {
			addLines(index, add);
		} else {
			addLinesAt(new String[]{"debug", "version"}, 1, "", add);
		}

		addLinesAt(new String[]{"# Hier kann angegeben werden welche Zutaten in den Kessel getan werden k??nnen"}, 1,
			"# Es braucht nur etwas hier eingetragen werden falls der Basistrank besondere Eigenschaften wie Name und Farbe haben soll");

		addLinesAt(new String[]{" # lore:", " # ingredients:"}, 1,
			" # customModelData: Custom Model Data Modelldaten. Mit dieser Zahl kann die Tranktextur mit einem Resourcepack ge??ndert werden");

		index = indexOfStart("# servercommands: Liste von Befehlen ausgef??hrt vom Server wenn");
		if (index != -1) {
			setLine(index, "# servercommands: Liste von Befehlen ausgef??hrt vom -Server- wenn der Trank getrunken wird (%player_name%  %quality% benutzbar)");
		}
		index = indexOfStart("# playercommands: Liste von Befehlen ausgef??hrt vom Spieler wenn");
		if (index != -1) {
			setLine(index, "# playercommands: Liste von Befehlen ausgef??hrt vom -Spieler- wenn der Trank getrunken wird (%player_name%  %quality% benutzbar)");
		}

		addLinesAt(new String[]{"# drinktitle:", "# drinkmessage:", "# playercommands:", "# alcohol:"}, 1,
			"# customModelData: Custom Model Data Modelldaten. Mit dieser Zahl kann die Tranktextur mit einem Resourcepack ge??ndert werden",
			"#   Es kann eine f??r alle, oder drei f??r die qualit??ten schlecht/normal/gut agegeben werden, mit / getrennt");

		addLinesAt(new String[]{"useOffhandForCauldron:", "# -- Verschiedene weitere Einstellungen", "# -- Plugin Kompatiblit"}, 1, "",
			"# Of Fass- und Kesseldaten Async/im Hintergrund geladen werden k??nnen [true]",
			"loadDataAsync: true");

	}

	private void update21fr() {
		int index = indexOfStart("# Combien de boissons de brasserie peuvent");
		if (index != -1) {
			setLine(index, "# Combien de boissons de brasserie peuvent ??tre mises dans les barils -Minecraft- [6]");
		}
		String add = "ageInMCBarrels: true";
		index = indexOfStart("maxBrewsInMCBarrels:");
		if (index != -1) {
			addLines(index, add);
		} else {
			addLinesAt(new String[]{"debug", "version"}, 1, "", add);
		}

		addLinesAt(new String[]{"# Quels sont les ingr??dients accept??s par le chaudron"}, 1,
			"# Il vous suffit d'ajouter quelque chose ici si vous voulez sp??cifier un nom ou une couleur pour la potion de base");

		addLinesAt(new String[]{" # lore:", " # ingredients:"}, 1,
			" # customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item.");

		index = indexOfStart("# servercommands: Liste des commandes ex??cut??es par le serveur");
		if (index != -1) {
			setLine(index, "# servercommands: Liste des commandes ex??cut??es par le -serveur- lors de la consommation de la potion (Peut utiliser %player_name%  %quality%)");
		}
		index = indexOfStart("# playercommands: Liste des commandes ex??cut??es par le joueur");
		if (index != -1) {
			setLine(index, "# playercommands: Liste des commandes ex??cut??es par le -joueur- lors de la consommation de la potion (Peut utiliser %player_name%  %quality%)");
		}

		addLinesAt(new String[]{"# drinktitle:", "# drinkmessage:", "# playercommands:", "# alcohol:"}, 1,
			"# customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item.",
			"#   Can specify one for all, or one for each quality, separated by /");

		addLinesAt(new String[]{"useOffhandForCauldron:", "# -- Divers autres param", "# -- Compatibilit?? entre Plugins"}, 1, "",
			"# If Barrel and Cauldron data can be loaded Async/in the Background [true]",
			"loadDataAsync: true");

	}

	private void update21en() {
		int index = indexOfStart("# How many Brewery drinks can be put into");
		if (index != -1) {
			setLine(index, "# If aging in -Minecraft- Barrels in enabled [true] and how many Brewery drinks can be put into them [6]");
		}
		String add = "ageInMCBarrels: true";
		index = indexOfStart("maxBrewsInMCBarrels:");
		if (index != -1) {
			addLines(index, add);
		} else {
			addLinesAt(new String[]{"debug", "version"}, 1, "", add);
		}

		addLinesAt(new String[]{"# Which Ingredients are accepted by the Cauldron and the base potion resulting"}, 1,
			"# You only need to add something here if you want to specify a custom name or color for the base potion");

		addLinesAt(new String[]{" # lore:", " # ingredients:"}, 1,
			" # customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item.");

		index = indexOfStart("# servercommands: List of Commands executed by the Server when drinking");
		if (index != -1) {
			setLine(index, "# servercommands: List of Commands executed by the -Server- when drinking the brew (Can use %player_name%  %quality%)");
		}
		index = indexOfStart("# playercommands: List of Commands executed by the Player when drinking the brew");
		if (index != -1) {
			setLine(index, "# playercommands: List of Commands executed by the -Player- when drinking the brew (Can use %player_name%  %quality%)");
		}

		addLinesAt(new String[]{"# drinktitle:", "# drinkmessage:", "# playercommands:", "# alcohol:"}, 1,
			"# customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item.",
			"#   Can specify one for all, or one for each quality, separated by /");

		addLinesAt(new String[]{"useOffhandForCauldron:", "# -- Various Other Settings", "# -- Plugin Compatibility"}, 1, "",
			"# If Barrel and Cauldron data can be loaded Async/in the Background [true]",
			"loadDataAsync: true");

	}

	private void update30CauldronParticles() {
		int start = config.indexOf("cauldron:");
		int end = config.indexOf("recipes:");
		if (start < 0 || end < 0 || start >= end) {
			return;
		}
		String c = "    cookParticles:";

		List<Tuple<String[], String[]>> additions = new ArrayList<>();
		additions.add(new Tuple<>(
			new String[]{"  ex:", "  bsp:"},
			new String[]{c, "      - 'RED/5'", "      - 'WHITE/10'", "      - '800000/25' # maroon"}));
		additions.add(new Tuple<>(
			new String[]{"  wheat:", "  wheat:"},
			new String[]{c, "      - '2d8686/8' # Dark Aqua"}));
		additions.add(new Tuple<>(
			new String[]{"  sugarcane:"},
			new String[]{c, "      - 'f1ffad/4'","      - '858547/10' # dark olive"}));
		additions.add(new Tuple<>(
			new String[]{"  sugar:"},
			new String[]{c, "      - 'WHITE/4'", "      - 'BRIGHT_GREY/25'"}));
		additions.add(new Tuple<>(
			new String[]{"  berries:"},
			new String[]{c, "      - 'ff6666/2' # bright red", "      - 'RED/7'", "      - 'ac6553/13' # brown-red"}));
		additions.add(new Tuple<>(
			new String[]{"  grass:"},
			new String[]{c, "      - 'GREEN/2'", "      - '99ff99/20' # faded green"}));
		additions.add(new Tuple<>(
			new String[]{"  rmushroom:"},
			new String[]{c, "      - 'fab09e/15' # faded red"}));
		additions.add(new Tuple<>(
			new String[]{"  bmushroom:"},
			new String[]{c, "      - 'c68c53/15'"}));
		additions.add(new Tuple<>(
			new String[]{"  cocoa:"},
			new String[]{c, "      - 'a26011/1'", "      - '5c370a/3'", "      - '4d4133/8' # Gray-brown"}));
		additions.add(new Tuple<>(
			new String[]{"  milk:"},
			new String[]{c, "      - 'fbfbd0/1' # yellow-white", "      - 'WHITE/6'"}));
		additions.add(new Tuple<>(
			new String[]{"  bl_flow:"},
			new String[]{c, "      - '0099ff'"}));
		additions.add(new Tuple<>(
			new String[]{"  cactus:"},
			new String[]{c, "      - '00b300/16'"}));
		additions.add(new Tuple<>(
			new String[]{"  vine:"},
			new String[]{c, "      - 'GREEN/2'", "      - '99ff99/20' # faded green"}));
		additions.add(new Tuple<>(
			new String[]{"  rot_flesh:"},
			new String[]{c, "      - '263300/8'", "      - 'BLACK/20'"}));
		additions.add(new Tuple<>(
			new String[]{"  cookie:"},
			new String[]{c, "      - 'a26011/1'", "      - '5c370a/3'", "      - '4d4133/8' # Gray-brown"}));
		additions.add(new Tuple<>(
			new String[]{"  Gold_Nugget:"},
			new String[]{c, "      - 'ffd11a'"}));
		additions.add(new Tuple<>(
			new String[]{"  glowstone_dust:"},
			new String[]{c, "      - 'ffff99/3'", "      - 'd9d926/15' # faded yellow"}));
		additions.add(new Tuple<>(
			new String[]{"  applemead_base:", "  apfelmet_basis:"},
			new String[]{c, "      - 'e1ff4d/4'"}));
		additions.add(new Tuple<>(
			new String[]{"  poi_grass:"},
			new String[]{c, "      - 'GREEN/2'", "      - '99ff99/20' # faded green"}));
		additions.add(new Tuple<>(
			new String[]{"  juniper:"},
			new String[]{c, "      - '00ccff/8'"}));
		additions.add(new Tuple<>(
			new String[]{"  gin_base:"},
			new String[]{c, "      - 'c68c53/15'"}));
		additions.add(new Tuple<>(
			new String[]{"  eggnog_base:"},
			new String[]{c, "      - 'ffecb3/2'"}));


		for (Tuple<String[], String[]> addition : additions) {
			end = config.indexOf("recipes:");
			int index = indexOfStart(addition.a()[0]);
			if (index == -1 && addition.a().length > 1) {
				index = indexOfStart(addition.a()[1]);
			}
			if (index >= start && index <= end) {
				if (config.get(++index).startsWith("    name:")) {
					if (config.get(++index).startsWith("    ingredients:")) {
						// If this is the ingredients line, check if the next line is color or empty
						// We can safely go after color, or before empty
						// If neither, go before this line, as ingredients could be multi line
						if (config.get(index + 1).startsWith("    color:")) {
							index += 2;
						} else if (config.get(index + 1).equals("")) {
							index += 1;
						}
					}
					addLines(index, addition.b());
				}
			}
		}
	}

	private void update30de() {
		addLinesAt(new String[]{"pukeDespawntime:", "enableKickOnOverdrink:", "language:"}, 1,
			"",
				"# Wie stark in Prozent der Spieler taumelt, je nach dem wie viel Alkohol er getrunken hat. Kann auf 0 und h??her als 100 gesetzt werden",
				"stumblePercent: 100",
				"",
				"# Ob seine Betrunkenheit dem Spieler kurz angezeigt werden soll wenn er etwas trinkt oder ein drainItem isst. [true]",
				"showStatusOnDrink: true");
		addLinesAt(new String[]{"hangoverDays:", "enableSealingTable:", "showStatusOnDrink:"}, 1,
			"",
				"# Partikel steigen von Kesseln auf wenn sie Zutaten und eine Feuerquelle haben [true]",
				"# Die sich ??ndernde Farbe der Partikel kann beim Fermentieren mancher Rezepte helfen",
				"enableCauldronParticles: true");
		addLinesAt(new String[]{" #   Oder RGB Farben", " #   Eine Liste von allen Materialien", " # lore: Auflistung von zus??tzlichem Text"}, 1,
			" # cookParticles:",
				" #   Farbe der Partikel ??ber dem Kessel w??hrend verschiedener Kochzeiten",
				" #   Farbe und Minute w??hrend die Farbe erscheinen soll. Z.B. eine Farbe bei 8 Minuten, ??bergehend zu einer anderen bei 18 minuten",
				" #   Als Liste, jede Farbe als Name oder RGB wie oben. Geschrieben 'Farbe/Minute'",
				" #   Zum Ende geht es in die letzte Farbe ??ber, gibt es nur eine Farbe in der Liste, wird es von dieser langsam zu grau.");
		int index = indexOfStart("# wood: Holz des Fasses 0=alle Holzsorten 1=Birke 2=Eiche");
		if (index > -1) {
			setLine(index, "# wood: Holz des Fasses 0=alle Holzsorten 1=Birke 2=Eiche 3=Jungel 4=Fichte 5=Akazie 6=Schwarzeiche 7=Karmesin 8=Wirr");
		}
		addLinesAt(new String[]{"# playercommands: Liste von Befehlen ausgef??hrt vom -Spieler-", "# drinktitle: Nachricht als Titel"}, 1, false,
			"#   Befehle nur f??r bestimmte Qualit??t m??glich mit + Schlecht, ++ Mittel, +++ Gut, vorne anh??ngen.");
		addLinesAt(new String[]{"# Andere Plugins (wenn installiert) nach Rechten zum ??ffnen von F??ssern checken"}, 1, false,
			"# Plugins 'Landlord' und 'Protection Stones' nutzen WorldGuard. 'ClaimChunk' wird nativ unterst??tzt.");
		addLinesAt(new String[]{"useGriefPrevention:", "useLWC:", "useWorldGuard:"}, 1,
			"useTowny: true",
				"useBlockLocker: true");
		addLinesAt(new String[]{"useGMInventories:", "# Plugins 'Landlord' und 'Prote", "# -- Plugin Kompatiblit??t --"}, 1,
			"",
				"# Beim Fass ??ffnen eine virtuelle Kiste nutzen um Rechte bei allen anderen Plugins abzufragen",
				"# K??nnte Anti-Cheat plugins verwirren aber sonst ok zu aktivieren",
				"# Diese Option f??r das Plugin 'Residence' aktivieren, und andere Plugins, die nicht alle F??lle des PlayerInteractEvent checken",
				"useVirtualChestPerms: false",
				"");
		addLinesAt(new String[]{"loadDataAsync:", "useOffhandForCauldron:", "# -- Verschiedene weitere", "useLogBlock:"}, 1,
			"",
			"# Ob nur ein Minimum an Kessel-Partikeln dargestellt werden sollen [false]",
			"minimalParticles: false");
	}

	private void update30en() {
		addLinesAt(new String[]{"pukeDespawntime:", "enableKickOnOverdrink:", "language:"}, 1,
			"",
				"# How much the Player stumbles depending on the amount of alcohol he drank. Can be set to 0 and higher than 100 [100]",
				"stumblePercent: 100",
				"",
				"# Display his drunkeness to the player when he drinks a brew or eats a drainItem [true]",
				"showStatusOnDrink: true");
		addLinesAt(new String[]{"hangoverDays:", "enableSealingTable:", "showStatusOnDrink:"}, 1,
			"",
				"# Show Particles over Cauldrons when they have ingredients and a heat source. [true]",
				"# The changing color of the particles can help with timing some recipes",
				"enableCauldronParticles: true");
		addLinesAt(new String[]{" #   Or RGB colors", " #   A list of materials can be found", " # lore: "}, 1,
			" # cookParticles:",
				" #   Color of the Particles above the cauldron at different cooking-times",
				" #   Color and minute during which each color should appear, i.e. one color at 8 minutes fading to another at 18 minutes.",
				" #   As List, each Color as name or RGB, see above. Written as 'Color/Minute'",
				" #   It will fade to the last color in the end, if there is only one color in the list, it will fade to grey");
		int index = indexOfStart("# wood: Wood of the barrel 0=any 1=Birch 2=Oak");
		if (index > -1) {
			setLine(index, "# wood: Wood of the barrel 0=any 1=Birch 2=Oak 3=Jungle 4=Spruce 5=Acacia 6=Dark Oak 7=Crimson 8=Warped");
		}
		addLinesAt(new String[]{"# playercommands: "}, 1, false,
			"#   Specific Commands for quality possible, using + bad, ++ normal, +++ good, added to the front of the line.");
		addLinesAt(new String[]{"# Enable checking of other Plugins (if installed) for"}, 1, false,
			"# Plugins 'Landlord' and 'Protection Stones' use the WorldGuard Flag. 'ClaimChunk' is natively supported.");
		addLinesAt(new String[]{"useGriefPrevention:", "useLWC:", "useWorldGuard:"}, 1,
			"useTowny: true",
				"useBlockLocker: true");
		addLinesAt(new String[]{"useGMInventories:", "# Plugins 'Landlord' and 'Protectio", "# -- Plugin Compatibility --"}, 1,
			"",
				"# Use a virtual chest when opening a Barrel to check with all other protection plugins",
				"# This could confuse Anti-Cheat plugins, but is otherwise good to use",
				"# use this for 'Residence' Plugin and any others that don't check all cases in the PlayerInteractEvent",
				"useVirtualChestPerms: false",
				"");
		addLinesAt(new String[]{"loadDataAsync:", "useOffhandForCauldron:", "# -- Various Other Settings", "useLogBlock:"}, 1,
			"",
				"# If Cauldron Particles should be reduced to the bare minimum [false]",
				"minimalParticles: false");
	}

	private void update31en() {
		addLinesAt(new String[]{"minimalParticles:", "loadDataAsync:", "openLargeBarrelEverywhere:", "colorInBrewer:"}, 1,
			"",
			"# Allow emptying brews into hoppers to discard brews while keeping the glass bottle [true]",
			"brewHopperDump: true");
	}

	private void update31de() {
		addLinesAt(new String[]{"minimalParticles:", "loadDataAsync:", "openLargeBarrelEverywhere:", "colorInBrewer:"}, 1,
			"",
			"# Ob das Entleeren von Brewery Tr??nken mit Hilfe von Trichtern m??glich ist, um die Glasflasche zur??ck zu bekommen [true]",
			"brewHopperDump: true");
	}



	private void convertCookedSection(FileConfiguration yml, int toLine) {
		ConfigurationSection cookedSection = yml.getConfigurationSection("cooked");
		if (cookedSection != null) {
			for (String ing : cookedSection.getKeys(false)) {
				String name = cookedSection.getString(ing);
				addLines(toLine,
					"  " + ing.toLowerCase() + ":",
				"    name: " + name,
				"    ingredients:",
				"      - " + ing,
				"");
				toLine += 5;
			}

			int index = indexOfStart("cooked:");
			if (index != -1) {
				int size = cookedSection.getKeys(false).size();
				while (size >= 0) {
					removeLine(index);
					size--;
				}
			}
		}


	}

	public void applyPatch(String resourcePath, int toLine) {
		try {
			List<String> patch = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(P.p.getResource(resourcePath), "Resource not found")));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				patch.add(currentLine);
			}
			reader.close();
			config.addAll(toLine, patch);
		} catch (IOException | NullPointerException e) {
			P.p.errorLog("Could not apply Patch: " + resourcePath);
			e.printStackTrace();
		}
	}

	// Update all Materials to Minecraft 1.13
	private void updateMaterials(boolean toMC113) {
		int index;
		if (toMC113) {
			index = indexOfStart("oldMat:");
			if (index != -1) {
				removeLine(index);
			}
		} else {
			index = indexOfStart("version:");
			if (index != -1) {
				addLines(index + 1, "oldMat: true");
			}
		}

		index = indexOfStart("pukeItem: ");
		String line;
		if (index != -1) {
			line = config.get(index);
			if (line.length() > 10) {
				setLine(index, convertMaterial(line, "pukeItem: ", "", toMC113));
			}
		}

		index = indexOfStart("drainItems:");
		if (index != -1) {
			index++;
			while (config.get(index).startsWith("-")) {
				setLine(index, convertMaterial(config.get(index), "- ", "(,.*|)/.*", toMC113));
				index++;
			}
		}

		index = indexOfStart("recipes:");
		if (index != -1) {
			index++;
			int endIndex = indexOfStart("useWorldGuard:");
			if (endIndex < index) {
				endIndex = indexOfStart("enableChatDistortion:");
			}
			if (endIndex < index) {
				endIndex = indexOfStart("words:");
			}
			if (endIndex < index) {
				endIndex = config.size();
			}
			while (index < endIndex) {
				if (config.get(index).matches("^\\s+ingredients:.*")) {
					index++;
					while (config.get(index).matches("^\\s+- .+")) {
						line = config.get(index);
						setLine(index, convertMaterial(line, "^\\s+- ", "(,.*|)/.*", toMC113));
						index++;
					}
				} else if (config.get(index).startsWith("cooked:")) {
					index++;
					while (config.get(index).matches("^\\s\\s+.+")) {
						line = config.get(index);
						setLine(index, convertMaterial(line, "^\\s\\s+", ":.*", toMC113));
						index++;
					}
				}
				index++;
			}
		}

		index = indexOfStart("cauldron:");
		if (index != -1) {
			index++;
			int endIndex = indexOfStart("recipes:");
			if (endIndex < index) {
				endIndex = indexOfStart("      cookingtime:");
			}
			if (endIndex < index) {
				endIndex = indexOfStart("useWorldGuard:");
			}
			while (index < endIndex) {
				if (config.get(index).matches("^\\s+ingredients:.*")) {
					index++;
					while (config.get(index).matches("^\\s+- .+")) {
						line = config.get(index);
						setLine(index, convertMaterial(line, "^\\s+- ", "(,.*|)/.*", toMC113));
						index++;
					}
				}
				index++;
			}
		}
	}

	private String convertMaterial(String line, String regexPrefix, String regexPostfix, boolean toMC113) {
		if (!toMC113) {
			return convertIdtoMaterial(line, regexPrefix, regexPostfix);
		}
		String mat = line.replaceFirst(regexPrefix, "").replaceFirst(regexPostfix, "");
		Material material;
		if (mat.equalsIgnoreCase("LONG_GRASS")) {
			material = Material.GRASS;
		} else {
			material = Material.matchMaterial(mat, true);
		}

		if (material == null) {
			return line;
		}
		String matnew = material.name();
		if (!mat.equalsIgnoreCase(matnew)) {
			return line.replaceAll(mat, matnew);
		} else {
			return line;
		}
	}

	private String convertIdtoMaterial(String line, String regexPrefix, String regexPostfix) {
		String idString = line.replaceFirst(regexPrefix, "").replaceFirst(regexPostfix, "");
		int id = P.p.parseInt(idString);
		if (id > 0) {
			Material material = LegacyUtil.getMaterial(id);
			if (material == null) {
				P.p.errorLog("Could not find Material with id: " + line);
				return line;
			} else {
				return line.replaceAll(idString, material.name());
			}
		} else {
			return line;
		}
	}

	private void updateMaterialDescriptions(boolean de) {
		int index;
		if (de) {
			index = indexOfStart("# ingredients: Auflistung von 'Material,Data/Anzahl'");
			if (index != -1) {
				setLine(index, "# ingredients: Auflistung von 'Material/Anzahl'");
			}

			index = indexOfStart("#   Es kann ein Data-Wert (durability) angegeben werden");
			if (index != -1) {
				removeLine(index);
			}

			index = indexOfStart("#   Wenn Vault installiert ist");
			if (index != -1) {
				removeLine(index);
			}

			index = indexOfStart("#   Vault erkennt Namen wie");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#     - Jungle Leaves/64  # Nur mit Vault");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#     - Green Dye/6       # Nur mit Vault");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#   Ein 'X' an den Namen");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#   Effekte sind ab der 1.9 immer verborgen");
			if (index != -1) {
				removeLine(index);
			}
		} else {
			index = indexOfStart("# ingredients: List of 'material,data/amount'");
			if (index != -1) {
				setLine(index, "# ingredients: List of 'material/amount'");
			}

			index = indexOfStart("#   You can specify a data (durability) value");
			if (index != -1) {
				removeLine(index);
			}

			index = indexOfStart("#   If Vault is installed normal names can be used");
			if (index != -1) {
				removeLine(index);
			}

			index = indexOfStart("#   Vault will recognize things");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#     - Jungle Leaves/64  # Only with Vault");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#     - Green Dye/6       # Only with Vault");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#   Suffix name with 'X' to hide effect");
			if (index != -1) {
				removeLine(index);
			}
			index = indexOfStart("#   Effects are always hidden in 1.9 and newer");
			if (index != -1) {
				removeLine(index);
			}
		}
	}


}
