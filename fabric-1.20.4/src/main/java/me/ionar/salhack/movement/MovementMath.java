package me.ionar.salhack.movement;

/** Normalized directional movement shared by player and mounted movement. */
public final class MovementMath {
    private MovementMath() {}
    public static double[] direction(double forward, double strafe, double yaw, double speed) {
        double length=Math.hypot(forward,strafe);
        if(length<1.0e-6) return new double[]{0,0};
        double angle=Math.toRadians(yaw);
        return new double[]{(-Math.sin(angle)*forward+Math.cos(angle)*strafe)*speed/length,
            (Math.cos(angle)*forward+Math.sin(angle)*strafe)*speed/length};
    }
    public static double relative(double received, double current, boolean relative) { return received+(relative?current:0); }
}
