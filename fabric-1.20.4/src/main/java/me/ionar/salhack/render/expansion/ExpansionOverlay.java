package me.ionar.salhack.render.expansion;
import java.util.*;
import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.ClientModule;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.entity.projectile.ProjectileUtil;
import org.joml.Vector3f;

public final class ExpansionOverlay {
 private final ModuleManager modules;
 private final MinecraftClient mc=MinecraftClient.getInstance();
 private Object world;
 private BlockPos origin;
 private int cursor,ticks,lastRadius=-1;
 private final Map<BlockPos,Boolean> holes=new LinkedHashMap<>();
 private final Set<BlockPos> voids=new LinkedHashSet<>();
 private final LinkedHashSet<Long> chunks=new LinkedHashSet<>();
 private boolean chunksOn; private static long lastBreakNotice;
 public static final Map<Integer,BreakMark> breaks=new HashMap<>();
 public record BreakMark(BlockPos pos,int stage,long time){}
 public static final Map<UUID,float[][]> poses=new HashMap<>();
 public ExpansionOverlay(ModuleManager modules){this.modules=modules;}
 public static void breaking(int id,BlockPos pos,int stage){if(stage<0||stage>9)breaks.remove(id);else {if(breaks.size()>1024)breaks.clear();breaks.put(id,new BreakMark(pos.toImmutable(),stage,System.currentTimeMillis()));if(RenderHooks.on("BreakHighlight")&&me.ionar.salhack.SalHackClient.INSTANCE.modules.get("BreakHighlight").flag("Debug")&&System.currentTimeMillis()-lastBreakNotice>1000){lastBreakNotice=System.currentTimeMillis();me.ionar.salhack.SalHackClient.INSTANCE.notify("Breaking entity "+id+", stage "+stage);}}}
 public void tick(){
  if(world!=mc.world){world=mc.world;holes.clear();voids.clear();chunks.clear();breaks.clear();poses.clear();origin=null;cursor=0;}
  if(mc.world==null||mc.player==null)return;
  ++ticks;if(ticks%100==0&&modules.on("NoRender")&&modules.get("NoRender").value("NoItemsMode")==1){var items=new ArrayList<Integer>();for(var entity:mc.world.getEntities())if(entity instanceof net.minecraft.entity.ItemEntity&&items.size()<4096)items.add(entity.getId());for(int id:items)mc.world.removeEntity(id,Entity.RemovalReason.DISCARDED);}breaks.values().removeIf(b->System.currentTimeMillis()-b.time()>5000);
  if(!modules.on("Skeleton"))poses.clear();else if(ticks%100==0)poses.keySet().removeIf(id->mc.world.getPlayerByUuid(id)==null);
  if(!modules.on("HoleESP")){holes.clear();}
  if(!modules.on("VoidESP"))voids.clear();
  var center=BlockPos.ofFloored(mc.gameRenderer.getCamera().getPos());
  if(origin==null||origin.getSquaredDistance(center)>16){origin=center;cursor=0;holes.clear();voids.clear();}
  if(modules.on("HoleESP")){
   int radius=modules.get("HoleESP").setting("Radius").integer(),w=radius*2+1,total=w*w*w;if(radius!=lastRadius){lastRadius=radius;cursor=0;holes.clear();}
   for(int i=0;i<Math.min(total,4096);i++){
    int at=cursor;cursor=(cursor+1)%total;BlockPos p=origin.add(at%w-radius,(at/w)%w-radius,at/(w*w)-radius);
    Boolean bedrock=hole(p);if(bedrock==null)holes.remove(p);else if(holes.size()<4096)holes.put(p,bedrock);
   }
  }
  if(modules.on("VoidESP")&&ticks%10==0){voids.clear();int r=modules.get("VoidESP").setting("Radius").integer();for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
   BlockPos p=new BlockPos(center.getX()+x,mc.world.getBottomY(),center.getZ()+z);if(!mc.world.isChunkLoaded(p))continue;for(int y=0;y<=4;y++){BlockPos at=p.up(y);if(!mc.world.getBlockState(at).isAir())break;if(y>0&&Math.abs(at.getY()-center.getY())<=r&&voids.size()<8192)voids.add(at);}
  }}
  boolean on=modules.on("NewChunks");if(!on||!chunksOn)chunks.clear();chunksOn=on;
  if(on&&ticks%10==0){int r=modules.get("NewChunks").setting("Range").integer()/16;ChunkPos c=new ChunkPos(center);for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(mc.world.getChunkManager().getWorldChunk(c.x+x,c.z+z)!=null)chunks.add(ChunkPos.toLong(c.x+x,c.z+z));while(chunks.size()>modules.get("NewChunks").value("Limit"))chunks.remove(chunks.iterator().next());}
 }
 private Boolean hole(BlockPos p){
  if(!mc.world.isChunkLoaded(p)||!mc.world.getBlockState(p).isAir()||!mc.world.getBlockState(p.up()).isAir()||!mc.world.getBlockState(p.up(2)).isAir())return null;
  if(mc.world.getBlockState(p.down()).isAir())return null;boolean bedrock=true;for(var d:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){var state=mc.world.getBlockState(p.offset(d));if(!state.isOf(Blocks.BEDROCK)){bedrock=false;if(!state.isOf(Blocks.OBSIDIAN))return null;}}return bedrock;
 }
 public void render(WorldRenderContext ctx){
  if(mc.world==null||mc.player==null||ctx.matrixStack()==null||mc.options.hudHidden)return;
  var matrix=ctx.matrixStack();var camera=ctx.camera().getPos();matrix.push();matrix.translate(-camera.x,-camera.y,-camera.z);
  var providers=mc.getBufferBuilders().getEntityVertexConsumers();var lines=providers.getBuffer(OverlayLayers.LINES);
  if(modules.on("BlockHighlight")&&mc.crosshairTarget instanceof BlockHitResult hit){var p=hit.getBlockPos();var shape=mc.world.getBlockState(p).getOutlineShape(mc.world,p);for(var b:shape.getBoundingBoxes())box(ctx,providers,b.offset(p).expand(.002),modules.get("BlockHighlight"),"");}
  if(modules.on("BreakHighlight"))for(var b:breaks.values())if(b.pos().getSquaredDistance(BlockPos.ofFloored(camera))<128*128){box(ctx,providers,new Box(b.pos()).contract((1-(b.stage()+1)/10d)*.5),modules.get("BreakHighlight"),"");}
  if(modules.on("HoleESP")){var mod=modules.get("HoleESP");for(var e:holes.entrySet())if(!mod.flag("IgnoreOwnHole")||!e.getKey().equals(mc.player.getBlockPos()))box(ctx,providers,new Box(e.getKey()),mod,e.getValue()?"Bedrock":"Obsidian");}
  if(modules.on("VoidESP"))for(var p:voids){WorldRenderer.renderFilledBox(matrix,providers.getBuffer(OverlayLayers.FILLED),p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1,.5f,0,1,.22f);WorldRenderer.drawBox(matrix,providers.getBuffer(OverlayLayers.LINES),new Box(p),.5f,0,1,.5f);}
  if(modules.on("CityESP"))for(var player:mc.world.getPlayers())if(player!=mc.player&&!me.ionar.salhack.SalHackClient.INSTANCE.isFriend(player.getName().getString())&&player.squaredDistanceTo(camera)<64*64){BlockPos feet=player.getBlockPos();for(var d:Direction.Type.HORIZONTAL){var p=feet.offset(d);if(mc.world.getBlockState(p).isOf(Blocks.OBSIDIAN)&&cityPlacement(p.offset(d).down()))box(ctx,providers,new Box(p),modules.get("CityESP"),"Obsidian");}}
  if(modules.on("NewChunks")&&modules.get("NewChunks").flag("Render")){int range=modules.get("NewChunks").setting("Range").integer();for(long key:chunks){ChunkPos c=new ChunkPos(key);if(Math.abs(c.getStartX()-camera.x)>range||Math.abs(c.getStartZ()-camera.z)>range)continue;double y=mc.player.getY();WorldRenderer.drawBox(matrix,providers.getBuffer(OverlayLayers.LINES),new Box(c.getStartX(),y,c.getStartZ(),c.getStartX()+16,y+.02,c.getStartZ()+16),1,.7f,0,.8f);}}
  if(modules.on("Skeleton"))for(var p:mc.world.getPlayers())if(p!=mc.player&&p.squaredDistanceTo(camera)<Math.pow(modules.get("Skeleton").value("Range"),2))skeleton(ctx,providers.getBuffer(OverlayLayers.LINES),p);
  if(modules.on("Trajectories"))trajectory(ctx,lines,providers);
  if(modules.on("MobOwnerModule"))for(var e:mc.world.getEntities())if(e.squaredDistanceTo(camera)<Math.pow(modules.get("MobOwnerModule").value("Range"),2)){
   UUID id=e instanceof TameableEntity tame?tame.getOwnerUuid():e instanceof AbstractHorseEntity horse?horse.getOwnerUuid():null;
   if(id!=null){var entry=mc.getNetworkHandler()==null?null:mc.getNetworkHandler().getPlayerListEntry(id);label(ctx,providers,e.getPos().add(0,e.getHeight()+.4,0),"Owner: "+(entry==null?id.toString():entry.getProfile().getName()));}
  }
  matrix.pop();providers.draw();
 }
 private boolean cityPlacement(BlockPos base){var state=mc.world.getBlockState(base);return (state.isOf(Blocks.OBSIDIAN)||state.isOf(Blocks.BEDROCK))&&mc.world.getBlockState(base.up()).isAir()&&mc.world.getBlockState(base.up(2)).isAir()&&mc.world.getOtherEntities(null,new Box(base.up()).stretch(0,1,0)).isEmpty();}
 private void box(WorldRenderContext c,VertexConsumerProvider providers,Box box,ClientModule mod,String prefix){
  int mode=mod.setting(mod.name.equals("BlockHighlight")||mod.name.equals("BreakHighlight")?"HighlightModes":"Mode").integer();if(mode==0)return;
  if(mode==1||mode==2)box=new Box(box.minX,box.minY,box.minZ,box.maxX,box.minY+.002,box.maxZ);
  float r=(float)mod.value(prefix+"Red"),g=(float)mod.value(prefix+"Green"),b=(float)mod.value(prefix+"Blue"),a=(float)mod.value(prefix+"Alpha");
  if(mode==2||mode==4)WorldRenderer.renderFilledBox(c.matrixStack(),providers.getBuffer(OverlayLayers.FILLED),box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ,r,g,b,a);
  if(mode==1||mode==3||mode==4)WorldRenderer.drawBox(c.matrixStack(),providers.getBuffer(OverlayLayers.LINES),box,r,g,b,a);
 }
 private void label(WorldRenderContext c,VertexConsumerProvider providers,Vec3d p,String text){var matrix=c.matrixStack();matrix.push();matrix.translate(p.x,p.y,p.z);matrix.multiply(c.camera().getRotation());matrix.scale(-.025f,-.025f,.025f);mc.textRenderer.draw(text,-mc.textRenderer.getWidth(text)/2f,0,0xffffffff,true,matrix.peek().getPositionMatrix(),providers,net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH,0x66000000,15728880);matrix.pop();}
 private void line(WorldRenderContext ctx,VertexConsumer lines,Vec3d from,Vec3d to,float r,float g,float b,float a){var entry=ctx.matrixStack().peek();var n=to.subtract(from).normalize();lines.vertex(entry.getPositionMatrix(),(float)from.x,(float)from.y,(float)from.z).color(r,g,b,a).normal(entry.getNormalMatrix(),(float)n.x,(float)n.y,(float)n.z).next();lines.vertex(entry.getPositionMatrix(),(float)to.x,(float)to.y,(float)to.z).color(r,g,b,a).normal(entry.getNormalMatrix(),(float)n.x,(float)n.y,(float)n.z).next();}
 private void skeleton(WorldRenderContext ctx,VertexConsumer lines,PlayerEntity player){
  float[][] pose=poses.get(player.getUuid());if(pose==null)return;Vec3d origin=player.getLerpedPos(ctx.tickDelta());float yaw=(float)Math.toRadians(-MathHelper.lerpAngleDegrees(ctx.tickDelta(),player.prevBodyYaw,player.bodyYaw));
  Vec3d hips=origin.add(0,player.isInSneakingPose()?.65:.75,0),shoulders=hips.add(0,.65,0);line(ctx,lines,hips,shoulders,.3f,1,1,1);
  Vec3d left=shoulders.add(new Vec3d(-.3,0,0).rotateY(yaw)),right=shoulders.add(new Vec3d(.3,0,0).rotateY(yaw));line(ctx,lines,left,right,.3f,1,1,1);
  Vec3d[] starts={shoulders,left,right,hips.add(new Vec3d(-.14,0,0).rotateY(yaw)),hips.add(new Vec3d(.14,0,0).rotateY(yaw))};
  for(int i=0;i<5;i++){Vec3d delta=new Vec3d(0,i==0?.3:-.65,0).rotateX(-pose[i][0]).rotateZ(-pose[i][2]).rotateY(yaw-pose[i][1]);line(ctx,lines,starts[i],starts[i].add(delta),.3f,1,1,1);}
 }
 private void trajectory(WorldRenderContext ctx,VertexConsumer lines,VertexConsumerProvider providers){
  var player=mc.player;ItemStack stack=player.getMainHandStack();if(!projectile(stack))stack=player.getOffHandStack();if(!projectile(stack))return;
  Item item=stack.getItem();double speed=1.5,gravity=.03;float pitch=player.getPitch();
  if(item instanceof BowItem){if(!player.isUsingItem())return;speed=BowItem.getPullProgress(player.getItemUseTime())*3;if(speed<.1)return;gravity=.05;}
  else if(item instanceof TridentItem){speed=2.5;gravity=.05;}
  else if(item instanceof CrossbowItem){if(!CrossbowItem.isCharged(stack))return;speed=3.15;gravity=.05;}
  else if(item instanceof SplashPotionItem||item instanceof LingeringPotionItem){speed=.5;gravity=.05;pitch-=20;}
  else if(item instanceof ExperienceBottleItem){speed=.7;gravity=.07;pitch-=20;}
  Vec3d pos=player.getEyePos().add(0,-.1,0),v=Vec3d.fromPolar(pitch,player.getYaw()).multiply(speed).add(player.getVelocity().x,player.isOnGround()?0:player.getVelocity().y,player.getVelocity().z);
  var mod=modules.get("Trajectories");if(mod.value("Width")<=0)return;lines=providers.getBuffer(OverlayLayers.lines(mod.value("Width")));float r=(float)mod.value("Red")/255,g=(float)mod.value("Green")/255,b=(float)mod.value("Blue")/255,a=(float)mod.value("Alpha")/255;
  for(int i=0;i<240;i++){
   Vec3d to=pos.add(v);if(!mc.world.isChunkLoaded(BlockPos.ofFloored(to)))break;
   var hit=mc.world.raycast(new RaycastContext(pos,to,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,player));
   Vec3d end=hit.getType()==HitResult.Type.MISS?to:hit.getPos();var entityHit=ProjectileUtil.raycast(player,pos,end,new Box(pos,end).expand(1),e->e!=player&&e.isAlive()&&e.canHit(),pos.squaredDistanceTo(end));
   if(entityHit!=null)end=entityHit.getPos();line(ctx,lines,pos,end,r,g,b,a);
   if(entityHit!=null||hit.getType()!=HitResult.Type.MISS){WorldRenderer.drawBox(ctx.matrixStack(),lines,new Box(end.add(-.15,-.15,-.15),end.add(.15,.15,.15)),r,g,b,a);break;}
   boolean water=mc.world.getFluidState(BlockPos.ofFloored(to)).isIn(net.minecraft.registry.tag.FluidTags.WATER);v=v.multiply(water?.8:.99).add(0,-gravity,0);pos=to;if(pos.y<mc.world.getBottomY())break;
  }
 }
 private boolean projectile(ItemStack s){return s.getItem() instanceof BowItem||s.getItem() instanceof CrossbowItem||s.getItem() instanceof TridentItem||s.isOf(Items.ENDER_PEARL)||s.isOf(Items.SNOWBALL)||s.isOf(Items.EGG)||s.getItem() instanceof SplashPotionItem||s.getItem() instanceof LingeringPotionItem||s.getItem() instanceof ExperienceBottleItem;}
}







