package io.github.piscescup.mcwiki.gui;

import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiCategory;

/**
 *
 * @author REN YuanTong
 * @since
 */
record PendingWikiScreen(
    WikiCategory category,
    String query,
    WikiLanguageConfig language,
    String url
) {
}