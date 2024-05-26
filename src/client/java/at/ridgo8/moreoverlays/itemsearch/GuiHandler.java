package at.ridgo8.moreoverlays.itemsearch;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class GuiHandler {

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            registerEvents();
        }
    }

    private static void registerEvents() {
        // GUI Init Event
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (false) {
                toggleMode();
                return;
            }
            JeiModule.updateModule();
            GuiRenderer.INSTANCE.guiInit(screen);
        });

        // GUI Open Event
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            GuiRenderer.INSTANCE.guiOpen(client.screen);
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                
            ScreenEvents.beforeRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                GuiRenderer.INSTANCE.preDraw(matrices.pose());
            });

        });

        // Draw Screen Post Event
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.afterRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                GuiRenderer.INSTANCE.renderTooltip();
                GuiRenderer.INSTANCE.postDraw();
            });
        });

        // Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Minecraft.getInstance().player == null) return;
            GuiRenderer.INSTANCE.tick();
        });
    }

    @Deprecated
    public static void toggleMode() {
        GuiRenderer.INSTANCE.toggleMode();
    }
}