package me.ionar.salhack.automation;
import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.ExpansionAutomationModules;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.block.entity.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import java.nio.file.*;
import java.util.*;
public final class WorldDiagnostics {
 private static ModuleManager manager;private static final Set<String> logged=new HashSet<>();private static Path file;
 private static long timeUpdate,accelerate;private static float tps=20;
 public static void register(ModuleManager m){manager=m;
  m.add(new ClientModule("GlobalLocation",Category.MISC,"Reports received global sounds and qualifying tracked entity spawns locally."){{flag("Thunder",true);for(String n:List.of("Slimes","Wither","End Portal","Ender Dragon","Donkey","Llama","Mule"))flag(n,false);}});
  m.add(new ClientModule("StashLogger",Category.WORLD,"Logs loaded chunk storage and chested animals locally, optionally to a per-session file."){
   {number("MaxCount",5,0,20,1);flag("Chests",true);flag("Shulkers",true);flag("Donkeys",true);flag("WriteToFile",true);}
   protected void onEnable(){logged.clear();file=FabricLoader.getInstance().getConfigDir().resolve("creepysalhack/stash-"+System.currentTimeMillis()+".txt");}
   protected void onDisable(){logged.clear();}
  });
  m.add(new ClientModule("Timer",Category.WORLD,"Changes client simulation tick rate; TPS sync uses received world time updates."){
   {number("Speed",4,0.1,10,0.1);flag("Accelerate",false);flag("TPSSync",false);}
   protected void onEnable(){accelerate=System.currentTimeMillis();timeUpdate=0;tps=20;}
   public void tick(){if(flag("Accelerate")&&System.currentTimeMillis()-accelerate>=2000){setting("Speed").set(value("Speed")+0.1);accelerate=System.currentTimeMillis();}}
  });
  ClientChunkEvents.CHUNK_LOAD.register((world,chunk)->{if(!manager.on("StashLogger"))return;var s=manager.get("StashLogger");int chests=0,shulkers=0;for(var e:chunk.getBlockEntities().values()){if(e instanceof ChestBlockEntity)chests++;if(e instanceof ShulkerBoxBlockEntity)shulkers++;}String key=world.getRegistryKey().getValue()+":"+chunk.getPos();if(s.flag("Chests")&&chests>=s.value("MaxCount")&&chests>0)log(key+":chests",chests+" chests at chunk "+chunk.getPos()+" ("+world.getRegistryKey().getValue()+")");if(s.flag("Shulkers")&&shulkers>0)log(key+":shulkers",shulkers+" shulkers at chunk "+chunk.getPos()+" ("+world.getRegistryKey().getValue()+")");});
  ClientEntityEvents.ENTITY_LOAD.register((e,w)->{if(manager.on("StashLogger")&&manager.get("StashLogger").flag("Donkeys")&&e instanceof AbstractDonkeyEntity d&&d.hasChest())log("entity:"+e.getUuid(),e.getName().getString()+" with chest at "+e.getBlockPos().toShortString()+" ("+w.getRegistryKey().getValue()+")");spawn(e);});
 }
 private static void log(String key,String message){if(logged.size()>=8192||!logged.add(key)||SalHackClient.INSTANCE.privacy())return;ExpansionAutomationModules.say(message);if(manager.get("StashLogger").flag("WriteToFile"))try{Files.createDirectories(file.getParent());Files.writeString(file,message+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){ExpansionAutomationModules.say("StashLogger: cannot write log: "+e.getClass().getSimpleName());manager.get("StashLogger").setting("WriteToFile").set(0);}}
 private static void spawn(Entity e){if(!manager.on("GlobalLocation")||SalHackClient.INSTANCE.privacy())return;var s=manager.get("GlobalLocation");boolean report=e instanceof DonkeyEntity&&s.flag("Donkey")||e instanceof MuleEntity&&s.flag("Mule")||e instanceof LlamaEntity&&s.flag("Llama");if(e instanceof SlimeEntity&&s.flag("Slimes")&&e.getY()<=40){var biome=e.getWorld().getBiome(e.getBlockPos());if(!biome.matchesKey(net.minecraft.world.biome.BiomeKeys.SWAMP)&&!biome.matchesKey(net.minecraft.world.biome.BiomeKeys.MANGROVE_SWAMP))ExpansionAutomationModules.say("Slime spawned in chunk "+e.getChunkPos());}else if(report)ExpansionAutomationModules.say(e.getName().getString()+" spawned at "+e.getBlockPos().toShortString());}
 public static void sound(PlaySoundS2CPacket packet){if(!manager.on("GlobalLocation")||SalHackClient.INSTANCE.privacy()||!manager.get("GlobalLocation").flag("Thunder")||packet.getSound().value()!=SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER)return;var mc=MinecraftClient.getInstance();if(mc.player==null)return;double angle=Math.toDegrees(Math.atan2(packet.getZ()-mc.player.getZ(),packet.getX()-mc.player.getX()))-90;ExpansionAutomationModules.say(String.format(Locale.ROOT,"Lightning sound at %.0f %.0f %.0f, bearing %.1f",packet.getX(),packet.getY(),packet.getZ(),MathHelper.wrapDegrees(angle)));}
 public static void worldEvent(WorldEventS2CPacket packet){if(!manager.on("GlobalLocation")||SalHackClient.INSTANCE.privacy())return;var s=manager.get("GlobalLocation");String name=switch(packet.getEventId()){case 1023->s.flag("Wither")?"Wither spawned":null;case 1038->s.flag("End Portal")?"End portal activated":null;case 1028->s.flag("Ender Dragon")?"Ender dragon died":null;default->null;};if(name!=null)ExpansionAutomationModules.say(name+" at reported position "+packet.getPos().toShortString());}
 public static float timerMultiplier(){var mc=MinecraftClient.getInstance();if(manager==null||!manager.on("Timer")||mc.world==null||mc.player==null)return 1;var s=manager.get("Timer");return s.flag("TPSSync")?Math.max(0.1f,Math.min(1,tps/20)):(float)s.value("Speed");}
 public static void timeUpdate(){long now=System.currentTimeMillis();if(timeUpdate!=0){float sample=Math.min(20,20000f/Math.max(1,now-timeUpdate));tps=tps*0.7f+sample*0.3f;}timeUpdate=now;}
 public static void correction(){if(manager!=null&&manager.on("Timer")&&manager.get("Timer").flag("Accelerate"))manager.get("Timer").setting("Speed").set(1);}
}
