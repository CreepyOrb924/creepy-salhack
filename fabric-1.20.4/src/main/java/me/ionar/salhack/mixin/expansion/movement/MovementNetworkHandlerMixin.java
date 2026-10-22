package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(ClientPlayNetworkHandler.class)
public class MovementNetworkHandlerMixin {
    @Unique private float salhack$yaw,salhack$pitch;
    @Unique private float salhack$prevYaw,salhack$prevPitch;
    @Unique private boolean salhack$restore;
    @Inject(method="onPlayerPositionLook",at=@At(value="INVOKE",target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",shift=At.Shift.AFTER),cancellable=true)
    private void salhack$position(PlayerPositionLookS2CPacket packet,CallbackInfo ci) {
        salhack$restore=false;
        if(!ready() || mc.player.age<1) return;
        if(on("PacketFly") && mc.currentScreen==null) {
            Vec3d p=mc.player.getPos(); var flags=packet.getFlags();
            double x=packet.getX()+(flags.contains(PositionFlag.X)?p.x:0),y=packet.getY()+(flags.contains(PositionFlag.Y)?p.y:0),z=packet.getZ()+(flags.contains(PositionFlag.Z)?p.z:0);
            float yaw=packet.getYaw()+(flags.contains(PositionFlag.Y_ROT)?mc.player.getYaw():0),pitch=packet.getPitch()+(flags.contains(PositionFlag.X_ROT)?mc.player.getPitch():0);
            mc.player.setPosition(x,y,z); sendOwned(new TeleportConfirmC2SPacket(packet.getTeleportId())); sendOwned(new PlayerMoveC2SPacket.Full(x,y,z,yaw,pitch,false)); ci.cancel(); return;
        }
        if(on("NoRotate") && mc.currentScreen==null) {salhack$yaw=mc.player.getYaw();salhack$pitch=mc.player.getPitch();salhack$prevYaw=mc.player.prevYaw;salhack$prevPitch=mc.player.prevPitch;salhack$restore=true;}
    }
    @Inject(method="onPlayerPositionLook",at=@At("RETURN"))
    private void salhack$restore(PlayerPositionLookS2CPacket packet,CallbackInfo ci) {
        if(salhack$restore && ready()) { mc.player.setYaw(salhack$yaw);mc.player.setPitch(salhack$pitch);mc.player.prevYaw=salhack$prevYaw;mc.player.prevPitch=salhack$prevPitch; salhack$restore=false; }
    }
    @Inject(method="onEntitiesDestroy",at=@At(value="INVOKE",target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",shift=At.Shift.AFTER))
    private void salhack$destroy(EntitiesDestroyS2CPacket packet,CallbackInfo ci) {
        if(on("EntityDesync") && desync.riding!=null && packet.getEntityIds().contains(desync.riding.getId())) { desync.abandon();desync.setEnabled(false); }
    }
    @Inject(method="onEntityPassengersSet",at=@At(value="INVOKE",target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",shift=At.Shift.AFTER))
    private void salhack$passengers(EntityPassengersSetS2CPacket packet,CallbackInfo ci) {
        if(on("EntityDesync") && ready() && desync.riding!=null && packet.getId()==desync.riding.getId()) {
            boolean present=false;for(int id:packet.getPassengerIds()) if(id==mc.player.getId()) present=true;
            if(!present) {desync.setEnabled(false);}
        }
    }
}
