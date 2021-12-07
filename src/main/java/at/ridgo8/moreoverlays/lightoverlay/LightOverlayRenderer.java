package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;


public class LightOverlayRenderer implements ILightRenderer {

    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();

    private static void renderCross(PoseStack matrixstack, BlockPos pos, float r, float g, float b) {
        Player player = Minecraft.getInstance().player;

        BlockState blockStateBelow = player.level.getBlockState(pos);
        float y = 0;
        if(blockStateBelow.getMaterial() == Material.TOP_SNOW){
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005f + (pos.getY()+0.125f);
            } else{
                // Block is below player
                y = (float) (0.005f + (pos.getY()+0.125f) + 0.01f * -(pos.getY()-player.getY()-1));
            }
        } else{
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005f + pos.getY();
            } else{
                // Block is below player
                y = (float) (0.005f + pos.getY() + 0.01f * -(pos.getY()-player.getY()-1));
            }
        }


        float x0 = pos.getX();
        float x1 = x0 + 1;
        float z0 = pos.getZ();
        float z1 = z0 + 1;

        Matrix4f matrix4f = matrixstack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float)camera.getPosition().y;
        float cameraZ = (float)camera.getPosition().z;


        renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z0-cameraZ).color(r, g, b, 1).endVertex();
        renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z1-cameraZ).color(r, g, b, 1).endVertex();

        renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z0-cameraZ).color(r, g, b, 1).endVertex();
        renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z1-cameraZ).color(r, g, b, 1).endVertex();

    }

    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        if(Minecraft.getInstance().options.getCameraType() == THIRD_PERSON_FRONT){
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.enableCull();

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            else if (mode == 1)
                renderCross(matrixstack, entry.getKey(), nr, ng, nb);
            else if (mode == 2)
                renderCross(matrixstack, entry.getKey(), ar, ag, ab);
        }
        tess.end();
    }
}
