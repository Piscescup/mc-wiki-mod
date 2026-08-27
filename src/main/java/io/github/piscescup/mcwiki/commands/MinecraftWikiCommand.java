package io.github.piscescup.mcwiki.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.piscescup.mcwiki.config.Settings;
import io.github.piscescup.mcwiki.gui.GUIInitializer;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;


import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class MinecraftWikiCommand {


	private MinecraftWikiCommand() {
	}

	public static void registerCommands(
		CommandDispatcher<FabricClientCommandSource> dispatcher,
		CommandBuildContext context
	) {
		LiteralCommandNode<FabricClientCommandSource> mcWikiCommand =
			dispatcher.register(createCommand("mc-wiki"));

		dispatcher.register(literal("mcwiki")
			.requires(FabricClientCommandSource::attended)
			.redirect(mcWikiCommand));

		dispatcher.register(literal("wiki")
			.requires(FabricClientCommandSource::attended)
			.redirect(mcWikiCommand));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String name) {
		return literal(name)
			.requires(FabricClientCommandSource::attended)
			.executes(MinecraftWikiCommand::sendUsage)
			.then(MinecraftWikiSettingsCommands.createSettingsCommand())
			.then(argument("category", StringArgumentType.word())
				.suggests(WikiCategory::suggestCategories)
				.then(argument("query", StringArgumentType.greedyString())
					.executes(GUIInitializer::openWiki)));
	}

	private static int sendUsage(CommandContext<FabricClientCommandSource> context) {
		context.getSource().sendError(WikiTranslations.component(
			Settings.currentLang(),
			"command.mc_wiki.usage"
		));
		return 0;
	}


}
