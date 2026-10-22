package me.ionar.salhack.mixin;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {
    @Inject(method="clipAtLedge",at=@At("RETURN"),cancellable=true)
    private void salhack$edge(CallbackInfoReturnable<Boolean> cir) {
        var mc=MinecraftClient.getInstance(); var c=SalHackClient.INSTANCE;
        if(c!=null && (Object)this==mc.player && c.modules.on("SafeWalk") && mc.player.isOnGround()) cir.setReturnValue(true);
    }
}
