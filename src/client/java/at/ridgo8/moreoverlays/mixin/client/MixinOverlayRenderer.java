package at.ridgo8.moreoverlays.mixin.client;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;



@Mixin(Gui.class)
public class MixinOverlayRenderer {
    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        try {
            // Checks if the debug screen is not shown
            if (!mc.getDebugOverlay().showDebugScreen()) {
                if (!ChunkBoundsHandler.regionInfo.isEmpty()) {
                    int y = 0;
                    for (String text : ChunkBoundsHandler.regionInfo) {
                        guiGraphics.drawString(mc.font, text, 10, y += 10, 0xFFFFFF);
                    }
                }
            }
        } catch (NoSuchMethodError e) {
            if (!ChunkBoundsHandler.regionInfo.isEmpty()) {
                int y = 0;
                for (String text : ChunkBoundsHandler.regionInfo) {
                    guiGraphics.drawString(mc.font, text, 10, y += 10, 0xFFFFFF);
                }
            }
        }
    }
}
