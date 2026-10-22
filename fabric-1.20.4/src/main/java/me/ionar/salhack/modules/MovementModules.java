package me.ionar.salhack.modules;

import me.ionar.salhack.*;
import me.ionar.salhack.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;

public final class MovementModules {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static void register(ModuleManager manager) {
        manager.add(new ClientModule("Sprint",Category.MOVEMENT,"Automatically sprints while moving forward and able to sprint.") {
            public void tick() { if(mc.currentScreen==null && mc.player.input.movementForward>0 && !mc.player.isSneaking() && mc.player.getHungerManager().getFoodLevel()>6) mc.player.setSprinting(true); }
            protected void onDisable() { if(mc.player!=null) mc.player.setSprinting(false); }
        });
        manager.add(new ClientModule("AutoWalk",Category.MOVEMENT,"Holds forward until disabled. Pauses in menus and releases on disconnect.") {
            public void tick() { mc.options.forwardKey.setPressed(mc.currentScreen==null); }
            protected void onDisable() { mc.options.forwardKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(),InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()).getCode())); }
        });
        manager.add(new ClientModule("Speed",Category.MOVEMENT,"Configurable ground strafe speed; servers may reject movement.") {
            { number("Speed",0.32,0.1,0.8,0.02); }
            public void tick() { if(mc.currentScreen==null && mc.player.isOnGround() && !mc.player.isSneaking() && !mc.player.isTouchingWater()) move(value("Speed"),false); }
        });
        manager.add(new ClientModule("ElytraFly",Category.MOVEMENT,"Controls velocity while already gliding; jump/sneak adjusts altitude. No packet bypass.") {
            { number("Speed",0.8,0.1,2,0.1); number("Vertical",0.3,0.05,1,0.05); }
            public void tick() {
                if(mc.currentScreen!=null || !mc.player.isFallFlying()) return;
                move(value("Speed"),true);
                var v=mc.player.getVelocity();
                mc.player.setVelocity(v.x,mc.options.jumpKey.isPressed()?value("Vertical"):mc.options.sneakKey.isPressed()?-value("Vertical"):-0.02,v.z);
            }
        });
        manager.add(new ClientModule("Step",Category.MOVEMENT,"Raises local step height; ordinary movement only, no packet stepping.") {{ number("Height",1,0.6,2,0.2); }});
        manager.add(new ClientModule("SafeWalk",Category.MOVEMENT,"Applies vanilla edge clipping on the ground without forcing sneak."));
        manager.add(new ClientModule("NoSlow",Category.MOVEMENT,"Removes the local movement multiplier when using an item; server correction remains possible."));
        manager.add(new ClientModule("Yaw",Category.MOVEMENT,"Locks facing direction to the nearest configured angle; useful on highways.") {
            { number("Increment",45,15,90,15); }
            public void tick() { if(mc.currentScreen==null) mc.player.setYaw((float)(Math.round(mc.player.getYaw()/value("Increment"))*value("Increment"))); }
        });
        manager.add(new ClientModule("AutoRespawn",Category.MISC,"Requests ordinary respawn after death.") {
            private int ticks;
            public void tick() { if(mc.player.isDead()) { if(++ticks>=20) { mc.player.requestRespawn(); ticks=0; } } else ticks=0; }
        });
    }
    private static void move(double speed, boolean stop) {
        double f=mc.player.input.movementForward,s=mc.player.input.movementSideways;
        double length=Math.hypot(f,s);
        if(length<0.01) { if(stop) mc.player.setVelocity(0,mc.player.getVelocity().y,0); return; }
        double yaw=Math.toRadians(mc.player.getYaw()); f/=length; s/=length;
        mc.player.setVelocity((-Math.sin(yaw)*f+Math.cos(yaw)*s)*speed,mc.player.getVelocity().y,(Math.cos(yaw)*f+Math.sin(yaw)*s)*speed);
    }
}
