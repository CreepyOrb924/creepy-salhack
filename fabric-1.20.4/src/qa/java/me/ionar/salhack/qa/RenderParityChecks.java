package me.ionar.salhack.qa;
import java.util.*;
import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.core.ClientModule;
import me.ionar.salhack.render.expansion.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.collection.DefaultedList;
/** Called inside the temporary QA world only; restores all owned toggles and keys. */
public final class RenderParityChecks {
 public static List<String> run(MinecraftClient mc){
  if(mc.player==null||mc.world==null)throw new AssertionError("Render checks require a QA world");
  var mods=SalHackClient.INSTANCE.modules;var free=mods.get("Freecam");var shulker=mods.get("ShulkerPreview");
  if(free==null||shulker==null)throw new AssertionError("Render registration absent");
  var originalCamera=mc.getCameraEntity();boolean freeEnabled=free.enabled(),previewEnabled=shulker.enabled(),forward=mc.options.forwardKey.isPressed();double mode=free.value("Mode"),cancel=free.value("Cancel Packets"),speed=free.value("Speed");float yaw=mc.player.getYaw(),pitch=mc.player.getPitch();var pos=mc.player.getPos();List<String> result=new ArrayList<>();
  try{
   free.setEnabled(false);originalCamera=mc.getCameraEntity();free.setting("Speed").set(1);free.setting("Mode").set(0);free.setting("Cancel Packets").set(1);free.setEnabled(true);
   check(FreeCamera.active()&&mc.getCameraEntity()!=mc.player,"Detached camera installed",result);
   mc.player.changeLookDirection(40,20);
   check(mc.player.getYaw()==yaw&&mc.player.getPitch()==pitch&&mc.getCameraEntity().getYaw()!=yaw,"Freecam mouse mixin routes look away from real body",result);
   mc.options.forwardKey.setPressed(true);free.tick();check(mc.getCameraEntity().squaredDistanceTo(pos)>0,"Freecam keyboard moves detached view",result);
   mc.player.input.movementForward=1;mc.player.tickMovement();check(mc.player.getPos().equals(pos),"Freecam normal movement mixin freezes body physics",result);
   check(FreeCamera.blockPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x,pos.y,pos.z,true)),"Normal mode isolates movement packets",result);
   free.setting("Mode").set(1);mc.player.input.tick(false,1);check(mc.player.input.movementForward==0&&!mc.player.input.jumping,"Camera mode physical input isolation mixin applied",result);
   check(!FreeCamera.blockPacket(new PlayerMoveC2SPacket.OnGroundOnly(true)),"Camera mode allows independent body automation packets",result);
   free.setEnabled(false);check(mc.getCameraEntity()==originalCamera&&!FreeCamera.active(),"Disabling restores previous camera",result);
   shulker.setEnabled(true);var stack=new ItemStack(Items.SHULKER_BOX);var items=DefaultedList.ofSize(27,ItemStack.EMPTY);items.set(13,new ItemStack(Items.DIAMOND,17));var nbt=new NbtCompound();Inventories.writeNbt(nbt,items);BlockItem.setBlockEntityNbt(stack,BlockEntityType.SHULKER_BOX,nbt);
   var tooltip=stack.getTooltipData();check(tooltip.isPresent()&&tooltip.get() instanceof ShulkerTooltips.Data data&&data.items().get(13).isOf(Items.DIAMOND)&&data.items().get(13).getCount()==17,"Shulker tooltip mixin decodes actual 27-slot NBT contents",result);
   check(stack.getTooltip(mc.player,net.minecraft.client.item.TooltipContext.BASIC).stream().noneMatch(t->t.getString().contains("Diamond")),"Shulker visual preview suppresses the redundant vanilla item list",result);
   shulker.setEnabled(false);check(stack.getTooltipData().isEmpty(),"Shulker tooltip hook dormant when disabled",result);
   check(stack.getTooltip(mc.player,net.minecraft.client.item.TooltipContext.BASIC).stream().anyMatch(t->t.getString().contains("Diamond")),"Disabling ShulkerPreview restores the vanilla item list",result);
  }finally{
   free.setEnabled(false);free.setting("Mode").set(mode);free.setting("Speed").set(speed);free.setting("Cancel Packets").set(cancel);mc.options.forwardKey.setPressed(forward);mc.player.setYaw(yaw);mc.player.setPitch(pitch);mc.player.input.tick(false,1);shulker.setEnabled(previewEnabled);free.setEnabled(freeEnabled);
  }
  return result;
 }
 /** Leave enabled for at least two rendered frames, then run the returned restore action. */
 public static Runnable beginOverlaySmoke(MinecraftClient mc){
  var modules=SalHackClient.INSTANCE.modules;Map<ClientModule,Boolean> states=new LinkedHashMap<>();
  for(String name:List.of("AntiFog","BlockHighlight","BreakHighlight","CityESP","HoleESP","MobOwnerModule","NoRender","Skeleton","SmallShield","Trajectories","ViewClip","VoidESP","NewChunks")){var module=modules.get(name);states.put(module,module.enabled());module.setEnabled(true);}
  var held=mc.player.getMainHandStack().copy();mc.player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND,new ItemStack(Items.SNOWBALL));
  var owner=new net.minecraft.entity.passive.WolfEntity(net.minecraft.entity.EntityType.WOLF,mc.world);owner.setOwnerUuid(mc.player.getUuid());owner.setId(-9777);owner.setPosition(mc.player.getPos().add(3,0,3));mc.world.addEntity(owner);
  mc.worldRenderer.setBlockBreakingInfo(-9776,mc.player.getBlockPos().add(2,0,2),5);
  return ()->{mc.player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND,held);mc.world.removeEntity(owner.getId(),net.minecraft.entity.Entity.RemovalReason.DISCARDED);mc.worldRenderer.setBlockBreakingInfo(-9776,mc.player.getBlockPos().add(2,0,2),-1);states.forEach(ClientModule::setEnabled);};
 }
 private static void check(boolean ok,String description,List<String> out){if(!ok)throw new AssertionError(description);out.add("PASS "+description);}
}


