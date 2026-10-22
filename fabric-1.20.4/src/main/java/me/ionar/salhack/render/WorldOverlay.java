package me.ionar.salhack.render;

import me.ionar.salhack.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;
import java.util.*;

public final class WorldOverlay {
    private final SalHackClient client;
    private final MinecraftClient mc=MinecraftClient.getInstance();
    private final List<BlockPos> storage=new ArrayList<>();
    private final LinkedHashSet<BlockPos> found=new LinkedHashSet<>();
    private final Set<Long> notified=new HashSet<>();
    private BlockPos origin;
    private int cursor, ticks;
    private boolean scanningStorage;
    private final VertexConsumerProvider.Immediate overlayConsumers=VertexConsumerProvider.immediate(new BufferBuilder(4096));
    public boolean draws(String name) { return client.modules.on(name) && !(java.util.Set.of("StorageESP","EntityESP","Tracers").contains(name) && client.modules.get(name).flag("PreferKTHREADD") && me.ionar.salhack.core.CombatCoexistence.otherOn(name)); }
    private static boolean storageEntity(BlockEntity entity) { return entity instanceof LockableContainerBlockEntity || entity instanceof EnderChestBlockEntity; }
    public Block searchBlock=Blocks.DIAMOND_ORE;
    public WorldOverlay(SalHackClient client) { this.client=client; }
    public void reset() { storage.clear(); found.clear(); notified.clear(); origin=null; cursor=0; scanningStorage=false; }
    public void tick() {
        if(mc.world==null || mc.player==null) return;
        var mods=client.modules;
        boolean wantStorage=mods.on("StorageESP") || mods.on("StashFinder");
        if((++ticks%10==0 || !scanningStorage) && wantStorage) {
            storage.clear();
            int range=(int)Math.ceil(Math.max(mods.get("StorageESP").value("Range"),mods.get("StashFinder").value("Range"))/16);
            ChunkPos center=mc.player.getChunkPos();
            for(int x=-range;x<=range;x++) for(int z=-range;z<=range;z++) {
                WorldChunk chunk=mc.world.getChunkManager().getWorldChunk(center.x+x,center.z+z);
                if(chunk==null) continue;
                int count=0;
                for(var entry:chunk.getBlockEntities().entrySet()) {
                    var b=entry.getValue();
                    if(storageEntity(b)) {
                        count++; if(storage.size()<4096) storage.add(entry.getKey().toImmutable());
                    }
                }
                if(mods.on("StashFinder") && count>=mods.get("StashFinder").value("Threshold") && notified.add(chunk.getPos().toLong()))
                    client.notify(client.privacy()?"Storage cluster found (location hidden)":"Storage cluster: " + count + " blocks in chunk " + chunk.getPos().x + ", " + chunk.getPos().z);
            }
        }
        scanningStorage=wantStorage;
        if(!mods.on("Search")) { found.clear(); origin=null; return; }
        int radius=mods.get("Search").setting("Range").integer();
        if(origin==null || origin.getSquaredDistance(mc.player.getBlockPos())>64) { origin=mc.player.getBlockPos(); cursor=0; found.clear(); }
        int width=radius*2+1, total=width*width*width;
        for(int i=0;i<mods.get("Search").value("Budget");i++) {
            int at=cursor++%total;
            BlockPos p=origin.add(at%width-radius,(at/width)%width-radius,at/(width*width)-radius);
            if(mc.world.isChunkLoaded(p.getX()>>4,p.getZ()>>4) && mc.world.getBlockState(p).isOf(searchBlock)) { if(found.size()<2048) found.add(p); } else found.remove(p);
        }
    }
    public void render(WorldRenderContext ctx) {
        if(mc.world==null || mc.player==null || mc.options.hudHidden) return;
        var matrices=ctx.matrixStack(); if(matrices==null) return;
        var camera=ctx.camera().getPos();
        matrices.push();
        var consumers=overlayConsumers;
        var entityModule=client.modules.get("EntityESP");var storageModule=client.modules.get("StorageESP");var tracerModule=client.modules.get("Tracers");
        var entityLayer=EspRenderLayers.lines(entityModule.value("LineWidth"),entityModule.flag("ThroughWalls"));
        var storageLayer=EspRenderLayers.lines(storageModule.value("LineWidth"),storageModule.flag("ThroughWalls"));
        var tracerLayer=EspRenderLayers.lines(tracerModule.value("LineWidth"),tracerModule.flag("ThroughWalls"));
        record Label(Vec3d position,String text) {}
        List<Label> labels=new ArrayList<>();
        int color=client.modules.accent(0); float r=((color>>16)&255)/255f,g=((color>>8)&255)/255f,b=(color&255)/255f;
        if(draws("EntityESP") || draws("Tracers") || client.modules.on("Nametags")) for(var entity:mc.world.getEntities()) {
            if(entity==mc.player || !entity.isAlive()) continue;
            double distance=entity.distanceTo(mc.player);
            var esp=client.modules.get("EntityESP");
            if(draws("EntityESP") && distance<esp.value("Range") && (!esp.flag("PlayersOnly") || entity instanceof PlayerEntity)) WorldRenderer.drawBox(matrices,consumers.getBuffer(entityLayer),OverlayGeometry.entityBox(entity,ctx.tickDelta(),camera),r,g,b,1);
            if(entity instanceof PlayerEntity player) {
                if(draws("Tracers") && distance<client.modules.get("Tracers").value("Range")) {
                    Vec3d from=OverlayGeometry.tracerOrigin(matrices.peek().getPositionMatrix());
                    Vec3d to=player.getLerpedPos(ctx.tickDelta()).add(0,player.getHeight()/2,0).subtract(camera), normal=to.subtract(from).normalize();
                    var lines=consumers.getBuffer(tracerLayer);
                    var entry=matrices.peek();
                    lines.vertex(entry.getPositionMatrix(),(float)from.x,(float)from.y,(float)from.z).color(r,g,b,1).normal(entry.getNormalMatrix(),(float)normal.x,(float)normal.y,(float)normal.z).next();
                    lines.vertex(entry.getPositionMatrix(),(float)to.x,(float)to.y,(float)to.z).color(r,g,b,1).normal(entry.getNormalMatrix(),(float)normal.x,(float)normal.y,(float)normal.z).next();
                }
                if(client.modules.on("Nametags") && distance<client.modules.get("Nametags").value("Range")) labels.add(new Label(player.getLerpedPos(ctx.tickDelta()).add(0,player.getHeight()+0.5,0),player.getName().getString()+" " + Math.round(player.getHealth()+player.getAbsorptionAmount())+" HP"));
            }
        }
        if(draws("StorageESP")) for(var p:storage) if(p.getSquaredDistance(mc.player.getBlockPos())<Math.pow(storageModule.value("Range"),2) && mc.world.isChunkLoaded(p.getX()>>4,p.getZ()>>4) && storageEntity(mc.world.getBlockEntity(p))) WorldRenderer.drawBox(matrices,consumers.getBuffer(storageLayer),new Box(p).expand(0.002).offset(-camera.x,-camera.y,-camera.z),1,0.65f,0.2f,1);
        if(client.modules.on("Search")) for(var p:found) WorldRenderer.drawBox(matrices,consumers.getBuffer(EspRenderLayers.lines(1,true)),new Box(p).expand(0.002).offset(-camera.x,-camera.y,-camera.z),r,g,b,1);
        if(client.modules.on("Waypoints") && !client.privacy()) for(var w:client.profile.waypoints) if(w.dimension.equals(mc.world.getRegistryKey().getValue().toString()) && mc.player.squaredDistanceTo(w.x,w.y,w.z)<1024*1024) {
            labels.add(new Label(new Vec3d(w.x,w.y+1,w.z),w.name+" ["+(int)Math.sqrt(mc.player.squaredDistanceTo(w.x,w.y,w.z))+"m]"));
        }
        consumers.draw();
        for(var label:labels) label(ctx,consumers,label.position(),label.text(),color);
        matrices.pop(); consumers.draw();
    }
    private void label(WorldRenderContext ctx,net.minecraft.client.render.VertexConsumerProvider consumers,Vec3d pos,String text,int color) {
        var matrices=ctx.matrixStack(); matrices.push(); matrices.translate(pos.x-ctx.camera().getPos().x,pos.y-ctx.camera().getPos().y,pos.z-ctx.camera().getPos().z); matrices.multiply(ctx.camera().getRotation()); matrices.scale(-0.025f,-0.025f,0.025f);
        mc.textRenderer.draw(text,-mc.textRenderer.getWidth(text)/2f,0,color,false,matrices.peek().getPositionMatrix(),consumers,net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH,0x66000000,15728880);
        matrices.pop();
    }
}
