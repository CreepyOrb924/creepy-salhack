package me.ionar.salhack.core;
public final class ExplosionMath {
    private ExplosionMath() {}
    public static float raw(double distance,double exposure) {
        if(!Double.isFinite(distance) || !Double.isFinite(exposure) || distance>12) return 0;
        double impact=(1-Math.max(0,distance)/12)*Math.max(0,Math.min(1,exposure)); return (float)(int)((impact*impact+impact)*42+1);
    }
    public static float difficulty(float damage,int difficulty) { return switch(difficulty) { case 0 -> 0; case 1 -> Math.min(damage/2+1,damage); case 3 -> damage*1.5f; default -> damage; }; }
    public static float resistance(float damage,int amplifier) { return Math.max(0,damage*(1-0.2f*(amplifier+1))); }
}
