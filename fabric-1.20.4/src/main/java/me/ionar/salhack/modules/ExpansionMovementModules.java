package me.ionar.salhack.modules;

import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.*;
import me.ionar.salhack.movement.*;

import me.ionar.salhack.mixin.expansion.movement.EntityRemovalAccess;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ElytraItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

/** Independent original movement and packet tools. Server acceptance is not assumed. */
public final class ExpansionMovementModules {
    public static MinecraftClient mc;
    private static ModuleManager manager;
    public static Blink blink;
    public static Ghost ghost;
    public static EntityDesync desync;
    public static boolean sendingOwned;
    private ExpansionMovementModules() {}
    public static ClientModule module(String name) { return manager==null?null:manager.get(name); }
    public static boolean on(String name) { return manager!=null && manager.on(name); }
    public static boolean ready() { return manager!=null && mc!=null && mc.player!=null && mc.world!=null; }
    public static boolean moving() { return ready() && (mc.player.input.movementForward!=0 || mc.player.input.movementSideways!=0); }
    public static Vec3d horizontal(double speed,double y) {
        double[] d=MovementMath.direction(mc.player.input.movementForward,mc.player.input.movementSideways,mc.player.getYaw(),speed);
        return new Vec3d(d[0],y,d[1]);
    }
    public static void sendOwned(Packet<?> packet) {
        if(mc.getNetworkHandler()==null) return;
        boolean prior=sendingOwned; sendingOwned=true;
        try { mc.getNetworkHandler().sendPacket(packet); } finally { sendingOwned=prior; }
    }
    public static void sneak(boolean pressed) {
        if(ready()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,pressed?ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY:ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }
    public static void register(ModuleManager m) {
        mc=MinecraftClient.getInstance();
        manager=m;
        m.add(new ClientModule("AntiLevitation",Category.MOVEMENT,"Suppresses the local levitation movement effect without removing its status icon."));
        blink=m.add(new Blink());
        m.add(new ClientModule("EntityControl",Category.MOVEMENT,"Local saddle and steering checks for horses, pigs and striders; server rules still apply."));
        m.add(new ClientModule("EntitySpeed",Category.MOVEMENT,"Sets mounted horizontal motion from player direction.") {
            { number("Speed",1,1,5,0.1); }
            public void tick() { Entity e=mc.player.getVehicle(); if(e==null || mc.currentScreen!=null) return; e.setYaw(mc.player.getYaw()); if(e instanceof LivingEntity living) living.setHeadYaw(mc.player.getHeadYaw()); e.setVelocity(horizontal(value("Speed"),e.getVelocity().y)); }
        });
        m.add(new ClientModule("Flight",Category.MOVEMENT,"Local directional flight with original glide and anti-kick motion; servers may correct it.") {
            { mode("Mode","Vanilla","Creative"); number("Speed",1,0,10,1); flag("Glide",false); flag("GlideWhileMoving",false); number("GlideSpeed",0,0,10,1); flag("Elytra",false); flag("AntiFallDmg",false); flag("AntiKick",true); }
        });
        m.add(new ClientModule("GlideModule",Category.MOVEMENT,"Limits descent to -0.125 and increases local air strafe while falling.") {
            public void tick() {
                if(!mc.options.jumpKey.isPressed() && !mc.player.isOnGround() && mc.player.fallDistance>0 && mc.player.getVelocity().y!=0 && !mc.player.isTouchingWater() && !mc.player.isInLava() && !mc.player.isClimbing() && !mc.player.verticalCollision) {
                    Vec3d v=mc.player.getVelocity(); mc.player.setVelocity(v.x,-0.125,v.z);
                }
            }
        });
        m.add(new ClientModule("HorseJump",Category.MOVEMENT,"Holds the mounted jump charge at full strength.") {
            
        });
        m.add(new ClientModule("Jesus",Category.MOVEMENT,"Local liquid surface collision and original periodic packet offset; server acceptance varies.") {
            { mode("Mode","NCP","VANILLA","BOUNCE"); number("Offset",0.18,0,0.9,0.01); }
        });
        NoFall noFall=m.add(new NoFall());
        ClientTickEvents.END_CLIENT_TICK.register(client -> noFall.restoreEquipment(false));
        m.add(new ClientModule("NoRotate",Category.MOVEMENT,"Accepts server position corrections while preserving your camera rotation."));
        m.add(new ClientModule("ParkourJump",Category.MOVEMENT,"Jumps when the projected movement reaches an unsupported block edge.") {
            public void tick() {
                if(mc.currentScreen!=null || !mc.player.isOnGround() || mc.player.isSneaking() || !moving()) return;
                Vec3d v=mc.player.getVelocity();
                if(mc.world.isSpaceEmpty(mc.player,mc.player.getBoundingBox().offset(v.x,-0.5,v.z).contract(0.001,0,0.001))) mc.player.jump();
            }
        });
        m.add(new ClientModule("Sneak",Category.MOVEMENT,"Original packet sneak, moving pulse and held-key modes.") {
            private boolean ownedKey;
            { mode("Mode","NCP","Vanilla","Always"); }
            public void tick() {
                int mode=setting("Mode").integer();
                if(ownedKey && mode!=2) { restoreSneakKey(); ownedKey=false; }
                if(mode==2) { if(mc.currentScreen==null) { mc.options.sneakKey.setPressed(true); ownedKey=true; } else if(ownedKey) {restoreSneakKey(); ownedKey=false;} }
                else if(!mc.player.isSneaking()) { sneak(true); if(mode==0 && moving()) sneak(false); }
            }
            protected void onDisable() { if(ownedKey) restoreSneakKey(); ownedKey=false; if(ready() && !mc.player.isSneaking()) sneak(false); }
        });
        desync=m.add(new EntityDesync()); ghost=m.add(new Ghost());
        m.add(new ClientModule("LiquidInteract",Category.EXPLOIT,"Includes liquids in the block crosshair raycast for placement."));
        m.add(new ClientModule("MountBypass",Category.EXPLOIT,"Skips the interact-at phase for chest horses, preserving ordinary interact."));
        m.add(new ClientModule("NoInteract",Category.EXPLOIT,"Temporarily sneaks around block use to bypass block interaction.") {{ flag("ContainerOnly",false); }});
        m.add(new ClientModule("NoMiningTrace",Category.EXPLOIT,"Ignores entities during the block mining crosshair raycast.") {{ flag("Pickaxe",true); }});
        m.add(new ClientModule("PacketCanceller",Category.EXPLOIT,"Drops selected outbound play packets. Can prevent ordinary movement or interaction.") {
            { for(String n:new String[]{"Input","Position","PositionRotation","Rotation","PlayerAbilities","PlayerDigging","PlayerTryUseItem","PlayerTryUseItemOnBlock","EntityAction","UseEntity","VehicleMove"}) flag(n,true); }
        });
        m.add(new ClientModule("PacketFly",Category.EXPLOIT,"Original two-packet flight experiment; modern servers usually reject spoofed positions.") {
            private long nextDip;
            { flag("AntiKick",true); mode("Type","Preserve","Up","Down"); }
            protected void onEnable() { nextDip=System.nanoTime()+3_000_000_000L; }
            public void tick() {
                if(mc.currentScreen!=null || mc.player.isDead()) return;
                double y=mc.options.sneakKey.isPressed()?-0.0031:flag("AntiKick") && mc.player.age%4==0?-0.04:0;
                if(mc.options.jumpKey.isPressed()) { y=mc.player.age%20==0?-0.04:0.031; if(System.nanoTime()>=nextDip) { y=-0.08; nextDip=System.nanoTime()+3_000_000_000L; } }
                Vec3d v=horizontal(0.031,y); mc.player.setVelocity(v);
                Vec3d p=mc.player.getPos().add(v);
                sendOwned(new PlayerMoveC2SPacket.Full(p.x,p.y,p.z,mc.player.getYaw(),mc.player.getPitch(),mc.player.isOnGround()));
                int type=setting("Type").integer();
                Vec3d spoof=type==0?p.add(java.util.concurrent.ThreadLocalRandom.current().nextInt(100000),0,java.util.concurrent.ThreadLocalRandom.current().nextInt(100000)):p.add(0,type==1?1337:-1337,0);
                sendOwned(new PlayerMoveC2SPacket.PositionAndOnGround(spoof.x,spoof.y,spoof.z,mc.player.isOnGround()));
            }
        });
        m.add(new ClientModule("PortalGodMode",Category.EXPLOIT,"Cancels teleport acknowledgments. Does not guarantee invulnerability on modern servers."));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client) -> { blink.discard(); desync.abandon(); ghost.abandon(); });
    }
    private static void restoreSneakKey() {
        var key=InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey());
        mc.options.sneakKey.setPressed(key.getCategory()==InputUtil.Type.KEYSYM && key.getCode()>=0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(),key.getCode()));
    }
    public static boolean flightActive() {
        ClientModule f=module("Flight"); return ready() && f!=null && f.enabled() && !mc.player.isDead() && mc.currentScreen==null && (!f.flag("Elytra") || mc.player.isFallFlying());
    }
    public static void flightMotion() {
        var f=module("Flight"); double speed=f.value("Speed");
        double y=mc.options.jumpKey.isPressed() && (f.setting("Mode").integer()==0 || !mc.player.isFallFlying())?speed:mc.options.sneakKey.isPressed()?-speed:0;
        if(f.setting("Mode").integer()==1 && f.flag("Glide") && (!f.flag("GlideWhileMoving") || moving())) y-=f.value("GlideSpeed");
        if(f.flag("AntiKick") && mc.player.age%4==0) y-=0.04;
        mc.player.setVelocity(horizontal(speed,y));
    }
    public static boolean liquidCollision(BlockPos pos) {
        if(!ready() || !on("Jesus") || mc.player.isSneaking() || mc.player.fallDistance>=3 || mc.player.getVelocity().y>=0.1) return false;
        Entity e=mc.player.getVehicle(); if(e==null) e=mc.player;
        return e.fallDistance<3 && pos.getY()<e.getY()-module("Jesus").value("Offset");
    }
    public static boolean onLiquid(double offset) {
        if(!ready()) return false;
        Entity e=mc.player.getVehicle(); if(e==null) e=mc.player;
        Box b=e.getBoundingBox().offset(0,-offset,0).contract(0.001,0,0.001); int y=MathHelper.floor(b.minY); boolean liquid=false;
        for(int x=MathHelper.floor(b.minX);x<=MathHelper.floor(b.maxX);x++) for(int z=MathHelper.floor(b.minZ);z<=MathHelper.floor(b.maxZ);z++) {
            var state=mc.world.getBlockState(new BlockPos(x,y,z));
            if(!state.getFluidState().isEmpty()) liquid=true; else if(!state.isAir()) return false;
        }
        return liquid;
    }
    public static final class Blink extends ClientModule {
        private final MovementPacketBuffer<Packet<?>> packets=new MovementPacketBuffer<>(4096);
        private ClientPlayNetworkHandler handler; private ClientWorld world; private OtherClientPlayerEntity visual; private DonkeyEntity visualMount;
        private net.minecraft.client.network.ClientPlayerEntity ownerPlayer;
        Blink() { super("Blink",Category.MOVEMENT,"Buffers movement until disabled; automatically flushes at 4096 packets. Never replays into another world."); flag("Visualize",true); flag("EntityBlink",true); }
        protected void onEnable() {
            discard(); if(!ready()) return; handler=mc.getNetworkHandler(); world=mc.world;ownerPlayer=mc.player; packets.begin(handler);
            if(flag("Visualize")) {
                visual=new OtherClientPlayerEntity(world,mc.player.getGameProfile()); visual.copyPositionAndRotation(mc.player); visual.setHeadYaw(mc.player.getHeadYaw()); visual.getInventory().clone(mc.player.getInventory());
                int id=-1; while(world.getEntityById(id)!=null) id--; visual.setId(id); world.addEntity(visual);
                if(mc.player.getVehicle() instanceof DonkeyEntity donkey) {
                    visualMount=new DonkeyEntity(EntityType.DONKEY,world); visualMount.copyPositionAndRotation(donkey); visualMount.setHasChest(donkey.hasChest());
                    id--;while(world.getEntityById(id)!=null) id--;visualMount.setId(id);world.addEntity(visualMount);visual.startRiding(visualMount,true);
                }
            }
        }
        public boolean hold(Packet<?> packet) {
            if(handler==null && ready()) onEnable();
            if(handler!=mc.getNetworkHandler() || world!=mc.world || ownerPlayer!=mc.player) { discard(); setEnabled(false); return false; }
            if(packets.offer(handler,packet)) return true;
            setEnabled(false); return false;
        }
        protected void onDisable() {
            if(handler==mc.getNetworkHandler() && world==mc.world && ownerPlayer==mc.player && ready() && !mc.player.isDead()) for(var p:packets.release(handler)) sendOwned(p);
            discard();
        }
        public void tick() { if(world!=mc.world || handler!=mc.getNetworkHandler() || ownerPlayer!=mc.player || mc.player.isDead()) {discard();setEnabled(false);} }
        public void discard() {
            packets.clear();
            if(visual!=null) visual.stopRiding();
            if(visual!=null && world!=null && world.getEntityById(visual.getId())==visual) world.removeEntity(visual.getId(),Entity.RemovalReason.DISCARDED);
            if(visualMount!=null && world!=null && world.getEntityById(visualMount.getId())==visualMount) world.removeEntity(visualMount.getId(),Entity.RemovalReason.DISCARDED);
            visual=null;visualMount=null;handler=null;world=null;ownerPlayer=null;
        }
    }
    public static final class Ghost extends ClientModule {
        public boolean ghosted; private net.minecraft.client.network.ClientPlayerEntity player;
        Ghost() { super("Ghost",Category.EXPLOIT,"Local movement after death; server still considers you dead. Disable requests respawn."); }
        protected void onEnable() { abandon(); }
        public void tick() {
            if(player!=null && player!=mc.player) {abandon();setEnabled(false);return;}
            if(mc.player.getHealth()<=0) { player=mc.player; ghosted=true; mc.player.setHealth(20); mc.player.deathTime=0; if(mc.currentScreen instanceof DeathScreen) mc.setScreen(null); }
        }
        protected void onDisable() { if(ghosted && player==mc.player && mc.getNetworkHandler()!=null) mc.player.requestRespawn(); abandon(); }
        public void abandon() { ghosted=false;player=null; }
    }
    public static final class EntityDesync extends ClientModule {
        public Entity riding; private ClientWorld world; private net.minecraft.client.network.ClientPlayerEntity ownerPlayer;
        EntityDesync() {super("EntityDesync",Category.EXPLOIT,"Local dismount while sending vehicle motion; restores only the same still-valid vehicle.");}
        protected void onEnable() {
            abandon(); if(!ready() || !mc.player.hasVehicle()) {setEnabled(false);return;}
            riding=mc.player.getVehicle(); world=mc.world;ownerPlayer=mc.player; mc.player.dismountVehicle(); world.removeEntity(riding.getId(),Entity.RemovalReason.UNLOADED_TO_CHUNK);
        }
        public void tick() {
            if(world!=mc.world || ownerPlayer!=mc.player || mc.player.isDead()) {abandon();setEnabled(false);return;}
            if(riding!=null && !mc.player.hasVehicle()) { mc.player.setOnGround(true); riding.setPosition(mc.player.getPos()); mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(riding)); }
        }
        protected void onDisable() {
            if(riding!=null && world==mc.world && ownerPlayer==mc.player && ready() && !mc.player.isDead() && world.getEntityById(riding.getId())==null) {
                ((EntityRemovalAccess)riding).salhack$unsetRemoved(); world.addEntity(riding); if(!mc.player.hasVehicle()) mc.player.startRiding(riding,true);
            }
            abandon();
        }
        public void abandon() { riding=null;world=null;ownerPlayer=null; }
    }
    private static final class NoFall extends ClientModule {
        private int oldSlot=-1; private float oldPitch; private boolean bucket;
        private net.minecraft.client.network.ClientPlayerEntity bucketPlayer,equipmentPlayer;
        private int equipmentSlot=-1;
        private ItemStack previousChest=ItemStack.EMPTY;
        private long restoreAfter;
        NoFall() { super("NoFall",Category.MOVEMENT,"Original ground spoof, small position offset, bucket and equipped-elytra fall handling."); mode("Mode","Packet","None","Anti","Bucket","Elytra"); flag("NoVoid",true); }
        public void tick() {
            if(bucket) { if(bucketPlayer==mc.player) {mc.player.getInventory().selectedSlot=oldSlot; mc.player.setPitch(oldPitch);} bucket=false;bucketPlayer=null; }
            if(mc.player.isFallFlying() || mc.player.isDead()) return;
            int bottom=mc.world.getBottomY();
            if(flag("NoVoid") && mc.player.getY()<=bottom+5 && mc.world.raycast(new RaycastContext(mc.player.getPos(),new Vec3d(mc.player.getX(),bottom,mc.player.getZ()),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mc.player)).getType()==HitResult.Type.MISS) mc.player.setVelocity(Vec3d.ZERO);
            if(mc.player.fallDistance<3 || mc.currentScreen!=null) return;
            int mode=setting("Mode").integer();
            if(mode==4) {
                if(!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && equipmentSlot<0 && inventorySafe()) {
                    for(int i=0;i<36;i++) {
                        ItemStack stack=mc.player.getInventory().getStack(i);
                        if(!stack.isOf(Items.ELYTRA) || !ElytraItem.isUsable(stack)) continue;
                        equipmentPlayer=mc.player;equipmentSlot=i<9?i+36:i;previousChest=mc.player.getEquippedStack(EquipmentSlot.CHEST).copy();
                        swapEquipment(equipmentSlot); restoreAfter=System.nanoTime()+1_000_000_000L;break;
                    }
                }
                if(mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && mc.player.checkFallFlying()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            if(mode!=3 || mc.world.getDimension().ultrawarm()) return;
            Vec3d eye=mc.player.getEyePos();
            BlockHitResult hit=mc.world.raycast(new RaycastContext(eye,eye.add(0,-4.5,0),RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE,mc.player));
            if(hit.getType()!=HitResult.Type.BLOCK) return;
            for(int i=0;i<9;i++) if(mc.player.getInventory().getStack(i).isOf(Items.WATER_BUCKET)) {
                oldSlot=mc.player.getInventory().selectedSlot; oldPitch=mc.player.getPitch(); bucket=true;bucketPlayer=mc.player; mc.player.getInventory().selectedSlot=i; mc.player.setPitch(90);
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(),90,mc.player.isOnGround()));
                mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND); break;
            }
        }
        private boolean inventorySafe() {return ready() && !mc.player.isDead() && mc.currentScreen==null && mc.interactionManager!=null && mc.player.currentScreenHandler==mc.player.playerScreenHandler && mc.player.currentScreenHandler.getCursorStack().isEmpty();}
        private void swapEquipment(int slot) {
            int sync=mc.player.playerScreenHandler.syncId;
            mc.interactionManager.clickSlot(sync,slot,0,SlotActionType.PICKUP,mc.player);
            mc.interactionManager.clickSlot(sync,6,0,SlotActionType.PICKUP,mc.player);
            mc.interactionManager.clickSlot(sync,slot,0,SlotActionType.PICKUP,mc.player);
        }
        private void restoreEquipment(boolean force) {
            if(equipmentSlot<0) return;
            if(equipmentPlayer!=mc.player || !ready()) {equipmentSlot=-1;equipmentPlayer=null;previousChest=ItemStack.EMPTY;return;}
            if(!force && enabled() && System.nanoTime()<restoreAfter) return;
            if(!inventorySafe()) return;
            if(mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && ItemStack.areEqual(mc.player.playerScreenHandler.getSlot(equipmentSlot).getStack(),previousChest)) swapEquipment(equipmentSlot);
            equipmentSlot=-1;equipmentPlayer=null;previousChest=ItemStack.EMPTY;
        }
        protected void onDisable() {
            if(bucket && ready() && bucketPlayer==mc.player) {mc.player.getInventory().selectedSlot=oldSlot;mc.player.setPitch(oldPitch);}
            bucket=false;bucketPlayer=null;restoreEquipment(true);
        }
    }
}

