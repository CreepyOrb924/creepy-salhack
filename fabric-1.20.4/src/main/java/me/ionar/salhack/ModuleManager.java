package me.ionar.salhack;

import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import java.util.*;

public final class ModuleManager {
    public final List<ClientModule> all = new ArrayList<>();
    private final Map<Integer, Boolean> down = new HashMap<>();
    public final ClientModule colors, gui, hud, privacy, notifications;
    public ModuleManager() {
        colors = add(new ClientModule("Colors", Category.UI, "Original cyan theme; RGB accent, background opacity and rainbow."));
        colors.configurationOnly=true;
        colors.number("Red",51,0,255,17); colors.number("Green",182,0,255,17); colors.number("Blue",215,0,255,17); colors.flag("Rainbow",false);
        gui = add(new ClientModule("ClickGUI",Category.UI,"Left Arrow opens category panels. Right-click expands settings; middle-click binds."));
        gui.configurationOnly=true;
        gui.number("Scale",1,0.65,1.5,0.05); gui.flag("Rounded",true); gui.flag("Animations",true); gui.flag("Tooltips",true); gui.number("Opacity",210,80,255,15);
        hud = add(new ClientModule("HUD",Category.UI,"Watermark, original right aligned ArrayList, coordinates, FPS and combat statistics."));
        hud.flag("Watermark",true); hud.flag("ImageLogo",true); hud.flag("ArrayList",true); hud.flag("Coordinates",true); hud.flag("Stats",true); hud.flag("Combat",true); hud.flag("Waypoints",true); hud.flag("Shadow",true);
        hud.setEnabled(true);
        privacy = add(new ClientModule("Privacy",Category.UI,"Hides this client's coordinate overlays, waypoint labels and stash location notifications."));
        notifications = add(new ClientModule("Notifications",Category.UI,"Local module toggle and diagnostic notifications.")); notifications.setEnabled(true);
        var coordinator=add(new ClientModule("CombatCoordinator",Category.COMBAT,"When both clients enable equivalent combat modules, KTHREADD owns the action; SalHack resumes when it is disabled."));coordinator.configurationOnly=true;coordinator.flag("PreferKTHREADD",true);
        MovementModules.register(this); InventoryModules.register(this); CombatModules.register(this);
        ExpansionMovementModules.register(this); ExpansionRenderModules.register(this); ExpansionAutomationModules.register(this);
        add(new ClientModule("Brightness",Category.RENDER,"Fullbright via lightmap brightness; no gamma setting or potion modification."));
        ClientModule esp=add(new ClientModule("EntityESP",Category.RENDER,"Outlined entity boxes. Range-capped; uses the modern world render pipeline.")); esp.number("Range",64,8,192,8); esp.flag("PlayersOnly",true); esp.flag("ThroughWalls",true); esp.number("LineWidth",1,0.5,3,0.25); esp.flag("PreferKTHREADD",true);
        ClientModule storage=add(new ClientModule("StorageESP",Category.RENDER,"Outlines nearby loaded chests, barrels, shulkers, hoppers and furnaces.")); storage.number("Range",48,8,96,8); storage.flag("ThroughWalls",true); storage.number("LineWidth",1,0.5,3,0.25); storage.flag("PreferKTHREADD",true);
        ClientModule tracers=add(new ClientModule("Tracers",Category.RENDER,"Lines from the camera to nearby players.")); tracers.number("Range",96,8,192,8); tracers.flag("ThroughWalls",true); tracers.number("LineWidth",1,0.5,3,0.25); tracers.flag("PreferKTHREADD",true);
        ClientModule names=add(new ClientModule("Nametags",Category.RENDER,"Player names and health above their heads, visible through terrain.")); names.number("Range",64,8,128,8);
        add(new ClientModule("Waypoints",Category.RENDER,"Dimension-aware world markers. Manage with /sal waypoint; disabled in privacy mode."));
        ClientModule stash=add(new ClientModule("StashFinder",Category.WORLD,"Checks loaded storage counts per chunk; local notification once per session.")); stash.number("Threshold",6,2,64,1); stash.number("Range",64,16,128,16);
        ClientModule search=add(new ClientModule("Search",Category.WORLD,"Incrementally scans nearby loaded blocks for selected ore. /sal search <block-id>.")); search.number("Range",16,4,32,4); search.number("Budget",512,128,2048,128);
        add(new ClientModule("HighwayNavigation",Category.WORLD,"HUD yaw/cardinal alignment and cross-track distance to cardinal/diagonal highways."));
        ClientModule weather=add(new ClientModule("NoWeather",Category.WORLD,"Suppresses client rain/thunder rendering while enabled; server state is untouched."));
        add(new ClientModule("CoordSpoofer",Category.WORLD,"Randomizes only the X/Z values shown in this client's coordinate HUD; does not move the player."));
        all.sort(Comparator.comparing(m -> m.name));
    }
    public <T extends ClientModule> T add(T m) { if(get(m.name)!=null) throw new IllegalArgumentException("Duplicate module: "+m.name); all.add(m); return m; }
    public ClientModule get(String name) { return all.stream().filter(m -> m.name.equalsIgnoreCase(name)).findFirst().orElse(null); }
    public boolean on(String name) { var m=get(name); return m!=null && m.enabled(); }
    public void tick(MinecraftClient mc) {
        if (mc.player==null || mc.world==null) return;
        for (var m: all) {
            if (!m.configurationOnly && m.key >= 32 && mc.currentScreen==null) {
                boolean pressed=InputUtil.isKeyPressed(mc.getWindow().getHandle(),m.key);
                if (pressed && !down.getOrDefault(m.key,false)) m.toggle();
            }
            if (m.enabled() && !CombatCoexistence.deferred(m.name)) {
                try { m.tick(); } catch (RuntimeException e) {
                    SalHackClient.LOG.error("Disabling failed module {}",m.name,e); m.setEnabled(false);
                    SalHackClient.INSTANCE.notify("Disabled " + m.name + ": " + e.getClass().getSimpleName());
                }
            }
        }
        for (var m:all) if (m.key>=32) down.put(m.key,InputUtil.isKeyPressed(mc.getWindow().getHandle(),m.key));
    }
    public void release() { for (var m:all) if (m.enabled()) { m.setEnabled(false); } down.clear(); }
    public int accent(int offset) {
        if (colors.flag("Rainbow")) return 0xff000000 | java.awt.Color.HSBtoRGB(((System.currentTimeMillis()/25+offset)%360)/360f,0.65f,1f);
        return 0xff000000 | colors.setting("Red").integer()<<16 | colors.setting("Green").integer()<<8 | colors.setting("Blue").integer();
    }
}
