package me.ionar.salhack.gui;

import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class ClickGuiScreen extends Screen {
    private final SalHackClient c;
    private TextFieldWidget search;
    private final Set<String> expanded=new HashSet<>();
    private final List<Hit> hits=new ArrayList<>();
    private record Hit(int x,int y,int w,int h,ClientModule module,Setting setting,boolean bind) { boolean contains(double px,double py) { return px>=x && py>=y && px<x+w && py<y+h; } }
    private ClientModule binding;
    private double scroll;
    private long opened=System.nanoTime();
    public ClickGuiScreen(SalHackClient c) { super(Text.literal("Creepy SalHack")); this.c=c; }
    protected void init() {
        String old=search==null?"":search.getText();
        search=new TextFieldWidget(textRenderer,12,31,Math.max(100,width-24),20,Text.literal("Search modules")); search.setPlaceholder(Text.literal("Search modules...")); search.setText(old); addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD editor"),b -> client.setScreen(new HudEditorScreen(c))).dimensions(12,height-24,90,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Profiles"),b -> client.setScreen(new ProfilesScreen(c,this))).dimensions(108,height-24,80,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Privacy"),b -> c.modules.privacy.toggle()).dimensions(194,height-24,80,20).build());
    }
    public boolean shouldPause() { return false; }
    @Override public void renderBackground(DrawContext ctx,int x,int y,float delta) { }
    private float scale() { return (float)c.modules.gui.value("Scale"); }
    public void render(DrawContext ctx,int mx,int my,float delta) {
        ctx.fill(0,0,width,height,0xa6101016);
        ctx.drawTexture(new net.minecraft.util.Identifier("creepysalhack","textures/watermark.png"),12,0,0,0,86,30,86,30);
        String status=binding!=null?"Press a key for "+binding.name+" | Esc clears": "LMB toggle | RMB settings | MMB bind | Wheel scroll | "+c.activeProfile;
        ctx.drawText(textRenderer,textRenderer.trimToWidth(status,width-160),150,11,0xffbbbbbb,true);
        hits.clear(); float scale=scale(); int virtualWidth=(int)(width/scale),columns=Math.max(1,(virtualWidth-16)/126);
        int column=0,y=0,rowHeight=0; String tooltip=null;
        ctx.enableScissor(0,57,width,height-28);
        ctx.getMatrices().push(); ctx.getMatrices().scale(scale,scale,1);
        double progress=c.modules.gui.flag("Animations")?Math.min(1,(System.nanoTime()-opened)/180_000_000.0):1;
        int top=(int)(62/scale+scroll-(1-progress)*12);
        for(Category category:Category.values()) {
            List<ClientModule> modules=c.modules.all.stream().filter(m -> m.category==category && (m.name+" "+m.description).toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT))).toList();
            if(modules.isEmpty() && !search.getText().isEmpty()) continue;
            int x=8+column*126,py=top+y; int panelHeight=20;
            for(var m:modules) panelHeight+=18+(expanded.contains(m.name)?18*(m.settings.size()+1):0);
            panel(ctx,x,py,120,panelHeight,(c.modules.gui.setting("Opacity").integer()<<24)|0x15151b);
            panel(ctx,x,py,120,19,c.modules.accent(category.ordinal()*15));
            ctx.drawText(textRenderer,category.label,x+5,py+5,0xffffffff,true);
            int at=py+20;
            if(modules.isEmpty()) { ctx.drawText(textRenderer,"No ported modules",x+4,at+3,0xff777777,false); }
            for(var m:modules) {
                boolean hover=mx/scale>=x && mx/scale<x+120 && my/scale>=at && my/scale<at+18;
                if(m.enabled()) ctx.fill(x+1,at,x+119,at+17,(c.modules.accent(0)&0xffffff)|0x99200000);
                if(hover) { ctx.fill(x+1,at,x+119,at+17,0x30ffffff); tooltip=m.description; }
                String text=m.name+(m.settings.isEmpty()?"":expanded.contains(m.name)?" -":" +");
                ctx.drawText(textRenderer,textRenderer.trimToWidth(text,110),x+5,at+5,m.enabled()?0xffffffff:0xffc4c4c8,true);
                hits.add(new Hit(x,at,120,18,m,null,false)); at+=18;
                if(expanded.contains(m.name)) {
                    for(var s:m.settings) {
                        ctx.fill(x+4,at,x+116,at+17,0x50101010);
                        ctx.drawText(textRenderer,textRenderer.trimToWidth(s.name+": "+s.display(),104),x+8,at+5,0xffaaaaaf,false);
                        hits.add(new Hit(x+4,at,112,18,m,s,false)); at+=18;
                    }
                    if(!m.configurationOnly) {
                        String key=m.key<0?"None":InputUtil.fromKeyCode(m.key,0).getLocalizedText().getString();
                        ctx.drawText(textRenderer,"Bind: "+key,x+8,at+5,0xff9999aa,false); hits.add(new Hit(x+4,at,112,18,m,null,true)); at+=18;
                    }
                }
            }
            rowHeight=Math.max(rowHeight,Math.max(panelHeight,40)+10); column++;
            if(column>=columns) { column=0; y+=rowHeight; rowHeight=0; }
        }
        int contentHeight=y+rowHeight;
        scroll=Math.max(Math.min(0,(height-92)/scale-contentHeight),Math.min(0,scroll));
        ctx.getMatrices().pop(); ctx.disableScissor();
        super.render(ctx,mx,my,delta);
        if(tooltip!=null && c.modules.gui.flag("Tooltips") && my>57 && my<height-28) ctx.drawOrderedTooltip(textRenderer,textRenderer.wrapLines(Text.literal(tooltip),Math.min(320,width-30)),mx,my);
    }
    private void panel(DrawContext ctx,int x,int y,int w,int h,int color) {
        if(c.modules.gui.flag("Rounded") && h>4) { ctx.fill(x+2,y,x+w-2,y+h,color); ctx.fill(x,y+2,x+w,y+h-2,color); }
        else ctx.fill(x,y,x+w,y+h,color);
    }
    public boolean mouseClicked(double x,double y,int button) {
        if(y>=57 && y<height-28) for(var hit:hits) if(hit.contains(x/scale(),y/scale())) {
            if(hit.setting!=null) { if(hit.setting.isText()) client.setScreen(new TextSettingScreen(c,this,hit.setting)); else { hit.setting.cycle(button==1); c.changed(); } return true; }
            if((button==2 || hit.bind) && !hit.module.configurationOnly) { binding=hit.module; search.setFocused(false); return true; }
            if(button==1) { if(!expanded.add(hit.module.name)) expanded.remove(hit.module.name); return true; }
            if(button==0) { if(hit.module.configurationOnly) { if(!expanded.add(hit.module.name)) expanded.remove(hit.module.name); } else hit.module.toggle(); return true; }
        }
        return super.mouseClicked(x,y,button);
    }
    public boolean mouseScrolled(double x,double y,double horizontal,double vertical) { scroll+=vertical*24; return true; }
    public boolean keyPressed(int key,int scan,int modifiers) {
        if(binding!=null) { binding.key=key==GLFW.GLFW_KEY_ESCAPE?-1:key; binding=null; c.changed(); return true; }
        return super.keyPressed(key,scan,modifiers);
    }
    public void close() { c.save(); super.close(); }
}
