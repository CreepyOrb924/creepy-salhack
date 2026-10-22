package me.ionar.salhack.gui;

import me.ionar.salhack.*;
import me.ionar.salhack.core.Profile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private final SalHackClient c;
    private String dragging;
    private double offsetX,offsetY;
    public HudEditorScreen(SalHackClient c) { super(Text.literal("Creepy SalHack HUD editor")); this.c=c; }
    public boolean shouldPause() { return false; }
    public void render(DrawContext ctx,int mx,int my,float delta) {
        ctx.fill(0,0,width,height,0x40000000);
        c.hud.render(ctx,true);
        ctx.drawCenteredTextWithShadow(textRenderer,"HUD editor | Drag elements | Right-click toggles | R resets | Esc saves",width/2,5,0xffffffff);
    }
    public boolean mouseClicked(double x,double y,int button) {
        for(var entry:c.hud.bounds.entrySet()) if(entry.getValue().contains(x,y)) {
            if(button==1) {
                String key=entry.getKey().equals("Highway")?null:entry.getKey();
                if(key!=null) { var s=c.modules.hud.setting(key); s.cycle(false); c.changed(); }
                return true;
            }
            if(button==0) { dragging=entry.getKey(); offsetX=x-entry.getValue().x(); offsetY=y-entry.getValue().y(); return true; }
        }
        return super.mouseClicked(x,y,button);
    }
    public boolean mouseDragged(double x,double y,int button,double dx,double dy) {
        if(dragging!=null) {
            c.profile.hud.put(dragging,new Profile.Position(Math.max(0,Math.min(1,(x-offsetX)/width)),Math.max(0,Math.min(1,(y-offsetY)/height)))); c.changed(); return true;
        }
        return super.mouseDragged(x,y,button,dx,dy);
    }
    public boolean mouseReleased(double x,double y,int button) { dragging=null; return super.mouseReleased(x,y,button); }
    public boolean keyPressed(int key,int scan,int modifiers) { if(key==GLFW.GLFW_KEY_R) { c.profile.hud.clear(); c.changed(); return true; } return super.keyPressed(key,scan,modifiers); }
    public void close() { c.save(); super.close(); }
}
