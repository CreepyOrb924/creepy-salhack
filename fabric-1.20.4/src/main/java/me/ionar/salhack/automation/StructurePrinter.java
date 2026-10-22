package me.ionar.salhack.automation;
import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import java.nio.file.*;
import java.util.*;
/** Deliberately limited replacement for the removed Forge Schematica dependency. */
public final class StructurePrinter extends ClientModule {
 private record Entry(BlockPos offset,Block block){}
 private final MinecraftClient mc=MinecraftClient.getInstance();private final ModuleManager manager;
 private List<Entry> blocks=List.of();private BlockPos origin;private int cursor;private long next;
 public static void register(ModuleManager m){m.add(new StructurePrinter(m));m.add(new ClientModule("PrinterBypass",Category.SCHEMATICA,"Faces structure-printer placements: packet-only or visible full rotation. Does not bypass server validation."){{mode("Mode","Packet","Full").set(1);number("Delay",0,0,1,0.1);}});}
 private StructurePrinter(ModuleManager m){super("Printer",Category.SCHEMATICA,"Places plain full blocks from a vanilla structure .nbt under config/creepysalhack/schematics at your enable position.");manager=m;text("File","structure.nbt");number("Delay",0.15,0.05,2,0.05);}
 protected void onEnable(){blocks=List.of();origin=null;cursor=0;next=0;}
 public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;if(origin==null){origin=mc.player.getBlockPos();try{load();ExpansionAutomationModules.say("Printer: loaded "+blocks.size()+" plain blocks at "+origin.toShortString());}catch(Exception e){ExpansionAutomationModules.say("Printer: "+e.getMessage());setEnabled(false);return;}}
  if(blocks.isEmpty()){setEnabled(false);return;}int budget=Math.min(128,blocks.size());for(int i=0;i<budget;i++){Entry e=blocks.get(cursor++%blocks.size());BlockPos p=origin.add(e.offset());if(mc.world.getBlockState(p).isOf(e.block())||!mc.world.getBlockState(p).isReplaceable())continue;int slot=WorldAutomation.blockSlot(e.block());if(slot<0||mc.player.squaredDistanceTo(Vec3d.ofCenter(p))>Math.pow(mc.interactionManager.getReachDistance(),2))continue;
   if(manager.on("PrinterBypass")){Vec3d delta=Vec3d.ofCenter(p).subtract(mc.player.getEyePos());float yaw=(float)(Math.toDegrees(Math.atan2(delta.z,delta.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z)));var bypass=manager.get("PrinterBypass");if(bypass.value("Mode")==1){mc.player.setYaw(yaw);mc.player.setPitch(pitch);}else mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(yaw,pitch,mc.player.isOnGround()));}
   if(WorldAutomation.place(p,slot)){double delay=value("Delay");if(manager.on("PrinterBypass")&&manager.get("PrinterBypass").value("Mode")==1)delay=Math.max(delay,manager.get("PrinterBypass").value("Delay"));next=System.currentTimeMillis()+(long)(delay*1000);return;}
  }
 }
 private void load() throws Exception {
  Path root=FabricLoader.getInstance().getConfigDir().resolve("creepysalhack/schematics").toAbsolutePath().normalize(),file=root.resolve(setting("File").text()).normalize();if(!file.startsWith(root)||!file.toString().endsWith(".nbt"))throw new IllegalArgumentException("File must be a .nbt inside the schematics directory");if(Files.size(file)>4*1024*1024)throw new IllegalArgumentException("compressed file exceeds 4 MiB");NbtCompound nbt=NbtIo.readCompressed(file,NbtSizeTracker.of(8*1024*1024));NbtList palette=nbt.getList("palette",NbtElement.COMPOUND_TYPE),data=nbt.getList("blocks",NbtElement.COMPOUND_TYPE);if(data.size()>32768||palette.size()>4096)throw new IllegalArgumentException("structure exceeds 32768 blocks or 4096 palette entries");
  List<Block> types=new ArrayList<>();for(int i=0;i<palette.size();i++){var tag=palette.getCompound(i);Identifier id=Identifier.tryParse(tag.getString("Name"));if(id==null||!Registries.BLOCK.containsId(id))throw new IllegalArgumentException("unknown palette block");Block b=Registries.BLOCK.get(id);if(b!=Blocks.AIR&&b!=Blocks.CAVE_AIR&&b!=Blocks.VOID_AIR&&(!b.getDefaultState().isFullCube(mc.world,origin)||!tag.getCompound("Properties").isEmpty()||b instanceof BlockEntityProvider))throw new IllegalArgumentException("unsupported state "+id+": only plain full blocks without properties/entities are supported");types.add(b);}
  if(!nbt.getList("entities",NbtElement.COMPOUND_TYPE).isEmpty())throw new IllegalArgumentException("entity data is unsupported");List<Entry> result=new ArrayList<>();for(int i=0;i<data.size();i++){var tag=data.getCompound(i);int state=tag.getInt("state");if(state<0||state>=types.size())throw new IllegalArgumentException("invalid palette index");NbtList pos=tag.getList("pos",NbtElement.INT_TYPE);if(pos.size()!=3)throw new IllegalArgumentException("invalid block position");if(!tag.getCompound("nbt").isEmpty())throw new IllegalArgumentException("block entity data is unsupported");Block b=types.get(state);if(b.getDefaultState().isAir())continue;BlockPos offset=new BlockPos(pos.getInt(0),pos.getInt(1),pos.getInt(2));if(Math.abs((long)offset.getX())>512||Math.abs((long)offset.getY())>512||Math.abs((long)offset.getZ())>512)throw new IllegalArgumentException("offset exceeds 512 blocks");result.add(new Entry(offset,b));}
  result.sort(Comparator.comparingInt(e->e.offset().getY()));blocks=result;
 }
 protected void onDisable(){blocks=List.of();origin=null;}
}
