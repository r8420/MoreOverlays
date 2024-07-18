package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4d;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.joml.Vector4f;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    public static void renderOverlays(Matrix4f matrix4f) {
        Player player = Minecraft.getInstance().player;
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);


        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.setShader(GameRenderer::getPositionColorShader);


        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        } 

        final int h = player.level().getHeight();
        final int h0 = (int) player.getY();
        final int h1 = Math.min(h, h0 - 16);
        final int h2 = Math.min(h, h0 + 16);
        final int h3 = Math.min(h1, 0);

        final int x0 = player.chunkPosition().x * 16;
        final int x1 = x0 + 16;
        final int x2 = x0 + 8;
        final int z0 = player.chunkPosition().z * 16;
        final int z1 = z0 + 16;
        final int z2 = z0 + 8;

        int regionX;
        int regionY = player.chunkPosition().getWorldPosition().getY() / ChunkBoundsHandler.REGION_SIZEY_CUBIC;
        int regionZ;

        if (player.chunkPosition().x < 0) {
            regionX = (player.chunkPosition().x + 1) / ChunkBoundsHandler.REGION_SIZEX;
            regionX--;
        } else {
            regionX = player.chunkPosition().x / ChunkBoundsHandler.REGION_SIZEX;
        }
        if (player.chunkPosition().getWorldPosition().getY() < 0) {
            regionY--;
        }
        if (player.chunkPosition().z < 0) {
            regionZ = (player.chunkPosition().z + 1) / ChunkBoundsHandler.REGION_SIZEZ;
            regionZ--;
        } else {
            regionZ = player.chunkPosition().z / ChunkBoundsHandler.REGION_SIZEZ;
        }

        final int regionBorderX0 = regionX * ChunkBoundsHandler.REGION_SIZEX * 16;
        final int regionBorderY0 = (regionY * ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16) - 64;
        final int regionBorderZ0 = regionZ * ChunkBoundsHandler.REGION_SIZEZ * 16;
        final int regionBorderX1 = regionBorderX0 + (ChunkBoundsHandler.REGION_SIZEX * 16);
        final int regionBorderY1 = regionBorderY0 + (ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16) - 128;
        final int regionBorderZ1 = regionBorderZ0 + (ChunkBoundsHandler.REGION_SIZEZ * 16);

        final int radius = Config.chunk_EdgeRadius.get() * 16;
        final int renderColorEdge = Config.render_chunkEdgeColor.get();
        final int renderColorMiddle = Config.render_chunkMiddleColor.get();
        final int renderColorGrid = Config.render_chunkGridColor.get();


        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int yo = -16 - radius; yo <= radius; yo += 16) {
                renderEdge(matrix4f, x0 - xo, z0 - yo, h3, h, renderColorEdge);
            }
        }

        if (Config.chunk_ShowMiddle.get()) {
            renderEdge(matrix4f, x2, z2, h3, h, renderColorMiddle);
        }

        if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.GRID) {
            renderGrid(matrix4f, x0, h1, z0 - 0.005f, x0, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrix4f, x1, h1, z0 - 0.005f, x1, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrix4f, x0 - 0.005f, h1, z0, x1 + 0.005f, h2, z0, 1.0f, renderColorGrid);
            renderGrid(matrix4f, x0 - 0.005f, h1, z1, x1 + 0.005f, h2, z1, 1.0f, renderColorGrid);
        } else if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.REGIONS) {
            renderGrid(matrix4f, regionBorderX0 - 0.005f, regionBorderY0 - 0.005f, regionBorderZ0 - 0.005f, regionBorderX1 + 0.005f,
                    regionBorderY1 + 0.005f, regionBorderZ1 + 0.005f, 16.0f, renderColorGrid);
        }

        // restore render settings
        RenderSystem.depthMask(true);
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();
        }
    }

    public static void renderEdge(Matrix4f matrix4f, float x, float z, float h3, float h, int color) {
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float) camera.getPosition().y;
        float cameraZ = (float) camera.getPosition().z;

        x -= cameraX;
        h3 -= cameraY;

        h -= cameraY;

        z -= cameraZ;

        BufferBuilder bufferBuilder = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        drawVertex(bufferBuilder, matrix4f, x, h3, z, r, g, b);
        drawVertex(bufferBuilder, matrix4f, x, h, z, r, g, b);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    public static void renderGrid(Matrix4f matrix4f, float x0, float y0, float z0, float x1, float y1, float z1, float step, int color) {
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float) camera.getPosition().y;
        float cameraZ = (float) camera.getPosition().z;

        
        BufferBuilder renderer = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        for (float x = x0; x <= x1; x += step) {
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);
        }
        for (float y = y0; y <= y1; y += step) {
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
        }
        for (float z = z0; z <= z1; z += step) {
            drawVertex(renderer, matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static void drawVertex(BufferBuilder renderer, Matrix4f matrix, float x, float y, float z, float r, float g, float b) {
        Vector4f vector4f = matrix.transform(new Vector4f(x, y, z, 1.0f));
        renderer.addVertex(vector4f.x(), vector4f.y(), vector4f.z()).setColor(r, g, b, 1);
    }
}
