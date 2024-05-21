package at.ridgo8.moreoverlays.mixin.main;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler.RenderMode;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsRenderer;

@Mixin(LevelRenderer.class)
public abstract class MixinOnRenderTail {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void injectAfterParticleRender(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            if (ChunkBoundsHandler.getMode() != RenderMode.NONE && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
                ChunkBoundsRenderer.renderOverlays(poseStack);
            }
        }
        if (LightOverlayHandler.isEnabled() && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner, poseStack);
        }
    }
}