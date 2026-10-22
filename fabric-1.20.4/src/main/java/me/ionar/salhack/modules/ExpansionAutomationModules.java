package me.ionar.salhack.modules;

import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.core.*;
import me.ionar.salhack.automation.*;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import com.mojang.authlib.GameProfile;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Independently registered practical automation. Every registered toggle has a live implementation. */
public final class ExpansionAutomationModules {
 public static ModuleManager manager;
 private static final MinecraftClient mc=MinecraftClient.getInstance();
 private static boolean registered;
 public static void register(ModuleManager modules) {
  manager=modules; if(registered) return; registered=true;
  add(new ClientModule("AntiAFK",Category.MISC,"Sends /stats every two minutes while connected, matching the original.") {
   private long next; protected void onEnable(){next=0;}
   public void tick(){if(System.currentTimeMillis()>=next && mc.getNetworkHandler()!=null){next=System.currentTimeMillis()+120000;mc.getNetworkHandler().sendChatCommand("stats");}}
  });
  add(new ClientModule("AutoEat",Category.MISC,"Eats hotbar food or a golden apple at low health; restores owned slot/use state.") {
   private int previous=-1,chosen=-1; private boolean using;
   {number("HealthToEatAt",15,0,36,3);number("Hunger",18,0,20,1);}
   public void tick(){
    if(mc.currentScreen!=null || mc.player.isDead()){restore();return;}
    if(using){if(mc.player.getInventory().selectedSlot!=chosen){restore();return;} if(mc.player.isUsingItem())return;restore();return;}
    if(mc.player.isUsingItem() || mc.options.useKey.isPressed())return;
    boolean apple=mc.player.getHealth()+mc.player.getAbsorptionAmount()<=value("HealthToEatAt");
    if(!apple && mc.player.getHungerManager().getFoodLevel()>value("Hunger"))return;
    for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getStack(i);if(s.isEmpty())continue;
     if(apple? s.isOf(Items.GOLDEN_APPLE)||s.isOf(Items.ENCHANTED_GOLDEN_APPLE):s.isFood()){
      previous=mc.player.getInventory().selectedSlot;chosen=i;mc.player.getInventory().selectedSlot=i;
      if(mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND).isAccepted()){using=true;mc.options.useKey.setPressed(true);}else restore();return;
     }
    }
   }
   private void restore(){if(using)mc.options.useKey.setPressed(false);if(mc.player!=null && previous>=0 && mc.player.getInventory().selectedSlot==chosen)mc.player.getInventory().selectedSlot=previous;previous=chosen=-1;using=false;}
   protected void onDisable(){restore();}
  });
  add(new ClientModule("AutoMount",Category.MISC,"Mounts the nearest enabled adult rideable using normal interaction.") {
   private long next;
   {for(String s:List.of("Boats","Horses","SkeletonHorses","Donkeys","Pigs","Llamas","Mules"))flag(s,true);number("Range",4,0,10,1);number("Delay",1,0,10,1);}
   public void tick(){if(!InventoryModules.ready()||mc.player.hasVehicle()||System.currentTimeMillis()<next)return;next=System.currentTimeMillis()+(long)(value("Delay")*1000); nearest(value("Range"),this::valid).ifPresent(e->mc.interactionManager.interactEntity(mc.player,e,Hand.MAIN_HAND));}
   private boolean valid(Entity e){if(e.hasPassengers() || e instanceof PassiveEntity p && p.isBaby())return false;return e instanceof BoatEntity&&flag("Boats") || e instanceof SkeletonHorseEntity&&flag("SkeletonHorses") || e instanceof HorseEntity&&flag("Horses") || e instanceof DonkeyEntity&&flag("Donkeys") || e instanceof MuleEntity&&flag("Mules") || e instanceof LlamaEntity&&flag("Llamas") || e instanceof PigEntity p&&p.isSaddled()&&flag("Pigs");}
  });
  add(new ClientModule("AutoTame",Category.MISC,"Repeatedly mounts nearby untamed adult horses with an empty hand.") {
   private long next; {number("Delay",0.1,0,1,0.1);}
   public void tick(){if(!InventoryModules.ready() || mc.player.hasVehicle() || !mc.player.getMainHandStack().isEmpty() || System.currentTimeMillis()<next)return;next=System.currentTimeMillis()+(long)(value("Delay")*1000);nearest(4,e->e instanceof AbstractHorseEntity h&&!h.isTame()&&!h.isBaby()&&!h.hasPassengers()).ifPresent(e->mc.interactionManager.interactEntity(mc.player,e,Hand.MAIN_HAND));}
  });
  add(new ClientModule("ChestSwap",Category.MISC,"Swaps elytra and chest armor once using an inventory hotbar swap.") {
   {flag("PreferElytra",true);flag("Curse",false);}
   public void tick(){if(!InventoryModules.ready())return;ItemStack chest=mc.player.getEquippedStack(EquipmentSlot.CHEST);boolean elytra=chest.isEmpty()?flag("PreferElytra"):!chest.isOf(Items.ELYTRA);
    int best=-1;float score=-1;for(int i=0;i<36;i++){var s=mc.player.getInventory().getStack(i);if(flag("Curse") && (EnchantmentHelper.getLevel(Enchantments.BINDING_CURSE,s)>0 || EnchantmentHelper.getLevel(Enchantments.VANISHING_CURSE,s)>0))continue;
     if(elytra?s.isOf(Items.ELYTRA):s.getItem() instanceof ArmorItem a&&a.getSlotType()==EquipmentSlot.CHEST){float n=s.getItem() instanceof ArmorItem a?a.getProtection():0;if(n>score){score=n;best=i;}}
    }if(best>=0){int hot=mc.player.getInventory().selectedSlot;mc.interactionManager.clickSlot(0,InventoryModules.slot(best),hot,SlotActionType.SWAP,mc.player);mc.interactionManager.clickSlot(0,6,hot,SlotActionType.SWAP,mc.player);mc.interactionManager.clickSlot(0,InventoryModules.slot(best),hot,SlotActionType.SWAP,mc.player);}setEnabled(false);
   }
  });
  add(new ClientModule("FakePlayer",Category.MISC,"Spawns a local player clone with copied equipment; never sends a player to the server.") {
   private OtherClientPlayerEntity fake; {text("Name","popbob");}
   public void tick(){if(fake!=null && fake.getWorld()==mc.world)return;remove();fake=new OtherClientPlayerEntity(mc.world,new GameProfile(UUID.nameUUIDFromBytes(("salhack:"+setting("Name").text()).getBytes(java.nio.charset.StandardCharsets.UTF_8)),setting("Name").text()));fake.setId(-2147483000);fake.copyPositionAndRotation(mc.player);fake.setHealth(mc.player.getHealth());fake.getInventory().clone(mc.player.getInventory());mc.world.addEntity(fake);}
   private void remove(){if(fake!=null)fake.discard();fake=null;}protected void onDisable(){remove();}
  });
  add(new ClientModule("Friends",Category.MISC,"Enables friend exclusions; manage names with /sal friend or MiddleClick.") {{setEnabled(true);}});
  add(new ClientModule("MiddleClick",Category.MISC,"Toggles a targeted player in the persistent Friends name list.") {
   private boolean down;
   public void tick(){boolean pressed=GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),2)==GLFW.GLFW_PRESS;if(pressed&&!down&&mc.currentScreen==null&&mc.crosshairTarget instanceof EntityHitResult hit&&hit.getEntity() instanceof net.minecraft.entity.player.PlayerEntity p){Set<String> names=friendNames();String n=p.getName().getString().toLowerCase(Locale.ROOT);boolean removed=names.remove(n);if(!removed)names.add(n);SalHackClient.INSTANCE.profile.friends=new ArrayList<>(names);SalHackClient.INSTANCE.changed();say(p.getName().getString()+(removed?" removed from":" added to")+" friends");}down=pressed;}
  });
  add(new ClientModule("HotbarCache",Category.MISC,"Caches hotbar item identities or refills matching stacks with normal container actions.") {
   private Item[] cache;private long next;{mode("Mode","Cache","Refill");number("Delay",1,0,10,1);}
   protected void onEnable(){cache=null;}
   public void tick(){if(!InventoryModules.ready())return;if(cache==null){cache=new Item[9];for(int i=0;i<9;i++)cache[i]=mc.player.getInventory().getStack(i).getItem();}if(System.currentTimeMillis()<next)return;
    for(int h=0;h<9;h++){var dst=mc.player.getInventory().getStack(h);for(int i=9;i<36;i++){var src=mc.player.getInventory().getStack(i);if(src.isEmpty())continue;
     if(value("Mode")==0 ? cache[h]!=Items.AIR && src.isOf(cache[h]) && !dst.isOf(cache[h]) : dst.isStackable() && dst.getCount()<dst.getMaxCount() && ItemStack.canCombine(dst,src)){
      if(value("Mode")==0)mc.interactionManager.clickSlot(0,i,h,SlotActionType.SWAP,mc.player);
      else {mc.interactionManager.clickSlot(0,i,0,SlotActionType.PICKUP,mc.player);mc.interactionManager.clickSlot(0,h+36,0,SlotActionType.PICKUP,mc.player);if(!mc.player.currentScreenHandler.getCursorStack().isEmpty())mc.interactionManager.clickSlot(0,i,0,SlotActionType.PICKUP,mc.player);}
      next=System.currentTimeMillis()+(long)(value("Delay")*1000);return;
     }
    }}
   }
  });
  add(new ClientModule("Stopwatch",Category.MISC,"Times elapsed wall time and reports it when stopped.") {private long start;protected void onEnable(){start=System.nanoTime();}protected void onDisable(){say(String.format(Locale.ROOT,"Stopwatch: %.3f seconds",(System.nanoTime()-start)/1e9));}});
  add(new ClientModule("ChatNotifier",Category.MISC,"Reports module state changes in local chat.") {
   private final Map<String,Boolean> states=new HashMap<>();protected void onEnable(){states.clear();for(var m:manager.all)states.put(m.name,m.enabled());}
   public void tick(){for(var m:manager.all){Boolean old=states.put(m.name,m.enabled());if(old!=null&&old!=m.enabled())say(m.name+(m.enabled()?" enabled":" disabled"));}}
  });
  add(new ClientModule("AutoNameTag",Category.WORLD,"Uses a named hotbar name tag on nearby eligible mobs.") {
   private long next;private final Set<UUID> done=new HashSet<>();{number("Radius",4,0,10,1);flag("ReplaceOldNames",true);flag("AutoSwitch",false);flag("WithersOnly",true);number("Delay",1,0,10,1);}
   public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;int previous=mc.player.getInventory().selectedSlot,slot=previous;if(!mc.player.getMainHandStack().isOf(Items.NAME_TAG)&&flag("AutoSwitch"))slot=hotbar(Items.NAME_TAG);if(slot<0||!mc.player.getInventory().getStack(slot).isOf(Items.NAME_TAG)||!mc.player.getInventory().getStack(slot).hasCustomName())return;var target=nearest(value("Radius"),e->e instanceof LivingEntity&&!(e instanceof net.minecraft.entity.player.PlayerEntity)&&(!flag("WithersOnly")||e instanceof WitherEntity)&&(!e.hasCustomName()||flag("ReplaceOldNames"))&&!done.contains(e.getUuid()));if(target.isPresent()){mc.player.getInventory().selectedSlot=slot;var e=target.get();if(mc.interactionManager.interactEntity(mc.player,e,Hand.MAIN_HAND).isAccepted())done.add(e.getUuid());mc.player.getInventory().selectedSlot=previous;next=System.currentTimeMillis()+(long)(value("Delay")*1000);}}
   protected void onDisable(){done.clear();}
  });
  AutomationHooks.register(modules);
  WorldAutomation.register(modules);
  modules.add(new MendingAutomation(modules));
  WorldDiagnostics.register(modules);
  StructurePrinter.register(modules);
  ChatAutomation.register(modules);
 }
 private static void add(ClientModule module){if(manager.get(module.name)==null)manager.add(module);}
 public static boolean isFriend(String name){return manager!=null&&manager.on("Friends")&&name!=null&&friendNames().contains(name.toLowerCase(Locale.ROOT));}
 private static Set<String> friendNames(){Set<String>s=new LinkedHashSet<>();if(SalHackClient.INSTANCE!=null)for(String n:SalHackClient.INSTANCE.profile.friends)if(n!=null&&!n.isBlank())s.add(n.strip().toLowerCase(Locale.ROOT));return s;}
 public static void say(String message){if(mc.player!=null)mc.player.sendMessage(Text.literal("[SalHack] "+message),false);}
 public static int hotbar(Item item){if(mc.player==null)return -1;for(int i=0;i<9;i++)if(mc.player.getInventory().getStack(i).isOf(item))return i;return -1;}
 private static Optional<Entity> nearest(double range,java.util.function.Predicate<Entity> filter){return mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(Math.min(range,mc.interactionManager.getReachDistance())),e->e.isAlive()&&mc.player.distanceTo(e)<=range&&mc.player.canSee(e)&&filter.test(e)).stream().min(Comparator.comparingDouble(mc.player::squaredDistanceTo));}
}

