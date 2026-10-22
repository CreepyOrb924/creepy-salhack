package me.ionar.salhack.modules.combat;
import me.ionar.salhack.SalHackClient;
import me.ionar.salhack.core.*;
import me.ionar.salhack.modules.InventoryModules;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import java.util.*;
import static me.ionar.salhack.modules.combat.CombatSupport.mc;

/** Shared modern crystal mechanics; original variants keep independent settings and selection modes. */
public final class CrystalCombatModule extends ClientModule {
    private final boolean rewrite;
    private int ticks;
    private BlockPos visual;
    private long visualUntil;
    private final Map<BlockPos,Integer> own=new HashMap<>();
    private net.minecraft.client.network.ClientPlayerEntity owner;
    public CrystalCombatModule(String name) {
        super(name,Category.COMBAT,"Crystal automation with sampled world exposure, difficulty, armor, toughness, enchantment and resistance damage evaluation. Original packet timing differs in 1.20.4.");
        rewrite=!name.equals("AutoCrystal");
        if(rewrite) { mode("BreakMode","Always","Smart","OnlyOwn"); mode("PlaceMode","Most","Lethal","Nearest"); number("PlaceRadius",4,0,5,0.5); number("BreakRadius",4,0,5,0.5); flag("MultiPlace",false); flag("AutoSwitch",true); }
        else { mode("Mode","ClientTick","MotionTick"); mode("PlaceMode","Nearest","Most"); mode("DestroyMode","Smart","All"); number("DestroyDistance",4,0,5,0.5); number("PlaceDistance",4,0,5,0.5); flag("Multiplace",true); flag("OnlyPlaceWithCrystal ",false); flag("MinHealthPause",false); number("RequiredHealth",11,0,20,1); flag("AutoMultiplace",false); number("HealthBelowAutoMultiplace",11,0,20,1); }
        number("Ticks",rewrite?2:1,0,20,1); number("WallsRange",3.5,0,5,0.5); number("MinDMG",4,0,20,1); number("MaxSelfDMG",4,0,20,1); number("FacePlace",8,0,20,0.5);
        flag("NoSuicide",true); flag("AntiWeakness",true); flag("PauseWhileEating",!rewrite); flag("PauseIfHittingBlock",false);
        flag("Players",true); flag("Hostile",false); flag("Animals",false); flag("Tamed",false); flag("ProtectFriends",true);
        flag("Place",true); flag("Break",true); number("TargetRange",10,1,16,1); number("CandidatesPerTick",16,1,64,1);
        flag("Render",true); number("Red",51,0,255,5); number("Green",255,0,255,5); number("Blue",243,0,255,5); number("Alpha",153,0,255,5);
        ClientTickEvents.START_CLIENT_TICK.register(client -> { if(enabled() && !rewrite && setting("Mode").integer()==1) update(); });
        WorldRenderEvents.LAST.register(ctx -> {
            if(!enabled() || !flag("Render") || visual==null || System.currentTimeMillis()>visualUntil || mc.world==null || ctx.matrixStack()==null) return;
            var stack=ctx.matrixStack(); var camera=ctx.camera().getPos(); stack.push(); stack.translate(-camera.x,-camera.y,-camera.z);
            var consumer=mc.getBufferBuilders().getEntityVertexConsumers(); WorldRenderer.drawBox(stack,consumer.getBuffer(RenderLayer.getLines()),new Box(visual), (float)value("Red")/255,(float)value("Green")/255,(float)value("Blue")/255,(float)value("Alpha")/255); consumer.draw(RenderLayer.getLines()); stack.pop();
        });
    }
    protected void onDisable() { visual=null; ticks=0; own.clear();owner=null; }
    public void tick() {
        if(!rewrite && setting("Mode").integer()==1) return;
        update();
    }
    private void update() {
        if(CombatCoexistence.deferred(name))return;
        if(!InventoryModules.ready() || ++ticks<Math.max(1,value("Ticks"))) return; ticks=0;
        if(owner!=mc.player){owner=mc.player;own.clear();visual=null;}own.entrySet().removeIf(e->mc.player.age-e.getValue()>200);
        var manager=SalHackClient.INSTANCE.modules;
        // Crystal variants share an interaction resource; one active owner avoids packet storms.
        if(!name.equals("AutoCrystalRewrite") && manager.on("AutoCrystalRewrite")) return;
        if(name.equals("OldAutoCrystalRewrite") && manager.on("AutoCrystal")) return;
        if(flag("PauseWhileEating") && mc.player.isUsingItem()) return;
        if(flag("PauseIfHittingBlock") && mc.options.attackKey.isPressed() && mc.crosshairTarget instanceof BlockHitResult) return;
        if(!rewrite && flag("MinHealthPause") && mc.player.getHealth()+mc.player.getAbsorptionAmount()<value("RequiredHealth")) return;
        List<LivingEntity> targets=new ArrayList<>();
        for(var e:mc.world.getEntities()) if(e instanceof LivingEntity target && target!=mc.player && target.isAlive() && !target.isSpectator() && target.distanceTo(mc.player)<value("TargetRange")) {
            if(target instanceof PlayerEntity p) { if(!flag("Players") || p.isCreative() || CombatSupport.friend(p) || me.ionar.salhack.modules.CombatModules.bot(p)) continue; }
            else if(target instanceof TameableEntity pet && pet.isTamed()) { if(!flag("Tamed")) continue; }
            else if(target instanceof HostileEntity) { if(!flag("Hostile")) continue; }
            else if(target instanceof AnimalEntity) { if(!flag("Animals")) continue; } else continue;
            targets.add(target);
        }
        targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player))); if(targets.size()>4) targets=new ArrayList<>(targets.subList(0,4)); if(targets.isEmpty()) return;
        double breakRange=value(rewrite?"BreakRadius":"DestroyDistance");
        if(flag("Break")) for(var e:mc.world.getEntities()) if(e instanceof EndCrystalEntity crystal && (!rewrite || !setting("BreakMode").text().equals("OnlyOwn") || own.containsKey(e.getBlockPos().down())) && mc.player.getEyePos().distanceTo(e.getPos())<=breakRange && (mc.player.canSee(e) || mc.player.getEyePos().distanceTo(e.getPos())<=value("WallsRange")) && acceptable(e.getPos(),targets, rewrite ? setting("BreakMode").integer()==0 : setting("DestroyMode").integer()==1)) {
            int weapon=mc.player.hasStatusEffect(StatusEffects.WEAKNESS) && flag("AntiWeakness")?CombatSupport.weapon():-1,old=mc.player.getInventory().selectedSlot;
            try { if(weapon>=0) mc.player.getInventory().selectedSlot=weapon; CombatSupport.look(e.getPos(),false); mc.interactionManager.attackEntity(mc.player,crystal); mc.player.swingHand(Hand.MAIN_HAND); }
            finally { mc.player.getInventory().selectedSlot=old; } break;
        }
        if(!flag("Place")) return;
        Hand hand=mc.player.getOffHandStack().isOf(Items.END_CRYSTAL)?Hand.OFF_HAND:Hand.MAIN_HAND;
        int slot=CombatSupport.find(Items.END_CRYSTAL);
        if(hand==Hand.MAIN_HAND && slot<0) return;
        if(rewrite && !flag("AutoSwitch") && hand==Hand.MAIN_HAND && !mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) return;
        if(!rewrite && flag("OnlyPlaceWithCrystal ") && !mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) && hand==Hand.MAIN_HAND) return;
        double placeRange=value(rewrite?"PlaceRadius":"PlaceDistance"); int range=(int)Math.ceil(placeRange); List<BlockPos> positions=new ArrayList<>();
        for(var p:BlockPos.iterate(mc.player.getBlockPos().add(-range,-range,-range),mc.player.getBlockPos().add(range,range,range))) {
            var state=mc.world.getBlockState(p); if(!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) continue;
            Vec3d crystal=Vec3d.ofBottomCenter(p.up()); if(mc.player.getEyePos().distanceTo(crystal)>placeRange || !mc.world.isAir(p.up()) || !mc.world.getOtherEntities(null,new Box(p.up()).stretch(0,1,0)).isEmpty()) continue;
            var hit=mc.world.raycast(new RaycastContext(mc.player.getEyePos(),Vec3d.ofCenter(p).add(0,0.49,0),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mc.player));
            if(!hit.getBlockPos().equals(p) && mc.player.getEyePos().distanceTo(crystal)>value("WallsRange")) continue;
            positions.add(p.toImmutable());
        }
        final var focus=targets.get(0); positions.sort(Comparator.comparingDouble(p -> focus.squaredDistanceTo(Vec3d.ofCenter(p))));
        record Candidate(BlockPos pos,float damage,boolean lethal) {} List<Candidate> selected=new ArrayList<>(); int evaluated=0;
        for(var p:positions) { if(evaluated++>=value("CandidatesPerTick")) break; var center=Vec3d.ofBottomCenter(p.up()); if(!acceptable(center,targets,false)) continue;
            float damage=0;boolean lethal=false; for(var t:targets){float d=CombatSupport.damage(center,t);damage=Math.max(damage,d);if(d>=t.getHealth()+t.getAbsorptionAmount())lethal=true;}
            selected.add(new Candidate(p,damage,lethal));
        }
        if(setting("PlaceMode").text().equals("Most")) selected.sort(Comparator.comparingDouble(Candidate::damage).reversed());
        if(setting("PlaceMode").text().equals("Lethal"))selected.sort(Comparator.comparing(Candidate::lethal).thenComparingDouble(Candidate::damage).reversed());
        int placements=rewrite?(flag("MultiPlace")?2:1):((flag("Multiplace") || flag("AutoMultiplace") && targets.stream().anyMatch(t -> t.getHealth()+t.getAbsorptionAmount()<=value("HealthBelowAutoMultiplace")))?2:1);
        float burstSelf=0;
        for(var candidate:selected) { if(placements--<=0) break; BlockPos best=candidate.pos();
        float additional=CombatSupport.damage(Vec3d.ofBottomCenter(best.up()),mc.player);if(burstSelf+additional>value("MaxSelfDMG")||flag("NoSuicide")&&burstSelf+additional>=mc.player.getHealth()+mc.player.getAbsorptionAmount()-0.5)continue;
        Vec3d point=Vec3d.ofCenter(best).add(0,0.5,0); CombatSupport.look(point,false); int old=mc.player.getInventory().selectedSlot;
        try { if(hand==Hand.MAIN_HAND) mc.player.getInventory().selectedSlot=slot; if(mc.interactionManager.interactBlock(mc.player,hand,new BlockHitResult(point,Direction.UP,best,false)).isAccepted()) { burstSelf+=additional;own.put(best,mc.player.age);mc.player.swingHand(hand); visual=best; visualUntil=System.currentTimeMillis()+500; } }
        finally { mc.player.getInventory().selectedSlot=old; }
        }
    }
    private boolean acceptable(Vec3d center,List<LivingEntity> targets,boolean anyTargetDamage) {
        float self=CombatSupport.damage(center,mc.player); if(self>value("MaxSelfDMG") || (flag("NoSuicide") && self>=mc.player.getHealth()+mc.player.getAbsorptionAmount()-0.5)) return false;
        if(flag("ProtectFriends")) for(var p:mc.world.getPlayers()) if(CombatSupport.friend(p) && p.getPos().distanceTo(center)<=12){float damage=CombatSupport.damage(center,p);if(damage>value("MaxSelfDMG")||damage>=p.getHealth()+p.getAbsorptionAmount()-0.5)return false;}
        for(var target:targets) { float damage=CombatSupport.damage(center,target); if(damage>=value("MinDMG") || (damage>=1 && target.getHealth()+target.getAbsorptionAmount()<=value("FacePlace")) || (anyTargetDamage && damage>0)) return true; } return false;
    }
}
