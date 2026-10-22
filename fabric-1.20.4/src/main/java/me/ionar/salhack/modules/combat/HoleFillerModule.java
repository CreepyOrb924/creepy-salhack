package me.ionar.salhack.modules.combat;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.InventoryModules;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import java.util.*;
import static me.ionar.salhack.modules.combat.CombatSupport.mc;
public final class HoleFillerModule extends ClientModule {
 private final List<BlockPos> holes=new ArrayList<>();private int scan,placed;
 public HoleFillerModule(){super("HoleFiller",Category.COMBAT,"Incrementally finds resistant single-block holes and fills a bounded number through normal placement.");flag("TogglesOff",true);number("MaxHoles",5,1,20,1);number("Radius",5,1,10,1);flag("Visualize",true);number("ObsidianRed",0,0,1,0.1);number("ObsidianGreen",1,0,1,0.1);number("ObsidianBlue",0,0,1,0.1);number("ObsidianAlpha",0.5,0,1,0.1);mode("HoleModed","Full","FlatOutline","Flat","Outline");
  WorldRenderEvents.LAST.register(ctx->{if(!enabled()||!flag("Visualize")||ctx.matrixStack()==null||mc.world==null)return;var matrices=ctx.matrixStack();var camera=ctx.camera().getPos();matrices.push();matrices.translate(-camera.x,-camera.y,-camera.z);var consumers=mc.getBufferBuilders().getEntityVertexConsumers();String mode=setting("HoleModed").text();boolean flat=mode.startsWith("Flat"),fill=mode.equals("Full")||mode.equals("Flat");for(var pos:holes){Box box=new Box(pos);if(flat)box=new Box(pos.getX(),pos.getY()+0.01,pos.getZ(),pos.getX()+1,pos.getY()+0.02,pos.getZ()+1);float r=(float)value("ObsidianRed"),g=(float)value("ObsidianGreen"),b=(float)value("ObsidianBlue"),a=(float)value("ObsidianAlpha");if(fill)WorldRenderer.renderFilledBox(matrices,consumers.getBuffer(RenderLayer.getDebugFilledBox()),box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ,r,g,b,a);else WorldRenderer.drawBox(matrices,consumers.getBuffer(RenderLayer.getLines()),box,r,g,b,a);}consumers.draw(RenderLayer.getLines());consumers.draw(RenderLayer.getDebugFilledBox());matrices.pop();});
 }
 protected void onEnable(){scan=placed=0;holes.clear();}
 protected void onDisable(){holes.clear();}
 public void tick(){if(!InventoryModules.ready())return;int r=(int)value("Radius"),side=2*r+1,total=side*side*side;for(int budget=0;budget<256&&scan<total;budget++,scan++){int x=scan%side-r,z=(scan/side)%side-r,y=scan/(side*side)-r;BlockPos p=mc.player.getBlockPos().add(x,y,z);if(p.getSquaredDistance(mc.player.getBlockPos())<=r*r&&CombatSupport.hole(p)&&mc.world.getOtherEntities(null,new Box(p)).isEmpty()&&!holes.contains(p))holes.add(p);}
  holes.removeIf(p->!mc.world.isAir(p));for(var pos:List.copyOf(holes))if(CombatSupport.place(pos,Items.OBSIDIAN,true)){holes.remove(pos);placed++;break;}if(placed>=value("MaxHoles")||scan>=total&&holes.isEmpty()){if(flag("TogglesOff"))setEnabled(false);else{scan=placed=0;}}
 }
}
