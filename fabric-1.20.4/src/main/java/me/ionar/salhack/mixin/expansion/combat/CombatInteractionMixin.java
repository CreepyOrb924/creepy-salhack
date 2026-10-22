package me.ionar.salhack.mixin.expansion.combat;
import me.ionar.salhack.modules.combat.CombatHooks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayerInteractionManager.class)
public abstract class CombatInteractionMixin {
 @Inject(method="attackEntity",at=@At("HEAD"))private void salhack$critical(PlayerEntity player,Entity target,CallbackInfo ci){CombatHooks.critical(target);}
}
