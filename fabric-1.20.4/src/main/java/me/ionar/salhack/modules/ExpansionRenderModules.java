package me.ionar.salhack.modules;

import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.*;
import me.ionar.salhack.render.expansion.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class ExpansionRenderModules {
    private static boolean registered;
    public static void register(ModuleManager m) {
        if (registered) return;
        registered=true;
        m.add(new FreeCamera());
        add(m,"AntiFog","Removes terrain and fluid fog locally.");
        color(add(m,"BlockHighlight","Highlights the targeted block shape."),"");
        var breaking=add(m,"BreakHighlight","Tracks server-reported block breaking progress."); color(breaking,""); breaking.flag("Debug",false);
        var city=add(m,"CityESP","Marks obsidian adjacent to other players with crystal clearance.");color(city,"Obsidian");city.setting("ObsidianRed").set(1);city.setting("ObsidianGreen").set(0);
        var holes=add(m,"HoleESP","Scans loaded blocks for safe single-block holes.");
        holes.mode("Mode","None","FlatOutline","Flat","Outline","Full").set(1); holes.number("Radius",8,0,32,1); holes.flag("IgnoreOwnHole",false);rgba(holes,"Obsidian");rgba(holes,"Bedrock");
        add(m,"MobOwnerModule","Shows owner names from the player list or UUID; never changes mob names.").number("Range",64,8,128,8);
        var no=add(m,"NoRender","Selectively suppresses visual effects without removing entities.");
        for(String s:new String[]{"Fire","HurtCamera","Blindness"})no.flag(s,true);
        for(String s:new String[]{"PumpkinOverlay","TotemAnimation","Skylight","SignText","NoArmor","NoArmorPlayers","Maps","BossHealth","EnchantingTable","Beacon"})no.flag(s,false);
        no.mode("NoItemsMode","Off","Remove","Hide");
        var shulker=add(m,"ShulkerPreview","27-slot item previews and read-only middle-click viewer; caches observed ender chest contents.");shulker.flag("MiddleClick",true);shulker.flag("EnderChest",true);
        add(m,"Skeleton","Renders player pose skeletons from actual player model rotations.").number("Range",64,8,128,8);
        var small=add(m,"SmallShield","Overrides held item equip progress.");small.number("MainHand",0.5,0,3,0.1);small.number("OffHand",0.5,0,3,0.1);small.flag("TallSword",false);
        var trajectory=add(m,"Trajectories","Predicts held projectile flight and first block/entity impact.");trajectory.number("Width",1,0,5,0.1);for(String s:new String[]{"Red","Green","Blue","Alpha"})trajectory.number(s,255,0,255,1);
        add(m,"ViewClip","Allows third-person camera to pass through blocks.");
        add(m,"VoidESP","Marks air columns open through the bottom four world layers.").number("Radius",8,0,32,1);
        var chunks=add(m,"NewChunks","Marks chunks first observed during this enable session; does not identify new generation.");chunks.flag("Render",true);chunks.number("Range",128,16,256,16);chunks.number("Limit",2048,128,8192,128);
        ExpansionOverlay overlay=new ExpansionOverlay(m);
        ClientTickEvents.END_CLIENT_TICK.register(mc->overlay.tick());
        ClientTickEvents.START_CLIENT_TICK.register(mc->FreeCamera.validate());
        WorldRenderEvents.AFTER_ENTITIES.register(overlay::render);
        ShulkerTooltips.register();
    }
    private static ClientModule add(ModuleManager m,String n,String d){return m.add(new ClientModule(n,Category.RENDER,d));}
    private static void color(ClientModule m,String prefix){ m.mode(prefix.isEmpty()?"HighlightModes":"Mode","None","FlatOutline","Flat","Outline","Full").set(4);rgba(m,prefix); }
    private static void rgba(ClientModule m,String p){m.number(p+"Red",0,0,1,0.1);m.number(p+"Green",1,0,1,0.1);m.number(p+"Blue",p.equals("Bedrock")?0.8:0,0,1,0.1);m.number(p+"Alpha",0.5,0,1,0.1);}
}






