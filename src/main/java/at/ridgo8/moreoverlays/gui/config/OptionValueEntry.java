package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public abstract class OptionValueEntry<V> extends ConfigOptionList.OptionEntry {

    public static final int CONTROL_WIDTH_NOVALIDATOR = 44;
    public static final int CONTROL_WIDTH_VALIDATOR = 64;
    public static final int TITLE_WIDTH = 80;
    protected final ForgeConfigSpec.ConfigValue<V> value;
    protected final ForgeConfigSpec.ValueSpec spec;
    private final List<String> tooltip;
    protected Button btnReset;
    protected Button btnUndo;
    protected V defaultValue;
    protected V newValue;
    protected boolean showValidity = false;
    private String txtUndo = "";
    private String txtReset = "";
    private String name = "";
    private boolean valid = false;
    private boolean changes = false;

    @SuppressWarnings("unchecked")
    public OptionValueEntry(ConfigOptionList list, ForgeConfigSpec.ConfigValue<V> confValue, ForgeConfigSpec.ValueSpec spec) {
        super(list);
        this.value = confValue;
        this.spec = spec;
        this.btnReset = new Button(list.getRowWidth() - 20, 0, 20, 20, ITextComponent.getTextComponentOrEmpty(ConfigOptionList.RESET_CHAR),
                (btn) -> this.reset());
        this.btnUndo = new Button(list.getRowWidth() - 42, 0, 20, 20, ITextComponent.getTextComponentOrEmpty(ConfigOptionList.UNDO_CHAR),
                (btn) -> this.undo());

        this.txtReset = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".undo");

        final Object defaultVal = this.spec.getDefault();
        if (defaultVal != null && spec.getClazz().isAssignableFrom(defaultVal.getClass())) {
            this.defaultValue = (V) defaultVal;
        } else {
            btnReset.active = false;
        }

        this.name = this.value.getPath().get(this.value.getPath().size() - 1);

        String[] lines = null;
        if (this.spec.getComment() != null) {
            lines = this.spec.getComment().split("\\n");
            tooltip = new ArrayList<>(lines.length + 1);
        } else {
            tooltip = new ArrayList<>(1);
        }


        tooltip.add(TextFormatting.RED + this.name);
        for (final String line : lines) {
            tooltip.add(TextFormatting.YELLOW + line);
        }

        this.updateValue(this.value.get());
    }

    @Override
    protected void renderControls(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX,
                                  int mouseY, boolean mouseOver, float partialTick) {
        AbstractGui.drawString(matrixStack, Minecraft.getInstance().fontRenderer, this.name, 60 - TITLE_WIDTH, 6, 0xFFFFFF);
        this.btnReset.render(matrixStack, mouseX, mouseY, partialTick);
        this.btnUndo.render(matrixStack, mouseX, mouseY, partialTick);

        if (this.showValidity) {
            if (this.valid) {
                AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, ConfigOptionList.VALID, this.getConfigOptionList().getRowWidth() - 53, 6, 0x00FF00);
            } else {
                AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, ConfigOptionList.INVALID, this.getConfigOptionList().getRowWidth() - 53, 6, 0xFF0000);
            }
        }
    }


    @Override
    protected void renderTooltip(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);

        List<ITextComponent> tooltipConverted = new ArrayList<ITextComponent>();

        for (String iTextComponent : this.tooltip) {
            tooltipConverted.add(ITextComponent.getTextComponentOrEmpty(iTextComponent));
        }
        if (btnReset.isHovered()) {
            this.getConfigOptionList().getScreen().renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(this.txtReset), mouseX, mouseY);
        } else if (btnUndo.isHovered()) {
            this.getConfigOptionList().getScreen().renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(this.txtUndo), mouseX, mouseY);
        } else if (mouseX < TITLE_WIDTH + rowLeft) {
            this.getConfigOptionList().getScreen().func_243308_b(matrixStack, tooltipConverted, mouseX, mouseY);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
    }

    protected abstract void overrideUnsaved(V value);

    protected boolean isUndoable(V current) {
        return current == null || !current.equals(this.value.get()) || !this.valid;
    }

    protected void updateValue(@Nullable V value) {
        this.valid = value != null && this.spec.test(value);
        btnReset.active = isResettable();
        this.changes = isUndoable(value);
        btnUndo.active = this.changes;
        this.newValue = value;
    }

    @Override
    public void undo() {
        this.overrideUnsaved(this.value.get());
        this.updateValue(this.value.get());
    }

    @Override
    public void reset() {
        if (this.defaultValue != null) {
            this.value.set(this.defaultValue);
            this.overrideUnsaved(this.defaultValue);
            this.updateValue(this.defaultValue);
        }
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Arrays.asList(this.btnReset, this.btnUndo);
    }

    // Custom changeFocus method because of issue with config buttons on 1.16.5
    @Override
    public boolean changeFocus(boolean p_231049_1_) {
        IGuiEventListener lvt_2_1_ = this.getListener();
        boolean lvt_3_1_ = lvt_2_1_ != null;
        if (lvt_3_1_ && lvt_2_1_.changeFocus(p_231049_1_)) {
            return true;
        } else {
            List<TextFieldWidget> lvt_4_1_ = new ArrayList<TextFieldWidget>();
            this.getEventListeners().forEach(l -> {
                // Filter out the buttons because they don't need to be focussed
                if(l instanceof TextFieldWidget){
                    lvt_4_1_.add((TextFieldWidget)l);
                }
            });

            int lvt_6_1_ = lvt_4_1_.indexOf(lvt_2_1_);
            int lvt_5_3_;
            if (lvt_3_1_ && lvt_6_1_ >= 0) {
                lvt_5_3_ = lvt_6_1_ + (p_231049_1_ ? 1 : 0);
            } else if (p_231049_1_) {
                lvt_5_3_ = 0;
            } else {
                lvt_5_3_ = lvt_4_1_.size();
            }

            ListIterator<? extends IGuiEventListener> lvt_7_1_ = lvt_4_1_.listIterator(lvt_5_3_);
            BooleanSupplier lvt_8_1_ = p_231049_1_ ? lvt_7_1_::hasNext : lvt_7_1_::hasPrevious;
            Supplier lvt_9_1_ = p_231049_1_ ? lvt_7_1_::next : lvt_7_1_::previous;

            IGuiEventListener lvt_10_1_;
            do {
                if (!lvt_8_1_.getAsBoolean()) {
                    this.setListener((IGuiEventListener)null);
                    return false;
                }

                lvt_10_1_ = (IGuiEventListener)lvt_9_1_.get();
            } while(!lvt_10_1_.changeFocus(p_231049_1_));

            this.setListener(lvt_10_1_);
            return true;
        }
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public boolean hasChanges() {
        return this.changes;
    }

    @Override
    public boolean isResettable() {
        return this.defaultValue != null && (this.value.get() == null || !this.value.get().equals(this.defaultValue));
    }

    @Override
    public void save() {
        this.value.set(this.newValue);
        this.value.save();
    }
}