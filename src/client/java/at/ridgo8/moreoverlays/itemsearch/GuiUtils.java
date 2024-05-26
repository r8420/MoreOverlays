package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.lang.reflect.Field;

public class GuiUtils {

    private static Field fieldLeft;
    private static Field fieldTop;

    public static void initUtil() {
        try {
            fieldLeft = AbstractContainerScreen.class.getDeclaredField("leftPos");
            fieldLeft.setAccessible(true);

            fieldTop = AbstractContainerScreen.class.getDeclaredField("topPos");
            fieldTop.setAccessible(true);
        } catch (NoSuchFieldException e) {
            MoreOverlays.logger.error("Tried to load gui coordinate fields for reflection");
            e.printStackTrace();
            fieldTop = null;
            fieldLeft = null;
        }
    }

    public static int getGuiTop(AbstractContainerScreen<?> container) {
        if (fieldTop == null) {
            return 0;
        }

        try {
            return fieldTop.getInt(container);
        } catch (IllegalAccessException e) {
            MoreOverlays.logger.error("Failed to access topPos field");
            e.printStackTrace();
        }
        return 0;
    }

    public static int getGuiLeft(AbstractContainerScreen<?> container) {
        if (fieldLeft == null) {
            return 0;
        }

        try {
            return fieldLeft.getInt(container);
        } catch (IllegalAccessException e) {
            MoreOverlays.logger.error("Failed to access leftPos field");
            e.printStackTrace();
        }
        return 0;
    }
}