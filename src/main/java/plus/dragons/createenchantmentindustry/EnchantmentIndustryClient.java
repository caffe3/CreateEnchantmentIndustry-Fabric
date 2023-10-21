package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.config.ui.BaseConfigScreen;

import net.fabricmc.api.ClientModInitializer;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.events.ClientEvents;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

public class EnchantmentIndustryClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CeiBlockPartials.register();

		CeiPonderIndex.register();
		CeiPonderIndex.registerTags();

		ClientEvents.register();
	}
}
