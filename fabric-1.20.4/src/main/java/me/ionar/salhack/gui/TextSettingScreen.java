package me.ionar.salhack.gui;
import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.core.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
public final class TextSettingScreen extends Screen {
    private final SalHackClient c; private final Screen parent; private final Setting setting; private TextFieldWidget field;
    public TextSettingScreen(SalHackClient c,Screen parent,Setting setting) { super(Text.literal(setting.name)); this.c=c; this.parent=parent; this.setting=setting; }
    protected void init() {
        field=new TextFieldWidget(textRenderer,15,height/2-15,width-30,20,Text.literal(setting.name)); field.setMaxLength(2048); field.setText(setting.text()); addDrawableChild(field); setInitialFocus(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Save"),b -> { setting.setText(field.getText()); c.changed(); close(); }).dimensions(width/2-95,height/2+20,90,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"),b -> close()).dimensions(width/2+5,height/2+20,90,20).build());
    }
    public void render(DrawContext ctx,int x,int y,float dt) { ctx.fill(0,0,width,height,0xef101016); ctx.drawCenteredTextWithShadow(textRenderer,setting.name,width/2,height/2-40,0xffffffff); super.render(ctx,x,y,dt); }
    public void renderBackground(DrawContext ctx,int x,int y,float dt) {}
    public boolean shouldPause() { return false; }
    public void close() { client.setScreen(parent); }
}
