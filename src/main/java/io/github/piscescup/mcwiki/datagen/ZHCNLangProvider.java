package io.github.piscescup.mcwiki.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ZHCNLangProvider extends FabricLanguageProvider {
	public ZHCNLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, "zh_cn", registryLookup);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
		builder.add("gui.mc_wiki.title", "Minecraft Wiki");
		builder.add("command.mc_wiki.usage", "用法: /mc-wiki <category> <query>");
		builder.add("command.mc_wiki.invalid_category", "未知分类: %s");
		builder.add("command.mc_wiki.invalid_language", "未知语言: %s");
		builder.add("command.mc_wiki.searching", "正在 Minecraft Wiki 中搜索 %s...");
		builder.add("command.mc_wiki.opening_result", "正在打开 Minecraft Wiki 条目: %s");
		builder.add("command.mc_wiki.no_results", "未找到匹配的 Minecraft Wiki 条目。");
		builder.add("command.mc_wiki.api_failed", "MediaWiki API 请求失败，无法打开 Wiki。");
		builder.add("command.mc_wiki.lang_current", "当前 Wiki 语言: %s");
		builder.add("command.mc_wiki.lang_updated", "Wiki 语言已设置为 %s");
		builder.add("gui.mc_wiki.search", "搜索: %s");
		builder.add("gui.mc_wiki.footer_hint", "Esc 关闭  鼠标滚轮滚动页面");
		builder.add("gui.mc_wiki.loading", "正在准备内置浏览器");
		builder.add("gui.mc_wiki.error", "浏览器初始化失败");
		builder.add("gui.mc_wiki.progress.not_started", "等待初始化");
		builder.add("gui.mc_wiki.progress.downloading", "正在下载 JCEF");
		builder.add("gui.mc_wiki.progress.extracting", "正在解压 JCEF");
		builder.add("gui.mc_wiki.progress.install", "正在安装浏览器运行时");
		builder.add("gui.mc_wiki.progress.initializing", "正在启动 Chromium");
		builder.add("gui.mc_wiki.progress.done", "浏览器已就绪");
		builder.add("option.mc_wiki.language.en_us", "英语（美国）");
		builder.add("option.mc_wiki.language.zh_cn", "简体中文");
		builder.add("option.mc_wiki.category.trade", "交易");
		builder.add("option.mc_wiki.category.brewing", "酿造");
		builder.add("option.mc_wiki.category.enchanting", "附魔");
		builder.add("option.mc_wiki.category.mobs", "生物");
		builder.add("option.mc_wiki.category.blocks", "方块");
		builder.add("option.mc_wiki.category.items", "物品");
		builder.add("option.mc_wiki.category.biome", "生物群系");
		builder.add("option.mc_wiki.category.status_effects", "状态效果");
		builder.add("option.mc_wiki.category.crafting", "合成");
		builder.add("option.mc_wiki.category.smelting", "烧炼");
		builder.add("option.mc_wiki.category.smithing", "锻造");
		builder.add("option.mc_wiki.category.structures", "结构");
		builder.add("option.mc_wiki.category.redstone", "红石");
		builder.add("option.mc_wiki.category.commands", "命令");
		builder.add("option.mc_wiki.category.version_history", "版本记录");
		builder.add("option.mc_wiki.category.tutorials", "教程");
	}
}
