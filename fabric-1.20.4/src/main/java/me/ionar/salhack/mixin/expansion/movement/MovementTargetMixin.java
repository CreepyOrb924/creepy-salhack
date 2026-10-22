package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import java.util.function.Predicate;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(GameRenderer.class)
public class MovementTargetMixin {
    @ModifyArg(method="updateTargetedEntity",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"),index=2)
    private boolean salhack$liquidRay(boolean original) {return original || on("LiquidInteract");}
    @WrapOperation(method="updateTargetedEntity",at=@At(value="INVOKE",target="Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    private EntityHitResult salhack$miningTrace(Entity entity,Vec3d min,Vec3d max,Box box,Predicate<Entity> predicate,double distance,Operation<EntityHitResult> original) {
        if(ready() && on("NoMiningTrace") && (!module("NoMiningTrace").flag("Pickaxe") || mc.player.getMainHandStack().getItem() instanceof PickaxeItem)) return null;
        return original.call(entity,min,max,box,predicate,distance);
    }
}
