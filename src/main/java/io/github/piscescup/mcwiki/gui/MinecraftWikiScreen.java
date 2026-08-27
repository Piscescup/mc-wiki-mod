package io.github.piscescup.mcwiki.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;
import io.github.piscescup.mcwiki.wiki.WikiTranslations;
import net.dimaskama.mcef.api.MCEFApi;
import net.dimaskama.mcef.api.MCEFBrowser;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletionException;

public class MinecraftWikiScreen extends Screen {
	private static final int OUTER_MARGIN = 6;
	private static final int HEADER_HEIGHT = 28;
	private static final int FOOTER_HEIGHT = 16;
	private static final int BROWSER_GAP = 4;
	private static final int ACCENT_COLOR = 0xFF52A535;
	private static final int ACCENT_DARK_COLOR = 0xFF244D1B;
	private static final int PANEL_COLOR = 0xF212181A;
	private static final GpuSampler BROWSER_SAMPLER = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

	private final WikiCategory category;
	private final String query;
	private final WikiLanguageConfig language;
	private final String url;
	private final MCEFApi.Initialization initialization;

	private MCEFBrowser browser;
	private Throwable initializationError;
	private boolean browserRequestQueued;
	private boolean closed;
	private boolean browserFocused = true;
	private int lastBrowserWidth = -1;
	private int lastBrowserHeight = -1;

	public MinecraftWikiScreen(WikiCategory category, String query, WikiLanguageConfig language, String url) {
		super(WikiTranslations.component(language, "gui.mc_wiki.title"));
		this.category = category;
		this.query = query;
		this.language = language;
		this.url = url;
		this.initialization = MCEFApi.initialize();
	}

	@Override
	protected void init() {
		queueBrowserCreation();
		resizeBrowserIfNeeded();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		extractTransparentBackground(graphics);

		int left = browserLeft();
		int top = browserTop();
		int right = left + browserWidth();
		int bottom = top + browserHeight();

		graphics.fill(0, 0, this.width, HEADER_HEIGHT, PANEL_COLOR);
		graphics.fill(0, HEADER_HEIGHT - 1, this.width, HEADER_HEIGHT, ACCENT_COLOR);
		graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height, PANEL_COLOR);
		graphics.fill(left - 1, top - 1, right + 1, bottom + 1, 0xFF36413D);
		graphics.fill(left, top, right, bottom, 0xFF0B0E0F);
		graphics.nextStratum();

		Component categoryText = this.category.displayName(this.language);
		int categoryWidth = this.font.width(categoryText) + 8;
		graphics.fill(8, 5, 8 + categoryWidth, 18, ACCENT_DARK_COLOR);
		graphics.text(this.font, categoryText, 12, 7, 0xFFE0F5D8);
		graphics.text(this.font, this.title, 16 + categoryWidth, 7, 0xFFFFFFFF);
		graphics.text(this.font, WikiTranslations.component(this.language, "gui.mc_wiki.search", this.query), 8, 18, 0xFFAEBBB6);
		graphics.fill(8, this.height - 10, 11, this.height - 7, ACCENT_COLOR);
		graphics.text(this.font, WikiTranslations.component(this.language, "gui.mc_wiki.footer_hint"), 16, this.height - 13, 0xFFB9C4C0);

		if (this.initializationError != null) {
			graphics.centeredText(this.font, WikiTranslations.component(this.language, "gui.mc_wiki.error"), this.width / 2, this.height / 2 - 12, 0xFFFF9B9B);
			graphics.centeredText(this.font, Component.literal(summarizeError(this.initializationError)), this.width / 2, this.height / 2 + 4, 0xFFE0E0E0);
			return;
		}

		if (this.browser == null) {
			graphics.centeredText(this.font, WikiTranslations.component(this.language, "gui.mc_wiki.loading"), this.width / 2, this.height / 2 - 12, 0xFFFFFFFF);
			graphics.centeredText(this.font, Component.literal(formatProgress()), this.width / 2, this.height / 2 + 4, 0xFFC7CDD6);
			return;
		}

		resizeBrowserIfNeeded();

		GpuTextureView textureView = this.browser.getTextureView();
		if (textureView == null) {
			return;
		}

		graphics.blit(
			textureView,
			BROWSER_SAMPLER,
			left,
			top,
			right,
			bottom,
			0.0F,
			1.0F,
			0.0F,
			1.0F
		);

		if (isHoveringBrowser(mouseX, mouseY)) {
			graphics.requestCursor(this.browser.getCursorType());
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (this.browser != null && isHoveringBrowser(event.x(), event.y())) {
			this.browserFocused = true;
			this.browser.setFocus(true);
			this.browser.onMouseClicked(translateMouseEvent(event), doubled);
			return true;
		}

		if (this.browser != null) {
			this.browserFocused = false;
			this.browser.setFocus(false);
		}

		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.browser != null && this.browserFocused) {
			this.browser.onMouseReleased(translateMouseEvent(event));
			return true;
		}

		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (this.browser != null && this.browserFocused) {
			this.browser.onMouseMoved(relativeMouseX(event.x()), relativeMouseY(event.y()));
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.browser != null && isHoveringBrowser(mouseX, mouseY)) {
			this.browser.onMouseScrolled(relativeMouseX(mouseX), relativeMouseY(mouseY), verticalAmount);
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void mouseMoved(double x, double y) {
		if (this.browser != null) {
			this.browser.onMouseMoved(relativeMouseX(x), relativeMouseY(y));
		}

		super.mouseMoved(x, y);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.browser != null && this.browserFocused) {
			this.browser.onKeyPressed(event);
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		if (this.browser != null && this.browserFocused) {
			this.browser.onKeyReleased(event);
			return true;
		}

		return super.keyReleased(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.browser != null && this.browserFocused) {
			this.browser.onCharTyped(event);
			return true;
		}

		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(null);
	}

	@Override
	public void removed() {
		this.closed = true;

		if (this.browser != null) {
			this.browser.close();
			this.browser = null;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void queueBrowserCreation() {
		if (this.browser != null || this.browserRequestQueued || this.initializationError != null) {
			return;
		}

		this.browserRequestQueued = true;
		this.initialization.getFuture().whenComplete((api, throwable) -> {
			if (this.minecraft == null) {
				return;
			}

			this.minecraft.execute(() -> {
				this.browserRequestQueued = false;

				if (this.closed) {
					return;
				}

				if (throwable != null) {
					this.initializationError = unwrap(throwable);
					return;
				}

				if (this.browser == null) {
					this.browser = api.createBrowser(this.url, false);
					this.browser.setFocus(this.browserFocused);
					resizeBrowserIfNeeded();
				}
			});
		});
	}

	private void resizeBrowserIfNeeded() {
		if (this.browser == null) {
			return;
		}

		int browserWidth = browserPixelWidth();
		int browserHeight = browserPixelHeight();
		if (browserWidth <= 0 || browserHeight <= 0) {
			return;
		}

		if (browserWidth != this.lastBrowserWidth || browserHeight != this.lastBrowserHeight) {
			this.browser.resize(browserWidth, browserHeight);
			this.lastBrowserWidth = browserWidth;
			this.lastBrowserHeight = browserHeight;
		}
	}

	private boolean isHoveringBrowser(double mouseX, double mouseY) {
		return mouseX >= browserLeft()
			&& mouseX < browserLeft() + browserWidth()
			&& mouseY >= browserTop()
			&& mouseY < browserTop() + browserHeight();
	}

	private MouseButtonEvent translateMouseEvent(MouseButtonEvent event) {
		return new MouseButtonEvent(
			relativeMouseX(event.x()),
			relativeMouseY(event.y()),
			event.buttonInfo()
		);
	}

	private int relativeMouseX(double mouseX) {
		return (int) Math.floor((mouseX - browserLeft()) * browserScale());
	}

	private int relativeMouseY(double mouseY) {
		return (int) Math.floor((mouseY - browserTop()) * browserScale());
	}

	private int browserScale() {
		return this.minecraft == null ? 1 : this.minecraft.getWindow().getGuiScale();
	}

	private int browserPixelWidth() {
		return browserWidth() * browserScale();
	}

	private int browserPixelHeight() {
		return browserHeight() * browserScale();
	}

	private int browserLeft() {
		return OUTER_MARGIN;
	}

	private int browserTop() {
		return HEADER_HEIGHT + BROWSER_GAP;
	}

	private int browserWidth() {
		return this.width - OUTER_MARGIN * 2;
	}

	private int browserHeight() {
		return this.height - browserTop() - FOOTER_HEIGHT - BROWSER_GAP;
	}

	private String formatProgress() {
		float percentage = this.initialization.getPercentage();
		String stage = switch (this.initialization.getStage()) {
			case NOT_STARTED -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.not_started");
			case DOWNLOADING -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.downloading");
			case EXTRACTING -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.extracting");
			case INSTALL -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.install");
			case INITIALIZING -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.initializing");
			case DONE -> WikiTranslations.text(this.language, "gui.mc_wiki.progress.done");
		};

		if (percentage < 0.0F) {
			return stage;
		}

		return stage + " (" + Math.round(percentage) + "%)";
	}

	private static Throwable unwrap(Throwable throwable) {
		Throwable current = throwable;
		while (current instanceof CompletionException && current.getCause() != null) {
			current = current.getCause();
		}
		return current;
	}

	private static String summarizeError(Throwable throwable) {
		String message = throwable.getMessage();
		if (message == null || message.isBlank()) {
			return throwable.getClass().getSimpleName();
		}
		return message;
	}
}
