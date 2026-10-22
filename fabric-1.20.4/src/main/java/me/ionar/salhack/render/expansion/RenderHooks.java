package me.ionar.salhack.render.expansion;
import me.ionar.salhack.SalHackClient;
public final class RenderHooks {
 public static boolean on(String name){return SalHackClient.INSTANCE!=null&&SalHackClient.INSTANCE.modules!=null&&SalHackClient.INSTANCE.modules.on(name);}
 public static boolean hide(String feature){return on("NoRender")&&SalHackClient.INSTANCE.modules.get("NoRender").flag(feature);}
}
