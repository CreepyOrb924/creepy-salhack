package me.ionar.salhack.qa;

import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.core.ClientModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/** Dev-only, stage-driven integrated-server checks. Call tick once per client tick until true. */
public final class InventoryParityChecks {
    private final Consumer<String> passed;
    private final MinecraftClient mc=MinecraftClient.getInstance();
    private final ClientModule offhand=SalHackClient.INSTANCE.modules.get("AutoTotem"),armor=SalHackClient.INSTANCE.modules.get("AutoArmor");
    private final List<Scenario> cases=new ArrayList<>();
    private final Map<ClientModule,Map<String,Double>> saved=new LinkedHashMap<>();
    private final Map<ClientModule,Boolean> enabled=new LinkedHashMap<>();
    private List<ItemStack> original;
    private float originalHealth;
    private int originalSelected;
    private StatusEffectInstance originalStrength;
    private int test,phase,wait;
    private CompletableFuture<?> pending;
    private boolean restoring;
    private OtherClientPlayerEntity crystalTarget;
    private record Scenario(String label,Consumer<ServerPlayerEntity> inventory,Runnable configure,Predicate<ServerPlayerEntity> serverCheck,BooleanSupplier clientCheck,Runnable activate) {}
    public InventoryParityChecks(Consumer<String> passed) {
        this.passed=passed;
        for(var m:List.of(offhand,armor)) { var state=new LinkedHashMap<String,Double>();for(var s:m.settings) state.put(s.name,s.get()); saved.put(m,state);enabled.put(m,m.enabled());m.setEnabled(false); }
        addOffhand("Totem",Items.TOTEM_OF_UNDYING);
        addOffhand("Gap",Items.GOLDEN_APPLE);
        addOffhand("Pearl",Items.ENDER_PEARL);
        addOffhand("Chorus",Items.CHORUS_FRUIT);
        addOffhand("Strength",Items.POTION);
        addOffhand("Shield",Items.SHIELD);
        cases.add(new Scenario("AutoTotem Crystal mode selects crystal with a nearby non-friend target",this::fixture,()->mode("Crystal"),p->p.getOffHandStack().isOf(Items.END_CRYSTAL),()->mc.player.getOffHandStack().isOf(Items.END_CRYSTAL),()->{
            crystalTarget=new OtherClientPlayerEntity(mc.world,new GameProfile(UUID.randomUUID(),"InventoryParityTarget"));crystalTarget.setPosition(mc.player.getPos().add(2,0,0));int id=-500;while(mc.world.getEntityById(id)!=null)id--;crystalTarget.setId(id);mc.world.addEntity(crystalTarget);offhand.setEnabled(true);
        }));
        cases.add(new Scenario("AutoTotem SwordGap overrides Totem mode above health threshold",this::fixture,()->{mode("Totem");offhand.setting("SwordGap").set(1);},p->p.getOffHandStack().isOf(Items.GOLDEN_APPLE),()->mc.player.getOffHandStack().isOf(Items.GOLDEN_APPLE),()->offhand.setEnabled(true)));
        cases.add(new Scenario("AutoTotem StrGap selects actual Strength potion before SwordGap",this::fixture,()->{mode("Totem");offhand.setting("SwordGap").set(1);offhand.setting("StrGap").set(1);},p->strength(p.getOffHandStack()),()->strength(mc.player.getOffHandStack()),()->offhand.setEnabled(true)));
        cases.add(new Scenario("AutoTotem low health overrides SwordGap and StrGap",p->{fixture(p);p.setHealth(1);},()->{mode("Shield");offhand.setting("SwordGap").set(1);offhand.setting("StrGap").set(1);},p->p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING),()->mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING),()->offhand.setEnabled(true)));
        cases.add(new Scenario("AutoTotem uses configured fallback when desired item is absent",p->{fixture(p);p.getInventory().setStack(15,ItemStack.EMPTY);},()->{mode("Shield");offhand.setting("Fallback").setText("Gap");},p->p.getOffHandStack().isOf(Items.GOLDEN_APPLE),()->mc.player.getOffHandStack().isOf(Items.GOLDEN_APPLE),()->offhand.setEnabled(true)));
        cases.add(new Scenario("AutoTotem emergency totem is used after desired and fallback are absent",p->{fixture(p);p.getInventory().setStack(15,ItemStack.EMPTY);p.getInventory().setStack(10,ItemStack.EMPTY);},()->{mode("Shield");offhand.setting("Fallback").setText("Gap");},p->p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING),()->mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING),()->offhand.setEnabled(true)));
        cases.add(new Scenario("AutoTotem waits while the inventory GUI is open",this::fixture,()->mode("Totem"),p->p.getOffHandStack().isEmpty(),()->mc.player.getOffHandStack().isEmpty(),()->{mc.setScreen(new InventoryScreen(mc.player));offhand.setEnabled(true);}));
        cases.add(new Scenario("AutoTotem and AutoArmor preserve an active cursor",p->{fixture(p);p.currentScreenHandler.setCursorStack(new ItemStack(Items.STONE));},()->mode("Totem"),p->p.getOffHandStack().isEmpty() && p.currentScreenHandler.getCursorStack().isOf(Items.STONE),()->mc.player.getOffHandStack().isEmpty() && mc.player.currentScreenHandler.getCursorStack().isOf(Items.STONE),()->{mc.player.currentScreenHandler.setCursorStack(new ItemStack(Items.STONE));offhand.setEnabled(true);armor.setEnabled(true);}));
        cases.add(new Scenario("AutoArmor selects Protection-ranked armor and rejects Binding when Curse enabled",p->{fixture(p);ItemStack cursed=new ItemStack(Items.NETHERITE_HELMET);cursed.addEnchantment(Enchantments.BINDING_CURSE,1);cursed.addEnchantment(Enchantments.PROTECTION,4);p.getInventory().setStack(20,cursed);ItemStack helmet=new ItemStack(Items.DIAMOND_HELMET);helmet.addEnchantment(Enchantments.PROTECTION,3);p.getInventory().setStack(21,helmet);},()->armor.setting("Curse").set(1),p->p.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.DIAMOND_HELMET),()->mc.player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.DIAMOND_HELMET),()->armor.setEnabled(true)));
        cases.add(new Scenario("AutoArmor Elytra preference fills an empty chest slot",p->{fixture(p);p.getInventory().setStack(20,new ItemStack(Items.DIAMOND_CHESTPLATE));p.getInventory().setStack(21,new ItemStack(Items.ELYTRA));},()->armor.setting("Elytra").set(1),p->p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA),()->mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA),()->armor.setEnabled(true)));
        cases.add(new Scenario("AutoArmor ElytraReplace exchanges broken wings and keeps the old wings",p->{fixture(p);ItemStack broken=new ItemStack(Items.ELYTRA);broken.setDamage(broken.getMaxDamage()-1);p.equipStack(EquipmentSlot.CHEST,broken);p.getInventory().setStack(20,new ItemStack(Items.ELYTRA));},()->armor.setting("ElytraReplace").set(1),p->ElytraItem.isUsable(p.getEquippedStack(EquipmentSlot.CHEST)) && p.getInventory().getStack(20).isOf(Items.ELYTRA) && !ElytraItem.isUsable(p.getInventory().getStack(20)),()->ElytraItem.isUsable(mc.player.getEquippedStack(EquipmentSlot.CHEST)),()->armor.setEnabled(true)));
    }
    private void addOffhand(String mode,Item expected) {
        cases.add(new Scenario("AutoTotem "+mode+" mode accepted by integrated server",this::fixture,()->mode(mode),p->p.getOffHandStack().isOf(expected),()->mc.player.getOffHandStack().isOf(expected),()->offhand.setEnabled(true)));
    }
    private static boolean strength(ItemStack stack) {return stack.isOf(Items.POTION) && PotionUtil.getPotionEffects(stack).stream().anyMatch(e->e.getEffectType()==StatusEffects.STRENGTH);}
    private void mode(String mode) {offhand.setting("Mode").setText(mode);}
    private void defaults() {
        offhand.setting("Health").set(16);offhand.setting("Mode").setText("Totem");offhand.setting("Fallback").setText("Crystal");offhand.setting("FallDistance").set(100);offhand.setting("TotemOnElytra").set(0);offhand.setting("SwordGap").set(0);offhand.setting("StrGap").set(0);offhand.setting("HotbarFirst").set(0);
        armor.setting("Delay").set(50);armor.setting("Curse").set(0);armor.setting("Elytra").set(0);armor.setting("ElytraReplace").set(0);
    }
    private void fixture(ServerPlayerEntity p) {
        if(original==null) {original=new ArrayList<>();for(int i=0;i<p.getInventory().size();i++) original.add(p.getInventory().getStack(i).copy());originalHealth=p.getHealth();originalSelected=p.getInventory().selectedSlot;var strength=p.getStatusEffect(StatusEffects.STRENGTH);originalStrength=strength==null?null:new StatusEffectInstance(strength);}
        p.getInventory().clear();p.currentScreenHandler.setCursorStack(ItemStack.EMPTY);p.setHealth(20);p.removeStatusEffect(StatusEffects.STRENGTH);p.getInventory().selectedSlot=0;
        p.getInventory().setStack(0,new ItemStack(Items.DIAMOND_SWORD));p.getInventory().setStack(9,new ItemStack(Items.TOTEM_OF_UNDYING));p.getInventory().setStack(10,new ItemStack(Items.GOLDEN_APPLE));p.getInventory().setStack(11,new ItemStack(Items.END_CRYSTAL));p.getInventory().setStack(12,new ItemStack(Items.ENDER_PEARL));p.getInventory().setStack(13,new ItemStack(Items.CHORUS_FRUIT));p.getInventory().setStack(14,PotionUtil.setPotion(new ItemStack(Items.POTION),Potions.STRENGTH));p.getInventory().setStack(15,new ItemStack(Items.SHIELD));
    }
    private ServerPlayerEntity serverPlayer() {return mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());}
    private void disable() {offhand.setEnabled(false);armor.setEnabled(false);if(crystalTarget!=null) {mc.world.removeEntity(crystalTarget.getId(),Entity.RemovalReason.DISCARDED);crystalTarget=null;}if(mc.currentScreen instanceof InventoryScreen) mc.setScreen(null);mc.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);}
    public boolean tick() {
        if(mc.getServer()==null || mc.player==null || mc.world==null) throw new AssertionError("Inventory QA requires integrated world");
        if(restoring) {
            if(!pending.isDone()) return false;pending.join();
            for(var entry:saved.entrySet()) {for(var setting:entry.getKey().settings) setting.set(entry.getValue().get(setting.name));entry.getKey().setEnabled(enabled.get(entry.getKey()));}
            return true;
        }
        if(test>=cases.size()) {
            disable();restoring=true;
            pending=mc.getServer().submit(()->{var p=serverPlayer();p.getInventory().clear();for(int i=0;i<original.size();i++) p.getInventory().setStack(i,original.get(i));p.currentScreenHandler.setCursorStack(ItemStack.EMPTY);p.getInventory().selectedSlot=originalSelected;p.setHealth(originalHealth);p.removeStatusEffect(StatusEffects.STRENGTH);if(originalStrength!=null)p.addStatusEffect(originalStrength);p.currentScreenHandler.sendContentUpdates();});return false;
        }
        Scenario s=cases.get(test);
        switch(phase) {
            case 0 -> {disable();defaults();s.configure.run();mc.player.getInventory().selectedSlot=0;mc.player.fallDistance=0;pending=mc.getServer().submit(()->{var p=serverPlayer();s.inventory.accept(p);p.currentScreenHandler.sendContentUpdates();});phase=1;wait=0;}
            case 1 -> {if(!pending.isDone())return false;pending.join();if(++wait<10)return false;s.activate.run();phase=2;wait=0;}
            case 2 -> {if(++wait<20)return false;boolean client=s.clientCheck.getAsBoolean();pending=mc.getServer().submit(()->client && s.serverCheck.test(serverPlayer()));phase=3;}
            case 3 -> {if(!pending.isDone())return false;if(!Boolean.TRUE.equals(pending.join()))throw new AssertionError(s.label);passed.accept(s.label);disable();phase=0;test++;}
        }
        return false;
    }
}
