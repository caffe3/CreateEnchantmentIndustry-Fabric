package plus.dragons.createenchantmentindustry.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;

import io.github.tropheusj.milk.Milk;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;

import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class FillingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */

	GeneratedRecipe

	EXPERIENCE_BOTTLE = create(EnchantmentIndustry.genRL("experience_bottle"), b -> b.require(Items.GLASS_BOTTLE)
		.require(CeiFluids.EXPERIENCE.get(), CeiFluids.EXPERIENCE_BOTTLE_AMOUNT)
		.output(Items.EXPERIENCE_BOTTLE)),

		HYPER_EXP_BOTTLE = create(EnchantmentIndustry.genRL("hyper_experience_bottle"), b -> b.require(Items.GLASS_BOTTLE)
			.require(CeiFluids.HYPER_EXPERIENCE.get(), CeiFluids.HYPER_EXP_BOTTLE_AMOUNT)
			.output(CeiItems.HYPER_EXP_BOTTLE))
	;

	public FillingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
