package at.ridgo8.moreoverlays;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MoreOverlays.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    // Lazy initialization for key mappings
    public static final Lazy<KeyMapping> lightOverlayKeyMapping = Lazy.of(() ->
            new KeyMapping("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F7,
                    "key." + MoreOverlays.MOD_ID + ".category"));

    public static final Lazy<KeyMapping> chunkBoundsKeyMapping = Lazy.of(() ->
            new KeyMapping("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F9,
                    "key." + MoreOverlays.MOD_ID + ".category"));

    // Remove any manual event bus registration from this class
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(lightOverlayKeyMapping.get());
        event.register(chunkBoundsKeyMapping.get());
    }
}