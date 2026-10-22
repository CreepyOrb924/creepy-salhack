package me.ionar.salhack.mixin.expansion.combat;
import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.modules.combat.CombatHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayNetworkHandler.class)
public abstract class CombatNetworkMixin {
 @Unique private Vec3d salhack$beforeVelocity,salhack$beforeExplosion,salhack$beforeBobber;
 @Unique private boolean salhack$active(){var c=SalHackClient.INSTANCE;return c!=null&&c.modules!=null&&!me.ionar.salhack.core.CombatCoexistence.deferred("Velocity")&&c.modules.on("Velocity")&&MinecraftClient.getInstance().player!=null;}
 @Unique private void salhack$scale(Vec3d old){if(old==null||!salhack$active())return;var p=MinecraftClient.getInstance().player;var m=SalHackClient.INSTANCE.modules.get("Velocity");Vec3d delta=p.getVelocity().subtract(old);p.setVelocity(old.add(delta.x*m.value("Horizontal")/100,delta.y*m.value("Veritcal")/100,delta.z*m.value("Horizontal")/100));}
 @Inject(method="onEntityVelocityUpdate",at=@At("HEAD"))private void salhack$before(EntityVelocityUpdateS2CPacket packet,CallbackInfo ci){var mc=MinecraftClient.getInstance();if(mc.isOnThread())salhack$beforeVelocity=salhack$active()&&packet.getId()==mc.player.getId()?mc.player.getVelocity():null;}
 @Inject(method="onEntityVelocityUpdate",at=@At("TAIL"))private void salhack$after(EntityVelocityUpdateS2CPacket packet,CallbackInfo ci){salhack$scale(salhack$beforeVelocity);salhack$beforeVelocity=null;}
 @Inject(method="onExplosion",at=@At("HEAD"))private void salhack$beforeExplosion(ExplosionS2CPacket packet,CallbackInfo ci){var mc=MinecraftClient.getInstance();if(mc.isOnThread())salhack$beforeExplosion=salhack$active()&&SalHackClient.INSTANCE.modules.get("Velocity").flag("Explosions")?mc.player.getVelocity():null;}
 @Inject(method="onExplosion",at=@At("TAIL"))private void salhack$afterExplosion(ExplosionS2CPacket packet,CallbackInfo ci){salhack$scale(salhack$beforeExplosion);salhack$beforeExplosion=null;}
 @Inject(method="onEntityStatus",at=@At("HEAD"))private void salhack$beforeBobber(EntityStatusS2CPacket packet,CallbackInfo ci){var mc=MinecraftClient.getInstance();if(mc.isOnThread())salhack$beforeBobber=salhack$active()&&SalHackClient.INSTANCE.modules.get("Velocity").flag("Bobbers")&&packet.getStatus()==31&&packet.getEntity(mc.world) instanceof FishingBobberEntity b&&b.getHookedEntity()==mc.player?mc.player.getVelocity():null;}
 @Inject(method="onEntityStatus",at=@At("TAIL"))private void salhack$afterBobber(EntityStatusS2CPacket packet,CallbackInfo ci){if(salhack$beforeBobber!=null&&MinecraftClient.getInstance().player!=null)MinecraftClient.getInstance().player.setVelocity(salhack$beforeBobber);salhack$beforeBobber=null;}
 @Inject(method="onWorldTimeUpdate",at=@At("TAIL"))private void salhack$time(WorldTimeUpdateS2CPacket packet,CallbackInfo ci){CombatHooks.time();}
}
