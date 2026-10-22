package me.ionar.salhack.core;
import me.ionar.salhack.SalHackClient;
import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.*;
import java.util.Map;
/** Optional interoperability: neither release JAR needs the other on its compile/runtime classpath. */
public final class CombatCoexistence {
 private static final Map<String,String> equivalent=Map.ofEntries(Map.entry("AutoCrystal","CrystalAura"),Map.entry("AutoCrystalRewrite","CrystalAura"),Map.entry("OldAutoCrystalRewrite","CrystalAura"),Map.entry("Surround","AutoFeetObby"),Map.entry("KillAura","KillAura"),Map.entry("AutoTrap","AutoTrap"),Map.entry("SelfTrap","SelfTrap"),Map.entry("AutoArmor","AutoArmor"),Map.entry("Criticals","Criticals"),Map.entry("Velocity","NoVelocity"));
 private static Field instance,modules;private static Method on;
 public static boolean otherFlag(String module,String setting){if(!otherOn(module))return false;try{Object manager=modules.get(instance.get(null));Object selected=modules.getType().getMethod("get",String.class).invoke(manager,module);return selected!=null&&Boolean.TRUE.equals(selected.getClass().getMethod("flag",String.class).invoke(selected,setting));}catch(ReflectiveOperationException|LinkageError e){return false;}}
 public static boolean otherOn(String name){
  if(!FabricLoader.getInstance().isModLoaded("kthreadd"))return false;
  try{if(instance==null){Class<?> type=Class.forName("dev.kthreadd.KthreaddClient",false,CombatCoexistence.class.getClassLoader());instance=type.getField("INSTANCE");modules=type.getField("modules");on=modules.getType().getMethod("on",String.class);}Object client=instance.get(null);if(client==null)return false;Object manager=modules.get(client);return manager!=null&&Boolean.TRUE.equals(on.invoke(manager,name));}
  catch(ReflectiveOperationException|LinkageError e){return false;}
 }
 public static boolean deferred(String name){var c=SalHackClient.INSTANCE;if(c==null||c.modules==null||c.modules.get("CombatCoordinator")==null||!c.modules.get("CombatCoordinator").flag("PreferKTHREADD"))return false;
  if(name.equals("AutoTotem"))return otherOn("AutoTotem")||otherOn("AutoOffhandGap")||otherOn("AutoOffhandCrystal");if(name.equals("Criticals")&&otherFlag("KillAura","Criticals"))return true;String other=equivalent.get(name);return other!=null&&otherOn(other);
 }
}
