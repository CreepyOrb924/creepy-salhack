package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEntityModel.class)
public abstract class SkeletonPoseMixin {
 @Inject(method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))private void salhack$pose(LivingEntity entity,float a,float b,float c,float d,float e,CallbackInfo ci){if(!RenderHooks.on("Skeleton"))return;var model=(BipedEntityModel<?>)(Object)this;ModelPart[] parts={model.head,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg};float[][] pose=new float[5][3];for(int i=0;i<5;i++){pose[i][0]=parts[i].pitch;pose[i][1]=parts[i].yaw;pose[i][2]=parts[i].roll;}ExpansionOverlay.poses.put(entity.getUuid(),pose);}
}
