package me.ionar.salhack.gui;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import java.util.*;
public final class ProfilesScreen extends Screen {
    private final SalHackClient c; private final Screen parent;
    private TextFieldWidget name; private String message="";
    public ProfilesScreen(SalHackClient c,Screen parent) { super(Text.literal("Profiles")); this.c=c; this.parent=parent; }
    protected void init() {
        name=new TextFieldWidget(textRenderer,width/2-120,45,240,20,Text.literal("Profile name")); name.setMaxLength(64); name.setText(c.activeProfile); addDrawableChild(name);
        addDrawableChild(ButtonWidget.builder(Text.literal("Save as"),b -> { try { c.saveAs(name.getText()); message="Saved "+c.activeProfile; } catch(Exception e) { message=e.getMessage(); } }).dimensions(width/2-120,72,115,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Load"),b -> { try { if(!c.store.list().contains(name.getText())) { message="Profile does not exist"; return; } c.save(); c.load(name.getText()); message="Loaded "+c.activeProfile; } catch(Exception e) { message=e.getMessage(); } }).dimensions(width/2+5,72,115,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"),b -> close()).dimensions(width/2-60,height-30,120,20).build());
    }
    public void render(DrawContext ctx,int x,int y,float delta) {
        ctx.fill(0,0,width,height,0xef101016); ctx.drawCenteredTextWithShadow(textRenderer,"Creepy SalHack profiles",width/2,18,c.modules.accent(0));
        int at=110;
        try { for(String profile:c.store.list()) { if(at>height-70) break; ctx.drawCenteredTextWithShadow(textRenderer,profile,width/2,at,0xffbbbbbb); at+=12; } } catch(Exception e) { message=e.getMessage(); }
        ctx.drawCenteredTextWithShadow(textRenderer,textRenderer.trimToWidth(message,width-20),width/2,height-50,0xffddbb88); super.render(ctx,x,y,delta);
    }
    public boolean shouldPause() { return false; }
    @Override public void renderBackground(DrawContext ctx,int x,int y,float delta) { }
    public void close() { c.save(); client.setScreen(parent); }
}
