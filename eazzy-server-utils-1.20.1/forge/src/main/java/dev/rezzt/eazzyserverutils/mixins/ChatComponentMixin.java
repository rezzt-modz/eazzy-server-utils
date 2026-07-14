package dev.rezzt.eazzyserverutils.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.rezzt.eazzyserverutils.ChatConfig;
import dev.rezzt.eazzyserverutils.client.ChatHeadRenderer;
import dev.rezzt.eazzyserverutils.client.ClientChatData;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    private static final ThreadLocal<GuiMessage.Line> CURRENT_LINE = new ThreadLocal<>();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V", at = @At("HEAD"))
    private void eazzyserverutils$clearRenderedHeads(CallbackInfo ci) {
        ClientChatData.RENDERED_ADDED_TIMES.clear();
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private GuiMessage.Line eazzyserverutils$captureLine(GuiMessage.Line line) {
        CURRENT_LINE.set(line);
        return line;
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"
            )
    )
    private int eazzyserverutils$renderChatHead(
            GuiGraphics guiGraphics,
            Font font,
            FormattedCharSequence text,
            int x,
            int y,
            int color,
            Operation<Integer> original
    ) {
        GuiMessage.Line line = CURRENT_LINE.get();
        UUID uuid = line == null ? null : ClientChatData.TIME_TO_UUID.get(line.addedTime());
        if (uuid == null || !ChatConfig.showHeadInline()) {
            return original.call(guiGraphics, font, text, x, y, color);
        }

        int headSize = 8;
        int spacing = 4;
        int leftMargin = 3;
        int offset = leftMargin + headSize + spacing;

        if (!ClientChatData.RENDERED_ADDED_TIMES.contains(line.addedTime())) {
            ChatHeadRenderer.render(guiGraphics, uuid, x + leftMargin, y, headSize);
            ClientChatData.RENDERED_ADDED_TIMES.add(line.addedTime());
        }

        return original.call(guiGraphics, font, text, x + offset, y, color);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V", at = @At("RETURN"))
    private void eazzyserverutils$clearCurrentLine(CallbackInfo ci) {
        CURRENT_LINE.remove();
    }
}
