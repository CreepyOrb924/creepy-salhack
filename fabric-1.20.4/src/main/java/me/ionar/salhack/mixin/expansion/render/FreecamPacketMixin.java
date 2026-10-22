package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.FreeCamera;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientConnection.class)
public abstract class FreecamPacketMixin {
 @Inject(method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V",at=@At("HEAD"),cancellable=true)private void salhack$packet(Packet<?> packet,PacketCallbacks callbacks,boolean flush,CallbackInfo ci){if(FreeCamera.blockPacket(packet))ci.cancel();}
}
