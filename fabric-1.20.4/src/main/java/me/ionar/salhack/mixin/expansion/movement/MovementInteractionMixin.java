package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(ClientPlayerInteractionManager.class)
public class MovementInteractionMixin {
    @Unique private boolean salhack$restoreSneak;
    @Inject(method="interactEntityAtLocation",at=@At("HEAD"),cancellable=true)
    private void salhack$mount(PlayerEntity player,Entity entity,EntityHitResult hit,Hand hand,CallbackInfoReturnable<ActionResult> ci) {
        if(on("MountBypass") && entity instanceof AbstractDonkeyEntity) ci.setReturnValue(ActionResult.PASS);
    }
    @Inject(method="interactBlock",at=@At("HEAD"))
    private void salhack$beforeBlock(ClientPlayerEntity player,Hand hand,BlockHitResult hit,CallbackInfoReturnable<ActionResult> ci) {
        salhack$restoreSneak=false;
        if(!ready()) return;
        if(on("Sneak") && module("Sneak").setting("Mode").integer()!=2 && !player.isSneaking()) sneak(false);
        if(on("NoInteract") && !player.isSneaking() && (!module("NoInteract").flag("ContainerOnly") || mc.world.getBlockState(hit.getBlockPos()).createScreenHandlerFactory(mc.world,hit.getBlockPos())!=null)) {
            sneak(true); player.input.sneaking=true; salhack$restoreSneak=true;
        }
    }
    @Inject(method="interactBlock",at=@At("RETURN"))
    private void salhack$afterBlock(ClientPlayerEntity player,Hand hand,BlockHitResult hit,CallbackInfoReturnable<ActionResult> ci) {
        if(salhack$restoreSneak) { player.input.sneaking=false; sneak(false); salhack$restoreSneak=false; }
    }
}
