package io.github.piscescup.mcwiki.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
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
	private static final int OUTER_MARGIN = 12;
	private static final int HEADER_HEIGHT = 30;
	private static final int FOOTER_HEIGHT = 24;
	private static final int BROWSER_BOTTOM_MARGIN = 6;
	private static final GpuSampler BROWSER_SAMPLER = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

	private final String query;
	private final String url;
	private final MCEFApi.Initialization initialization;

	private MCEFBrowser browser;
	private Throwable initializationError;
	private boolean browserRequestQueued;
	private boolean closed;
	private boolean browserFocused = true;
	private int lastBrowserWidth = -1;
	private int lastBrowserHeight = -1;

	public MinecraftWikiScreen(String query, String url) {
		super(Component.literal("Minecraft Wiki"));
		this.query = query;
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

		graphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0xFF2A2A2A);
		graphics.fill(left, top, right, bottom, 0xFF111111);
		graphics.fill(0, 0, this.width, HEADER_HEIGHT, 0xD0101010);
		graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height, 0xD0101010);

		graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
		graphics.text(this.font, Component.literal("搜索: " + this.query), 16, 10, 0xDDEBFF);
		graphics.text(this.font, Component.literal("Esc 关闭  鼠标滚轮滚动页面"), 16, this.height - 17, 0xC7CDD6);

		if (this.initializationError != null) {
			graphics.centeredText(this.font, Component.literal("浏览器初始化失败"), this.width / 2, this.height / 2 - 12, 0xFF9B9B);
			graphics.centeredText(this.font, Component.literal(summarizeError(this.initializationError)), this.width / 2, this.height / 2 + 4, 0xE0E0E0);
			return;
		}

		if (this.browser == null) {
			graphics.centeredText(this.font, Component.literal("正在准备内置浏览器"), this.width / 2, this.height / 2 - 12, 0xFFFFFF);
			graphics.centeredText(this.font, Component.literal(formatProgress()), this.width / 2, this.height / 2 + 4, 0xC7CDD6);
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
			browserLeft(),
			browserTop(),
			browserWidth(),
			browserHeight(),
			0.0F,
			0.0F,
			1.0F,
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

		int browserWidth = browserWidth();
		int browserHeight = browserHeight();
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
			event.x() - browserLeft(),
			event.y() - browserTop(),
			event.buttonInfo()
		);
	}

	private int relativeMouseX(double mouseX) {
		return (int) Math.floor(mouseX - browserLeft());
	}

	private int relativeMouseY(double mouseY) {
		return (int) Math.floor(mouseY - browserTop());
	}

	private int browserLeft() {
		return OUTER_MARGIN;
	}

	private int browserTop() {
		return HEADER_HEIGHT + 6;
	}

	private int browserWidth() {
		return this.width - OUTER_MARGIN * 2;
	}

	private int browserHeight() {
		return this.height - browserTop() - FOOTER_HEIGHT - BROWSER_BOTTOM_MARGIN;
	}

	private String formatProgress() {
		float percentage = this.initialization.getPercentage();
		String stage = switch (this.initialization.getStage()) {
			case NOT_STARTED -> "等待初始化";
			case DOWNLOADING -> "正在下载 JCEF";
			case EXTRACTING -> "正在解压 JCEF";
			case INSTALL -> "正在安装浏览器运行时";
			case INITIALIZING -> "正在启动 Chromium";
			case DONE -> "浏览器已就绪";
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
