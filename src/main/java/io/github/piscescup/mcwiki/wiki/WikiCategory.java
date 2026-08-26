package io.github.piscescup.mcwiki.wiki;

import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Optional;

public enum WikiCategory {
	TRADE("trade", "trade", "交易"),
	BREWING("brewing", "brewing", "酿造"),
	ENCHANTING("enchanting", "enchanting", "附魔"),
	MOBS("mobs", "mobs", "生物"),
	BLOCKS("blocks", "blocks", "方块"),
	ITEMS("items", "items", "物品"),
	MOB_ECOLOGY("mob_ecology", "mob ecology", "生物群系"),
	STATUS_EFFECTS("status_effects", "status effects", "状态效果"),
	CRAFTING("crafting", "crafting", "合成"),
	SMELTING("smelting", "smelting", "烧炼"),
	SMITHING("smithing", "smithing", "锻造"),
	STRUCTURES("structures", "structures", "结构"),
	REDSTONE("redstone", "redstone", "红石"),
	COMMANDS("commands", "commands", "命令"),
	VERSION_HISTORY("version_history", "version history", "版本记录"),
	TUTORIALS("tutorials", "tutorials", "教程");

	private final String id;
	private final String englishSearchTerm;
	private final String chineseSearchTerm;

	WikiCategory(String id, String englishSearchTerm, String chineseSearchTerm) {
		this.id = id;
		this.englishSearchTerm = englishSearchTerm;
		this.chineseSearchTerm = chineseSearchTerm;
	}

	public String id() {
		return this.id;
	}

	public Component displayName(WikiLanguageConfig language) {
		return WikiTranslations.component(language, "option.mc_wiki.category." + this.id);
	}

	public String searchTerm(WikiLanguageConfig language) {
		return language == WikiLanguageConfig.EN_US ? this.englishSearchTerm : this.chineseSearchTerm;
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
}
