package me.ionar.salhack.modules;
import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.combat.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import static me.ionar.salhack.modules.combat.CombatSupport.mc;

public final class CombatModules {
 public static void register(ModuleManager m){
  m.add(new ClientModule("Aimbot",Category.COMBAT,"Rotation service for aura, crystal and placement. Packet keeps the camera steady; Client turns it."){{mode("Mode","Packet","Client");}});
  m.add(new ClientModule("AntiBots",Category.COMBAT,"Excludes unlisted remote players from combat targeting; modern tab-list heuristic replaces broken upstream bot removal."));
  m.add(new ClientModule("Criticals",Category.COMBAT,"Attempts a critical immediately before an attack; server validation applies."){{mode("Mode","Packet","Jump");}});
  m.add(new ClientModule("Velocity",Category.COMBAT,"Scales local knockback, explosion and bobber impulses; composes with KTHREADD."){{number("Horizontal",0,0,100,5);number("Veritcal",0,0,100,5);flag("Explosions",true);flag("Bobbers",true);flag("NoPush",true);}});
  for(String name:List.of("AutoCrystal","AutoCrystalRewrite","OldAutoCrystalRewrite"))m.add(new CrystalCombatModule(name));
  m.add(new ClientModule("Surround",Category.COMBAT,"Centers and places reachable obsidian beside the feet."){
   private boolean centered;
   {flag("Toggles",false);flag("ToggleOffGround",false);mode("Center","NCP","Teleport","None");flag("Rotate",true);number("BlocksPerTick",1,1,10,1);flag("ActivateOnlyOnShift",false);}
   protected void onEnable(){centered=false;}
   public void tick(){if(!InventoryModules.ready())return;if(!mc.player.isOnGround()){if(flag("ToggleOffGround"))setEnabled(false);return;}if(flag("ActivateOnlyOnShift")&&!mc.options.sneakKey.isPressed())return;
    if(!centered&&!setting("Center").text().equals("None")){Vec3d center=Vec3d.ofBottomCenter(mc.player.getBlockPos());double dx=center.x-mc.player.getX(),dz=center.z-mc.player.getZ();if(setting("Center").text().equals("Teleport")){mc.player.setPosition(center.x,mc.player.getY(),center.z);centered=true;}else if(Math.hypot(dx,dz)>0.05){mc.player.setVelocity(dx*0.3,mc.player.getVelocity().y,dz*0.3);return;}else centered=true;}
    int placed=0;boolean complete=true;for(Direction d:Direction.Type.HORIZONTAL){BlockPos pos=mc.player.getBlockPos().offset(d);if(mc.world.getBlockState(pos).isReplaceable()){complete=false;if(placed<value("BlocksPerTick")&&CombatSupport.place(pos,Items.OBSIDIAN,flag("Rotate")))placed++;}}if(flag("Toggles")&&(complete||placed>0))setEnabled(false);
   }
  });
  m.add(new ClientModule("SelfTrap",Category.COMBAT,"Places reachable obsidian two blocks above the player."){
   {flag("HoleCheck",true);flag("Toggles",false);}
   public void tick(){if(!InventoryModules.ready()||!mc.player.isOnGround()||flag("HoleCheck")&&!CombatSupport.hole(mc.player.getBlockPos()))return;var p=mc.player.getBlockPos().up(2);if(CombatSupport.place(p,Items.OBSIDIAN,true)||!mc.world.isAir(p)){if(flag("Toggles"))setEnabled(false);}}
  });
  m.add(new ClientModule("AntiCityBoss",Category.COMBAT,"Places four reachable obsidian blocks two and three blocks in the direction faced."){
   {flag("TrapCheck",false);}
   public void tick(){if(!InventoryModules.ready()||flag("TrapCheck")&&mc.world.isAir(mc.player.getBlockPos().up(2)))return;for(int distance=2;distance<=3;distance++)for(int y=0;y<2;y++)if(CombatSupport.place(mc.player.getBlockPos().offset(mc.player.getHorizontalFacing(),distance).up(y),Items.OBSIDIAN,true))return;}
  });
  m.add(trap("AutoTrap",false));m.add(trap("AutoTrapFeet",true));m.add(new HoleFillerModule());m.add(new AutoCityModule());
  m.add(new ClientModule("BowSpam",Category.COMBAT,"Releases a held bow after three use ticks through normal interaction."){
   public void tick(){if(mc.player.isUsingItem()&&mc.player.getActiveItem().getItem() instanceof BowItem&&mc.player.getItemUseTime()>=3)mc.interactionManager.stopUsingItem(mc.player);}
  });
  m.add(new ClientModule("MiddleClickPearl",Category.COMBAT,"Throws one hotbar pearl per middle-button press; coordinates with MiddleClickFriends."){
   private boolean down;{flag("MiddleClickFriend",false);}
   public void tick(){boolean pressed=mc.currentScreen==null&&GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),GLFW.GLFW_MOUSE_BUTTON_MIDDLE)==GLFW.GLFW_PRESS;boolean click=pressed&&!down;down=pressed;if(!click||!InventoryModules.ready())return;if(!flag("MiddleClickFriend")&&m.on("MiddleClickFriends")&&mc.crosshairTarget instanceof EntityHitResult hit&&hit.getEntity() instanceof PlayerEntity)return;int slot=CombatSupport.find(Items.ENDER_PEARL);if(slot<0)return;int old=mc.player.getInventory().selectedSlot;try{mc.player.getInventory().selectedSlot=slot;mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);}finally{mc.player.getInventory().selectedSlot=old;}}
   protected void onDisable(){down=false;}
  });
  m.add(new AuraModule());
 }
 public static boolean bot(PlayerEntity p){return SalHackClient.INSTANCE.modules.on("AntiBots")&&mc.getCurrentServerEntry()!=null&&p.age>40&&mc.getNetworkHandler()!=null&&mc.getNetworkHandler().getPlayerListEntry(p.getUuid())==null;}
 private static ClientModule trap(String name,boolean feet){return new ClientModule(name,Category.COMBAT,"Places a reachable cage around the nearest non-friend player using normal placement."){
  private String announced="";
  {number(feet?"Distance":"range",5,1,10,0.5);number("BlocksPerTick",4,1,10,1);flag("Rotate",true);flag(feet?"Toggles":"toggleMode",!feet);if(!feet){mode("Mode","Full","Tall");flag("announceUsage",true);flag("EChests",false);}}
  public void tick(){if(!InventoryModules.ready())return;var targets=CombatSupport.players(value(feet?"Distance":"range"));if(targets.isEmpty())return;var target=targets.get(0);if(!feet&&flag("announceUsage")&&!announced.equals(target.getName().getString())){announced=target.getName().getString();SalHackClient.INSTANCE.notify("AutoTrap: "+announced);}var base=target.getBlockPos();List<BlockPos> positions=new ArrayList<>();for(int y=0;y<=(feet?0:2);y++)for(Direction d:Direction.Type.HORIZONTAL)positions.add(base.offset(d).up(y));if(!feet){positions.add(base.north().up(3));positions.add(base.up(3));if(setting("Mode").integer()==1)positions.add(base.up(4));}int placed=0;boolean complete=true;for(var pos:positions)if(mc.world.getBlockState(pos).isReplaceable()){complete=false;if(placed<value("BlocksPerTick")&&CombatSupport.place(pos,!feet&&flag("EChests")?Items.ENDER_CHEST:Items.OBSIDIAN,flag("Rotate")))placed++;}if(complete&&flag(feet?"Toggles":"toggleMode"))setEnabled(false);}
  protected void onDisable(){announced="";}
 };}
 private static final class AuraModule extends ClientModule {
  private int ticks,index,lastAttack=-100;private Entity prior;
  AuraModule(){super("KillAura",Category.COMBAT,"Attacks visible selected entities with modern cooldowns. Illegal 32k-only mode is excluded.");mode("Mode","Closest","Priority","Switch");number("Distance",3,0,6,0.25);flag("Hit Delay",true);flag("TPSSync",false);flag("Players",true);flag("Monsters",true);for(String s:List.of("Neutrals","Animals","Tamed","Projectile","SwordOnly","PauseIfCrystal","PauseIfEating","AutoSwitch"))flag(s,false);number("Ticks",10,0,40,1);number("Iterations",1,1,10,1);}
  public void tick(){if(!InventoryModules.ready()||flag("PauseIfEating")&&mc.player.isUsingItem()||flag("PauseIfCrystal")&&mc.player.isHolding(Items.END_CRYSTAL))return;if(flag("Hit Delay")?(mc.player.getAttackCooldownProgress(0)<1||flag("TPSSync")&&mc.player.age-lastAttack<(20/Math.max(0.1,mc.player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED)))*20/CombatHooks.tps()):++ticks<Math.max(1,value("Ticks")))return;ticks=0;
   List<Entity> targets=new ArrayList<>();for(var e:mc.world.getEntities())if(valid(e))targets.add(e);targets.sort(Comparator.comparingDouble(e->e.squaredDistanceTo(mc.player)));if(targets.isEmpty()){prior=null;return;}if(setting("Mode").text().equals("Priority")&&prior!=null&&targets.contains(prior)){targets.remove(prior);targets.add(0,prior);}int at=setting("Mode").text().equals("Switch")?Math.floorMod(index++,targets.size()):0;Entity target=targets.get(at);int old=mc.player.getInventory().selectedSlot,slot=CombatSupport.weapon();try{if(flag("AutoSwitch")&&slot>=0)mc.player.getInventory().selectedSlot=slot;if(flag("SwordOnly")&&!(mc.player.getMainHandStack().getItem() instanceof SwordItem))return;CombatSupport.look(target.getBoundingBox().getCenter(),false);for(int i=0;i<value("Iterations");i++){mc.interactionManager.attackEntity(mc.player,target);mc.player.swingHand(Hand.MAIN_HAND);if(flag("Hit Delay"))break;}prior=target;lastAttack=mc.player.age;}finally{mc.player.getInventory().selectedSlot=old;}
  }
  private boolean valid(Entity e){if(e==mc.player||!e.isAlive()||e.isSpectator()||e.distanceTo(mc.player)>value("Distance")||!mc.player.canSee(e))return false;if(e instanceof PlayerEntity p)return flag("Players")&&!p.isCreative()&&!CombatSupport.friend(p)&&!bot(p);if(e instanceof TameableEntity p&&p.isTamed())return flag("Tamed");if(e instanceof HostileEntity)return flag("Monsters");if(e instanceof AnimalEntity)return flag("Animals");if(e instanceof LivingEntity)return flag("Neutrals");return flag("Projectile")&&(e instanceof FireballEntity||e instanceof ShulkerBulletEntity);}
  protected void onDisable(){prior=null;ticks=index=0;lastAttack=-100;}
 }
}
