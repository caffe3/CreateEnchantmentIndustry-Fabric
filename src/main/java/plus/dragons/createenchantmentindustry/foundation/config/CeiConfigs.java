package plus.dragons.createenchantmentindustry.foundation.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	private static CClient client;
	private static CServer server;

	public static CClient client() {
		return client;
	}

	public static CServer server() {
		return server;
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static void register() {
		client = register(CClient::new, ModConfig.Type.CLIENT);
		server = register(CServer::new, ModConfig.Type.SERVER);

		for (Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			ForgeConfigRegistry.INSTANCE.register(EnchantmentIndustry.ID, pair.getKey(), pair.getValue().specification);

		ModConfigEvents.loading(EnchantmentIndustry.ID).register(CeiConfigs::onLoad);
		ModConfigEvents.reloading(EnchantmentIndustry.ID).register(CeiConfigs::onReload);
	}

	public static void onLoad(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
					.getSpec())
				config.onLoad();
	}

	public static void onReload(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
					.getSpec())
				config.onReload();
	}

	public static BaseConfigScreen createConfigScreen(Screen parent) {
		BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.ID, (base) ->
				base.withSpecs(client.specification, null, server.specification)
						.withTitles("Client Config", null, "Server Config")
		);
		return new BaseConfigScreen(parent, EnchantmentIndustry.ID);
	}

	public static BaseConfigScreen createConfigScreen(@Nullable Minecraft mc, Screen parent) {
		return createConfigScreen(parent);
	}
}
