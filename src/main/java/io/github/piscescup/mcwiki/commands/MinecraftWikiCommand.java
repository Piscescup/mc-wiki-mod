package io.github.piscescup.mcwiki.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.piscescup.mcwiki.config.Settings;
import io.github.piscescup.mcwiki.gui.MinecraftWikiScreen;
import io.github.piscescup.mcwiki.wiki.MediaWikiApiClient;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class MinecraftWikiCommand {
	private static volatile PendingWikiScreen pendingScreen;

	private MinecraftWikiCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			LiteralCommandNode<FabricClientCommandSource> mcWikiCommand = dispatcher.register(createCommand("mc-wiki"));
			dispatcher.register(literal("mcwiki")
				.requires(FabricClientCommandSource::attended)
				.redirect(mcWikiCommand));
			dispatcher.register(literal("wiki")
				.requires(FabricClientCommandSource::attended)
				.redirect(mcWikiCommand));
		});
		ClientTickEvents.END_CLIENT_TICK.register(MinecraftWikiCommand::openPendingScreen);
	}

	private static void openPendingScreen(Minecraft client) {
		if (pendingScreen == null) {
			return;
		}

		PendingWikiScreen request = pendingScreen;
		pendingScreen = null;
		client.gui.setScreen(new MinecraftWikiScreen(request.category(), request.query(), request.language(), request.url()));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String name) {
		return literal(name)
			.requires(FabricClientCommandSource::attended)
			.executes(MinecraftWikiCommand::sendUsage)
			.then(MinecraftWikiSettingsCommands.createSettingsCommand())
			.then(argument("category", StringArgumentType.word())
				.suggests(MinecraftWikiCommand::suggestCategories)
				.then(argument("query", StringArgumentType.greedyString())
					.executes(MinecraftWikiCommand::openWiki)));
	}

	private static int sendUsage(CommandContext<FabricClientCommandSource> context) {
		context.getSource().sendError(WikiTranslations.component(
			MinecraftWikiSettingsCommands.language(),
			"command.mc_wiki.usage"
		));
		return 0;
	}

	private static int openWiki(CommandContext<FabricClientCommandSource> context) {
		String categoryId = StringArgumentType.getString(context, "category");
		WikiCategory category = WikiCategory.fromId(categoryId).orElse(null);
		if (category == null) {
			context.getSource().sendError(WikiTranslations.component(
				MinecraftWikiSettingsCommands.language(),
				"command.mc_wiki.invalid_category",
				categoryId
			));
			return 0;
		}

		return openWiki(context.getSource(), category, StringArgumentType.getString(context, "query"));
	}

	private static int openWiki(FabricClientCommandSource source, WikiCategory category, String rawQuery) {
		String query = rawQuery.trim();
		if (query.isEmpty()) {
			source.sendError(WikiTranslations.component(
				MinecraftWikiSettingsCommands.language(),
				"command.mc_wiki.usage"
			));
			return 0;
		}

		WikiLanguageConfig language = Settings.currentLang();
		source.sendFeedback(WikiTranslations.component(
			language,
			"command.mc_wiki.searching",
			query
		));
		MediaWikiApiClient.search(language, category, query).whenComplete((result, throwable) -> {
			Minecraft client = source.getClient();
			client.execute(() -> completeSearch(source, category, query, language, result, throwable));
		});
		return 1;
	}

	private static void completeSearch(
		FabricClientCommandSource source,
		WikiCategory category,
		String query,
		WikiLanguageConfig language,
		Optional<MediaWikiApiClient.SearchResult> result,
		Throwable throwable
	) {
		if (throwable != null) {
			pendingScreen = new PendingWikiScreen(category, query, language, buildSearchUrl(category, query, language));
			source.sendError(WikiTranslations.component(language, "command.mc_wiki.api_failed"));
			return;
		}

		if (result.isEmpty()) {
			pendingScreen = new PendingWikiScreen(category, query, language, buildSearchUrl(category, query, language));
			source.sendFeedback(WikiTranslations.component(language, "command.mc_wiki.no_results"));
			return;
		}

		MediaWikiApiClient.SearchResult searchResult = result.get();
		pendingScreen = new PendingWikiScreen(category, query, language, searchResult.url());
		source.sendFeedback(WikiTranslations.component(
			language,
			"command.mc_wiki.opening_result",
			searchResult.title()
		));
	}

	private static String buildSearchUrl(WikiCategory category, String query, WikiLanguageConfig language) {
		String searchUrl = "https://" + language.host() + "/w/Special:Search?search=%s";
		String searchText = category.searchTerm(language) + " " + query;
		return searchUrl.formatted(URLEncoder.encode(searchText, StandardCharsets.UTF_8));
	}

	private static CompletableFuture<Suggestions> suggestCategories(
		CommandContext<FabricClientCommandSource> context,
		SuggestionsBuilder builder
	) {
		Arrays.stream(WikiCategory.values()).forEach(category -> builder.suggest(category.id()));
		return builder.buildFuture();
	}

	private record PendingWikiScreen(WikiCategory category, String query, WikiLanguageConfig language, String url) {
	}
}
