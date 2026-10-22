package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.ChatAutomation;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.client.gui.hud.MessageIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChatHud.class)
public class AutomationChatMixin {
 @ModifyVariable(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",at=@At("HEAD"),argsOnly=true) private Text format(Text text){Text result=ChatAutomation.incoming(text);return result==null?text:result;}
 @Inject(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",at=@At("HEAD"),cancellable=true) private void filter(Text text,MessageSignatureData signature,MessageIndicator indicator,CallbackInfo ci){if(ChatAutomation.filtered(text))ci.cancel();}
 @ModifyConstant(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",constant=@Constant(intValue=100)) private int history(int vanilla){return ChatAutomation.historyLimit();}
}
