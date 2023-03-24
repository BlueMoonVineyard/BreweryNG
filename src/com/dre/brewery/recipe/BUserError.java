package com.dre.brewery.recipe;

import java.util.concurrent.ThreadLocalRandom;

import org.mini2Dx.gettext.GetText;

import com.dre.brewery.P;

public interface BUserError {
    public String userMessage();

    public class BadIngredientKindError implements BUserError {
        private Ingredient excessItemStack;

        public BadIngredientKindError(Ingredient of) {
            this.excessItemStack = of;
        }

        @Override
        public String userMessage() {
            int messagePermutation = ThreadLocalRandom.current().nextInt(1, 3+1);
			switch (messagePermutation) {
			case 1:
				return GetText.tr("Eugh, the taste of the {0} clashes with the other ingredients...", excessItemStack.displayName());
			case 2:
				return GetText.tr("The {0} doesn''t taste very good here...", excessItemStack.displayName());
			case 3:
				return GetText.tr("There's something in here that shouldn't be in here...", excessItemStack.displayName());
			default:
				throw new AssertionError("invalid case");
			}
        }
    }
    public class MissingIngredientKindError implements BUserError {
        private RecipeItem missingItem;

        public MissingIngredientKindError(RecipeItem missingItem) {
            this.missingItem = missingItem;
        }

        @Override
        public String userMessage() {
            int messagePermutation = ThreadLocalRandom.current().nextInt(1, 3+1);
			switch (messagePermutation) {
				case 1:
					return GetText.tr("Seems like something's missing...", missingItem.displayName());
				case 2:
					return GetText.tr("This would be better with some {0}...", missingItem.displayName());
				case 3:
					return GetText.tr("Missing {0}}...", missingItem.displayName());
				default:
					throw new AssertionError("invalid case");
			}
        }
    }

    public class IngredientQuantityError implements BUserError {
        private RecipeItem item;
        private int userCount;

        public IngredientQuantityError(RecipeItem item, int actualCount) {
            this.item = item;
            this.userCount = actualCount;
			assert userCount != item.getAmount() : "it's not an error if the ingredient amounts match";
        }

        @Override
        public String userMessage() {
            int messagePermutation = ThreadLocalRandom.current().nextInt(1, 2+1);
            if (userCount > item.getAmount()) {
				switch (messagePermutation) {
				case 1:
					return GetText.tr("Whoa, that's definitely too much {0}!", item.displayName());
				case 2:
					return GetText.tr("That's a lot of {0}...", item.displayName());
				default:
					throw new AssertionError("invalid case");
				}
            } else if (userCount < item.getAmount()) {
				switch (messagePermutation) {
				case 1:
					return GetText.tr("It could do with a bit more of the taste of {0}...", item.displayName());
				case 2:
					return GetText.tr("Not getting enough of that {0} in here.", item.displayName());
				default:
					throw new AssertionError("invalid case");
				}
            }

            return "";
        }
    }
    public class DistillationError implements BUserError {
        private boolean actuallyDistilled;
        private boolean neededDistillation;

        public DistillationError(boolean actuallyDistilled, boolean neededDistillation) {
            this.actuallyDistilled = actuallyDistilled;
            this.neededDistillation = neededDistillation;
        }

        @Override
        public String userMessage() {
            int messagePermutation = ThreadLocalRandom.current().nextInt(1, 2+1);

            if (actuallyDistilled && !neededDistillation) {
                switch (messagePermutation) {
				case 1:
					return GetText.tr("There's something in here that shouldn't be in here...");
				case 2:
					return GetText.tr("I think this needs some distillation...");
				default:
					throw new AssertionError("invalid case");
				}
            } else if (neededDistillation && !actuallyDistilled) {
                switch (messagePermutation) {
				case 1:
					return GetText.tr("Seems like something's missing...");
				case 2:
					return GetText.tr("I think this didn't need distillation...");
				default:
					throw new AssertionError("invalid case");
				}
            }

            return "";
        }
    }
}
