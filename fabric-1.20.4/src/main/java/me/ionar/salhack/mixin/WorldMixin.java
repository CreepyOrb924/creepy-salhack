package me.ionar.salhack.mixin;
import me.ionar.salhack.SalHackClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(method={"getRainGradient","getThunderGradient"},at=@At("HEAD"),cancellable=true)
    private void salhack$weather(float delta,CallbackInfoReturnable<Float> cir) {
        var c=SalHackClient.INSTANCE;
        if(((World)(Object)this).isClient && c!=null && c.modules.on("NoWeather")) cir.setReturnValue(0F);
    }
}
