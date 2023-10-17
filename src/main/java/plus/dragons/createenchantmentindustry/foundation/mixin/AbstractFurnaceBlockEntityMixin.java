package plus.dragons.createenchantmentindustry.foundation.mixin;

import java.util.ArrayList;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract public class AbstractFurnaceBlockEntityMixin<T> extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, SidedStorageBlockEntity {
	private static final FluidVariant EXPERIENCE = FluidVariant.of(CeiFluids.EXPERIENCE.getSource());

	protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Shadow
	private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

	@Unique
	public final SingleVariantStorage<FluidVariant> experienceStorage = new SingleVariantStorage<FluidVariant>() {
		@Override
		protected void onFinalCommit() {
			EnchantmentIndustry.LOGGER.debug("Furnace XP onFinalCommit");
			//markDirty();
		}

		@Override
		protected FluidVariant getBlankVariant() {
			return FluidVariant.blank();
		}

		@Override
		protected long getCapacity(FluidVariant variant) {
			return java.lang.Long.MAX_VALUE;
		}

		@Override
		public boolean isResourceBlank() {
			EnchantmentIndustry.LOGGER.debug("Furnace XP isResourceBlank");
			return getResource().isBlank();
		}

		@Override
		public FluidVariant getResource() {
			EnchantmentIndustry.LOGGER.debug("Furnace XP getResource");
			return EXPERIENCE;
		}

		@Override
		protected boolean canInsert(FluidVariant variant) {
			EnchantmentIndustry.LOGGER.debug("Furnace XP canInsert");
			return false;
		}

		@Override
		protected boolean canExtract(FluidVariant variant) {
			EnchantmentIndustry.LOGGER.debug("Furnace canExtract");
			return (variant.equals(EXPERIENCE) && getAmount() > 0);
		}

		@Override
		public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			var extractedFluid = extractedVariant.getFluid();
			EnchantmentIndustry.LOGGER.debug("Furnace XP extracting {} of {}", maxAmount, extractedFluid.getClass().getSimpleName());

			if (extractedVariant.isBlank() || maxAmount <= 0) {
				EnchantmentIndustry.LOGGER.debug("Furnace XP extracting nowt");
				return 0;
			}

			if (canExtract(extractedVariant)) {
				long extractedAmount = Math.min(maxAmount, getAmount());
				EnchantmentIndustry.LOGGER.debug("Furnace XP extracting");

				if (extractedAmount > 0) {
					//updateSnapshots(transaction);
					extractedAmount = drain(extractedAmount);

					if (getAmount() == 0) {
						variant = getBlankVariant();
					}

					return extractedAmount;
				}
			}

			return 0;
		}

		@Override
		public long getAmount() {
			AtomicDouble result = new AtomicDouble(0);
			for (var entry : recipesUsed.object2IntEntrySet()) {
				getLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
					result.addAndGet(((AbstractCookingRecipe) recipe).getExperience() * entry.getIntValue());
				});
			}
			EnchantmentIndustry.LOGGER.debug("Furnace XP amount: {}", result.floatValue());
			return (int)Math.floor(result.floatValue());
		}

		private long drain(long maxDrain) {
			long total = getAmount();

			EnchantmentIndustry.LOGGER.debug("Furnace XP draining {} from total of {}", maxDrain, total);

			if (maxDrain <= total) {
				recipesUsed.clear();
			}

			ArrayList<Recipe<?>> allRecipes = new ArrayList<>();
			for (var entry : recipesUsed.object2IntEntrySet()) {
				getLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
					for(int i = 0; i < entry.getIntValue();i++){
						allRecipes.add(recipe);
					}
				});
			}

			var done = false;
			var result = 0;
			for (var recipe: allRecipes){
				if (done) {
					setRecipeUsed(recipe);
				} else {
					var exp = ((AbstractCookingRecipe) recipe).getExperience();
					if (exp <= maxDrain - result) {
						result += exp;
					} else {
						done = true;
						recipesUsed.clear();
					}
				}
			}

			return result;
		}
	};

    @Override
    public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
        if (side.getAxis().isHorizontal()) {
            return experienceStorage;
        }
        return null;
    }
}
