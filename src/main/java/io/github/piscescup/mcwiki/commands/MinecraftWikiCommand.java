package io.github.piscescup.mcwiki.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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

import java.util.Optional;

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
		client.gui.setScreen(
			new MinecraftWikiScreen(
				request.category(), request.query(), request.language(), request.url()
			)
		);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String name) {
		return literal(name)
			.requires(FabricClientCommandSource::attended)
			.executes(MinecraftWikiCommand::sendUsage)
			.then(MinecraftWikiSettingsCommands.createSettingsCommand())
			.then(argument("category", StringArgumentType.word())
				.suggests(WikiCategory::suggestCategories)
				.then(argument("query", StringArgumentType.greedyString())
					.executes(MinecraftWikiCommand::openWiki)));
	}

	private static int sendUsage(CommandContext<FabricClientCommandSource> context) {
		context.getSource().sendError(WikiTranslations.component(
			Settings.currentLang(),
			"command.mc_wiki.usage"
		));
		return 0;
	}

	private static int openWiki(CommandContext<FabricClientCommandSource> context) {
		String categoryId = StringArgumentType.getString(context, "category");
		WikiCategory category = WikiCategory.fromId(categoryId).orElse(null);
		if (category == null) {
			context.getSource().sendError(WikiTranslations.component(
				Settings.currentLang(),
				"command.mc_wiki.invalid_category",
				categoryId
			));
			return 0;
		}

		return openWiki(context.getSource(), category, StringArgumentType.getString(context, "query"));
	}

	private static int openWiki(FabricClientCommandSource source, WikiCategory category, String rawQuery) {
		String query = rawQuery.trim();
		WikiLanguageConfig language = Settings.currentLang();

		if (query.isEmpty()) {
			source.sendError(WikiTranslations.component(
				language,
				"command.mc_wiki.usage"
			));
			return 0;
		}

		source.sendFeedback(WikiTranslations.component(
			language,
			"command.mc_wiki.searching",
			query
		));

		MediaWikiApiClient
			.search(language, category, query)
			.whenComplete((result, throwable) -> {
				Minecraft client = source.getClient();
				client.execute(
					() -> completeSearch(source, category, query, language, result, throwable)
				);
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
			source.sendError(WikiTranslations.component(language, "command.mc_wiki.api_failed"));
			return;
		}

		if (result.isEmpty()) {
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

	private record PendingWikiScreen(
		WikiCategory category,
		String query,
		WikiLanguageConfig language,
		String url
	) {
	}
}
