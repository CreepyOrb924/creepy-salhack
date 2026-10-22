package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderDispatcher.class)
public abstract class ItemEntityMixin {
 @Inject(method="render",at=@At("HEAD"),cancellable=true)private void salhack$item(Entity entity,double x,double y,double z,float yaw,float delta,MatrixStack matrices,VertexConsumerProvider providers,int light,CallbackInfo ci){if(entity instanceof ItemEntity&&RenderHooks.on("NoRender")&&me.ionar.salhack.SalHackClient.INSTANCE.modules.get("NoRender").value("NoItemsMode")>0)ci.cancel();}
}
