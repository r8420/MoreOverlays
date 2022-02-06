package at.feldim2425.moreoverlays.lightoverlay;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightRenderer;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightScanner;
import at.feldim2425.moreoverlays.config.Config;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.settings.PointOfView.THIRD_PERSON_FRONT;

public class LightOverlayRenderer implements ILightRenderer {

    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRendererManager render = Minecraft.getInstance().getRenderManager();

    private static void renderCross(BlockPos pos, float r, float g, float b) {
        PlayerEntity player = Minecraft.getInstance().player;

        BlockState blockStateBelow = player.world.getBlockState(pos);
        double y = 0;
        if(blockStateBelow.getMaterial() == Material.SNOW){
            if(pos.getY() > player.getPosY()){
                // Block is above player
                y = 0.005D + (pos.getY()+0.125D);
            } else{
                // Block is below player
                y = 0.005D + (pos.getY()+0.125D) + 0.01D * -(pos.getY()-player.getPosY()-1);
            }
        } else{
            if(pos.getY() > player.getPosY()){
                // Block is above player
                y = 0.005D + pos.getY();
            } else{
                // Block is below player
                y = 0.005D + pos.getY() + 0.01D * -(pos.getY()-player.getPosY()-1);
            }
        }


        double x0 = pos.getX();
        double x1 = x0 + 1;
        double z0 = pos.getZ();
        double z1 = z0 + 1;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder renderer = tess.getBuffer();

        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x0, y, z0).color(r, g, b, 1).endVertex();
        renderer.pos(x1, y, z1).color(r, g, b, 1).endVertex();

        renderer.pos(x1, y, z0).color(r, g, b, 1).endVertex();
        renderer.pos(x0, y, z1).color(r, g, b, 1).endVertex();
        tess.draw();
    }

    public void renderOverlays(ILightScanner scanner) {
        PlayerEntity player = Minecraft.getInstance().player;
        if(Minecraft.getInstance().gameSettings.getPointOfView() == THIRD_PERSON_FRONT){
            return;
        }
        Minecraft.getInstance().getTextureManager().bindTexture(BLANK_TEX);
        GlStateManager.pushMatrix();
        GL11.glLineWidth((float) (double) Config.render_spawnLineWidth.get());
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        final Vector3d view = render.info.getProjectedView();
        GlStateManager.rotatef(player.getPitch(0), 1, 0, 0); // Fixes camera rotation.
        GlStateManager.rotatef(player.getYaw(0) + 180, 0, 1, 0); // Fixes camera rotation.
        GlStateManager.translated(-view.x, -view.y, -view.z);

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;


        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            else if (mode == 1)
                renderCross(entry.getKey(), nr, ng, nb);
            else if (mode == 2)
                renderCross(entry.getKey(), ar, ag, ab);
        }


        GlStateManager.popMatrix();
    }
}
