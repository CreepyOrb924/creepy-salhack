package me.ionar.salhack.modules;

import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.Hand;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.PotionUtil;
import me.ionar.salhack.inventory.InventoryDecisions;
import me.ionar.salhack.inventory.InventoryDecisions.Offhand;

public final class InventoryModules {
    private static final MinecraftClient mc=MinecraftClient.getInstance();
    public static boolean ready() { return mc.player!=null && mc.interactionManager!=null && mc.currentScreen==null && mc.player.currentScreenHandler==mc.player.playerScreenHandler && mc.player.currentScreenHandler.getCursorStack().isEmpty(); }
    public static int slot(int inventorySlot) { return inventorySlot<9?inventorySlot+36:inventorySlot; }
    public static void register(ModuleManager manager) {
        manager.add(new ClientModule("AutoTotem",Category.COMBAT,"Original offhand modes, fallback and health/flight/fall overrides; waits for an empty cursor.") {
            {
                number("Health",16,0,20,0.5); mode("Mode","Totem","Gap","Crystal","Pearl","Chorus","Strength","Shield");
                mode("Fallback","Crystal","Totem","Gap","Pearl","Chorus","Strength","Shield");
                number("FallDistance",15,0,100,10); flag("TotemOnElytra",true); flag("SwordGap",false); flag("StrGap",false); flag("HotbarFirst",false);
            }
            public void tick() {
                if(!ready() || mc.player.isDead() || mc.player.isSpectator()) return;
                Offhand selected=Offhand.valueOf(setting("Mode").text());
                boolean nearby=mc.world.getPlayers().stream().anyMatch(p -> p!=mc.player && mc.player.distanceTo(p)<=15 && !SalHackClient.INSTANCE.isFriend(p.getGameProfile().getName()));
                Offhand desired=InventoryDecisions.desired(selected,mc.player.getHealth()+mc.player.getAbsorptionAmount(),value("Health"),mc.player.fallDistance,value("FallDistance"),mc.player.isFallFlying(),flag("TotemOnElytra"),mc.player.getMainHandStack().getItem() instanceof SwordItem,mc.player.hasStatusEffect(StatusEffects.STRENGTH),flag("SwordGap"),flag("StrGap"),nearby);
                if(matches(mc.player.getOffHandStack(),desired)) return;
                int found=find(desired,flag("HotbarFirst"));
                Offhand fallback=Offhand.valueOf(setting("Fallback").text());
                if(found<0 && fallback!=desired) {
                    if(matches(mc.player.getOffHandStack(),fallback)) return;
                    found=find(fallback,true);
                    if(found<0 && fallback!=Offhand.Totem) {if(matches(mc.player.getOffHandStack(),Offhand.Totem)) return;found=find(Offhand.Totem,true);}
                }
                if(found>=0) mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,found,40,SlotActionType.SWAP,mc.player);
            }
            private int find(Offhand mode,boolean hotbarFirst) {
                for(int i:InventoryDecisions.candidateSlots(hotbarFirst)) if(i!=45 && mc.player.playerScreenHandler.getSlot(i).canTakeItems(mc.player) && matches(mc.player.playerScreenHandler.getSlot(i).getStack(),mode)) return i;
                return -1;
            }
            private boolean matches(ItemStack stack,Offhand mode) {
                return switch(mode) {
                    case Totem -> stack.isOf(Items.TOTEM_OF_UNDYING);
                    case Gap -> stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE);
                    case Crystal -> stack.isOf(Items.END_CRYSTAL);
                    case Pearl -> stack.isOf(Items.ENDER_PEARL);
                    case Chorus -> stack.isOf(Items.CHORUS_FRUIT);
                    case Strength -> stack.isOf(Items.POTION) && PotionUtil.getPotionEffects(stack).stream().anyMatch(e -> e.getEffectType()==StatusEffects.STRENGTH);
                    case Shield -> stack.isOf(Items.SHIELD);
                };
            }
        });
        manager.add(new ClientModule("AutoArmor",Category.COMBAT,"Fills empty armor slots with protection-ranked armor; optional elytra preference and broken-wing replacement.") {
            private long lastAction;
            { number("Delay",50,0,1000,1); flag("Curse",false); flag("Elytra",false); flag("ElytraReplace",false); }
            protected void onEnable() {lastAction=0;}
            public void tick() {
                if(!ready() || mc.player.isDead() || manager.on("AutoMendArmor") || manager.on("AutoMend") || System.nanoTime()-lastAction<value("Delay")*1_000_000) return;
                for(EquipmentSlot type:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
                    ItemStack current=mc.player.getEquippedStack(type);
                    if(!current.isEmpty()) continue;
                    int best=-1; double score=-1;
                    for(int i:InventoryDecisions.candidateSlots(false)) {
                        var source=mc.player.playerScreenHandler.getSlot(i);ItemStack candidate=source.getStack();
                        if(!source.canTakeItems(mc.player) || flag("Curse") && EnchantmentHelper.hasBindingCurse(candidate)) continue;
                        if(type==EquipmentSlot.CHEST && flag("Elytra") && candidate.isOf(Items.ELYTRA) && remaining(candidate)>3 && ElytraItem.isUsable(candidate)) {best=i;break;}
                        if(candidate.getItem() instanceof ArmorItem armor && armor.getSlotType()==type) {
                            double candidateScore=armor.getProtection()+EnchantmentHelper.getLevel(Enchantments.PROTECTION,candidate);
                            if(candidateScore>score) {score=candidateScore;best=i;}
                        }
                    }
                    if(best>=0) {swap(best,8-type.getEntitySlotId());return;}
                }
                ItemStack chest=mc.player.getEquippedStack(EquipmentSlot.CHEST);
                if(!flag("ElytraReplace") || !chest.isOf(Items.ELYTRA) || ElytraItem.isUsable(chest) || EnchantmentHelper.hasBindingCurse(chest)) return;
                for(int i:InventoryDecisions.candidateSlots(false)) {
                    var source=mc.player.playerScreenHandler.getSlot(i);ItemStack candidate=source.getStack();
                    if(source.canTakeItems(mc.player) && candidate.isOf(Items.ELYTRA) && ElytraItem.isUsable(candidate) && (!flag("Curse") || !EnchantmentHelper.hasBindingCurse(candidate))) {swap(i,6);return;}
                }
            }
            private double remaining(ItemStack stack) {return stack.isDamageable()?100D*(stack.getMaxDamage()-stack.getDamage())/stack.getMaxDamage():100;}
            private void swap(int source,int target) {
                int sync=mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync,source,0,SlotActionType.PICKUP,mc.player);
                mc.interactionManager.clickSlot(sync,target,0,SlotActionType.PICKUP,mc.player);
                if(!mc.player.playerScreenHandler.getCursorStack().isEmpty()) mc.interactionManager.clickSlot(sync,source,0,SlotActionType.PICKUP,mc.player);
                lastAction=System.nanoTime();
            }
        });
        manager.add(new ClientModule("AutoTool",Category.WORLD,"Selects the best hotbar tool for the targeted block while mining; restores the prior slot on release.") {
            private int previous=-1;
            public void tick() {
                if(!ready() || !mc.options.attackKey.isPressed() || !(mc.crosshairTarget instanceof BlockHitResult hit)) { restore(); return; }
                var state=mc.world.getBlockState(hit.getBlockPos()); int best=mc.player.getInventory().selectedSlot; float speed=1;
                for(int i=0;i<9;i++) { var stack=mc.player.getInventory().getStack(i); float v=stack.getMiningSpeedMultiplier(state); if(v>speed && (!stack.isDamageable() || stack.getMaxDamage()-stack.getDamage()>2)) { speed=v; best=i; } }
                if(best!=mc.player.getInventory().selectedSlot) { if(previous<0) previous=mc.player.getInventory().selectedSlot; mc.player.getInventory().selectedSlot=best; }
            }
            private void restore() { if(previous>=0 && mc.player!=null) { mc.player.getInventory().selectedSlot=previous; previous=-1; } }
            protected void onDisable() { restore(); }
        });
        manager.add(new ClientModule("ChestStealer",Category.MISC,"Quick-moves container items one at a time while its screen is open; no automatic opening.") {
            private int ticks;
            { number("Delay",4,2,20,1); }
            public void tick() {
                if(mc.interactionManager==null || !(mc.player.currentScreenHandler instanceof net.minecraft.screen.GenericContainerScreenHandler h) || !h.getCursorStack().isEmpty() || ++ticks<value("Delay")) return;
                ticks=0; if(mc.player.getInventory().getEmptySlot()<0) return;
                for(int i=0;i<h.getRows()*9;i++) if(!h.getSlot(i).getStack().isEmpty()) { mc.interactionManager.clickSlot(h.syncId,i,0,SlotActionType.QUICK_MOVE,mc.player); return; }
            }
        });
    }
}
