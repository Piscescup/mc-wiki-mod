package io.github.piscescup.mcwiki.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static io.github.piscescup.mcwiki.References.MOD_LOGGER;

public final class Settings {
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
		.getConfigDir()
		.resolve("minecraft-wiki")
		.resolve("mc-wiki-client.properties");

	private static final Settings INSTANCE = load();

	private final Properties values;

	private Settings(Properties values) {
		this.values = values;
	}

	public static Settings getInstance() {
		return INSTANCE;
	}

	public synchronized Optional<String> get(String key) {
		return Optional.ofNullable(this.values.getProperty(requireKey(key)));
	}

	public synchronized String getOrDefault(String key, String defaultValue) {
		Objects.requireNonNull(defaultValue, "defaultValue");
		return this.values.getProperty(requireKey(key), defaultValue);
	}

	public synchronized <T extends Enum<T> & ConfigValue> T get(
		String key,
		Class<T> valueType,
		T defaultValue
	) {
		Objects.requireNonNull(valueType, "valueType");
		Objects.requireNonNull(defaultValue, "defaultValue");
		String checkedKey = requireKey(key);
		String rawValue = this.values.getProperty(checkedKey);
		if (rawValue == null) {
			return defaultValue;
		}

		for (T candidate : valueType.getEnumConstants()) {
			if (candidate.configKey().equals(checkedKey) && candidate.configValue().equals(rawValue)) {
				return candidate;
			}
		}
		return defaultValue;
	}

	public synchronized void set(String key, String value) {
		this.values.setProperty(requireKey(key), Objects.requireNonNull(value, "value"));
	}

	public synchronized void set(ConfigValue value) {
		Objects.requireNonNull(value, "value");
		set(value.configKey(), value.configValue());
	}

	public synchronized void remove(String key) {
		this.values.remove(requireKey(key));
	}

	public synchronized void save() {
		Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (OutputStream outputStream = Files.newOutputStream(temporaryPath)) {
				this.values.store(outputStream, "Minecraft Wiki Mod client settings");
			}

			try {
				Files.move(
					temporaryPath,
					CONFIG_PATH,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING
				);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			MOD_LOGGER.warn("Failed to save Minecraft Wiki settings to {}", CONFIG_PATH, exception);
			try {
				Files.deleteIfExists(temporaryPath);
			} catch (IOException cleanupException) {
				MOD_LOGGER.debug("Failed to clean up temporary settings file {}", temporaryPath, cleanupException);
			}
		}
	}

	private static Settings load() {
		Properties values = new Properties();
		if (!Files.exists(CONFIG_PATH)) {
			return new Settings(values);
		}

		try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
			values.load(inputStream);
		} catch (IOException exception) {
			MOD_LOGGER.warn("Failed to load Minecraft Wiki settings from {}", CONFIG_PATH, exception);
		}
		return new Settings(values);
	}

	private static String requireKey(String key) {
		String checkedKey = Objects.requireNonNull(key, "key").trim();
		if (checkedKey.isEmpty()) {
			throw new IllegalArgumentException("Setting key cannot be empty");
		}
		return checkedKey;
	}

	public static WikiLanguageConfig currentLang() {
		return INSTANCE.get(
			WikiLanguageConfig.LANG_CONF_KEY,
			WikiLanguageConfig.class,
			WikiLanguageConfig.defaultLanguage()
		);
	}
}
