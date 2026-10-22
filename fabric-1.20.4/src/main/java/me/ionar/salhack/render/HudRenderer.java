package me.ionar.salhack.render;

import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import java.util.*;

public final class HudRenderer {
    public record Bounds(int x,int y,int width,int height) { public boolean contains(double px,double py) { return px>=x && py>=y && px<x+width && py<y+height; } }
    private final SalHackClient c;
    private final MinecraftClient mc=MinecraftClient.getInstance();
    public final Map<String,Bounds> bounds=new LinkedHashMap<>();
    public HudRenderer(SalHackClient c) { this.c=c; }
    public void render(DrawContext ctx,boolean editing) {
        bounds.clear();
        if(mc.player==null || mc.world==null || (mc.options.hudHidden && !editing) || (!c.modules.hud.enabled() && !editing)) return;
        var h=c.modules.hud; int color=c.modules.accent(0);
        if(h.flag("Watermark") || editing) draw(ctx,"Watermark",List.of("Creepy SalHack 2.08.2 | 1.20.4"),0.01,0.02,color,editing,false);
        if(h.flag("ArrayList") || editing) {
            List<String> enabled=c.modules.all.stream().filter(ClientModule::enabled).map(m -> m.name).sorted(Comparator.comparingInt((String s) -> mc.textRenderer.getWidth(s)).reversed()).toList();
            draw(ctx,"ArrayList",enabled.isEmpty()?List.of("ArrayList"):enabled,0.99,0.02,color,editing,true);
        }
        if(h.flag("Coordinates") || editing) {
            if(c.privacy()) { if(editing) draw(ctx,"Coordinates",List.of("Coordinates [privacy]"),0.01,0.95,color,true,false); }
            else {
                boolean nether=mc.world.getRegistryKey().equals(net.minecraft.world.World.NETHER);
                double shownX=mc.player.getX(),shownZ=mc.player.getZ();
                if(c.modules.on("CoordSpoofer")){shownX+=java.util.concurrent.ThreadLocalRandom.current().nextInt(-30000000,30000001);shownZ+=java.util.concurrent.ThreadLocalRandom.current().nextInt(-30000000,30000001);}
                String xyz=String.format(Locale.ROOT,"XYZ %.1f %.1f %.1f",shownX,mc.player.getY(),shownZ);
                List<String> lines=new ArrayList<>(); lines.add(xyz);
                if(nether || mc.world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)) lines.add(String.format(Locale.ROOT,"%s %.1f %.1f",nether?"Overworld":"Nether",Coordinates.convert(shownX,!nether),Coordinates.convert(shownZ,!nether)));
                draw(ctx,"Coordinates",lines,0.01,0.95,color,editing,false);
            }
        }
        if(h.flag("Stats") || editing) draw(ctx,"Stats",List.of(mc.getCurrentFps()+" FPS | "+Math.round(mc.player.getVelocity().horizontalLength()*20*10)/10.0+" m/s | "+mc.player.getHorizontalFacing().asString()),0.01,0.88,color,editing,false);
        if(h.flag("Combat") || editing) {
            int totems=0; for(int i=0;i<mc.player.getInventory().size();i++) { var s=mc.player.getInventory().getStack(i); if(s.isOf(Items.TOTEM_OF_UNDYING)) totems+=s.getCount(); }
            draw(ctx,"Combat",List.of("HP "+Math.round(mc.player.getHealth()+mc.player.getAbsorptionAmount())+" | Armor "+mc.player.getArmor()+" | Totems "+totems),0.01,0.82,color,editing,false);
        }
        if((h.flag("Waypoints") && c.modules.on("Waypoints")) || editing) {
            List<String> waypoints=new ArrayList<>();
            if(!c.privacy()) for(var w:c.profile.waypoints) if(w.dimension.equals(mc.world.getRegistryKey().getValue().toString()) && waypoints.size()<8) waypoints.add(w.name+" "+(int)Math.sqrt(mc.player.squaredDistanceTo(w.x,w.y,w.z))+"m");
            if(!waypoints.isEmpty() || editing) draw(ctx,"Waypoints",waypoints.isEmpty()?List.of("Waypoints"+(c.privacy()?" [privacy]":"")):waypoints,0.7,0.65,color,editing,false);
        }
        if(c.modules.on("HighwayNavigation") || editing) {
            double x=mc.player.getX(),z=mc.player.getZ();
            double cross=Math.min(Math.min(Math.abs(x),Math.abs(z)),Math.min(Math.abs(x-z),Math.abs(x+z))/Math.sqrt(2));
            String line=String.format(Locale.ROOT,"Highway %s | yaw %.1f%s",mc.player.getHorizontalFacing().asString(),MathHelper.wrapDegrees(mc.player.getYaw()),c.privacy()?"":" | offset "+Math.round(cross)+"m");
            draw(ctx,"Highway",List.of(line),0.35,0.92,color,editing,false);
        }
        if(!editing && c.modules.notifications.enabled()) {
            int y=mc.getWindow().getScaledHeight()-25-c.notices.size()*13;
            for(var n:c.notices) { String text=mc.textRenderer.trimToWidth(n.text(),mc.getWindow().getScaledWidth()-24); int width=mc.textRenderer.getWidth(text); ctx.fill(8,y-2,width+16,y+11,0xc0181818); ctx.fill(8,y-2,10,y+11,color); ctx.drawText(mc.textRenderer,text,13,y,0xffffffff,true); y+=14; }
        }
    }
    private void draw(DrawContext ctx,String id,List<String> lines,double dx,double dy,int color,boolean editing,boolean right) {
        int width=lines.stream().mapToInt(mc.textRenderer::getWidth).max().orElse(30)+6,height=lines.size()*11+4;
        boolean logo=id.equals("Watermark") && c.modules.hud.flag("ImageLogo");
        if(logo) { width=144; height=50; }
        int sw=mc.getWindow().getScaledWidth(),sh=mc.getWindow().getScaledHeight();
        var p=c.profile.hud.getOrDefault(id,new Profile.Position(dx,dy));
        int x=(int)Math.max(0,Math.min(sw-width,p.x*sw)),y=(int)Math.max(0,Math.min(sh-height,p.y*sh));
        bounds.put(id,new Bounds(x,y,width,height));
        if(editing) { ctx.fill(x,y,x+width,y+height,0x80111111); ctx.drawBorder(x,y,width,height,color); }
        if(logo) { ctx.drawTexture(new net.minecraft.util.Identifier("creepysalhack","textures/watermark.png"),x,y,0,0,width,height,width,height); return; }
        int lineY=y+2,i=0;
        for(String line:lines) {
            int lx=right?x+width-mc.textRenderer.getWidth(line)-3:x+3;
            if(right) { ctx.fill(lx-3,lineY-2,x+width,lineY+9,0x88111111); ctx.fill(x+width-1,lineY-2,x+width+1,lineY+9,c.modules.accent(i*14)); }
            ctx.drawText(mc.textRenderer,line,lx,lineY,right?c.modules.accent(i*14):color,c.modules.hud.flag("Shadow")); lineY+=11; i++;
        }
    }
}
