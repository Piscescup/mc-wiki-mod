package io.github.piscescup.mcwiki.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.piscescup.mcwiki.config.Settings;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class MinecraftWikiSettingsCommands {
	private static final String SETTER = "set";

	private static final String GETTER = "get";

	private static final Settings SETTINGS = Settings.getInstance();

	public static final WikiLanguageConfig LANGUAGE = SETTINGS.get(
		WikiLanguageConfig.LANG_CONF_KEY,
		WikiLanguageConfig.class,
		WikiLanguageConfig.EN_US
	);

	public static final LiteralArgumentBuilder<FabricClientCommandSource> SETTING_ROOT =
		literal("settings")
			.requires(FabricClientCommandSource::attended);

	public static final LiteralArgumentBuilder<FabricClientCommandSource> LANG_SETTINGS =
		buildSettingCommands(
			"lang",
			WikiLanguageConfig.LANG_CONF_KEY,
			"language",
			WikiLanguageConfig.langSuggestions()
		);

	private MinecraftWikiSettingsCommands() {}

	public static LiteralArgumentBuilder<FabricClientCommandSource> createSettingsCommand() {
		return SETTING_ROOT
			.then(LANG_SETTINGS);
	}

	public static LiteralArgumentBuilder<FabricClientCommandSource> buildSettingCommands(
		String settingCommandName,
		String configKey,
		String argName,
		List<String> suggestions
	) {
		final var getter = literal(GETTER)
			.requires(FabricClientCommandSource::attended)
			.executes( context ->
				showConfig(context, configKey)
			);

		final var setter = literal(SETTER)
			.requires(FabricClientCommandSource::attended)
			.then(argument(argName, StringArgumentType.word())
				.suggests((context, builder) ->
					buildSuggestions(context, builder, suggestions)
				)
				.executes(context ->
					setConfig(context, configKey, argName)
				)
			);

		return literal(settingCommandName)
			.then(getter)
			.then(setter);
	}

	private static int showConfig(
		CommandContext<FabricClientCommandSource> context,
		String configKey
	) {
		Optional<String> config = SETTINGS.get(configKey);

		if (config.isEmpty()) {
			context.getSource().sendFeedback(WikiTranslations.component(
				LANGUAGE,
				"command.mc_wiki.unknown_config"
			));
		}

		context.getSource().sendFeedback(WikiTranslations.component(
			LANGUAGE,
			"command.mc_wiki.current_config",
			config.get()
		));
		return 1;
	}

	private static CompletableFuture<Suggestions> buildSuggestions(
		CommandContext<FabricClientCommandSource> context,
		SuggestionsBuilder builder,
		List<String> suggestions
	) {
		suggestions
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	private static int setConfig(
		CommandContext<FabricClientCommandSource> context,
		String configKey,
		String argName
	) {
		String newValue = StringArgumentType.getString(context, argName);


		SETTINGS.set(configKey, newValue);
		SETTINGS.save();
		context.getSource().sendFeedback(WikiTranslations.component(
			LANGUAGE,
			"command.mc_wiki.config_updated",
			newValue
		));
		return 1;
	}

}
