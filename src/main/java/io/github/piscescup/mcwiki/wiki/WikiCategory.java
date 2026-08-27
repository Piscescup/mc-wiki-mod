package io.github.piscescup.mcwiki.wiki;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public enum WikiCategory {
	TRADE("trade"),
	BREWING("brewing"),
	ENCHANTING("enchanting"),
	MOBS("mobs"),
	BLOCKS("blocks"),
	ITEMS("items"),
	BIOME("biome"),
	STATUS_EFFECTS("status_effects"),
	CRAFTING("crafting"),
	SMELTING("smelting"),
	SMITHING("smithing"),
	STRUCTURES("structures"),
	REDSTONE("redstone"),
	COMMANDS("commands"),
	VERSION_HISTORY("version_history"),
	TUTORIALS("tutorials");

	private final String id;

	WikiCategory(String id) {
		this.id = id;
	}

	public String id() {
		return this.id;
	}

	public Component displayName(WikiLanguageConfig language) {
		return WikiTranslations.component(language, translationKey());
	}

	public String searchTerm(WikiLanguageConfig language) {
		return WikiTranslations.text(language, translationKey());
	}

	private String translationKey() {
		return "option.mc_wiki.category." + this.id;
	}

	public static Optional<WikiCategory> fromId(String id) {
		String normalized = id.toLowerCase(Locale.ROOT);
		for (WikiCategory category : values()) {
			if (category.id.equals(normalized)) {
				return Optional.of(category);
			}
		}

		return Optional.empty();
	}

	public static CompletableFuture<Suggestions> suggestCategories(
		CommandContext<FabricClientCommandSource> context,
		SuggestionsBuilder builder
	) {
		Arrays.stream(WikiCategory.values())
			.map(WikiCategory::id)
			.forEach(builder::suggest);
		return builder.buildFuture();
	}
}
