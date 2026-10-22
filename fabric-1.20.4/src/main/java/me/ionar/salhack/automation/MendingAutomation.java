package me.ionar.salhack.automation;
import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import java.util.*;
public final class MendingAutomation extends ClientModule {
 private final MinecraftClient mc=MinecraftClient.getInstance();private final ModuleManager manager;
 private final Map<EquipmentSlot,ItemStack> removed=new LinkedHashMap<>();private long next;private boolean restoreArmor;private Float pitch;
 public MendingAutomation(ModuleManager manager){super("AutoMendArmor",Category.MISC,"Mends equipped armor one piece at a time; moves other pieces into free inventory space and restores them.");this.manager=manager;number("Delay",1,0,10,1);number("Pct",90,0,100,10);flag("GhostHand",false);}
 protected void onEnable(){removed.clear();next=0;restoreArmor=false;pitch=null;}
 public void tick(){if(!InventoryModules.ready()||System.currentTimeMillis()<next)return;next=System.currentTimeMillis()+Math.max(50,(long)(value("Delay")*1000));
  if(manager.on("AutoArmor")){manager.get("AutoArmor").setEnabled(false);restoreArmor=true;}
  EquipmentSlot target=null;double least=101;for(var slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.ARMOR){ItemStack s=mc.player.getEquippedStack(slot);double pct=pct(s);if(EnchantmentHelper.getLevel(Enchantments.MENDING,s)>0&&pct<value("Pct")&&pct<least){least=pct;target=slot;}}
  // Restore a removed, still-damaged piece when the current target is done.
  if(target==null){if(restoreOne())return;setEnabled(false);return;}
  for(var slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.ARMOR&&slot!=target){var s=mc.player.getEquippedStack(slot);if(s.isEmpty())continue;if(EnchantmentHelper.hasBindingCurse(s))continue;if(mc.player.getInventory().getEmptySlot()<0){ExpansionAutomationModules.say("AutoMendArmor: free an inventory slot");setEnabled(false);return;}removed.put(slot,s.copy());mc.interactionManager.clickSlot(0,8-slot.getEntitySlotId(),0,SlotActionType.QUICK_MOVE,mc.player);return;}
  int bottle=ExpansionAutomationModules.hotbar(Items.EXPERIENCE_BOTTLE);if(bottle<0){ExpansionAutomationModules.say("AutoMendArmor: no hotbar XP bottles");setEnabled(false);return;}int old=mc.player.getInventory().selectedSlot;mc.player.getInventory().selectedSlot=bottle;
  if(pitch==null)pitch=mc.player.getPitch();if(flag("GhostHand"))mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(),90,mc.player.isOnGround()));else mc.player.setPitch(90);
  mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);mc.player.getInventory().selectedSlot=old;
  if(flag("GhostHand"))mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(),mc.player.getPitch(),mc.player.isOnGround()));
 }
 private static double pct(ItemStack s){return !s.isDamageable()?100:100d*(s.getMaxDamage()-s.getDamage())/s.getMaxDamage();}
 private boolean restoreOne(){for(var it=removed.entrySet().iterator();it.hasNext();){var e=it.next();if(!mc.player.getEquippedStack(e.getKey()).isEmpty()){it.remove();continue;}for(int i=0;i<36;i++)if(ItemStack.areEqual(e.getValue(),mc.player.getInventory().getStack(i))){mc.interactionManager.clickSlot(0,InventoryModules.slot(i),0,SlotActionType.QUICK_MOVE,mc.player);it.remove();return true;}it.remove();}return false;}
 protected void onDisable(){if(InventoryModules.ready()){for(int i=0;i<4&&restoreOne();i++){}if(pitch!=null&&mc.player.getPitch()==90)mc.player.setPitch(pitch);}if(!removed.isEmpty())ExpansionAutomationModules.say("AutoMendArmor: restore moved armor manually after closing the screen");removed.clear();if(restoreArmor&&mc.world!=null&&mc.getNetworkHandler()!=null&&mc.getNetworkHandler().getConnection().isOpen())manager.get("AutoArmor").setEnabled(true);restoreArmor=false;pitch=null;}
}
