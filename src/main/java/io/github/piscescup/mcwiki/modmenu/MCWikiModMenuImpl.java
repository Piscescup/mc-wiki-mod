package io.github.piscescup.mcwiki.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class MCWikiModMenuImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return MCWikiModMenuConfigScreen::new;
	}
}
