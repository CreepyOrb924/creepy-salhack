package me.ionar.salhack.render;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import java.util.*;
/** Dedicated line pipeline; depth writes are disabled so overlays cannot corrupt later rendering. */
public final class EspRenderLayers extends RenderLayer {
    private static final Map<Integer,RenderLayer> CACHE=new HashMap<>();
    private EspRenderLayers(String name,Runnable start,Runnable end){super(name,VertexFormats.LINES,VertexFormat.DrawMode.LINES,4096,false,true,start,end);}
    public static RenderLayer lines(double width,boolean throughWalls){
        int pixels=(int)Math.round(Math.max(.5,Math.min(3,width))*4),key=pixels*2+(throughWalls?1:0);
        return CACHE.computeIfAbsent(key,k->new EspRenderLayers("salhack_esp_"+k,()->{
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
            if(throughWalls)RenderSystem.disableDepthTest();else RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);RenderSystem.lineWidth(pixels/4f);
        },()->{RenderSystem.lineWidth(1);RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.enableCull();RenderSystem.disableBlend();}));
    }
}
