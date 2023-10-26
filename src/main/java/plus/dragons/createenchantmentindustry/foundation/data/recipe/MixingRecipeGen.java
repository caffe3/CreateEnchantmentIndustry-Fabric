package plus.dragons.createenchantmentindustry.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.recipe.BlockTagIngredient;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class MixingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	INK = create(EnchantmentIndustry.genRL("ink"), b -> b.require(Fluids.WATER, FluidConstants.BUCKET / 4)
		.require(CeiTags.ItemTag.INK_INGREDIENT.tag)
		.output(CeiFluids.INK.get(), FluidConstants.BUCKET / 4)),

		HYPER_EXPERIENCE = create(EnchantmentIndustry.genRL("hyper_experience"), b -> b
			.require(Items.GLOW_INK_SAC)
			.require(Items.LAPIS_LAZULI)
			.require(CeiFluids.EXPERIENCE.get(), FluidConstants.BUCKET / 10)
			.output(CeiFluids.HYPER_EXPERIENCE.get(), FluidConstants.BUCKET / 100)
			.requiresHeat(HeatCondition.SUPERHEATED))
	;

	public MixingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
