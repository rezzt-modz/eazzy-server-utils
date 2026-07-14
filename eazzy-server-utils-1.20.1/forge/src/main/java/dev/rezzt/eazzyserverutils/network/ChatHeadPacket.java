package dev.rezzt.eazzyserverutils.network;

import dev.rezzt.eazzyserverutils.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ChatHeadPacket {
    private final Component message;
    private final UUID sender;

    public ChatHeadPacket(Component message, UUID sender) {
        this.message = message;
        this.sender = sender;
    }

    public Component getMessage() {
        return message;
    }

    public UUID getSender() {
        return sender;
    }

    public static void encode(ChatHeadPacket packet, FriendlyByteBuf buf) {
        buf.writeComponent(packet.message);
        buf.writeUUID(packet.sender);
    }

    public static ChatHeadPacket decode(FriendlyByteBuf buf) {
        return new ChatHeadPacket(buf.readComponent(), buf.readUUID());
    }

    public static void handle(ChatHeadPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handle(packet)));
        ctx.get().setPacketHandled(true);
    }
}
