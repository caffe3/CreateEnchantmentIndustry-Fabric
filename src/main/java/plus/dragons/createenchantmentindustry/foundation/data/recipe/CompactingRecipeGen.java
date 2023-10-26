package plus.dragons.createenchantmentindustry.foundation.data.recipe;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import com.simibubi.create.AllBlocks;
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

public class CompactingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	EXPERIENCE_BLOCK_FROM_HONEYCOMB = create(EnchantmentIndustry.genRL("experience_block_from_honeycomb"), b -> b.require(CeiFluids.EXPERIENCE.get(), 27 * UNIT_PER_MB)
		.require(Items.HONEYCOMB)
		.output(AllBlocks.EXPERIENCE_BLOCK.get())),

		EXPERIENCE_BLOCK_FROM_SLIME_BALL = create(EnchantmentIndustry.genRL("experience_block_from_slime_ball"), b -> b.require(CeiFluids.EXPERIENCE.get(), 27 * UNIT_PER_MB)
			.require(Items.SLIME_BALL)
			.output(AllBlocks.EXPERIENCE_BLOCK.get()))
	;

	public CompactingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
