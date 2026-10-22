package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientCommonNetworkHandler.class)
public class AutomationNetworkMixin {
 @Shadow @Final protected ServerInfo serverInfo;
 @Inject(method="onDisconnected",at=@At("HEAD")) private void reconnect(Text reason,CallbackInfo ci){AutomationHooks.disconnected(serverInfo);}
 @Inject(method="sendPacket",at=@At("HEAD"),cancellable=true) private void xcarry(Packet<?> packet,CallbackInfo ci){if(packet instanceof CloseHandledScreenC2SPacket close&&AutomationHooks.on("XCarry")&&(close.getSyncId()==0||AutomationHooks.manager.get("XCarry").flag("ForceCancel")))ci.cancel();}
}
