package io.github.piscescup.mcwiki;

import io.github.piscescup.mcwiki.commands.MinecraftWikiCommand;
import io.github.piscescup.mcwiki.gui.GUIInitializer;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.dimaskama.mcef.api.MCEFApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MinecraftWikiModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WikiTranslations.register();

		MCEFApi.initialize();

		ClientCommandRegistrationCallback.EVENT.register(MinecraftWikiCommand::registerCommands);
		ClientTickEvents.END_CLIENT_TICK.register(GUIInitializer::openPendingScreen);
	}
}
