package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.config.Config;
//import at.ridgo8.moreoverlays.gui.ConfigScreen;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//@Mod(modid = MoreOverlays.MOD_ID, updateJSON = MoreOverlays.UPDATE_JSON, version = MoreOverlays.VERSION, name = MoreOverlays.NAME, clientSideOnly = true, dependencies = "required-after:forge@[14.23.5.2768,);after:jei@[4.15.0.268,);", guiFactory = "at.ridgo8.moreoverlays.config.GuiFactory")
@Mod(MoreOverlays.MOD_ID)
public class MoreOverlays {

    public static final String MOD_ID = "moreoverlays";
    public static final String NAME = "MoreOverlays";
    //public static final String VERSION = "1.15.1";
    //public static final String UPDATE_JSON = "https://raw.githubusercontent.com/ridgo8/Mod_Update-JSONs/master/MoreOverlays.json";

    public static Logger logger = LogManager.getLogger(NAME);

    public MoreOverlays() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ModLoadingContext ctx = ModLoadingContext.get();

        modBus.addListener(this::onClientInit);

        Config.initialize();

        ctx.registerConfig(ModConfig.Type.CLIENT, Config.config_client, MOD_ID + ".toml");
        ctx.registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ctx.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ClientRegistrationHandler::openSettings);
    }

    public void onClientInit(FMLClientSetupEvent event) {
        ClientRegistrationHandler.setupClient();
    }


}
