package plus.dragons.createenchantmentindustry.foundation.events;

import com.mojang.blaze3d.shaders.FogShape;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.Level;
import net.minecraft.client.renderer.FogRenderer;

import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents.ColorData;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class ClientEvents {
	public static void register(){
		FogEvents.RENDER_FOG.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);
	}

	public static boolean getFogDensity(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, FogShape shape, FogEvents.FogData fogData) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = camera.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);

		if (camera.getPosition().y >= blockPos.getY() + fluidState.getHeight(level, blockPos))
			return false;

		Fluid fluid = fluidState.getType();
		Entity entity = camera.getEntity();

		if (CeiFluids.INK.get()
			.isSame(fluid)) {
			fogData.scaleFarPlaneDistance(1f / 8f * CeiConfigs.client().inkTransparencyMultiplier.getF());
			EnchantmentIndustry.LOGGER.debug("ink fogType: {}", type.name());
			return true;
		}

		return false;
	}

	public static void getFogColor(ColorData event, float partialTicks) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (CeiFluids.INK.get()
			.isSame(fluid)) {
			event.setRed(7f / 255f);
			event.setGreen(0f / 255f);
			event.setBlue(31f / 255f);
			return;
		}
	}
}
