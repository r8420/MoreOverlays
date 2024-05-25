package at.ridgo8.moreoverlays.lightoverlay.integration;

import at.ridgo8.moreoverlays.api.lightoverlay.LightOverlayReloadHandlerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class AlternateLightHandler {

    public static void init() {
        NeoForge.EVENT_BUS.register(new AlternateLightHandler());
    }

    @SubscribeEvent
    public void onLightOverlayEnable(LightOverlayReloadHandlerEvent event) {
        if (event.isIgnoringSpawner()) {
            return;
        }

		/*
		if(ModList.get().isLoaded("customspawner")){
			event.setScanner(CustomSpawnerLightScanner.class);
		}
		*/
    }

}
