package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class HyperExperienceBottle extends ThrowableItemProjectile {
    public HyperExperienceBottle(EntityType<? extends HyperExperienceBottle> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HyperExperienceBottle(double pX, double pY, double pZ, Level pLevel) {
        super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pX, pY, pZ, pLevel);
    }

    public HyperExperienceBottle(LivingEntity pShooter, Level pLevel) {
        super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pShooter, pLevel);
    }


    @Override
    protected Item getDefaultItem() {
        return CeiItems.HYPER_EXP_BOTTLE.get();
    }

    @SuppressWarnings("unchecked")
    public static FabricEntityTypeBuilder<?> build(FabricEntityTypeBuilder<?> builder) {
        return builder.dimensions(EntityDimensions.fixed(.25f, .25f));
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    protected float getGravity() {
        return 0.07F;
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
		Level level = this.getCommandSenderWorld();
        if (level instanceof ServerLevel) {
			level.levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));
            int amount = 3 + level.random.nextInt(5) + level.random.nextInt(5);
            CeiFluids.HYPER_EXPERIENCE.get().drop((ServerLevel) level, this.position(), amount);
            this.discard();
        }
    }

}
