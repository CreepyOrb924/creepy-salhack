package me.ionar.salhack;

import me.ionar.salhack.core.*;
import me.ionar.salhack.gui.*;
import me.ionar.salhack.render.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.*;
import java.util.*;

public final class SalHackClient implements ClientModInitializer {
    public static final Logger LOG=LoggerFactory.getLogger("CreepySalHack");
    public static SalHackClient INSTANCE;
    public ModuleManager modules;
    public Profile profile=new Profile();
    public ProfileStore store;
    public WorldOverlay worldOverlay;
    public HudRenderer hud;
    public String activeProfile="default";
    public final Deque<Notice> notices=new ArrayDeque<>();
    public record Notice(String text,long expires) {}
    private boolean dirty, applying;
    private ModuleProfiles moduleProfiles;
    private int ticks;
    public void onInitializeClient() {
        INSTANCE=this;
        modules=new ModuleManager();
        moduleProfiles=new ModuleProfiles(modules.all);
        store=new ProfileStore(FabricLoader.getInstance().getConfigDir().resolve("creepysalhack/profiles"));
        for(var m:modules.all) m.onChange=() -> {
            changed();
            if(m==modules.privacy && m.enabled()) notices.clear();
            if(!applying && modules.notifications.enabled()) notify(m.name+(m.enabled()?" enabled":" disabled"));
        };
        worldOverlay=new WorldOverlay(this); hud=new HudRenderer(this);
        load("default");
        KeyBinding guiKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.creepysalhack.gui",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_LEFT,"category.creepysalhack"));
        KeyBinding hudKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.creepysalhack.hud",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,"category.creepysalhack"));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while(guiKey.wasPressed()) mc.setScreen(mc.currentScreen instanceof ClickGuiScreen?null:new ClickGuiScreen(this));
            while(hudKey.wasPressed()) mc.setScreen(new HudEditorScreen(this));
            modules.tick(mc); worldOverlay.tick();
            if(dirty && ++ticks>=40) { save(); ticks=0; }
            notices.removeIf(n -> n.expires()<System.currentTimeMillis());
        });
        HudRenderCallback.EVENT.register((context,delta) -> { if(!(MinecraftClient.getInstance().currentScreen instanceof HudEditorScreen)) hud.render(context,false); });
        WorldRenderEvents.LAST.register(worldOverlay::render);
        ClientPlayConnectionEvents.JOIN.register((handler,sender,mc) -> {
            String key=mc.getCurrentServerEntry()!=null?mc.getCurrentServerEntry().address:"singleplayer:"+(mc.getServer()!=null?mc.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT).toAbsolutePath().normalize().toString():"world");
            save();
            String server=ProfileStore.serverKey(key);
            try { if(!store.list().contains(server)) store.save(server,snapshot()); } catch(Exception e) { LOG.error("Cannot seed server profile",e); }
            load(server); worldOverlay.reset();
            notify("Creepy SalHack 1.20.4 | Left Arrow: ClickGUI | Right Shift: HUD");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,mc) -> { save(); applying=true; modules.release(); applying=false; worldOverlay.reset(); load("default"); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> save());
        ClientCommands.register(this);
        LOG.info("Initialized independent 1.20.4 partial port: {} modules; GUI Left Arrow; HUD Right Shift",modules.all.size());
        notify("Partial modernization build: read compatibility/testing report.");
    }
    public boolean privacy() { return modules.privacy.enabled(); }
    public boolean isFriend(String name) { return name!=null && modules!=null && modules.on("Friends") && profile.friends.stream().anyMatch(n -> n.equalsIgnoreCase(name)); }
    public void changed() { if(!applying) { dirty=true; ticks=0; } }
    public void notify(String text) { if(notices.size()>=5) notices.removeFirst(); notices.addLast(new Notice(text,System.currentTimeMillis()+5000)); }
    public Profile snapshot() {
        for(var m:modules.all) {
            var state=new Profile.ModuleState(); state.enabled=m.enabled(); state.key=m.key;
            for(var s:m.settings) { state.settings.put(s.name,s.get()); if(s.isText()) state.textSettings.put(s.name,s.text()); }
            profile.modules.put(m.name,state);
        }
        return profile;
    }
    public void save() {
        try { store.save(activeProfile,snapshot()); dirty=false; }
        catch(Exception e) { dirty=false; LOG.error("Could not save profile {}",activeProfile,e); notify("Save failed: " + e.getMessage()); }
    }
    public void saveAs(String name) throws java.io.IOException { store.save(name,snapshot()); activeProfile=name; dirty=false; }
    public void load(String name) {
        Profile next=store.load(name);
        applying=true;
        try {
            moduleProfiles.apply(next);
            profile=next; activeProfile=name; dirty=false;
            var search=net.minecraft.util.Identifier.tryParse(next.searchBlock==null?"minecraft:diamond_ore":next.searchBlock);
            worldOverlay.searchBlock=search!=null && net.minecraft.registry.Registries.BLOCK.containsId(search)?net.minecraft.registry.Registries.BLOCK.get(search):net.minecraft.block.Blocks.DIAMOND_ORE;
            worldOverlay.reset();
        } finally { applying=false; }
        if(store.lastError!=null) { LOG.warn(store.lastError); notify(store.lastError); store.lastError=null; }
    }
}
