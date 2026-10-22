package me.ionar.salhack.mixin.expansion.movement;
import me.ionar.salhack.modules.ExpansionMovementModules;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;

@Mixin(ClientConnection.class)
public class MovementConnectionMixin {
    @Inject(method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V",at=@At("HEAD"),cancellable=true)
    private void salhack$outbound(Packet<?> packet,PacketCallbacks callbacks,boolean flush,CallbackInfo ci) {
        if(!ready() || !mc.isOnThread() || sendingOwned || mc.getNetworkHandler()==null || mc.getNetworkHandler().getConnection()!=(Object)this) return;
        if(on("PacketCanceller")) {
            String setting=packet instanceof PlayerInputC2SPacket?"Input":packet instanceof PlayerMoveC2SPacket.Full?"PositionRotation":packet instanceof PlayerMoveC2SPacket.PositionAndOnGround?"Position":packet instanceof PlayerMoveC2SPacket.LookAndOnGround?"Rotation":packet instanceof UpdatePlayerAbilitiesC2SPacket?"PlayerAbilities":packet instanceof PlayerActionC2SPacket?"PlayerDigging":packet instanceof PlayerInteractItemC2SPacket?"PlayerTryUseItem":packet instanceof PlayerInteractBlockC2SPacket?"PlayerTryUseItemOnBlock":packet instanceof ClientCommandC2SPacket?"EntityAction":packet instanceof PlayerInteractEntityC2SPacket?"UseEntity":packet instanceof VehicleMoveC2SPacket?"VehicleMove":null;
            if(setting!=null && module("PacketCanceller").flag(setting)) {ci.cancel();return;}
        }
        if(packet instanceof TeleportConfirmC2SPacket && on("PortalGodMode")) {ci.cancel();return;}
        if(packet instanceof PlayerMoveC2SPacket move) {
            if(on("Ghost") && ghost.ghosted || on("PacketFly") && mc.currentScreen==null && !mc.player.isDead()) {ci.cancel();return;}
            MovementPacketAccess mutable=(MovementPacketAccess)move;
            if(!mc.player.isFallFlying()) {
                if(on("NoFall") && mc.player.fallDistance>3) {
                    int mode=module("NoFall").setting("Mode").integer();
                    if(mode==0) mutable.salhack$onGround(true);
                    else if(mode==2 && move.changesPosition()) mutable.salhack$y(mc.player.getY()+0.1);
                }
                if(flightActive() && module("Flight").flag("AntiFallDmg") && mc.player.fallDistance>3.8) {mutable.salhack$onGround(true);mc.player.fallDistance=0;}
            }
            if(on("Jesus") && module("Jesus").setting("Mode").integer()!=1 && !mc.player.hasVehicle() && !mc.options.jumpKey.isPressed() && mc.player.age%3==0 && !mc.player.isSneaking() && mc.player.fallDistance<3 && !onLiquid(0) && onLiquid(module("Jesus").value("Offset")) && move.changesPosition()) mutable.salhack$y(move.getY(mc.player.getY())-module("Jesus").value("Offset"));
        }
        if(on("Blink") && (packet instanceof PlayerMoveC2SPacket || packet instanceof TeleportConfirmC2SPacket || packet instanceof VehicleMoveC2SPacket && blink.flag("EntityBlink")) && blink.hold(packet)) ci.cancel();
    }
}
