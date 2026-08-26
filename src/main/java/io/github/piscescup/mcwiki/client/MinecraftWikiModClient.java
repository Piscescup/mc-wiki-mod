package io.github.piscescup.mcwiki.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.dimaskama.mcef.api.MCEFApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class MinecraftWikiModClient implements ClientModInitializer {
	private static final String WIKI_SEARCH_URL = "https://zh.minecraft.wiki/w/Special:Search?search=%s";

	@Override
	public void onInitializeClient() {
		MCEFApi.initialize();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			dispatcher.register(createCommand("mcwiki"));
			dispatcher.register(createCommand("wiki"));
		});
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String name) {
		return literal(name)
			.requires(FabricClientCommandSource::attended)
			.then(argument("query", StringArgumentType.greedyString())
				.executes(context -> openWiki(context.getSource(), StringArgumentType.getString(context, "query"))));
	}

	private static int openWiki(FabricClientCommandSource source, String rawQuery) {
		String query = rawQuery.trim();
		if (query.isEmpty()) {
			source.sendError(Component.literal("用法: /mcwiki <要搜索的内容>"));
			return 0;
		}

		Minecraft client = source.getClient();
		client.gui.setScreen(new MinecraftWikiScreen(query, buildSearchUrl(query)));
		source.sendFeedback(Component.literal("正在打开 Minecraft Wiki: " + query));
		return 1;
	}

	private static String buildSearchUrl(String query) {
		return WIKI_SEARCH_URL.formatted(URLEncoder.encode(query, StandardCharsets.UTF_8));
	}
}
