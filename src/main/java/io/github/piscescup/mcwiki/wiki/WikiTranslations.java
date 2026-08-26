package io.github.piscescup.mcwiki.wiki;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;

import static io.github.piscescup.mcwiki.References.MOD_LOGGER;

public final class WikiTranslations {
	private static final Map<WikiLanguageConfig, Map<String, String>> TRANSLATIONS = loadTranslations();

	private WikiTranslations() {
	}

	public static Component component(WikiLanguageConfig language, String key, Object... arguments) {
		return Component.literal(text(language, key, arguments));
	}

	public static String text(WikiLanguageConfig language, String key, Object... arguments) {
		String template = translations(language).get(key);
		if (template == null) {
			template = translations(WikiLanguageConfig.EN_US).getOrDefault(key, key);
		}

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

	private static Map<String, String> translations(WikiLanguageConfig language) {
		return TRANSLATIONS.getOrDefault(language, Map.of());
	}

	private static Map<WikiLanguageConfig, Map<String, String>> loadTranslations() {
		Map<WikiLanguageConfig, Map<String, String>> translations = new EnumMap<>(WikiLanguageConfig.class);
		for (WikiLanguageConfig language : WikiLanguageConfig.values()) {
			translations.put(language, loadLanguage(language));
		}
		return Map.copyOf(translations);
	}

	private static Map<String, String> loadLanguage(WikiLanguageConfig language) {
		String resourcePath = "assets/mc-wiki/lang/" + language.configValue() + ".json";
		try (InputStream inputStream = WikiTranslations.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				MOD_LOGGER.warn("Missing Minecraft Wiki translation resource: {}", resourcePath);
				return Map.of();
			}

			JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
			Map<String, String> languageTranslations = new java.util.HashMap<>();
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				languageTranslations.put(entry.getKey(), entry.getValue().getAsString());
			}
			return Map.copyOf(languageTranslations);
		} catch (IOException | RuntimeException exception) {
			MOD_LOGGER.warn("Failed to load Minecraft Wiki translation resource: {}", resourcePath, exception);
			return Map.of();
		}
	}
}
