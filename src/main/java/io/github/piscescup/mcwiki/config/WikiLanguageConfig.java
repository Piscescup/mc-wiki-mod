package io.github.piscescup.mcwiki.config;

import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum WikiLanguageConfig implements ConfigValue {
	ZH_CN("zh_cn", "zh.minecraft.wiki"),
	EN_US("en_us", "minecraft.wiki");

	public static final String LANG_CONF_KEY = "wikiLanguage";

	private final String id;
	private final String host;

	WikiLanguageConfig(String id, String host) {
		this.id = id;
		this.host = host;
	}

	@Override
	public @NotNull String configKey() {
		return LANG_CONF_KEY;
	}

	@Override
	public @NotNull String configValue() {
		return this.id;
	}

	@Override
	public @NotNull String configTranslationKey() {
		return "option.mc_wiki.language." + this.id;
	}

	public String host() {
		return this.host;
	}

	public Component displayName(WikiLanguageConfig language) {
		return WikiTranslations.component(language, configTranslationKey());
	}

	public static WikiLanguageConfig defaultLanguage() {
		return EN_US;
	}

	public static Optional<WikiLanguageConfig> fromId(String id) {
		String normalized = id.toLowerCase(Locale.ROOT);
		for (WikiLanguageConfig language : values()) {
			if (language.id.equals(normalized)) {
				return Optional.of(language);
			}
		}

		return Optional.empty();
	}

	public static List<String> langSuggestions() {
		return Arrays.stream(values())
			.map(WikiLanguageConfig::configValue)
			.toList();
	}
}
