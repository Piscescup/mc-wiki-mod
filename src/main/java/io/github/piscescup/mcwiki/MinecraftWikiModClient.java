package io.github.piscescup.mcwiki;

import io.github.piscescup.mcwiki.commands.MinecraftWikiCommand;
import net.dimaskama.mcef.api.MCEFApi;
import net.fabricmc.api.ClientModInitializer;

public class MinecraftWikiModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MCEFApi.initialize();
		MinecraftWikiCommand.register();
	}
}
