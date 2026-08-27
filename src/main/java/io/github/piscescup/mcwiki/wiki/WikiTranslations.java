package io.github.piscescup.mcwiki.wiki;

import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.piscescup.mcwiki.References.MOD_LOGGER;
import static io.github.piscescup.mcwiki.References.ofPath;

public final class WikiTranslations {
	private static volatile Map<WikiLanguageConfig, ClientLanguage> translations = Map.of();

	private WikiTranslations() {
	}

	public static void register() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
			ofPath("wiki_translations"),
			(ResourceManagerReloadListener) WikiTranslations::reload
		);
	}

	public static Component component(WikiLanguageConfig language, String key, Object... arguments) {
		return Component.literal(text(language, key, arguments));
	}

	public static String text(WikiLanguageConfig language, String key, Object... arguments) {
		String template = translation(language, key);

		if (arguments.length == 0) {
			return template;
		}

		Object[] formattedArguments = Arrays.stream(arguments)
			.map(WikiTranslations::formatArgument)
			.toArray();
		try {
			return String.format(Locale.ROOT, template, formattedArguments);
		} catch (IllegalFormatException exception) {
			MOD_LOGGER.warn("Invalid Minecraft Wiki translation format for key {}", key, exception);
			return template;
		}
	}

	private static Object formatArgument(Object argument) {
		return argument instanceof Component component ? component.getString() : argument;
	}

	private static String translation(WikiLanguageConfig language, String key) {
		ClientLanguage selectedLanguage = translations.get(language);
		if (selectedLanguage != null && selectedLanguage.has(key)) {
			return selectedLanguage.getOrDefault(key, key);
		}

		ClientLanguage fallbackLanguage = translations.get(WikiLanguageConfig.defaultLanguage());
		return fallbackLanguage == null ? key : fallbackLanguage.getOrDefault(key, key);
	}

	private static void reload(ResourceManager resourceManager) {
		translations = loadTranslations(resourceManager);
	}

	private static Map<WikiLanguageConfig, ClientLanguage> loadTranslations(ResourceManager resourceManager) {
		Map<WikiLanguageConfig, ClientLanguage> translations = new EnumMap<>(WikiLanguageConfig.class);
		for (WikiLanguageConfig language : WikiLanguageConfig.values()) {
			translations.put(language, ClientLanguage.loadFrom(
				resourceManager,
				List.of(language.configValue()),
				false
			));
		}
		return Map.copyOf(translations);
	}
}
