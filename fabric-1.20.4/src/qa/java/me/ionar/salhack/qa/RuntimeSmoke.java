package me.ionar.salhack.qa;

import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import me.ionar.salhack.gui.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.*;
import net.minecraft.world.*;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.gen.*;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.item.*;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;

/** Separate dev-only mod. Never bundled in the release JAR. */
public final class RuntimeSmoke implements ClientModInitializer {
    private int stage,ticks,stageAt;
    private long started;
    private InventoryParityChecks inventoryChecks;
    private final List<String> report=new ArrayList<>();
    private final Path output=Path.of("qa-results");
    public void onInitializeClient() {
        SalHackClient.LOG.info("Runtime QA initializer active");
        started=System.currentTimeMillis();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ticks++;
            if(ticks%100==0) SalHackClient.LOG.info("Runtime QA stage {} screen {}",stage,mc.currentScreen==null?"null":mc.currentScreen.getClass().getSimpleName());
            try {
                if(System.currentTimeMillis()-started>240_000) throw new AssertionError("Runtime QA timed out at stage "+stage);
                if(stage!=8 && ticks-stageAt<20) return;
                run(mc);
            } catch(Throwable e) {
                e.printStackTrace(); report.add("FAIL stage "+stage+": "+e); write(); mc.scheduleStop(); stage=999;
            }
        });
    }
    private void next() { stage++; stageAt=ticks; }
    private void check(boolean condition,String result) { if(!condition) throw new AssertionError(result); report.add("PASS "+result); }
    private void screenshot(MinecraftClient mc,String name) throws Exception {
        Files.createDirectories(output); try(var image=ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) { image.writeTo(output.resolve(name+".png")); }
    }
    private void run(MinecraftClient mc) throws Exception {
        var c=SalHackClient.INSTANCE;
        switch(stage) {
            case 0 -> {
                if(mc.currentScreen!=null && mc.currentScreen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")) { mc.setScreen(new TitleScreen()); return; }
                if(!(mc.currentScreen instanceof TitleScreen) || mc.getOverlay()!=null) return;
                check(c!=null && c.modules.all.size()>20,"Fabric client initialization and module registration");
                Files.createDirectories(output);
                var registry=c.modules.all.stream().map(m -> Map.of("name",m.name,"category",m.category.name(),"settings",m.settings.stream().map(s -> Map.of("name",s.name,"type",s.isText()?"text":s.isMode()?"mode":"number","value",s.text(),"min",s.min,"max",s.max)).toList())).toList();
                Files.writeString(output.resolve("current-module-inventory.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(registry));
                KeyBinding.onKeyPressed(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT)); next();
            }
            case 1 -> {
                check(mc.currentScreen instanceof ClickGuiScreen,"Fabric Left Arrow key opens ClickGUI");
                try(var image=ScreenshotRecorder.takeScreenshot(mc.getFramebuffer())) {
                    int px=(int)(10.0*image.getWidth()/mc.getWindow().getScaledWidth());
                    int py=(int)(64.0*image.getHeight()/mc.getWindow().getScaledHeight());
                    int pixel=image.getColor(px,py);
                    check((pixel&255)==51 && ((pixel>>8)&255)==182 && ((pixel>>16)&255)==215,"Rendered GUI header has original cyan color (no vanilla background overdraw)");
                }
                screenshot(mc,"01-clickgui");
                ((TextFieldWidget)mc.currentScreen.children().stream().filter(e -> e instanceof TextFieldWidget).findFirst().orElseThrow()).setText("Sprint"); next();
            }
            case 2 -> {
                check(!c.modules.on("Sprint"),"Sprint initially disabled");
                mc.currentScreen.mouseClicked(18,89,0);
                check(c.modules.on("Sprint"),"Filtered GUI module click toggles Sprint");
                mc.currentScreen.mouseClicked(18,89,2); mc.currentScreen.keyPressed(GLFW.GLFW_KEY_K,0,0);
                check(c.modules.get("Sprint").key==GLFW.GLFW_KEY_K,"GUI key capture assigns module bind");
                c.saveAs("qa-roundtrip"); c.modules.get("Sprint").setEnabled(false); c.load("qa-roundtrip");
                check(c.modules.on("Sprint") && c.modules.get("Sprint").key==GLFW.GLFW_KEY_K,"Runtime JSON save/load retains toggle and bind");
                c.modules.get("Sprint").setEnabled(false); c.modules.get("Sprint").key=-1;
                mc.setScreen(new TitleScreen());
                var rules=new GameRules(); rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false,null); rules.get(GameRules.DO_MOB_SPAWNING).set(false,null);
                mc.createIntegratedServerLoader().createAndStart("SalHack-QA-"+System.currentTimeMillis(),new LevelInfo("SalHack isolated QA",GameMode.SURVIVAL,false,Difficulty.PEACEFUL,true,rules,DataConfiguration.SAFE_MODE),new GeneratorOptions(12345L,false,false),WorldPresets::createDemoOptions,new TitleScreen()); next();
            }
            case 3 -> {
                if(mc.player==null || mc.world==null || mc.getServer()==null) return;
                mc.setScreen(null); check(c.activeProfile.startsWith("server-"),"Integrated world selects isolated per-world profile");
                var uuid=mc.player.getUuid(); mc.getServer().execute(() -> {
                    var player=mc.getServer().getPlayerManager().getPlayer(uuid);
                    if(player!=null) { player.getInventory().setStack(9,new ItemStack(Items.TOTEM_OF_UNDYING)); player.getInventory().setStack(10,new ItemStack(Items.DIAMOND_HELMET)); player.currentScreenHandler.sendContentUpdates(); }
                });
                c.modules.get("AutoTotem").setEnabled(true); next();
            }
            case 4 -> {
                if(ticks-stageAt<60) return;
                check(mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING),"AutoTotem moves real integrated-server inventory to offhand"); c.modules.get("AutoTotem").setEnabled(false);
                c.modules.get("AutoArmor").setEnabled(true); next();
            }
            case 5 -> {
                if(ticks-stageAt<60) return;
                check(mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isOf(Items.DIAMOND_HELMET),"AutoArmor equips real integrated-server helmet"); c.modules.get("AutoArmor").setEnabled(false);
                for(String name:List.of("Brightness","SafeWalk","Step","NoSlow","NoWeather","EntityESP","StorageESP","Tracers","Nametags","Search","Waypoints","HighwayNavigation")) c.modules.get(name).setEnabled(true);
                for(int i=0;i<2;i++) {
                    var dummy=new net.minecraft.client.network.OtherClientPlayerEntity(mc.world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"RenderQA"+i));
                    dummy.setId(-900-i); dummy.setPosition(mc.player.getX()+3+i*2,mc.player.getY(),mc.player.getZ()+3); mc.world.addEntity(dummy);
                }
                var chest=mc.player.getBlockPos().add(2,0,2); var server=mc.getServer(); server.execute(() -> server.getOverworld().setBlockState(chest,net.minecraft.block.Blocks.CHEST.getDefaultState()));
                c.profile.waypoints.add(new Profile.Waypoint("QA",mc.world.getRegistryKey().getValue().toString(),mc.player.getX()+4,mc.player.getY(),mc.player.getZ()));
                mc.setScreen(new HudEditorScreen(c)); next();
            }
            case 6 -> {
                check(c.hud.bounds.containsKey("Coordinates"),"HUD editor renders measured components in real world"); screenshot(mc,"02-hud-editor");
                check(net.minecraft.client.render.LightmapTextureManager.getBrightness(mc.world.getDimension(),0)==1F,"Fullbright mixin changes actual lightmap brightness");
                check(mc.player.getStepHeight()==1F,"Step mixin changes actual local player step height");
                c.modules.get("Step").setEnabled(false); check(mc.player.getStepHeight()==0.6F,"Disabling Step restores vanilla step height"); c.modules.get("Step").setEnabled(true);
                check(mc.world.getRainGradient(1)==0 && mc.world.getThunderGradient(1)==0,"NoWeather suppresses actual client precipitation gradients");
                var b=c.hud.bounds.get("Watermark"); mc.currentScreen.mouseClicked(b.x()+3,b.y()+3,0); mc.currentScreen.mouseDragged(b.x()+55,b.y()+45,0,52,42); mc.currentScreen.mouseReleased(0,0,0);
                check(c.profile.hud.containsKey("Watermark"),"HUD editor drag updates persisted position");
                c.notify("Storage cluster in chunk 123, 456");
                c.modules.privacy.setEnabled(true); mc.currentScreen.close(); next();
            }
            case 7 -> {
                check(!c.hud.bounds.containsKey("Coordinates") && !c.hud.bounds.containsKey("Waypoints"),"Privacy suppresses coordinate/waypoint HUD in real world"); screenshot(mc,"03-privacy-world");
                check(c.notices.stream().noneMatch(n -> n.text().contains("123, 456")),"Enabling privacy clears already queued coordinate notifications");
                report.add("PASS World rendering ran with two QA player nametags, entity boxes, tracers, storage, Search and waypoints enabled together without a render failure");
                c.save(); check(Files.exists(mc.runDirectory.toPath().resolve("config/creepysalhack/profiles/"+c.activeProfile+".json")),"Runtime profile saved in isolated game directory");
                c.modules.privacy.setEnabled(false); mc.setScreen(null);
                report.addAll(RenderParityChecks.run(mc));
                inventoryChecks=new InventoryParityChecks(report::add);next();
            }
            case 8 -> { if(inventoryChecks.tick()){report.add("LIMIT: These are targeted smoke tests; complete original feature parity and remote-server PvP remain unverified.");write();mc.scheduleStop();stage=999;} }
        }
    }
    private void write() { try { Files.createDirectories(output); Files.write(output.resolve("runtime-smoke.txt"),report); } catch(Exception e) { e.printStackTrace(); } }
}
