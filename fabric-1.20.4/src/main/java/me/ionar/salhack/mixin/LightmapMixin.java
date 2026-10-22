package me.ionar.salhack.mixin;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LightmapTextureManager.class)
public abstract class LightmapMixin {
    @Inject(method="getBrightness",at=@At("HEAD"),cancellable=true)
    private static void salhack$brightness(DimensionType dimension,int light,CallbackInfoReturnable<Float> cir) {
        var c=SalHackClient.INSTANCE;
        if(c!=null && c.modules.on("Brightness")) cir.setReturnValue(1F);
    }
}
