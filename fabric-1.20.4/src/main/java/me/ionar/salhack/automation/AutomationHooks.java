package me.ionar.salhack.automation;
import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.ExpansionAutomationModules;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import java.util.*;
public final class AutomationHooks {
 public static ModuleManager manager;
 private static ServerInfo reconnect;private static long reconnectAt;private static boolean wasDisconnected;
 private static final Map<UUID,Integer> pops=new HashMap<>();
 public static boolean on(String name){return manager!=null&&manager.on(name);}
 public static void register(ModuleManager m){manager=m;
  m.add(new ClientModule("AutoReconnect",Category.MISC,"Reconnects after a remote disconnect while its disconnect screen remains open."){{number("Delay",5,0,20,1);}});
  m.add(new ClientModule("AutoSign",Category.MISC,"Fills newly opened sign editors; optional completion after three seconds."){{text("Line1","SalHack");text("Line2","on");text("Line3","top!");text("Line4","~currentdate~");flag("AutoComplete",true);}});
  m.add(new ClientModule("FastPlace",Category.WORLD,"Removes vanilla item-use delay, optionally restricted to XP bottles or crystals."){{flag("XP",false);flag("Crystals",false);}});
  m.add(new ClientModule("BuildHeight",Category.MISC,"Uses the down face when interacting upward at the dimension's build limit."));
  m.add(new ClientModule("XCarry",Category.MISC,"Suppresses player crafting-window close packets; server support varies."){{flag("ForceCancel",false);}protected void onDisable(){var mc=MinecraftClient.getInstance();if(mc.player!=null&&mc.getNetworkHandler()!=null)mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket(0));}});
  m.add(new ClientModule("TotemPopNotifier",Category.MISC,"Counts observed totem pops and reports deaths."){
   public void tick(){var mc=MinecraftClient.getInstance();for(var p:mc.world.getPlayers())if(p.isDead()&&pops.containsKey(p.getUuid()))ExpansionAutomationModules.say(p.getName().getString()+" died after popping "+pops.remove(p.getUuid())+" totem(s)");}
   protected void onDisable(){pops.clear();}
  });
  m.add(new ClientModule("VisualRange",Category.MISC,"Reports loaded players entering or leaving view."){
   private Map<UUID,String> prior=new HashMap<>();{mode("Mode","Chat","Notification","Both").set(2);flag("Friends",true);flag("Enter",true);flag("Leave",true);}
   public void tick(){var mc=MinecraftClient.getInstance();Map<UUID,String> now=new HashMap<>();for(var p:mc.world.getPlayers()){if(p==mc.player||p instanceof net.minecraft.client.network.OtherClientPlayerEntity&&p.getId()<0||!flag("Friends")&&ExpansionAutomationModules.isFriend(p.getName().getString()))continue;now.put(p.getUuid(),p.getName().getString());}if(flag("Enter"))now.forEach((id,n)->{if(!prior.containsKey(id))notify(n+" entered visual range");});if(flag("Leave"))prior.forEach((id,n)->{if(!now.containsKey(id))notify(n+" left visual range");});prior=now;}
   private void notify(String text){if(value("Mode")!=1)ExpansionAutomationModules.say(text);if(value("Mode")!=0)SalHackClient.INSTANCE.notify(text);}
   protected void onDisable(){prior.clear();}
  });
  ClientTickEvents.END_CLIENT_TICK.register(mc->{if(reconnect==null)return;if(mc.currentScreen instanceof DisconnectedScreen){wasDisconnected=true;if(System.currentTimeMillis()>=reconnectAt){reconnectAt=System.currentTimeMillis()+Math.max(1000,(long)(manager.get("AutoReconnect").value("Delay")*1000));ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()),mc,ServerAddress.parse(reconnect.address),reconnect,false);}}else if(mc.currentScreen instanceof ConnectScreen){/* retain retries while connecting */}else {reconnect=null;wasDisconnected=false;}});
 }
 public static void disconnected(ServerInfo info){if((on("AutoReconnect")||reconnect!=null)&&info!=null){reconnect=info;reconnectAt=System.currentTimeMillis()+Math.max(1000,(long)(manager.get("AutoReconnect").value("Delay")*1000));wasDisconnected=false;}}
 public static void status(Entity e,byte status){if(on("TotemPopNotifier")&&status==35&&e!=null){int n=pops.merge(e.getUuid(),1,Integer::sum);ExpansionAutomationModules.say(e.getName().getString()+" popped "+n+" totem(s)");SalHackClient.INSTANCE.notify(e.getName().getString()+" popped "+n+" totem(s)");}}
 public static boolean fastPlace(){var mc=MinecraftClient.getInstance();if(!on("FastPlace")||mc.player==null)return false;var s=manager.get("FastPlace");return s.flag("XP")?mc.player.isHolding(Items.EXPERIENCE_BOTTLE):s.flag("Crystals")?mc.player.isHolding(Items.END_CRYSTAL):true;}
}
