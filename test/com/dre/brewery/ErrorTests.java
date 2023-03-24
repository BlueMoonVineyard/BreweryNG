package com.dre.brewery;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.recipe.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

public class ErrorTests {
	private ServerMock mock = MockBukkit.mock();

	@Test()
	public void testVodkaWithOnlyOnePotato() {
		BreweryApi.removeRecipe("Vodka");
		BRecipe recipe = BreweryApi.recipeBuilder("Lousy Vodka", "Vodka", "Russian Vodka")
			.color(PotionColor.WHITE)
			.addIngredient(new ItemStack(Material.POTATO, 10))
			.cook(15)
			.distill((byte)3, 7)
			.alcohol(20)
			.difficulty(4)
			.get();
		BreweryApi.addRecipe(recipe, false);
		assertEquals(1, BRecipe.getAddedRecipes().size());

		BIngredients ingredients = new BIngredients();
		ingredients.add(new ItemStack(Material.POTATO, 1));

		List<BUserError> errors = ingredients.getErrorsForRecipe(0, 2, true, recipe);
		assertEquals(1, errors.size());
		assertInstanceOf(BUserError.IngredientQuantityError.class, errors.get(0));
	}
}
