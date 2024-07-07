package com.dre.brewery.listeners;

import com.dre.brewery.*;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.filedata.CraftedBrewTracker;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.recipe.RecipeItem;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.PermissionUtil;
import com.dre.brewery.utility.Tuple;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.google.common.collect.Lists;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mini2Dx.gettext.GetText;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static com.dre.brewery.utility.PermissionUtil.BPermission.*;

public class CommandListener implements CommandExecutor {

	public P p = P.p;

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		String cmd = "help";
		if (args.length > 0) {
			cmd = args[0];
		}

		if (cmd.equalsIgnoreCase("help")) {

			cmdHelp(sender, args);

		} else if (cmd.equalsIgnoreCase("collection")) {

			cmdCollection(sender);

		} else if (cmd.equalsIgnoreCase("reload")) {

			if (sender.hasPermission("brewery.cmd.reload")) {
				p.reload(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("configname") || cmd.equalsIgnoreCase("itemname") || cmd.equalsIgnoreCase("iteminfo")) {

			if (sender.hasPermission("brewery.cmd.reload")) {
				cmdItemName(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("wakeup")) {

			if (sender.hasPermission("brewery.cmd.wakeup")) {
				cmdWakeup(sender, args);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("create") || cmd.equalsIgnoreCase("give")) {

			if (sender.hasPermission("brewery.cmd.create")) {
				cmdCreate(sender, args);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("info")) {

			if (args.length > 1) {
				if (sender.hasPermission("brewery.cmd.infoOther")) {
					cmdInfo(sender, args[1]);
				} else {
					p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
				}
			} else {
				if (sender.hasPermission("brewery.cmd.info")) {
					cmdInfo(sender, null);
				} else {
					p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
				}
			}

		} else if (cmd.equalsIgnoreCase("seal") || cmd.startsWith("seal") || cmd.startsWith("Seal")) {

			if (sender.hasPermission("brewery.cmd.seal")) {
				cmdSeal(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("copy") || cmd.equalsIgnoreCase("cp")) {

			if (sender.hasPermission("brewery.cmd.copy")) {
				if (args.length > 1) {
					cmdCopy(sender, p.parseInt(args[1]));
				} else {
					cmdCopy(sender, 1);
				}
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("delete") || cmd.equalsIgnoreCase("rm") || cmd.equalsIgnoreCase("remove")) {

			if (sender.hasPermission("brewery.cmd.delete")) {
				cmdDelete(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("static")) {

			if (sender.hasPermission("brewery.cmd.static")) {
				cmdStatic(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("unlabel")) {

			if (sender.hasPermission("brewery.cmd.unlabel")) {
				cmdUnlabel(sender);
			} else {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			}

		} else if (cmd.equalsIgnoreCase("debuginfo")) {

			debugInfo(sender, args.length > 1 ? args[1] : null);

		} else if (cmd.equalsIgnoreCase("showstats")) {

			showStats(sender);

		} else if (cmd.equalsIgnoreCase("puke") || cmd.equalsIgnoreCase("vomit") || cmd.equalsIgnoreCase("barf")) {

			cmdPuke(sender, args);

		} else if (cmd.equalsIgnoreCase("drink")) {

			cmdDrink(sender, args);

		} else {

			if (p.getServer().getPlayerExact(cmd) != null || BPlayer.hasPlayerbyName(cmd)) {

				if (args.length == 1) {
					if (sender.hasPermission("brewery.cmd.infoOther")) {
						cmdInfo(sender, cmd);
					}
				} else {
					if (sender.hasPermission("brewery.cmd.player")) {
						cmdPlayer(sender, args);
					} else {
						p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
					}
				}

			} else {

				p.msg(sender, GetText.tr("Unknown Command"));
				p.msg(sender, GetText.tr("Use &6/brew help &fto display the help"));

			}
		}

		return true;
	}

	public void cmdHelp(CommandSender sender, String[] args) {

		int page = 1;
		if (args.length > 1) {
			page = p.parseInt(args[1]);
		}

		ArrayList<String> commands = getCommands(sender);

		if (page == 1) {
			p.msg(sender, "&6" + p.getDescription().getName() + " v" + p.getDescription().getVersion());
		}

		BUtil.list(sender, commands, page);

	}

	private Pane recipePagePane(Player player, List<BRecipe> recipes) {
		OutlinePane pane = new OutlinePane(9, 5);
		Set<String> crafted = CraftedBrewTracker.playerBrews(player.getUniqueId().toString());

		for (BRecipe recipe : recipes) {
			if (crafted.contains(recipe.getOptionalID().get())) {
				pane.addItem(new GuiItem(recipe.create(10), ev -> ev.setCancelled(true)));
			} else {
				ItemStack item = new ItemStack(Material.BARRIER);
				Brew brew = recipe.createBrew(10);
				ItemMeta meta = brew.unLabel(brew.createItem(recipe));
				ItemMeta itemMeta = item.getItemMeta();
				itemMeta.setDisplayName(meta.getDisplayName());
				item.setItemMeta(itemMeta);
				List<String> lore = recipe.getUndiscoveredLore() == null ? null : recipe.getUndiscoveredLore().stream()
					.map(line -> "§r§f" + line)
					.toList();
				item.setLore(lore);
				pane.addItem(new GuiItem(item, ev -> ev.setCancelled(true)));
			}
		}

		return pane;
	}

	public void cmdCollection(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player pSender = (Player)sender;

		PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 5);
		List<BRecipe> eligible = BRecipe.getAllRecipes().stream().filter(recipe -> recipe.getOptionalID().isPresent()).filter(recipe -> !recipe.isHidden()).toList();
		List<List<BRecipe>> pageRecipes = Lists.partition(eligible, 5*9);
		ChestGui gui = new ChestGui(6, MessageFormat.format("{0} ({1}/{2})", GetText.tr("Crafted Brews"), 1, pageRecipes.size()));

		int pageNumber = 0;
		for (List<BRecipe> page : pageRecipes) {
			paginatedPane.addPane(pageNumber, recipePagePane(pSender, page));
			pageNumber++;
		}

		Function<Integer, Void> adjust = num -> {
			int target = paginatedPane.getPage() + num;
			if (target < 0) {
				target = 0;
			}
			if (target >= pageRecipes.size()-1) {
				target = pageRecipes.size()-1;
			}
			gui.setTitle(MessageFormat.format("{0} ({1}/{2})", GetText.tr("Crafted Brews"), target+1, pageRecipes.size()));
			paginatedPane.setPage(target);
			gui.update();
			return null;
		};

		OutlinePane paginatorPane = new OutlinePane(0, 5, 9, 1);
		ItemStack goBackItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta goBackMeta = goBackItem.getItemMeta();
		goBackMeta.setDisplayName(GetText.tr("Go Back"));
		goBackItem.setItemMeta(goBackMeta);
		paginatorPane.addItem(new GuiItem(goBackItem, ig -> adjust.apply(-1)));
		for (int i = 0; i < 7; i++) {
			paginatorPane.addItem(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), ev -> ev.setCancelled(true)));
		}
		ItemStack goForwardItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		ItemMeta goForwardMeta = goForwardItem.getItemMeta();
		goForwardMeta.setDisplayName(GetText.tr("Go Forward"));
		goForwardItem.setItemMeta(goForwardMeta);
		paginatorPane.addItem(new GuiItem(goForwardItem, ig -> adjust.apply(1)));

		gui.addPane(paginatedPane);
		gui.addPane(paginatorPane);
		gui.show(pSender);

	}

	public ArrayList<String> getCommands(CommandSender sender) {

		ArrayList<String> cmds = new ArrayList<>();
		cmds.add(GetText.tr("&6/brew help [Page] &9Shows a specific help-page"));
		PermissionUtil.evaluateExtendedPermissions(sender);

		if (PLAYER.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew <Player> <%Drunkeness> [Quality]&9 Sets Drunkeness (and Quality) of a Player"));
		}

		if (INFO.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew info&9 Displays your current Drunkeness and Quality"));
		}

		if (P.use1_13 && SEAL.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew seal &9Seal Brews for selling in shops"));
		}

		if (UNLABEL.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew unlabel &9Removes the detailled label of a potion"));
		}

		if (PermissionUtil.noExtendedPermissions(sender)) {
			return cmds;
		}

		if (COLLECTION.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew collection&9 Shows what brews you have and haven't crafted"));
		}

		if (INFO_OTHER.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew info [Player]&9 Displays the current Drunkeness and Quality of [Player]"));
		}

		if (CREATE.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew create <Recipe> [Quality] [Player] &9Create a Brew with optional quality (1-10)"));
			cmds.add(GetText.tr("&6/brew give <Recipe> [Quality] [Player] &9Alias for /brew create"));
		}

		if (DRINK.checkCached(sender) || DRINK_OTHER.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew drink <Recipe> [Quality] [Player] &9Simulates [Player] drinking a Brew"));
		}

		if (RELOAD.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew ItemName &9Display name of item in hand for the config"));
			cmds.add(GetText.tr("&6/brew reload &9Reload config"));
		}

		if (PUKE.checkCached(sender) || PUKE_OTHER.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew puke [Player] [Amount] &9Makes you or [Player] puke"));
		}

		if (WAKEUP.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew wakeup list <Page>&9 Lists all wakeup points"));
			cmds.add(GetText.tr("&6/brew wakeup list <Page> [World]&9 Lists all wakeup points of <world>"));
			cmds.add(GetText.tr("&6/brew wakeup check &9Teleports to all wakeup points"));
			cmds.add(GetText.tr("&6/brew wakeup check <id> &9Teleports to the wakeup point with <id>"));
			cmds.add(GetText.tr("&6/brew wakeup add &9Adds a wakeup point at your current position"));
			cmds.add(GetText.tr("&6/brew wakeup remove <id> &9Removes the wakeup point with <id>"));
		}

		if (STATIC.checkCached(sender)) {
			cmds.add(GetText.tr("&6/brew static &9Make Brew static -> No further ageing or distilling"));
		}

		if (COPY.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew copy [Quantity]&9 Copies the potion in your hand"));
		}

		if (DELETE.checkCached(sender)) {
			cmds.add (GetText.tr("&6/brew delete &9Deletes the potion in your hand"));
		}

		return cmds;
	}

	public void cmdWakeup(CommandSender sender, String[] args) {

		if (args.length == 1) {
			cmdHelp(sender, args);
			return;
		}

		if (args[1].equalsIgnoreCase("add")) {

			Wakeup.set(sender);

		} else if (args[1].equalsIgnoreCase("list")){

			int page = 1;
			String world = null;
			if (args.length > 2) {
				page = p.parseInt(args[2]);
			}
			if (args.length > 3) {
				world = args[3];
			}
			Wakeup.list(sender, page, world);

		} else if (args[1].equalsIgnoreCase("remove")){

			if (args.length > 2) {
				int id = p.parseInt(args[2]);
				Wakeup.remove(sender, id);
			} else {
				p.msg(sender, GetText.tr("Usage:"));
				p.msg(sender, GetText.tr("&6/brew wakeup remove <id> &9Removes the wakeup point with <id>"));
			}

		} else if (args[1].equalsIgnoreCase("check")){

			int id = -1;
			if (args.length > 2) {
				id = p.parseInt(args[2]);
				if (id < 0) {
					id = 0;
				}
			}
			Wakeup.check(sender, id, id == -1);

		} else if (args[1].equalsIgnoreCase("cancel")){

			Wakeup.cancel(sender);

		} else {

			p.msg(sender, GetText.tr("Unknown Command"));
			p.msg(sender, GetText.tr("Use &6/brew help &fto display the help"));

		}
	}

	public void cmdPlayer(CommandSender sender, String[] args) {

		int drunkeness = p.parseInt(args[1]);
		if (drunkeness < 0) {
			return;
		}
		int quality = -1;
		if (args.length > 2) {
			quality = p.parseInt(args[2]);
			if (quality < 1 || quality > 10) {
				p.msg(sender, GetText.tr("&cThe quality has to be between 1 and 10!"));
				return;
			}
		}

		String playerName = args[0];
		Player player = P.p.getServer().getPlayerExact(playerName);
		BPlayer bPlayer;
		if (player == null) {
			bPlayer = BPlayer.getByName(playerName);
		} else {
			bPlayer = BPlayer.get(player);
		}
		if (bPlayer == null && player != null) {
			if (drunkeness == 0) {
				return;
			}
			bPlayer = BPlayer.addPlayer(player);
		}
		if (bPlayer == null) {
			return;
		}

		if (drunkeness == 0) {
			bPlayer.remove();
		} else {
			bPlayer.setData(drunkeness, quality);
			if (BConfig.showStatusOnDrink) {
				bPlayer.showDrunkeness(player);
			}
		}

		if (drunkeness > 100) {
			if (player != null) {
				bPlayer.drinkCap(player);
			} else {
				if (!BConfig.overdrinkKick) {
					bPlayer.setData(100, 0);
				}
			}
		}
		p.msg(sender, GetText.tr("&a{0} is now &6{1}% &adrunk, with a quality of &6{2}", playerName, "" + drunkeness, "" + bPlayer.getQuality()));

	}

	public void cmdInfo(CommandSender sender, String playerName) {

		boolean selfInfo = playerName == null;
		if (selfInfo) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				playerName = player.getName();
			} else {
				p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
				return;
			}
		}

		Player player = P.p.getServer().getPlayerExact(playerName);
		BPlayer bPlayer;
		if (player == null) {
			bPlayer = BPlayer.getByName(playerName);
		} else {
			bPlayer = BPlayer.get(player);
		}
		if (bPlayer == null) {
			p.msg(sender, GetText.tr("{0} is not drunk", playerName));
		} else {
			if (selfInfo) {
				bPlayer.showDrunkeness(player);
			} else {
				p.msg(sender, GetText.tr("{0} is &6{1}% &fdrunk, with a quality of &6{2}", playerName, "" + bPlayer.getDrunkeness(), "" + bPlayer.getQuality()));
			}
		}

	}

	public void cmdItemName(CommandSender sender) {
		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}

		Player player = (Player) sender;
		@SuppressWarnings("deprecation")
		ItemStack hand = P.use1_9 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
		if (hand != null) {
			p.msg(sender, GetText.tr("&aName for the Config is: &f{0}", hand.getType().name().toLowerCase(Locale.ENGLISH)));
		} else {
			p.msg(sender, GetText.tr("&cCould not find item in your hand"));
		}

	}

	public void cmdSeal(CommandSender sender) {
		if (!P.use1_13) {
			P.p.msg(sender, "Sealing requires minecraft 1.13 or higher");
			return;
		}
		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}

		Player player = (Player) sender;
		player.openInventory(new BSealer(player).getInventory());
	}

	@Deprecated
	public void cmdCopy(CommandSender sender, int count) {

		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		if (count < 1 || count > 36) {
			p.msg(sender, GetText.tr("Usage:"));
			p.msg(sender, GetText.tr("&6/brew copy [Quantity]&9 Copies the potion in your hand"));
			return;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand != null) {
			if (Brew.isBrew(hand)) {
				while (count > 0) {
					ItemStack item = hand.clone();
					if (!(player.getInventory().addItem(item)).isEmpty()) {
						p.msg(sender, GetText.tr("&6{0} &cPotions did not fit into your inventory", "" + count));
						return;
					}
					count--;
				}
				return;
			}
		}

		p.msg(sender, GetText.tr("&cThe item in your hand could not be identified as a potion!"));

	}

	@Deprecated
	public void cmdDelete(CommandSender sender) {

		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand != null) {
			if (Brew.isBrew(hand)) {
				player.setItemInHand(new ItemStack(Material.AIR));
				return;
			}
		}
		p.msg(sender, GetText.tr("&cThe item in your hand could not be identified as a potion!"));

	}

	public void debugInfo(CommandSender sender, String recipeName) {
		if (!P.use1_9 || !sender.isOp()) return;
		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand != null) {
			Brew brew = Brew.get(hand);
			if (brew == null) return;
			P.p.log(brew.toString());
			BIngredients ingredients = brew.getIngredients();
			if (recipeName == null) {
				P.p.log("&lIngredients:");
				for (Ingredient ing : ingredients.getIngredientList()) {
					P.p.log(ing.toString());
				}
				P.p.log("&lTesting Recipes");
				for (BRecipe recipe : BRecipe.getAllRecipes()) {
					int ingQ = ingredients.getIngredientQuality(recipe);
					int cookQ = ingredients.getCookingQuality(recipe, false);
					int cookDistQ = ingredients.getCookingQuality(recipe, true);
					int ageQ = ingredients.getAgeQuality(recipe, brew.getAgeTime());
					P.p.log(recipe.getRecipeName() + ": ingQlty: " + ingQ + ", cookQlty:" + cookQ + ", cook+DistQlty: " + cookDistQ + ", ageQlty: " + ageQ);
				}
				BRecipe distill = ingredients.getBestRecipe(brew.getWood(), brew.getAgeTime(), true, brew.getLiquidType());
				BRecipe nonDistill = ingredients.getBestRecipe(brew.getWood(), brew.getAgeTime(), false, brew.getLiquidType());
				P.p.log("&lWould prefer Recipe: " + (nonDistill == null ? "none" : nonDistill.getRecipeName()) + " and Distill-Recipe: " + (distill == null ? "none" : distill.getRecipeName()));
			} else {
				BRecipe recipe = BRecipe.getMatching(recipeName);
				if (recipe == null) {
					P.p.msg(player, "Could not find Recipe " + recipeName);
					return;
				}
				P.p.log("&lIngredients in Recipe " + recipe.getRecipeName() + ":");
				for (RecipeItem ri : recipe.getIngredients()) {
					P.p.log(ri.toString());
				}
				P.p.log("&lIngredients in Brew:");
				for (Ingredient ingredient : ingredients.getIngredientList()) {
					int amountInRecipe = recipe.amountOf(ingredient);
					P.p.log(ingredient.toString() + ": " + amountInRecipe + " of this are in the Recipe");
				}
				int ingQ = ingredients.getIngredientQuality(recipe);
				int cookQ = ingredients.getCookingQuality(recipe, false);
				int cookDistQ = ingredients.getCookingQuality(recipe, true);
				int ageQ = ingredients.getAgeQuality(recipe, brew.getAgeTime());
				P.p.log("ingQlty: " + ingQ + ", cookQlty:" + cookQ + ", cook+DistQlty: " + cookDistQ  + ", ageQlty: " + ageQ);
			}

			P.p.msg(player, "Debug Info for item written into Log");
		}
	}

	public void showStats(CommandSender sender) {
		if (sender instanceof ConsoleCommandSender && !sender.isOp()) return;

		P.p.msg(sender, "Drunk Players: " + BPlayer.numDrunkPlayers());
		P.p.msg(sender, "Brews created: " + P.p.stats.brewsCreated);
		P.p.msg(sender, "Barrels built: " + Barrel.barrels.size());
		P.p.msg(sender, "Cauldrons boiling: " + BCauldron.bcauldrons.size());
		P.p.msg(sender, "Number of Recipes: " + BRecipe.getAllRecipes().size());
		P.p.msg(sender, "Wakeups: " + Wakeup.wakeups.size());
	}

	@SuppressWarnings("deprecation")
	public void cmdStatic(CommandSender sender) {

		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand != null) {
			Brew brew = Brew.get(hand);
			if (brew != null) {
				if (brew.isStatic()) {
					if (!brew.isStripped()) {
						brew.setStatic(false, hand);
						p.msg(sender, GetText.tr("&ePotion is not static anymore and will normally age in barrels."));
					} else {
						p.msg(sender, GetText.tr("Sealed Brews are always static!"));
						return;
					}
				} else {
					brew.setStatic(true, hand);
					p.msg(sender, GetText.tr("&aPotion is now static and will not change in barrels or brewing stands."));
				}
				brew.touch();
				ItemMeta meta = hand.getItemMeta();
				assert meta != null;
				BrewModifyEvent modifyEvent = new BrewModifyEvent(brew, meta, BrewModifyEvent.Type.STATIC);
				P.p.getServer().getPluginManager().callEvent(modifyEvent);
				if (modifyEvent.isCancelled()) {
					return;
				}
				brew.save(meta);
				hand.setItemMeta(meta);
				return;
			}
		}
		p.msg(sender, GetText.tr("&cThe item in your hand could not be identified as a potion!"));

	}

	@SuppressWarnings("deprecation")
	public void cmdUnlabel(CommandSender sender) {

		if (!(sender instanceof Player)) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand != null) {
			Brew brew = Brew.get(hand);
			if (brew != null) {
				if (!brew.isUnlabeled()) {
					ItemMeta origMeta = hand.getItemMeta();
					brew.unLabel(hand);
					brew.touch();
					ItemMeta meta = hand.getItemMeta();
					assert meta != null;
					BrewModifyEvent modifyEvent = new BrewModifyEvent(brew, meta, BrewModifyEvent.Type.UNLABEL);
					P.p.getServer().getPluginManager().callEvent(modifyEvent);
					if (modifyEvent.isCancelled()) {
						hand.setItemMeta(origMeta);
						return;
					}
					brew.save(meta);
					hand.setItemMeta(meta);
					p.msg(sender, GetText.tr("&aLabel removed!"));
					return;
				} else {
					p.msg(sender, GetText.tr("&cThe Brew in your hand is already unlabeled!"));
					return;
				}
			}
		}
		p.msg(sender, GetText.tr("&cThe item in your hand could not be identified as a potion!"));

	}

	public void cmdCreate(CommandSender sender, String[] args) {
		if (args.length < 2) {
			p.msg(sender, GetText.tr("Usage:"));
			p.msg(sender, GetText.tr("&6/brew create <Recipe> [Quality] [Player] &9Create a Brew with optional quality (1-10)"));
			return;
		}

		Tuple<Brew, Player> brewForPlayer = getFromCommand(sender, args);

		if (brewForPlayer != null) {
			if (brewForPlayer.b().getInventory().firstEmpty() == -1) {
				p.msg(sender, GetText.tr("&6{0} &cPotions did not fit into your inventory", "1"));
				return;
			}

			ItemStack item = brewForPlayer.a().createItem(null);
			if (item != null) {
				brewForPlayer.b().getInventory().addItem(item);
				p.msg(sender, GetText.tr("&aBrew Created"));
			}
		}

	}

	@Nullable
	public Tuple<Brew, Player> getFromCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			return null;
		}

		int quality = 10;
		boolean hasQuality = false;
		String pName = null;
		if (args.length > 2) {
			quality = p.parseInt(args[args.length - 1]);

			if (quality <= 0 || quality > 10) {
				pName = args[args.length - 1];
				if (args.length > 3) {
					quality = p.parseInt(args[args.length - 2]);
				}
			}
			if (quality > 0 && quality <= 10) {
				hasQuality = true;
			} else {
				quality = 10;
			}
		}
		Player player = null;
		if (pName != null) {
			player = p.getServer().getPlayer(pName);
		}

		if (!(sender instanceof Player) && player == null) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return null;
		}

		if (player == null) {
			player = ((Player) sender);
			pName = null;
		}
		int stringLength = args.length - 1;
		if (pName != null) {
			stringLength--;
		}
		if (hasQuality) {
			stringLength--;
		}

		String name;
		if (stringLength > 1) {
			StringBuilder builder = new StringBuilder(args[1]);

			for (int i = 2; i < stringLength + 1; i++) {
				builder.append(" ").append(args[i]);
			}
			name = builder.toString();
		} else {
			name = args[1];
		}
		name = name.replaceAll("\"", "");

		BRecipe recipe = BRecipe.getMatching(name);
		if (recipe != null) {
			return new Tuple<>(recipe.createBrew(quality), player);
		} else {
			p.msg(sender, GetText.tr("&cNo Recipe with Name: '{0}&c' found!", name));
		}
		return null;
	}

	public void cmdPuke(CommandSender sender, String[] args) {
		if (!sender.hasPermission("brewery.cmd.puke")) {
			p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			return;
		}

		Player player = null;
		if (args.length > 1) {
			player = p.getServer().getPlayer(args[1]);
			if (player == null) {
				p.msg(sender, GetText.tr("Player not found: {0}", args[1]));
				return;
			}
		}

		if (!(sender instanceof Player) && player == null) {
			p.msg(sender, GetText.tr("&cThis command can only be executed as a player!"));
			return;
		}
		if (player == null) {
			player = ((Player) sender);
		} else {
			if (!sender.hasPermission("brewery.cmd.pukeOther") && !player.equals(sender)) {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
				return;
			}
		}
		int count = 0;
		if (args.length > 2) {
			count = P.p.parseInt(args[2]);
		}
		if (count <= 0) {
			count = 20 + (int) (Math.random() * 40);
		}
		BPlayer.addPuke(player, count);
	}

	public void cmdDrink(CommandSender sender, String[] args) {
		if (!sender.hasPermission("brewery.cmd.drink") || !sender.hasPermission("brewery.cmd.drink")) {
			p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			return;
		}

		if (args.length < 2) {
			p.msg(sender, GetText.tr("Usage:"));
			p.msg(sender, GetText.tr("&6/brew drink <Recipe> [Quality] [Player] &9Simulates [Player] drinking a Brew"));
			return;
		}

		Tuple<Brew, Player> brewForPlayer = getFromCommand(sender, args);
		if (brewForPlayer != null) {
			Player player = brewForPlayer.b();
			if ((!sender.equals(player) && !sender.hasPermission("brewery.cmd.drinkOther")) ||
				(sender.equals(player) && !sender.hasPermission("brewery.cmd.drink"))) {
				p.msg(sender, GetText.tr("&cYou don't have permissions to do this!"));
			} else {
				Brew brew = brewForPlayer.a();
				String brewName = brew.getCurrentRecipe().getName(brew.getQuality());
				BPlayer.drink(brew, null, player);

				p.msg(player, GetText.tr("&aYou drank one {0}", brewName));
				if (!sender.equals(player)) {
					p.msg(sender, GetText.tr("&a{0} drinks one {1}", player.getDisplayName(), brewName));
				}
			}
		}
	}

}
