package io.github.piscescup.mcwiki.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.piscescup.mcwiki.config.ConfigValue;
import io.github.piscescup.mcwiki.config.Settings;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class MinecraftWikiSettingsCommands {
	private static final String SETTER = "set";

	private static final String GETTER = "get";

	private static final Settings SETTINGS = Settings.getInstance();

	private MinecraftWikiSettingsCommands() {}

	public static LiteralArgumentBuilder<FabricClientCommandSource> createSettingsCommand() {
		return literal("settings")
			.requires(FabricClientCommandSource::attended)
			.then(buildSettingCommands(
				"lang",
				WikiLanguageConfig.LANG_CONF_KEY,
				"language",
				WikiLanguageConfig.langSuggestions(),
				WikiLanguageConfig.class,
				WikiLanguageConfig.defaultLanguage(),
				WikiLanguageConfig::fromId,
				"command.mc_wiki.invalid_language",
				"command.mc_wiki.lang_current",
				"command.mc_wiki.lang_updated"
			));
	}

	private static <T extends Enum<T> & ConfigValue>
	LiteralArgumentBuilder<FabricClientCommandSource> buildSettingCommands(
		String settingCommandName,
		String configKey,
		String argName,
		List<String> suggestions,
		Class<T> valueType,
		T defaultValue,
		Function<String, Optional<T>> parser,
		String invalidTranslationKey,
		String currentTranslationKey,
		String updatedTranslationKey
	) {
		final var getter = literal(GETTER)
			.requires(FabricClientCommandSource::attended)
			.executes( context ->
				showConfig(context, configKey, valueType, defaultValue, currentTranslationKey)
			);

		final var setter = literal(SETTER)
			.requires(FabricClientCommandSource::attended)
			.then(argument(argName, StringArgumentType.word())
				.suggests((context, builder) ->
					buildSuggestions(builder, suggestions)
				)
				.executes(context ->
					setConfig(
						context,
						argName,
						parser,
						invalidTranslationKey,
						updatedTranslationKey
					)
				)
			);

		return literal(settingCommandName)
			.then(getter)
			.then(setter);
	}

	private static <T extends Enum<T> & ConfigValue> int showConfig(
		CommandContext<FabricClientCommandSource> context,
		String configKey,
		Class<T> valueType,
		T defaultValue,
		String translationKey
	) {
		T config = SETTINGS.get(configKey, valueType, defaultValue);

		context.getSource().sendFeedback(WikiTranslations.component(
			language(),
			translationKey,
			config.configValue()
		));
		return 1;
	}

	private static CompletableFuture<Suggestions> buildSuggestions(
		SuggestionsBuilder builder,
		List<String> suggestions
	) {
		suggestions
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	private static <T extends ConfigValue> int setConfig(
		CommandContext<FabricClientCommandSource> context,
		String argName,
		Function<String, Optional<T>> parser,
		String invalidTranslationKey,
		String updatedTranslationKey
	) {
		String newValue = StringArgumentType.getString(context, argName);
		Optional<T> parsedValue = parser.apply(newValue);
		if (parsedValue.isEmpty()) {
			context.getSource().sendError(WikiTranslations.component(
				language(),
				invalidTranslationKey,
				newValue
			));
			return 0;
		}

		T value = parsedValue.get();
		SETTINGS.set(value);
		SETTINGS.save();
		context.getSource().sendFeedback(WikiTranslations.component(
			language(),
			updatedTranslationKey,
			value.configValue()
		));
		return 1;
	}

	private static WikiLanguageConfig language() {
		return Settings.currentLang();
	}
}
