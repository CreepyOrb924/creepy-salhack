package me.ionar.salhack.render.expansion;
import java.util.*;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.*;
import net.minecraft.inventory.Inventories;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.text.Text;
public final class ShulkerTooltips {
 private static Object world;
 private static long enderClicked;
 private static final DefaultedList<ItemStack> ender=DefaultedList.ofSize(27,ItemStack.EMPTY);
 private static boolean hasEnder; private static Screen observedScreen;
 public record Data(List<ItemStack> items) implements TooltipData {}
 public static void register(){net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback.EVENT.register(data->data instanceof Data d?new Grid(d):null);net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(world!=mc.world){world=mc.world;hasEnder=false;ender.clear();enderClicked=0;observedScreen=null;}
  if(!RenderHooks.on("ShulkerPreview"))return;
  if(observedScreen!=null&&mc.currentScreen!=observedScreen){observedScreen=null;enderClicked=0;} if(enderClicked>0&&(observedScreen!=null||System.currentTimeMillis()-enderClicked<3000)&&mc.currentScreen instanceof GenericContainerScreen screen&&screen.getScreenHandler().getRows()==3){observedScreen=screen;for(int i=0;i<27;i++)ender.set(i,screen.getScreenHandler().getSlot(i).getStack().copy());hasEnder=true;}
 });}
 public static void clickedEnder(){observedScreen=null;enderClicked=System.currentTimeMillis();}
 public static Data data(ItemStack stack){
  if(!RenderHooks.on("ShulkerPreview"))return null;
  var module=SalHackClient.INSTANCE.modules.get("ShulkerPreview");
  if(stack.isOf(Items.ENDER_CHEST)&&module.flag("EnderChest"))return hasEnder?new Data(ender.stream().map(ItemStack::copy).toList()):null;
  if(!(stack.getItem() instanceof BlockItem item)||!(item.getBlock() instanceof net.minecraft.block.ShulkerBoxBlock))return null;
  var contents=DefaultedList.ofSize(27,ItemStack.EMPTY);var nbt=BlockItem.getBlockEntityNbt(stack);if(nbt!=null)Inventories.readNbt(nbt,contents);return new Data(contents);
 }
 public static boolean open(ItemStack stack){if(!RenderHooks.on("ShulkerPreview")||!SalHackClient.INSTANCE.modules.get("ShulkerPreview").flag("MiddleClick"))return false;var data=data(stack);if(data==null)return false;var mc=MinecraftClient.getInstance();mc.setScreen(new Preview(mc.currentScreen,stack.getName(),data));return true;}
 public static final class Grid implements TooltipComponent {
  private final Data data;public Grid(Data data){this.data=data;}
  public int getWidth(TextRenderer text){return 166;}public int getHeight(){return 56;}
  public void drawItems(TextRenderer text,int x,int y,DrawContext ctx){for(int i=0;i<27;i++){int sx=x+(i%9)*18,sy=y+(i/9)*18;ctx.fill(sx,sy,sx+18,sy+18,0xff373043);ctx.fill(sx+1,sy+1,sx+17,sy+17,0xff19151e);var item=data.items.get(i);ctx.drawItem(item,sx+1,sy+1);ctx.drawItemInSlot(text,item,sx+1,sy+1);}}
 }
 private static final class Preview extends Screen {
  private final Screen parent;private final Data data;Preview(Screen parent,Text title,Data data){super(title);this.parent=parent;this.data=data;}
  public void render(DrawContext ctx,int mx,int my,float delta){renderBackground(ctx,mx,my,delta);int x=(width-166)/2,y=(height-56)/2;ctx.drawTextWithShadow(textRenderer,title,x,y-14,0xffffffff);new Grid(data).drawItems(textRenderer,x,y,ctx);for(int i=0;i<27;i++){int sx=x+(i%9)*18,sy=y+(i/9)*18;if(mx>=sx&&mx<sx+18&&my>=sy&&my<sy+18&&!data.items.get(i).isEmpty())ctx.drawItemTooltip(textRenderer,data.items.get(i),mx,my);}super.render(ctx,mx,my,delta);}
  public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
 }
}

