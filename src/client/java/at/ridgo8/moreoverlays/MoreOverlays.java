package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;



public class MoreOverlays implements ClientModInitializer {

	public static final String MOD_ID = "moreoverlays";
    public static final String NAME = "MoreOverlays";

	public static Logger logger = LogManager.getLogger(NAME);

	@Override
	public void onInitializeClient() {

		// Correctly register the event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return; // Ensures the player is not null
			ChunkBoundsHandler.updateRegionInfo();

			if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && LightOverlayHandler.isEnabled() &&
                (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.isPauseScreen())) {
            LightOverlayHandler.scanner.update(Minecraft.getInstance().player);
        }
        });

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LightOverlayHandler.setEnabled(false);
        });

		KeyMapping lightOverlayKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key." + MOD_ID + ".lightoverlay.desc", // The translation key of the keybinding's name
			InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
			GLFW.GLFW_KEY_F7, // The keycode of the key
			"key." + MOD_ID + ".category" // The translation key of the keybinding's category.
		));
	
		KeyMapping chunkBoundsKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key." + MOD_ID + ".chunkbounds.desc", // The translation key of the keybinding's name
			InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
			GLFW.GLFW_KEY_F9, // The keycode of the key
			"key." + MOD_ID + ".category" // The translation key of the keybinding's category.
		));


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (chunkBoundsKeyMapping.consumeClick()) {
				ChunkBoundsHandler.toggleMode();
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (lightOverlayKeyMapping.consumeClick()) {
				LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
			}
		});




		Config.initialize();
        ClientRegistrationHandler.setupClient();
	}
}