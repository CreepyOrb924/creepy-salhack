package me.ionar.salhack.render.expansion;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import java.util.*;
/** Own render layers keep depth/cull/blend changes scoped to these diagnostics. */
public final class OverlayLayers extends RenderLayer {
 private static final Map<Integer,RenderLayer> WIDTHS=new HashMap<>();
 public static final RenderLayer LINES=lines(1);
 public static final RenderLayer FILLED=new OverlayLayers("salhack_overlay_fill",VertexFormats.POSITION_COLOR,VertexFormat.DrawMode.TRIANGLE_STRIP,()->{RenderSystem.setShader(GameRenderer::getPositionColorProgram);begin();},OverlayLayers::end);
 private OverlayLayers(String name,VertexFormat format,VertexFormat.DrawMode mode,Runnable start,Runnable finish){super(name,format,mode,4096,false,true,start,finish);}
 public static RenderLayer lines(double width){int key=(int)Math.round(MathHelperClamp(width)*10);return WIDTHS.computeIfAbsent(key,k->new OverlayLayers("salhack_overlay_lines_"+k,VertexFormats.LINES,VertexFormat.DrawMode.LINES,()->{RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);begin();RenderSystem.lineWidth(k/10f);},()->{RenderSystem.lineWidth(1);end();}));}
 private static double MathHelperClamp(double width){return Math.max(.1,Math.min(5,width));}
 private static void begin(){RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();}
 private static void end(){RenderSystem.enableDepthTest();RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.disableBlend();}
}
