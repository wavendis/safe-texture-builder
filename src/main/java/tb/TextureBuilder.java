package tb;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;

// Places with the held slot, then switches to a random hotbar block slot on the
// NEXT tick. Never switches and places in the same tick, at most one switch per tick,
// so the server sees the same packets as pressing 1-9 by hand.
public class TextureBuilder implements ClientModInitializer {
	static boolean enabled;
	static final boolean[] slotOn = new boolean[9];
	static final int[] weight = new int[9];
	private static boolean placed;
	private static boolean openConfig;
	private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("tbsafe.properties");

	@Override
	public void onInitializeClient() {
		load();
		KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.tbsafe.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_V,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tbsafe", "main"))));

		// Opened next tick: the chat screen closes itself right after running the command.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) -> dispatcher.register(
				ClientCommands.literal("texturebuildersafe").then(ClientCommands.literal("config").executes(c -> {
					openConfig = true;
					return 1;
				}))));

		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (enabled && level.isClientSide() && player.getItemInHand(hand).getItem() instanceof BlockItem) placed = true;
			return InteractionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (openConfig) {
				openConfig = false;
				mc.setScreenAndShow(new ConfigScreen());
			}
			while (toggle.consumeClick()) {
				enabled = !enabled;
				save();
				if (mc.player != null) mc.player.sendOverlayMessage(Component.literal("Texture Builder: " + (enabled ? "ON" : "OFF")));
			}
			if (!placed || mc.player == null) return;
			placed = false;
			switchSlot(mc.player.getInventory());
		});
	}

	// Weighted pick among enabled hotbar slots holding blocks. Same slot sends no packet.
	private static void switchSlot(Inventory inv) {
		int total = 0;
		for (int i = 0; i < 9; i++) total += usable(inv, i);
		if (total == 0) return;
		int r = ThreadLocalRandom.current().nextInt(total);
		for (int i = 0; i < 9; i++) {
			r -= usable(inv, i);
			if (r < 0) {
				inv.setSelectedSlot(i);
				return;
			}
		}
	}

	private static int usable(Inventory inv, int i) {
		return slotOn[i] && inv.getItem(i).getItem() instanceof BlockItem ? weight[i] : 0;
	}

	private static void load() {
		Properties p = new Properties();
		if (Files.exists(CONFIG)) {
			try (Reader r = Files.newBufferedReader(CONFIG)) {
				p.load(r);
			} catch (IOException | IllegalArgumentException ignored) {
				// Corrupt or unreadable config falls back to defaults.
			}
		}
		enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
		for (int i = 0; i < 9; i++) {
			slotOn[i] = Boolean.parseBoolean(p.getProperty("slot" + i + ".on", "true"));
			try {
				weight[i] = Math.clamp(Integer.parseInt(p.getProperty("slot" + i + ".weight", "50")), 0, 100);
			} catch (NumberFormatException e) {
				weight[i] = 50;
			}
		}
	}

	static void save() {
		Properties p = new Properties();
		p.setProperty("enabled", String.valueOf(enabled));
		for (int i = 0; i < 9; i++) {
			p.setProperty("slot" + i + ".on", String.valueOf(slotOn[i]));
			p.setProperty("slot" + i + ".weight", String.valueOf(weight[i]));
		}
		try (Writer w = Files.newBufferedWriter(CONFIG)) {
			p.store(w, "Texture Builder (Safe)");
		} catch (IOException ignored) {
			// Settings still apply this session.
		}
	}
}
