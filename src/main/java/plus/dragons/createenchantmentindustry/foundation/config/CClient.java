package plus.dragons.createenchantmentindustry.foundation.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CClient extends ConfigBase {
	public final ConfigGroup client = group(0, "client",
			Comments.client);

	public final ConfigGroup fluidFogSettings = group(1, "fluidFogSettings", Comments.fluidFogSettings);
	public final ConfigFloat inkTransparencyMultiplier =
		f(1, .125f, 256, "ink", Comments.inkTransparencyMultiplier);

	@Override
	public String getName() {
		return "client";
	}

	private static class Comments {
		static String client = "Client-only settings - If you're looking for general settings, look inside your worlds serverconfig folder!";
		static String fluidFogSettings = "Configure your vision range when submerged in CEI's custom fluids";
		static String inkTransparencyMultiplier = "The vision range through ink will be multiplied by this factor";
	}
}
