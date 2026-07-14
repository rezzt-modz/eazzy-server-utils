package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.rezzt.eazzyserverutils.EazzyServerUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EazzyServerUtils.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        HomeCommands.register(dispatcher);
        WarpCommands.register(dispatcher);
        HeadCommand.register(dispatcher);
        UtilityCommands.register(dispatcher);
        TPACommands.register(dispatcher);
        StaffCommands.register(dispatcher);
        PunishmentCommands.register(dispatcher);
        BackCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        NearCommand.register(dispatcher);
        MessageCommands.register(dispatcher);
        BroadcastCommand.register(dispatcher);
        LockChatCommand.register(dispatcher);
        LagCommand.register(dispatcher);
        DimensionCommand.register(dispatcher);
    }
}
