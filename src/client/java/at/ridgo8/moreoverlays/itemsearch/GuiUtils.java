package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.lang.reflect.Field;

import org.jetbrains.annotations.NotNull;

public class GuiUtils {

    private static Field fieldLeft;
    private static Field fieldTop;

    public static void initUtil() {
        try {
            
            fieldLeft = findField(AbstractContainerScreen.class, "leftPos");
            fieldLeft.setAccessible(true);

            fieldTop = findField(AbstractContainerScreen.class, "topPos");
            fieldTop.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                fieldLeft = findField(AbstractContainerScreen.class, "field_2776");
                fieldLeft.setAccessible(true);
    
                fieldTop = findField(AbstractContainerScreen.class, "field_2800");
                fieldTop.setAccessible(true);
            } catch (NoSuchFieldException f) {
                MoreOverlays.logger.error("Failed to load gui coordinate fields for reflection");
                f.printStackTrace();
                fieldTop = null;
                fieldLeft = null;
            }
        }
    }

    public static <T> Field findField(@NotNull final Class<? super T> clazz, @NotNull final String fieldName) throws NoSuchFieldException {
        try {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            String currentNamespace = resolver.getCurrentRuntimeNamespace();
            String className = clazz.getName().replace('.', '/');
            String mappedFieldName = resolver.mapFieldName(currentNamespace, className, fieldName, null);

            Field f = clazz.getDeclaredField(mappedFieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            throw new NoSuchFieldException("Failed to find field: " + e.getMessage());
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