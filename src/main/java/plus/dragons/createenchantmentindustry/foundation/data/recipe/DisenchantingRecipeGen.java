package plus.dragons.createenchantmentindustry.foundation.data.recipe;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;

import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class DisenchantingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	ENCHANTED_GOLDEN_APPLE = create(EnchantmentIndustry.genRL("enchanted_golden_apple"), b -> b.require(Items.ENCHANTED_GOLDEN_APPLE)
		.output(Items.GOLDEN_APPLE)
		.output(CeiFluids.EXPERIENCE.get(), 100 * UNIT_PER_MB)),

		EXPERIENCE_BLOCK = create(EnchantmentIndustry.genRL("experience_block"), b -> b.require(AllBlocks.EXPERIENCE_BLOCK)
		.output(CeiFluids.EXPERIENCE.get(), 27 * UNIT_PER_MB)),

		EXPERIENCE_NUGGET = create(EnchantmentIndustry.genRL("experience_nugget"), b -> b.require(AllItems.EXP_NUGGET)
		.output(CeiFluids.EXPERIENCE.get(), 3 * UNIT_PER_MB))
	;

	public DisenchantingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected CeiRecipeTypes getRecipeType() {
		return CeiRecipeTypes.DISENCHANTING;
	}

}
