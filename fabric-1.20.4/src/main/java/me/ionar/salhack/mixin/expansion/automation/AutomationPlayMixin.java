package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import me.ionar.salhack.automation.WorldDiagnostics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayNetworkHandler.class)
public class AutomationPlayMixin {
 @Inject(method="onEntityStatus",at=@At("TAIL")) private void status(EntityStatusS2CPacket packet,CallbackInfo ci){var mc=MinecraftClient.getInstance();if(mc.world!=null)AutomationHooks.status(packet.getEntity(mc.world),packet.getStatus());}
 @Inject(method="onPlaySound",at=@At("TAIL")) private void sound(PlaySoundS2CPacket packet,CallbackInfo ci){WorldDiagnostics.sound(packet);}
 @Inject(method="onWorldEvent",at=@At("TAIL")) private void worldEvent(WorldEventS2CPacket packet,CallbackInfo ci){WorldDiagnostics.worldEvent(packet);}
 @Inject(method="onWorldTimeUpdate",at=@At("TAIL")) private void time(WorldTimeUpdateS2CPacket packet,CallbackInfo ci){WorldDiagnostics.timeUpdate();}
 @Inject(method="onPlayerPositionLook",at=@At("TAIL")) private void correction(PlayerPositionLookS2CPacket packet,CallbackInfo ci){WorldDiagnostics.correction();}
}
