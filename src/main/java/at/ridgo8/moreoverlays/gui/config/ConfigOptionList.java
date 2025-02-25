package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

// TODO: As I wrote this system I noticed, that the way AbstractOptionList renders items and passes events is not optimal for this purpose
// Rendering is done in one pass therefore Tooltips will usually be rendered below other items further down and events are only passed
// to the hoverd / selected item which makes unfocosing of textfields a challange. Custom system needed.
public class ConfigOptionList extends ContainerObjectSelectionList<ConfigOptionList.OptionEntry> {

    public static final String UNDO_CHAR = "\u21B6";
    public static final String RESET_CHAR = "\u2604";
    public static final String VALID = "\u2714";
    public static final String INVALID = "\u2715";
    private static final int ITEM_HEIGHT = 22;

    private final ConfigScreen parent;
    private final String modId;

    private ForgeConfigSpec rootConfig;
    private List<String> configPath = Collections.emptyList();
    private Map<String, Object> currentMap;
    private CommentedConfig comments;

    public ConfigOptionList(Minecraft minecraft, String modId, ConfigScreen configs) {
        // Width, Height, Y-Start, Y-End, item_height
        super(minecraft, configs.width, configs.height - 32, 43, ITEM_HEIGHT);
        this.parent = configs;
        this.modId = modId;
    }

    public static List<String> splitPath(String path) {
        return Arrays.asList(path.split("\\."));
    }

    public ConfigScreen getScreen() {
        return this.parent;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 64;
    }

    public void updateGui() {
        this.setSize(this.parent.width, this.parent.height - 32);
    }


    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int p_renderDecorations_1_, int p_renderDecorations_2_) {
        int i = this.getItemCount();
        for (int j = 0; j < i; ++j) {
            int k = this.getRowTop(j);
            int l = this.getRowTop(j) + ITEM_HEIGHT;
            if (l >= this.getY() && k <= this.getBottom()) {
                ConfigOptionList.OptionEntry e = this.getEntry(j);
                e.runRenderTooltip(guiGraphics);
            }
        }
    }

    public String categoryTitleKey(List<String> path) {
        if (path.isEmpty()) {
            return null;
        }
        return "config." + this.modId + ".category." + path.stream().collect(Collectors.joining("."));
    }

    public void setConfiguration(ForgeConfigSpec rootConfig) {
        this.setConfiguration(rootConfig, Collections.emptyList());
    }

    public void setConfiguration(ForgeConfigSpec rootConfig, List<String> path) {
        this.rootConfig = rootConfig;
        try {
            final Field forgeconfigspec_childconfig = ForgeConfigSpec.class.getDeclaredField("childConfig");
            forgeconfigspec_childconfig.setAccessible(true);
            final Object childConfig_raw = forgeconfigspec_childconfig.get(rootConfig);
            if (childConfig_raw instanceof CommentedConfig) {
                this.comments = (CommentedConfig) childConfig_raw;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            MoreOverlays.logger.warn("Couldn't reflect childConfig from ForgeConfigSpec! Comments will be missing.", e);
        }
        this.updatePath(path);
    }

    private void setPath(List<String> path) {
        Object val;
        if (path.isEmpty()) {
            val = this.rootConfig.getValues();
        } else {
            val = this.rootConfig.getValues().getRaw(path);
        }

        if (val instanceof UnmodifiableConfig) {
            this.configPath = path;
            this.currentMap = ((UnmodifiableConfig) val).valueMap();
            this.refreshEntries();
            this.parent.updatePath(this.getCurrentPath());
        } else {

            // There's a bug where we end up with a duplicate path here,
            // which seems to be related to keyboard race conditions allowing
            // us to 'double' select a child path.
            // In this event, we attempt to fail gracefully.
            int n = path.size();
            if (n > 1) {
                if (path.get(n - 1) == path.get(n - 2)) {
                    MoreOverlays.logger.error("Attempting to load duplicate path:", path);
                    MoreOverlays.logger.warn("This could be caused by key event race condition");
                    // Trim and reload
                    path.remove(n - 1);
                    this.setPath(path);
                    return;
                }
            }

            throw new IllegalArgumentException("Path in config list has to point to another config object");
        }
    }

    public void updatePath(List<String> path) {
        this.setPath(new ArrayList<>(path));
    }

    public void push(String path) {
        this.push(splitPath(path));
    }

    public void push(List<String> path) {
        final List<String> tmp = new ArrayList<>(this.configPath.size() + path.size());
        tmp.addAll(this.configPath);
        tmp.addAll(path);
        this.setPath(tmp);
    }

    public void pop() {
        pop(1);
    }

    public void pop(int amount) {
        final List<String> tmp = new ArrayList<>(this.configPath);
        for (int i = 0; i < amount && !tmp.isEmpty(); i++) {
            tmp.remove(tmp.size() - 1);
        }
        setPath(tmp);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        boolean flag = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        OptionEntry selected = this.getEntryAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);
        for (final OptionEntry entry : this.children()) {
            if (entry != selected) {
                entry.setFocused(null);
            }
        }

        return flag;
    }

    public void refreshEntries() {
        this.clearEntries();
        for (final Map.Entry<String, Object> cEntry : this.currentMap.entrySet()) {
            final List<String> fullPath = new ArrayList<>(this.configPath.size() + 1);
            fullPath.addAll(this.configPath);
            fullPath.add(cEntry.getKey());

            String comment = null;
            if (this.comments != null) {
                comment = this.comments.getComment(fullPath);
            }

            if (cEntry.getValue() instanceof UnmodifiableConfig) {
                final String name = I18n.get(categoryTitleKey(fullPath));
                this.addEntry(new OptionCategory(this, Arrays.asList(cEntry.getKey()), name, comment));
            } else if (cEntry.getValue() instanceof ForgeConfigSpec.BooleanValue) {
                this.addEntry(new OptionBoolean(this, (ForgeConfigSpec.BooleanValue) cEntry.getValue(), rootConfig.getSpec().get(fullPath)));
            } else {
                this.addEntry(new OptionGeneric<>(this, (ForgeConfigSpec.ConfigValue<?>) cEntry.getValue(), (ForgeConfigSpec.ValueSpec) rootConfig.getSpec().get(fullPath)));
            }
        }
        this.setFocused(null);
    }

    public List<String> getCurrentPath() {
        return Collections.unmodifiableList(this.configPath);
    }

    public ForgeConfigSpec getConfig() {
        return this.rootConfig;
    }

    public String getModId() {
        return this.modId;
    }

    public boolean isSaveable() {
        boolean hasChanges = false;
        for (final OptionEntry entry : this.children()) {
            if (!entry.isValid()) {
                return false;
            }
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public boolean isResettable() {
        boolean resettable = false;
        for (final OptionEntry entry : this.children()) {
            resettable = resettable || entry.isResettable();
        }
        return resettable;
    }

    public boolean isUndoable() {
        boolean hasChanges = false;
        for (final OptionEntry entry : this.children()) {
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public void reset() {
        for (final OptionEntry entry : this.children()) {
            entry.reset();
        }
    }

    public void undo() {
        for (final OptionEntry entry : this.children()) {
            entry.undo();
        }
    }

    public void save() {
        for (final OptionEntry entry : this.children()) {
            if (entry.isValid()) {
                entry.save();
            }
        }
    }

    public abstract static class OptionEntry extends ContainerObjectSelectionList.Entry<ConfigOptionList.OptionEntry> implements ContainerEventHandler {
        private final ConfigOptionList optionList;

        protected int rowTop, rowLeft;

        private int rowWidth, itemHeight, mouseX, mouseY;
        private boolean mouseOver;

        public OptionEntry(ConfigOptionList list) {
            this.optionList = list;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int itemindex, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                           boolean mouseOver, float partialTick) {
            this.rowTop = rowTop;
            this.rowLeft = rowLeft;
            this.rowWidth = rowWidth;
            this.itemHeight = itemHeight;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.mouseOver = mouseOver;

            mouseX -= rowLeft;
            mouseY -= rowTop;
            guiGraphics.pose().translate(rowLeft, rowTop, 0);
            renderControls(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);

            guiGraphics.pose().translate(-rowLeft, -rowTop, 0);
        }

        protected abstract void renderControls(GuiGraphics guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                               boolean mouseOver, float partialTick);

        /*
         * This is part of the "hacky" way to render tooltips above the other entries.
         * The values to render are stored by the render() method and after that the ConfigOptionList iterates over the entries again
         * to call this runRenderTooltip() which calls the renderTooltip() method with the stored parameters.
         * Not the best way but AbstractOptionList doesn't seem to have any better hooks to do that.
         * A custom Implementation would be better but I'm too lazy to do that
         */
        public void runRenderTooltip(GuiGraphics guiGraphics) {
            if (this.mouseOver) {
                this.renderTooltip(guiGraphics, this.rowTop, this.rowLeft, this.rowWidth, this.itemHeight, this.mouseX, this.mouseY);
                Lighting.setupForFlatItems();;
                GlStateManager._disableBlend(); // TODO: Replace this
            }
        }

        protected void renderTooltip(GuiGraphics guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        public ConfigOptionList getConfigOptionList() {
            return this.optionList;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX - this.rowLeft, mouseY - this.rowTop, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return super.mouseReleased(mouseX - this.rowLeft, mouseY - this.rowTop, button);
        }

        @Override
        public boolean mouseDragged(double fromX, double fromY, int button, double toX, double toY) {
            return super.mouseDragged(fromX - this.rowLeft, fromY - this.rowTop, button, toX - this.rowLeft, toY - this.rowTop);
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount, double unknown) {
            return super.mouseScrolled(mouseX - this.rowLeft, mouseY - this.rowTop, amount, unknown);
        }

        public boolean isValid() {
            return true;
        }

        public boolean hasChanges() {
            return false;
        }

        public boolean isResettable() {
            return false;
        }

        public void reset() {
        }

        public void undo() {
        }

        public void save() {
        }
    }

}