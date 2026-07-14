package dev.rezzt.eazzyserverutils;

import dev.rezzt.eazzyserverutils.commands.BroadcastCommand;
import dev.rezzt.eazzyserverutils.network.ESUPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(EazzyServerUtils.MODID)
public class EazzyServerUtilsForge {

    public EazzyServerUtilsForge() {
        FileConfig.load();
        ChatConfig.load();
        ESUPacketHandler.register();
        MinecraftForge.EVENT_BUS.register(EazzyServerUtils.class);
        MinecraftForge.EVENT_BUS.register(CommonEvents.class);
        MinecraftForge.EVENT_BUS.register(ChatEvents.class);
        MinecraftForge.EVENT_BUS.register(StaffEvents.class);
        MinecraftForge.EVENT_BUS.register(BroadcastCommand.class);
        MinecraftForge.EVENT_BUS.register(DimensionEvents.class);
    }
}
