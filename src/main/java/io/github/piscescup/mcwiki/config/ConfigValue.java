package io.github.piscescup.mcwiki.config;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface ConfigValue {

    @NotNull
    String configKey();

    @NotNull
    String configValue();

    @NotNull
    String configTranslationKey();
}
