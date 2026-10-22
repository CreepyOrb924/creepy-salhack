package me.ionar.salhack.automation;
import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.*;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import java.util.*;
import java.util.function.Predicate;

public final class WorldAutomation {
 private static final MinecraftClient mc=MinecraftClient.getInstance();
 public static void register(ModuleManager m){
  m.add(new ClientModule("NoGlitchBlocks",Category.WORLD,"Waits for server confirmation instead of predicting local block removal and placement."){{flag("Destroy",true);flag("Place",true);}});
  m.add(new ClientModule("SpeedyGonzales",Category.WORLD,"Removes mining cooldown and completes client damage at the configured threshold; server validation still applies."){
   {number("Threshold",0.7,0.1,1,0.1);flag("Reset",true);flag("FastFall",false);}
   public void tick(){if(flag("FastFall")&&mc.player.isOnGround())mc.player.setVelocity(mc.player.getVelocity().subtract(0,1,0));}
   protected void onDisable(){if(mc.interactionManager!=null)mc.interactionManager.cancelBlockBreaking();}
  });
  m.add(new ClientModule("Scaffold",Category.WORLD,"Places a solid hotbar block beneath your feet using a reachable support face."){
   private long next;{mode("Mode","Tower","Normal");flag("StopMotion",true);number("Delay",0,0,1,0.1);}
   public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;BlockPos p=mc.player.getBlockPos().down();int slot=blockSlot(null);if(slot<0)return;boolean needs=mc.world.getBlockState(p).isReplaceable();if(needs){boolean placed=place(p,slot);if(!placed&&flag("StopMotion"))mc.player.setVelocity(0,mc.player.getVelocity().y,0);next=System.currentTimeMillis()+(long)(value("Delay")*1000);}if(value("Mode")==0&&mc.options.jumpKey.isPressed()&&mc.player.isOnGround())mc.player.jump();}
  });
  m.add(new ClientModule("TorchAnnihilator",Category.WORLD,"Breaks the nearest reachable torch using ordinary mining."){{number("Radius",4,0,10,1);flag("RedstoneTorches",true);}public void tick(){if(!InventoryModules.ready())return;mine(nearestBlock(value("Radius"),p->{var b=mc.world.getBlockState(p).getBlock();return b instanceof TorchBlock&&(!(b instanceof RedstoneTorchBlock)||flag("RedstoneTorches"));}),true);}});
  m.add(new ClientModule("Nuker",Category.WORLD,"Mines nearby reachable blocks one at a time; creative mode still requires creative permissions."){
   private Block selected;{mode("Mode","Survival","Creative");flag("Click Select",false);flag("Flatten",false);flag("Rotates",true);flag("Raytrace",true);number("Range",3,0,10,1);}
   public void tick(){if(!InventoryModules.ready())return;if(flag("Click Select")&&mc.options.attackKey.isPressed()&&mc.crosshairTarget instanceof BlockHitResult h)selected=mc.world.getBlockState(h.getBlockPos()).getBlock();if(flag("Click Select")&&selected==null)return;if(value("Mode")==1&&!mc.player.isCreative())return;BlockPos target=nearestBlock(value("Range"),p->(!flag("Flatten")||p.getY()>=mc.player.getBlockY())&&(!flag("Click Select")||mc.world.getBlockState(p).getBlock()==selected)&&mc.world.getBlockState(p).getHardness(mc.world,p)>=0,flag("Raytrace"));mine(target,flag("Rotates"),flag("Raytrace"));}
   protected void onDisable(){selected=null;if(mc.interactionManager!=null)mc.interactionManager.cancelBlockBreaking();}
  });
  m.add(new ClientModule("AutoTunnel",Category.WORLD,"Mines a cardinal tunnel with normal reachable mining; pauses AutoWalk at the active face."){
   private boolean resumeWalk;{mode("Mode","Tunnel1x2","Tunnel2x2","Tunnel2x3","Tunnel3x3");flag("PauseAutoWalk",true);}
   public void tick(){if(!InventoryModules.ready())return;int mode=(int)value("Mode"),width=mode==0?1:mode==3?3:2,height=mode<2?2:3;Direction forward=mc.player.getHorizontalFacing(),right=forward.rotateYClockwise();BlockPos origin=mc.player.getBlockPos();BlockPos best=null;double distance=Double.MAX_VALUE;for(int z=1;z<=3;z++)for(int x=-(width/2);x<width-width/2;x++)for(int y=0;y<height;y++){BlockPos p=origin.offset(forward,z).offset(right,x).up(y);if(!mc.world.getBlockState(p).isAir()&&mc.world.getBlockState(p).getHardness(mc.world,p)>=0&&visible(p)){double d=mc.player.squaredDistanceTo(Vec3d.ofCenter(p));if(d<distance){distance=d;best=p;}}}var walk=m.get("AutoWalk");if(walk!=null&&flag("PauseAutoWalk")){if(best!=null&&walk.enabled()){resumeWalk=true;walk.setEnabled(false);}else if(best==null&&resumeWalk){walk.setEnabled(true);resumeWalk=false;}}mine(best,true);}
   protected void onDisable(){if(resumeWalk&&m.get("AutoWalk")!=null&&mc.world!=null)m.get("AutoWalk").setEnabled(true);resumeWalk=false;if(mc.interactionManager!=null)mc.interactionManager.cancelBlockBreaking();}
  });
  m.add(new ClientModule("EnderChestFarmer",Category.WORLD,"Mines reachable ender chests, placing a hotbar chest when none is present."){
   private long next;{number("Radius",4,0,10,1);number("Delay",0.5,0,10,0.1);}
   public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;BlockPos p=nearestBlock(value("Radius"),pos->mc.world.getBlockState(pos).isOf(Blocks.ENDER_CHEST));if(p!=null){mine(p,true);return;}int slot=ExpansionAutomationModules.hotbar(Items.ENDER_CHEST);if(slot>=0){BlockPos target=mc.player.getBlockPos().offset(mc.player.getHorizontalFacing());if(place(target,slot))next=System.currentTimeMillis()+(long)(value("Delay")*1000);}}
  });
  m.add(new Builder("AutoBuilder",false));m.add(new Builder("AutoHighwayBuilder",true));
  m.add(new ClientModule("AutoWither",Category.WORLD,"Builds the soul-sand and skull T at the selected block with server-confirmed placements."){
   private BlockPos origin;private Direction facing;private int index;private long next;
   protected void onEnable(){origin=null;index=0;}
   public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;if(origin==null){if(!(mc.crosshairTarget instanceof BlockHitResult hit)){setEnabled(false);return;}origin=hit.getBlockPos().offset(hit.getSide());facing=mc.player.getHorizontalFacing();}
    var cells=BuildPlans.wither();while(index<cells.size()){var cell=cells.get(index);BlockPos p=transform(origin,facing,cell);Block expected=index<4?Blocks.SOUL_SAND:Blocks.WITHER_SKELETON_SKULL;if(mc.world.getBlockState(p).isOf(expected)){index++;continue;}if(!mc.world.getBlockState(p).isReplaceable()){ExpansionAutomationModules.say("AutoWither: obstructed at "+p.toShortString());setEnabled(false);return;}int slot=blockSlot(expected);if(slot<0)return;place(p,slot);next=System.currentTimeMillis()+150;return;}setEnabled(false);
   }
  });
 }
 private static final class Builder extends ClientModule {
  private final boolean highway;private BlockPos origin;private Direction facing;private long next;
  Builder(String name,boolean highway){super(name,Category.WORLD,"Places planned blocks using normal interaction; waits for reachable support faces.");this.highway=highway;
   if(highway)mode("Mode","ThreeWide","FourWide","FiveWide","SevenWide","RightRail","LeftRail","HighwayTunnel","HighwayWall");else mode("Mode","Portal","Flat","Tower","Wall","Stair");mode("BuildingMode","Dynamic","Static");number("BlocksPerTick",4,1,10,1);number("Delay",0,0,1,0.1);
  }
  protected void onEnable(){origin=null;}
  public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;if(origin==null||value("BuildingMode")==0){origin=mc.player.getBlockPos();facing=mc.player.getHorizontalFacing();}int slot=blockSlot(!highway&&value("Mode")==0?Blocks.OBSIDIAN:null);if(slot<0)return;int count=0;for(var c:plan()){BlockPos p=transform(origin,facing,c);if(mc.world.getBlockState(p).isReplaceable()&&place(p,slot)&&++count>=value("BlocksPerTick"))break;}next=System.currentTimeMillis()+(long)(value("Delay")*1000);}
  private List<BuildPlans.Cell> plan(){int mode=(int)value("Mode");List<BuildPlans.Cell> p=new ArrayList<>();if(highway){if(mode<4)return BuildPlans.highway(new int[]{3,4,5,7}[mode]);for(int z=0;z<3;z++){if(mode==4||mode==5)p.add(new BuildPlans.Cell(mode==4?2:-2,0,z));else{for(int y=0;y<3;y++){p.add(new BuildPlans.Cell(-2,y,z));p.add(new BuildPlans.Cell(2,y,z));}if(mode==6)for(int x=-2;x<=2;x++)p.add(new BuildPlans.Cell(x,3,z));}}return p;}
   switch(mode){case 0:return BuildPlans.portal();case 1:for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)p.add(new BuildPlans.Cell(x,-1,z));break;case 2:for(int y=-1;y<=1;y++)p.add(new BuildPlans.Cell(0,y,0));break;case 3:for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++)p.add(new BuildPlans.Cell(x,y,2));break;case 4:for(int z=1;z<=4;z++)for(int x=-1;x<=1;x++)p.add(new BuildPlans.Cell(x,z-2,z));break;}return p;
  }
 }
 public static BlockPos transform(BlockPos origin,Direction facing,BuildPlans.Cell c){return origin.offset(facing,c.z()).offset(facing.rotateYClockwise(),c.x()).up(c.y());}
 public static int blockSlot(Block exact){for(int i=0;i<9;i++){var s=mc.player.getInventory().getStack(i);if(s.getItem() instanceof BlockItem b&&(exact!=null?b.getBlock()==exact:b.getBlock().getDefaultState().isFullCube(mc.world,mc.player.getBlockPos())))return i;}return -1;}
 public static boolean place(BlockPos pos,int slot){if(!InventoryModules.ready()||!mc.world.getBlockState(pos).isReplaceable()||!mc.world.getWorldBorder().contains(pos)||!mc.world.isInBuildLimit(pos)||mc.player.squaredDistanceTo(Vec3d.ofCenter(pos))>Math.pow(mc.interactionManager.getReachDistance(),2))return false;
  for(Direction side:Direction.values()){BlockPos support=pos.offset(side);if(mc.world.getBlockState(support).isReplaceable()||mc.world.getBlockState(support).getCollisionShape(mc.world,support).isEmpty())continue;Vec3d hit=Vec3d.ofCenter(support).add(Vec3d.of(side.getOpposite().getVector()).multiply(0.5));BlockHitResult ray=mc.world.raycast(new RaycastContext(mc.player.getEyePos(),hit,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mc.player));if(ray.getType()!=HitResult.Type.BLOCK||!ray.getBlockPos().equals(support))continue;
   int previous=mc.player.getInventory().selectedSlot;mc.player.getInventory().selectedSlot=slot;try{var result=mc.interactionManager.interactBlock(mc.player,Hand.MAIN_HAND,new BlockHitResult(hit,side.getOpposite(),support,false));if(result.isAccepted()){mc.player.swingHand(Hand.MAIN_HAND);return true;}}finally{mc.player.getInventory().selectedSlot=previous;}}
  return false;
 }
 private static BlockPos nearestBlock(double range,Predicate<BlockPos> filter){return nearestBlock(range,filter,true);}
 private static BlockPos nearestBlock(double range,Predicate<BlockPos> filter,boolean raytrace){int r=(int)Math.min(range,6);BlockPos center=mc.player.getBlockPos(),best=null;double distance=Math.pow(Math.min(range,mc.interactionManager.getReachDistance()),2);for(BlockPos p:BlockPos.iterate(center.add(-r,-r,-r),center.add(r,r,r))){double d=mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(p));if(d>=distance||mc.world.getBlockState(p).isAir()||!filter.test(p)||raytrace&&!visible(p))continue;if(d<distance){distance=d;best=p.toImmutable();}}return best;}
 private static boolean visible(BlockPos p){var ray=mc.world.raycast(new RaycastContext(mc.player.getEyePos(),Vec3d.ofCenter(p),RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE,mc.player));return ray.getBlockPos().equals(p)&&ray.getType()==HitResult.Type.BLOCK;}
 private static void mine(BlockPos pos,boolean rotate){mine(pos,rotate,true);}
 private static void mine(BlockPos pos,boolean rotate,boolean raytrace){if(pos==null||mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos))>Math.pow(mc.interactionManager.getReachDistance(),2)||raytrace&&!visible(pos))return;if(rotate){Vec3d delta=Vec3d.ofCenter(pos).subtract(mc.player.getEyePos());mc.player.setYaw((float)(Math.toDegrees(Math.atan2(delta.z,delta.x))-90));mc.player.setPitch((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));}if(!mc.interactionManager.isBreakingBlock())mc.interactionManager.attackBlock(pos,Direction.UP);else mc.interactionManager.updateBlockBreakingProgress(pos,Direction.UP);mc.player.swingHand(Hand.MAIN_HAND);}
}


