package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.block.*;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(FluidBlock.class)
public class MovementFluidMixin {
    @Inject(method="getCollisionShape",at=@At("HEAD"),cancellable=true)
    private void salhack$liquid(BlockState state,BlockView world,BlockPos pos,ShapeContext context,CallbackInfoReturnable<VoxelShape> ci) {
        if(liquidCollision(pos)) {
            // A remote entity must never acquire the local player's water collision.
            if(!(context instanceof EntityShapeContext entity) || (entity.getEntity()!=mc.player && entity.getEntity()!=mc.player.getVehicle())) return;
            double height=mc.player.hasVehicle()?1-module("Jesus").value("Offset"):module("Jesus").setting("Mode").integer()==2?0.9:1;
            ci.setReturnValue(VoxelShapes.cuboid(0,0,0,1,Math.max(0.01,height),1));
        }
    }
}
