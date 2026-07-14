package dev.rezzt.eazzyserverutils;

import com.mojang.logging.LogUtils;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import dev.rezzt.eazzyserverutils.storage.BanManager;
import dev.rezzt.eazzyserverutils.storage.HomeManager;
import dev.rezzt.eazzyserverutils.storage.WarningManager;
import dev.rezzt.eazzyserverutils.storage.WarpManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

public class EazzyServerUtils {
    public static final String MODID = "eazzyserverutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Loading Eazzy Server Utils data...");
        HomeManager.load(event.getServer());
        WarpManager.load(event.getServer());
        WarningManager.load(event.getServer());
        BanManager.load(event.getServer());
        DimensionLockManager.load(event.getServer());
    }
}
