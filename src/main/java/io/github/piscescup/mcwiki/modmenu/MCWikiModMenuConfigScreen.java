package io.github.piscescup.mcwiki.modmenu;

import io.github.piscescup.mcwiki.config.Settings;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class MCWikiModMenuConfigScreen extends Screen {
	private static final int PANEL_MAX_WIDTH = 460;
	private static final int PANEL_HEIGHT = 238;
	private static final int SCREEN_MARGIN = 24;
	private static final int CONTENT_MARGIN = 18;
	private static final int BUTTON_HEIGHT = 20;

	private static final int COLOR_PANEL = 0xFF101915;
	private static final int COLOR_PANEL_INSET = 0xFF16231D;
	private static final int COLOR_PANEL_BORDER = 0xFF3A5145;
	private static final int COLOR_CARD = 0xFF1B2923;
	private static final int COLOR_CARD_BORDER = 0xFF496356;
	private static final int COLOR_ACCENT = 0xFF52A535;
	private static final int COLOR_ACCENT_DARK = 0xFF244D1B;
	private static final int COLOR_TEXT = 0xFFF2F6F3;
	private static final int COLOR_MUTED = 0xFFAAB8B0;

	private final Screen parent;
	private final Settings settings;
	private final WikiLanguageConfig initialLanguage;
	private WikiLanguageConfig selectedLanguage;

	private Button languageButton;
	private Button resetButton;
	private Button cancelButton;
	private Button saveButton;

	public MCWikiModMenuConfigScreen(Screen parent) {
		this(parent, Settings.currentLang());
	}

	private MCWikiModMenuConfigScreen(Screen parent, WikiLanguageConfig language) {
		super(WikiTranslations.component(language, "config.mc_wiki.title"));
		this.parent = parent;
		this.settings = Settings.getInstance();
		this.initialLanguage = language;
		this.selectedLanguage = language;
	}

	@Override
	protected void init() {
		int panelLeft = panelLeft();
		int panelTop = panelTop();
		int panelWidth = panelWidth();
		int cardTop = panelTop + 73;
		int buttonY = panelTop + PANEL_HEIGHT - 32;
		boolean compact = panelWidth < 380;

		int languageWidth = compact ? panelWidth - CONTENT_MARGIN * 2 : 138;
		int languageX = compact
			? panelLeft + CONTENT_MARGIN
			: panelLeft + panelWidth - CONTENT_MARGIN - languageWidth;
		int languageY = compact ? cardTop + 34 : cardTop + 29;
		this.languageButton = addRenderableWidget(Button.builder(
			languageButtonText(),
			button -> cycleLanguage()
		).bounds(languageX, languageY, languageWidth, BUTTON_HEIGHT).build());

		int buttonGap = 6;
		int actionWidth = compact
			? (panelWidth - CONTENT_MARGIN * 2 - buttonGap * 2) / 3
			: 92;
		int resetWidth = compact ? actionWidth : 88;
		int resetX = panelLeft + CONTENT_MARGIN;
		int saveX = panelLeft + panelWidth - CONTENT_MARGIN - actionWidth;
		int cancelX = compact
			? resetX + resetWidth + buttonGap
			: saveX - actionWidth - buttonGap;

		this.resetButton = addRenderableWidget(Button.builder(
			text("config.mc_wiki.reset"),
			button -> selectLanguage(WikiLanguageConfig.defaultLanguage())
		).bounds(resetX, buttonY, resetWidth, BUTTON_HEIGHT).build());

		this.saveButton = addRenderableWidget(Button.builder(
			text("config.mc_wiki.save"),
			button -> saveAndClose()
		).bounds(saveX, buttonY, actionWidth, BUTTON_HEIGHT).build());

		this.cancelButton = addRenderableWidget(Button.builder(
			text("config.mc_wiki.cancel"),
			button -> onClose()
		).bounds(cancelX, buttonY, actionWidth, BUTTON_HEIGHT).build());

		refreshControls();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int panelLeft = panelLeft();
		int panelTop = panelTop();
		int panelWidth = panelWidth();
		int panelRight = panelLeft + panelWidth;
		int cardTop = panelTop + 73;
		int cardBottom = cardTop + 78;
		boolean compact = panelWidth < 380;

		graphics.fill(panelLeft + 4, panelTop + 5, panelRight + 4, panelTop + PANEL_HEIGHT + 5, 0x99000000);
		graphics.fill(panelLeft, panelTop, panelRight, panelTop + PANEL_HEIGHT, COLOR_PANEL_BORDER);
		graphics.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelTop + PANEL_HEIGHT - 1, COLOR_PANEL);
		graphics.fillGradient(panelLeft + 1, panelTop + 1, panelRight - 1, panelTop + 59, COLOR_PANEL_INSET, COLOR_PANEL);
		graphics.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelTop + 4, COLOR_ACCENT);

		drawLogo(graphics, panelLeft + CONTENT_MARGIN, panelTop + 17);
		graphics.text(this.font, text("config.mc_wiki.heading"), panelLeft + 64, panelTop + 18, COLOR_TEXT);
		graphics.text(this.font, text("config.mc_wiki.subtitle"), panelLeft + 64, panelTop + 34, COLOR_MUTED);

		graphics.fill(panelLeft + CONTENT_MARGIN, cardTop, panelRight - CONTENT_MARGIN, cardBottom, COLOR_CARD_BORDER);
		graphics.fill(panelLeft + CONTENT_MARGIN + 1, cardTop + 1, panelRight - CONTENT_MARGIN - 1, cardBottom - 1, COLOR_CARD);
		graphics.fill(panelLeft + CONTENT_MARGIN + 1, cardTop + 1, panelLeft + CONTENT_MARGIN + 4, cardBottom - 1, COLOR_ACCENT);

		int textX = panelLeft + CONTENT_MARGIN + 14;
		graphics.text(this.font, text("config.mc_wiki.language"), textX, cardTop + 12, COLOR_TEXT);
		if (!compact) {
			int descriptionWidth = this.languageButton.getX() - textX - 12;
			graphics.textWithWordWrap(
				this.font,
				text("config.mc_wiki.language.description"),
				textX,
				cardTop + 28,
				descriptionWidth,
				COLOR_MUTED
			);
			graphics.text(this.font, text("config.mc_wiki.destination"), textX, cardTop + 58, COLOR_MUTED);
			graphics.text(this.font, this.selectedLanguage.host(), textX + 69, cardTop + 58, COLOR_ACCENT);
		} else {
			graphics.centeredText(
				this.font,
				this.selectedLanguage.host(),
				panelLeft + panelWidth / 2,
				cardTop + 59,
				COLOR_ACCENT
			);
		}

		Component status = hasChanges()
			? text("config.mc_wiki.status.unsaved")
			: text("config.mc_wiki.status.saved");
		int statusY = panelTop + 169;
		graphics.fill(panelLeft + CONTENT_MARGIN, statusY + 2, panelLeft + CONTENT_MARGIN + 5, statusY + 7,
			hasChanges() ? COLOR_ACCENT : COLOR_ACCENT_DARK);
		graphics.text(this.font, status, panelLeft + CONTENT_MARGIN + 10, statusY, COLOR_MUTED);

		graphics.fill(panelLeft + CONTENT_MARGIN, panelTop + PANEL_HEIGHT - 43,
			panelRight - CONTENT_MARGIN, panelTop + PANEL_HEIGHT - 42, 0xFF293A31);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	private void cycleLanguage() {
		WikiLanguageConfig[] languages = WikiLanguageConfig.values();
		int nextIndex = (this.selectedLanguage.ordinal() + 1) % languages.length;
		selectLanguage(languages[nextIndex]);
	}

	private void selectLanguage(WikiLanguageConfig language) {
		this.selectedLanguage = language;
		refreshControls();
	}

	private void refreshControls() {
		this.languageButton.setMessage(languageButtonText());
		this.resetButton.setMessage(text("config.mc_wiki.reset"));
		this.cancelButton.setMessage(text("config.mc_wiki.cancel"));
		this.saveButton.setMessage(text("config.mc_wiki.save"));
		this.resetButton.active = this.selectedLanguage != WikiLanguageConfig.defaultLanguage();
		this.saveButton.active = hasChanges();
	}

	private void saveAndClose() {
		if (hasChanges()) {
			this.settings.set(this.selectedLanguage);
			this.settings.save();
		}
		onClose();
	}

	private Component languageButtonText() {
		return Component.literal(this.selectedLanguage.displayName(this.selectedLanguage).getString() + "  >");
	}

	private Component text(String key) {
		return WikiTranslations.component(this.selectedLanguage, key);
	}

	private boolean hasChanges() {
		return this.selectedLanguage != this.initialLanguage;
	}

	private int panelWidth() {
		return Math.min(PANEL_MAX_WIDTH, this.width - SCREEN_MARGIN);
	}

	private int panelLeft() {
		return (this.width - panelWidth()) / 2;
	}

	private int panelTop() {
		return (this.height - PANEL_HEIGHT) / 2;
	}

	private void drawLogo(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 34, y + 34, COLOR_ACCENT_DARK);
		graphics.fill(x + 3, y + 3, x + 31, y + 31, COLOR_ACCENT);
		graphics.fill(x + 3, y + 3, x + 31, y + 8, 0xFF70C957);
		graphics.fill(x + 7, y + 25, x + 11, y + 29, COLOR_ACCENT_DARK);
		graphics.fill(x + 23, y + 25, x + 27, y + 29, COLOR_ACCENT_DARK);
		graphics.centeredText(this.font, "W", x + 17, y + 13, 0xFFFFFFFF);
	}
}
