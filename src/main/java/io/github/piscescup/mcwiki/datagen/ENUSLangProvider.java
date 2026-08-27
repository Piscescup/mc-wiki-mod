package io.github.piscescup.mcwiki.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ENUSLangProvider extends FabricLanguageProvider {
	public ENUSLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, "en_us", registryLookup);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
		builder.add("gui.mc_wiki.title", "Minecraft Wiki");
		builder.add("command.mc_wiki.usage", "Usage: /mc-wiki <category> <query>");
		builder.add("command.mc_wiki.invalid_category", "Unknown category: %s");
		builder.add("command.mc_wiki.invalid_language", "Unknown language: %s");
		builder.add("command.mc_wiki.searching", "Searching Minecraft Wiki for %s...");
		builder.add("command.mc_wiki.opening_result", "Opening Minecraft Wiki article: %s");
		builder.add("command.mc_wiki.no_results", "No matching Minecraft Wiki article was found.");
		builder.add("command.mc_wiki.api_failed", "MediaWiki API request failed; unable to open the wiki.");
		builder.add("command.mc_wiki.lang_current", "Current wiki language: %s");
		builder.add("command.mc_wiki.lang_updated", "Wiki language set to %s");
		builder.add("gui.mc_wiki.search", "Search: %s");
		builder.add("gui.mc_wiki.footer_hint", "Esc close  Mouse wheel scroll");
		builder.add("gui.mc_wiki.loading", "Preparing built-in browser");
		builder.add("gui.mc_wiki.error", "Browser initialization failed");
		builder.add("gui.mc_wiki.progress.not_started", "Waiting to initialize");
		builder.add("gui.mc_wiki.progress.downloading", "Downloading JCEF");
		builder.add("gui.mc_wiki.progress.extracting", "Extracting JCEF");
		builder.add("gui.mc_wiki.progress.install", "Installing browser runtime");
		builder.add("gui.mc_wiki.progress.initializing", "Starting Chromium");
		builder.add("gui.mc_wiki.progress.done", "Browser ready");
		builder.add("option.mc_wiki.language.en_us", "English (US)");
		builder.add("option.mc_wiki.language.zh_cn", "Chinese (Simplified)");
		builder.add("option.mc_wiki.category.trade", "Trade");
		builder.add("option.mc_wiki.category.brewing", "Brewing");
		builder.add("option.mc_wiki.category.enchanting", "Enchanting");
		builder.add("option.mc_wiki.category.mobs", "Mobs");
		builder.add("option.mc_wiki.category.blocks", "Blocks");
		builder.add("option.mc_wiki.category.items", "Items");
		builder.add("option.mc_wiki.category.biome", "Biomes");
		builder.add("option.mc_wiki.category.status_effects", "Status effects");
		builder.add("option.mc_wiki.category.crafting", "Crafting");
		builder.add("option.mc_wiki.category.smelting", "Smelting");
		builder.add("option.mc_wiki.category.smithing", "Smithing");
		builder.add("option.mc_wiki.category.structures", "Structures");
		builder.add("option.mc_wiki.category.redstone", "Redstone");
		builder.add("option.mc_wiki.category.commands", "Commands");
		builder.add("option.mc_wiki.category.version_history", "Version history");
		builder.add("option.mc_wiki.category.tutorials", "Tutorials");
	}
}
