package at.ridgo8.moreoverlays.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionGeneric<V>
        extends OptionValueEntry<V> {

    private final TextFieldWidget tfConfigEntry;

    public OptionGeneric(ConfigOptionList list, ForgeConfigSpec.ConfigValue<V> valSpec, ForgeConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        this.showValidity = true;

        this.tfConfigEntry = new TextFieldWidget(Minecraft.getInstance().fontRenderer, OptionValueEntry.TITLE_WIDTH + 5, 2, this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 16, ITextComponent.getTextComponentOrEmpty(""));
        this.overrideUnsaved(this.value.get());
    }

    @Override
    protected void renderControls(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.tfConfigEntry.render(matrixStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(V value) {
        this.tfConfigEntry.setText(value.toString());
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        List<IGuiEventListener> childs = new ArrayList<>(super.getEventListeners());
        childs.add(this.tfConfigEntry);
        return childs;
    }

    @Override
    public void setFocusedDefault(IGuiEventListener focused) {
        if (focused == null) {
            this.tfConfigEntry.setFocused2(false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean keyReleased(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        final boolean flag = super.keyReleased(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        try {
            if (this.spec.getClazz() == String.class) {
                this.updateValue((V) this.tfConfigEntry.getText());
            } else if (this.value instanceof ForgeConfigSpec.IntValue) {
                this.updateValue((V) Integer.valueOf(this.tfConfigEntry.getText()));
            } else if (this.value instanceof ForgeConfigSpec.DoubleValue) {
                this.updateValue((V) Double.valueOf(this.tfConfigEntry.getText()));
            } else if (this.value instanceof ForgeConfigSpec.BooleanValue) {
                this.updateValue((V) Boolean.valueOf(this.tfConfigEntry.getText()));
            }
        } catch (NumberFormatException e) {
            this.updateValue(null);
        }

        return flag;
    }

}