package io.github.piscescup.mcwiki;

import io.github.piscescup.mcwiki.commands.MinecraftWikiCommand;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.dimaskama.mcef.api.MCEFApi;
import net.fabricmc.api.ClientModInitializer;

public class MinecraftWikiModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WikiTranslations.register();
		MCEFApi.initialize();
		MinecraftWikiCommand.register();
	}
}
