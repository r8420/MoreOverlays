package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.ridgo8.moreoverlays.config.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.Map;

public class GuiRenderer {

    public static final GuiRenderer INSTANCE = new GuiRenderer();

    private static final float OVERLAY_ZLEVEL = 299F;
    private static final float FRAME_RADIUS = 1.0F;

    private static boolean enabled = false;

    private static String lastFilterText = "";
    private static boolean emptyFilter = true;
    private static BiMap<Slot, SlotViewWrapper> views = HashBiMap.create();

    private boolean allowRender = false;
    private int guiOffsetX = 0;
    private int guiOffsetY = 0;

    public void guiInit(Screen gui) {
        if (!canShowIn(gui)) {
            return;
        }

        guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) gui);
        guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) gui);

    }

    public void guiOpen(Screen gui) {

    }

    public void preDraw(PoseStack matrixstack) {
        Screen guiscr = Minecraft.getInstance().screen;

        EditBox textField = JeiModule.getJEITextField();

        if (canShowIn(guiscr)) {
            allowRender = true;
            if (textField != null && enabled) {
                drawSearchFrame(textField, matrixstack);
            }
        }
    }

    public void postDraw() {
        Screen guiscr = Minecraft.getInstance().screen;

        if (allowRender && canShowIn(guiscr)) {
            allowRender = false;
            drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
        }
    }

    private void drawSearchFrame(EditBox textField, PoseStack matrixstack) {
        Matrix4f matrix4f = matrixstack.last().pose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        float x = textField.x - 2;
        float y = textField.y - 4;
        float width = textField.getWidth() + 8;
        float height = textField.getHeight() - 4;

        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y - FRAME_RADIUS, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y - FRAME_RADIUS, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y, 1000).color(1, 1, 0, 1F).endVertex();

        renderer.vertex(matrix4f, x, y, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y + height, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x, y + height, 1000).color(1, 1, 0, 1F).endVertex();

        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y + height, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y + height, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x - FRAME_RADIUS, y + height + FRAME_RADIUS, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y + height + FRAME_RADIUS, 1000).color(1, 1, 0, 1F).endVertex();

        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x + width, y, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x + width, y + height, 1000).color(1, 1, 0, 1F).endVertex();
        renderer.vertex(matrix4f, x + width + FRAME_RADIUS, y + height, 1000).color(1, 1, 0, 1F).endVertex();

        tess.end();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public void renderTooltip(ItemStack stack) {
        Screen guiscr = Minecraft.getInstance().screen;
        if (allowRender && canShowIn(guiscr)) {
            //AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) guiscr;
            allowRender = false;
            drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
        }
    }

    private void drawSlotOverlay(AbstractContainerScreen<?> gui) {
        Lighting.setupForFlatItems();

        if (!enabled || views == null || views.isEmpty())
            return;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Map.Entry<Slot, SlotViewWrapper> slot : views.entrySet()) {
            if (slot.getValue().isEnableOverlay()) {
                Vec2 posvec = slot.getValue().getView().getRenderPos(guiOffsetX, guiOffsetY);
                float px = posvec.x;
                float py = posvec.y;
                renderer.vertex(px + 16 + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).color(0, 0, 0, 0.5F).endVertex();
                renderer.vertex(px + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).color(0, 0, 0, 0.5F).endVertex();
                renderer.vertex(px + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).color(0, 0, 0, 0.5F).endVertex();
                renderer.vertex(px + 16 + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).color(0, 0, 0, 0.5F).endVertex();
            }
        }

        tess.end();

        RenderSystem.enableTexture();

        RenderSystem.disableBlend();
    }

    public boolean canShowIn(Screen gui) {
        return (gui instanceof AbstractContainerScreen<?>) && ((AbstractContainerScreen<?>) gui).getMenu() != null && !((AbstractContainerScreen<?>) gui).getMenu().slots.isEmpty();
    }

    private void checkSlots(AbstractContainerScreen<?> container) {
        if (views == null) {
            views = HashBiMap.create();
        } else {
            views.clear();
        }
        for (Slot slot : container.getMenu().slots) {
            //System.out.println(slot);
            SlotViewWrapper wrapper;
            if (!views.containsKey(slot)) {
                wrapper = new SlotViewWrapper(SlotHandler.INSTANCE.getViewSlot(container, slot));
                views.put(slot, wrapper);
            } else {
                wrapper = views.get(slot);
            }

            wrapper.setEnableOverlay(wrapper.getView().canSearch() && !isSearchedItem(slot.getItem()));
        }
    }

    private boolean isSearchedItem(ItemStack stack) {
        if (emptyFilter) return true;
        else if (stack.isEmpty()) return false;
        for (Object ingredient : JeiModule.filter.getFilteredIngredients(VanillaTypes.ITEM_STACK)) {
            if (ItemUtils.ingredientMatches(ingredient, stack)) {
                return true;
            }
        }
        return Config.search_searchCustom.get() && stack.getDisplayName().getString().toLowerCase().contains(JeiModule.getJEITextField().getValue().toLowerCase());
    }

    public void tick() {
        final Screen screen = Minecraft.getInstance().screen;
        if (!canShowIn(screen))
            return;
        if (enabled && !JeiModule.filter.getFilterText().equals(lastFilterText)) {
            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        }


        if (enabled && screen instanceof AbstractContainerScreen<?>) {
            checkSlots((AbstractContainerScreen<?>) screen);
            guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) screen);
            guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) screen);
        } else if (views != null) {
            views.clear();
        }
    }

    public void toggleMode() {
        enabled = !enabled;
        if (enabled) {
            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        } else {
            lastFilterText = "";
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
