package me.ionar.salhack.modules.combat;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.InventoryModules;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.*;
import static me.ionar.salhack.modules.combat.CombatSupport.mc;
public final class AutoCityModule extends ClientModule {
 private BlockPos mining;private int previous=-1,owned=-1;
 public AutoCityModule(){super("AutoCity",Category.COMBAT,"Mines a reachable obsidian block beside the nearest enemy with normal block-breaking progress; restores owned tool selection.");}
 public void tick(){if(!InventoryModules.ready())return;if(mining!=null&&mc.world.isAir(mining)){setEnabled(false);return;}
  if(mining==null){for(var target:CombatSupport.players(5))for(Direction d:Direction.Type.HORIZONTAL){var p=target.getBlockPos().offset(d);if(mc.world.getBlockState(p).isOf(Blocks.OBSIDIAN)&&mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(p))<=20.25){mining=p;break;}}}if(mining==null)return;
  var state=mc.world.getBlockState(mining);float speed=0;int best=-1;for(int i=0;i<9;i++){var stack=mc.player.getInventory().getStack(i);float v=stack.getMiningSpeedMultiplier(state);if(stack.isSuitableFor(state)&&v>speed){speed=v;best=i;}}if(best<0)return;
  if(previous<0)previous=mc.player.getInventory().selectedSlot;owned=best;mc.player.getInventory().selectedSlot=best;CombatSupport.look(Vec3d.ofCenter(mining),false);mc.interactionManager.updateBlockBreakingProgress(mining,Direction.UP);mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
 }
 protected void onDisable(){if(mc.player!=null&&previous>=0&&mc.player.getInventory().selectedSlot==owned)mc.player.getInventory().selectedSlot=previous;if(mc.interactionManager!=null)mc.interactionManager.cancelBlockBreaking();mining=null;previous=owned=-1;}
}
