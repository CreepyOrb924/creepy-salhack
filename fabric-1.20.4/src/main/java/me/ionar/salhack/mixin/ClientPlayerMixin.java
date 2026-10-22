package me.ionar.salhack.mixin;
import me.ionar.salhack.SalHackClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin {
    @ModifyExpressionValue(method="tickMovement",at=@At(value="CONSTANT",args="floatValue=0.2"))
    private float salhack$noSlow(float original) {
        var c=SalHackClient.INSTANCE;
        return c!=null && c.modules.on("NoSlow") ? 1F : original;
    }
}
