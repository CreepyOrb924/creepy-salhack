package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemMixin {
 @Shadow private float equipProgressMainHand;
 @Shadow private float equipProgressOffHand;
 @Shadow private float prevEquipProgressMainHand;
 @Shadow private float prevEquipProgressOffHand;
 @Inject(method="updateHeldItems",at=@At("TAIL"))private void salhack$hands(CallbackInfo ci){if(!RenderHooks.on("SmallShield"))return;var mc=MinecraftClient.getInstance();if(mc.player==null)return;var m=me.ionar.salhack.SalHackClient.INSTANCE.modules.get("SmallShield");equipProgressMainHand=prevEquipProgressMainHand=(float)(m.flag("TallSword")&&mc.player.getMainHandStack().getItem() instanceof SwordItem?2:m.value("MainHand"));equipProgressOffHand=prevEquipProgressOffHand=(float)m.value("OffHand");}
 @Inject(method="renderFirstPersonMap",at=@At("HEAD"),cancellable=true)private void salhack$map(CallbackInfo ci){if(RenderHooks.hide("Maps"))ci.cancel();}
}
