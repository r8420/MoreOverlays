package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.itemsearch.GuiHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiUtils;


public final class ClientRegistrationHandler {

    private ClientRegistrationHandler() {
        // EMPTY
    }

    public static void setupClient() {
        KeyBindings.init();
        GuiUtils.initUtil();
        GuiHandler.init();
    }
}