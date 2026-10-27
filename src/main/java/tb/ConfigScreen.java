package tb;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// Table of hotbar slots: slot number, current item, on/off toggle, weight slider.
public class ConfigScreen extends Screen {
	private static final int ROW = 21, TABLE_W = 320;
	private static final int ITEM_X = 34, TOGGLE_X = 176, SLIDER_X = 216;

	public ConfigScreen() {
		super(Component.literal("Texture Builder (Safe)"));
	}

	private int left() { return (width - TABLE_W) / 2; }
	private int top() { return 40; }

	@Override
	protected void init() {
		int x = left();
		for (int i = 0; i < 9; i++) {
			int slot = i, y = top() + i * ROW;
			addRenderableWidget(Button.builder(onOff(TextureBuilder.slotOn[i]), b -> {
				TextureBuilder.slotOn[slot] = !TextureBuilder.slotOn[slot];
				b.setMessage(onOff(TextureBuilder.slotOn[slot]));
				TextureBuilder.save();
			}).bounds(x + TOGGLE_X, y, 36, 20).build());

			addRenderableWidget(new AbstractSliderButton(x + SLIDER_X, y, TABLE_W - SLIDER_X, 20, Component.empty(), TextureBuilder.weight[i] / 100.0) {
				{ updateMessage(); }

				@Override
				protected void updateMessage() {
					setMessage(Component.literal("Weight: " + Math.round(value * 100)));
				}

				@Override
				protected void applyValue() {
					TextureBuilder.weight[slot] = (int) Math.round(value * 100);
					TextureBuilder.save();
				}
			});
		}

		int y = top() + 9 * ROW + 8;
		addRenderableWidget(Button.builder(modLabel(), b -> {
			TextureBuilder.enabled = !TextureBuilder.enabled;
			b.setMessage(modLabel());
			TextureBuilder.save();
		}).bounds(width / 2 - 154, y, 150, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(width / 2 + 4, y, 150, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		int x = left();
		g.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
		g.text(font, "Slot", x, top() - 12, 0xFFAAAAAA);
		g.text(font, "Item", x + ITEM_X, top() - 12, 0xFFAAAAAA);
		g.text(font, "Use", x + TOGGLE_X, top() - 12, 0xFFAAAAAA);
		g.text(font, "Prominence", x + SLIDER_X, top() - 12, 0xFFAAAAAA);

		var inv = minecraft.player == null ? null : minecraft.player.getInventory();
		for (int i = 0; i < 9; i++) {
			int y = top() + i * ROW;
			if (i % 2 == 0) g.fill(x - 4, y - 1, x + TABLE_W + 4, y + ROW - 1, 0x33FFFFFF);
			g.text(font, String.valueOf(i + 1), x + 4, y + 6, 0xFFFFFFFF);
			ItemStack stack = inv == null ? ItemStack.EMPTY : inv.getItem(i);
			if (stack.isEmpty()) {
				g.text(font, "(empty)", x + ITEM_X, y + 6, 0xFF777777);
			} else {
				g.item(stack, x + ITEM_X, y + 2);
				String name = font.plainSubstrByWidth(stack.getHoverName().getString(), TOGGLE_X - ITEM_X - 26);
				g.text(font, name, x + ITEM_X + 20, y + 6, 0xFFFFFFFF);
			}
		}
	}

	@Override
	public boolean isPauseScreen() { return false; }

	private static Component onOff(boolean on) {
		return Component.literal(on ? "ON" : "OFF").withColor(on ? 0x55FF55 : 0xFF5555);
	}

	private static Component modLabel() {
		return Component.literal("Mod: " + (TextureBuilder.enabled ? "ON" : "OFF"));
	}
}
