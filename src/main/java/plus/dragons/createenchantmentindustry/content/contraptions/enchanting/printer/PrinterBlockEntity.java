package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;
import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;
import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createdragonlib.mixin.AdvancementBehaviourAccessor;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiTriggers;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class PrinterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, SidedStorageBlockEntity {

    public static final int COPYING_TIME = 100;
    protected BeltProcessingBehaviour beltProcessing;
    public int processingTicks;
    SmartFluidTankBehaviour tank;
    private ItemStack copyTarget;
    public boolean tooExpensive;
    public PrintEntry printEntry;
    boolean sendParticles;
    LazyOptional<PrinterTargetItemHandler> itemHandler = LazyOptional.of(()->new PrinterTargetItemHandler(this));

	SnapshotParticipant<ItemStack> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected ItemStack createSnapshot() {
			return getCopyTarget();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			setCopyTarget(snapshot == ItemStack.EMPTY ? null : snapshot);
		}

		@Override
		protected void onFinalCommit() {
			notifyUpdate();
		}
	};

    public PrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        processingTicks = -1;
        copyTarget = null;
        tooExpensive = false;
    }

    @Override
    @SuppressWarnings("deprecation") //Fluid Tags are still useful for mod interaction
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(tank = FilteringFluidTankBehaviour
            .single(fluidStack -> fluidStack.getFluid().is(CeiTags.FluidTag.PRINTER_INPUT.tag),
                this, CeiConfigs.server().copierTankCapacity.get() * UNIT_PER_MB));
        behaviours.add(beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
                .whileItemHeld(this::whenItemHeld));
        registerAwardables(behaviours,
                CeiAdvancements.COPIABLE_MASTERPIECE.asCreateAdvancement(),
                CeiAdvancements.COPIABLE_MYSTERY.asCreateAdvancement(),
                CeiAdvancements.RELIC_RESTORATION.asCreateAdvancement(),
                CeiAdvancements.EMERGING_BRAND.asCreateAdvancement());
    }

    public void tick() {
        super.tick();

        if (processingTicks >= 0) {
            processingTicks--;
        }
    }

    public ItemStack getCopyTarget() {
        if(copyTarget==null) return ItemStack.EMPTY;
        return copyTarget;
    }

    public void setCopyTarget(@NotNull ItemStack copyTarget) {
        if(copyTarget.isEmpty()) {
            this.copyTarget = null;
            tooExpensive = false;
            printEntry = null;
        }
        else {
            this.copyTarget = copyTarget;
            matchPrintEntry(copyTarget);
            tooExpensive = Printing.isTooExpensive(printEntry, copyTarget, CeiConfigs.server().copierTankCapacity.get() * UNIT_PER_MB);
        }
        processingTicks = -1;
        notifyUpdate();
    }

    private void matchPrintEntry(ItemStack copyTarget){
        var entry = Printing.match(copyTarget);
        if(entry==null){
            // Happen when mod that contains a PrintEntry is removed.
            this.copyTarget = null;
            tooExpensive = false;
        }
        printEntry = entry;
    }

    protected static int ENCHANT_PARTICLE_COUNT = 20;

    protected void spawnParticles() {
        if (isVirtual())
            return;
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        vec = vec.subtract(0, 11 / 16f, 0);
        ParticleOptions particle = ParticleTypes.ENCHANT;
        for (int i = 0; i < ENCHANT_PARTICLE_COUNT; i++) {
            Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1f);
            m = new Vec3(m.x, Math.abs(m.y), m.z);
            level.addAlwaysVisibleParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
        level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, level.random.nextFloat() * .1f + .9f, true);
    }

    protected BeltProcessingBehaviour.ProcessingResult onItemReceived(TransportedItemStack transported,
                                                                      TransportedItemStackHandlerBehaviour handler) {
        if (handler.blockEntity.isVirtual())
            return PASS;
        if (tooExpensive || copyTarget == null)
            return PASS;
        if (!Printing.valid(printEntry,copyTarget,transported.stack))
            return PASS;
        if (tank.isEmpty() || Printing.isCorrectInk(printEntry, getCurrentFluidInTank(), copyTarget))
            return HOLD;
        if (Printing.getRequiredAmountForItem(printEntry,copyTarget) == -1)
            return PASS;
        return HOLD;
    }

    protected BeltProcessingBehaviour.ProcessingResult whenItemHeld(TransportedItemStack transported,
                                                                    TransportedItemStackHandlerBehaviour handler) {
        if (processingTicks != -1 && processingTicks != 10)
            return HOLD;
        if (tooExpensive || copyTarget == null)
            return PASS;
        if (!Printing.valid(printEntry, copyTarget,transported.stack))
            return PASS;
        if (tank.isEmpty() || !Printing.isCorrectInk(printEntry, getCurrentFluidInTank(), copyTarget))
            return HOLD;
        FluidStack fluid = getCurrentFluidInTank();
        int requiredAmountForItem = Printing.getRequiredAmountForItem(printEntry, copyTarget);
        if (requiredAmountForItem == -1)
            return PASS;
        if (requiredAmountForItem > fluid.getAmount())
            return HOLD;

        if (processingTicks == -1) {
            processingTicks = COPYING_TIME;
            notifyUpdate();
            return HOLD;
        }

        // Award Advancement
        var item = copyTarget.copy();
        if (!level.isClientSide()) {
            if (item.is(Items.WRITTEN_BOOK)) {
                award(CeiAdvancements.COPIABLE_MASTERPIECE.asCreateAdvancement());
                if (item.getOrCreateTag().getInt("generation") == 3)
                    award(CeiAdvancements.RELIC_RESTORATION.asCreateAdvancement());
            } else if(item.is(Items.ENCHANTED_BOOK))
                award(CeiAdvancements.COPIABLE_MYSTERY.asCreateAdvancement());
            else if(item.is(Items.NAME_TAG) && !transported.stack.is(Items.NAME_TAG))
                award(CeiAdvancements.EMERGING_BRAND.asCreateAdvancement());
            var advancementBehaviour = getBehaviour(AdvancementBehaviour.TYPE);
            var playerId = ((AdvancementBehaviourAccessor) advancementBehaviour).getPlayerId();
            if (playerId != null) {
                var player = level.getPlayerByUUID(playerId);
                if(player!=null)
                    CeiTriggers.BOOK_PRINTED.trigger(player, 1);
            }
        }

        // Process finished
        ItemStack copy = Printing.print(printEntry,copyTarget, requiredAmountForItem, transported.stack, fluid);
        List<TransportedItemStack> outList = new ArrayList<>();
        TransportedItemStack held = null;
        TransportedItemStack result = transported.copy();
        result.stack = copy;
        if (!transported.stack.isEmpty())
            held = transported.copy();
        outList.add(result);
        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, held));
        tank.getPrimaryHandler().setFluid(fluid);
        sendParticles = true;
        notifyUpdate();
        return HOLD;
    }

    private FluidStack getCurrentFluidInTank() {
        return tank.getPrimaryHandler()
                .getFluid();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level instanceof ServerLevel serverLevel) {
            ItemStack heldItemStack = copyTarget;
            var pos = getBlockPos();
            if(heldItemStack != null)
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
            var fluidStack = tank.getPrimaryHandler().getFluid();
            if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), (int) fluidStack.getAmount());
            }
        }
    }

    @Override
    protected void write(CompoundTag compoundTag, boolean clientPacket) {
        super.write(compoundTag, clientPacket);
        compoundTag.putInt("ProcessingTicks", processingTicks);
        compoundTag.putBoolean("tooExpensive", tooExpensive);
        if (copyTarget != null)
            compoundTag.put("copyTarget", NBTSerializer.serializeNBT(copyTarget));
        if (sendParticles && clientPacket) {
            compoundTag.putBoolean("SpawnParticles", true);
            sendParticles = false;
        }
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.putBoolean("tooExpensive", tooExpensive);
    }

    @Override
    protected void read(CompoundTag compoundTag, boolean clientPacket) {
        super.read(compoundTag, clientPacket);
        copyTarget = null;
        processingTicks = compoundTag.getInt("ProcessingTicks");
        tooExpensive = compoundTag.getBoolean("tooExpensive");
        if (compoundTag.contains("copyTarget")){
            copyTarget = ItemStack.of(compoundTag.getCompound("copyTarget"));
            matchPrintEntry(copyTarget);
        }
        if (!clientPacket)
            return;
        if (compoundTag.contains("SpawnParticles"))
            spawnParticles();

    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		if(side!=Direction.DOWN)
			return tank.getCapability();
		return null;
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return itemHandler.getValueUnsafer();
	}

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LANG.translate("gui.goggles.printer").forGoggles(tooltip);
        if (copyTarget == null) {
            LANG.translate("gui.goggles.printer.no_target")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        } else {
            printEntry.addToGoggleTooltip(tooltip,isPlayerSneaking,copyTarget);
        }
        containedFluidTooltip(tooltip, isPlayerSneaking, getFluidStorage(null));
        return true;
    }

}
