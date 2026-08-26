package io.github.piscescup.mcwiki;

import net.fabricmc.api.ModInitializer;

import static io.github.piscescup.mcwiki.References.MOD_LOGGER;

public class MinecraftWikiMod implements ModInitializer {
	@Override
	public void onInitialize() {
		MOD_LOGGER.info("Minecraft Wiki mod initialized.");
	}
}
