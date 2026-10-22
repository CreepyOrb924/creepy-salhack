package me.ionar.salhack.core;

import java.util.*;

public final class Profile {
    public int schema = 1;
    public String searchBlock = "minecraft:diamond_ore";
    public List<String> friends = new ArrayList<>();
    public Map<String, ModuleState> modules = new LinkedHashMap<>();
    public Map<String, Position> hud = new LinkedHashMap<>();
    public List<Waypoint> waypoints = new ArrayList<>();
    public static final class ModuleState {
        public boolean enabled;
        public int key = -1;
        public Map<String, Double> settings = new LinkedHashMap<>();
        public Map<String, String> textSettings = new LinkedHashMap<>();
    }
    public static final class Position {
        public double x, y;
        public Position(double x, double y) { this.x=x; this.y=y; }
    }
    public static final class Waypoint {
        public String name, dimension;
        public double x, y, z;
        public Waypoint(String name, String dimension, double x, double y, double z) {
            this.name=name; this.dimension=dimension; this.x=x; this.y=y; this.z=z;
        }
    }
    public void sanitize() {
        if(friends==null) friends=new ArrayList<>();
        friends=friends.stream().filter(n -> n!=null && n.matches("[A-Za-z0-9_]{1,16}")).map(n -> n.toLowerCase(Locale.ROOT)).distinct().limit(1000).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (modules == null) modules = new LinkedHashMap<>();
        modules.values().removeIf(Objects::isNull);
        modules.values().forEach(m -> { if (m.settings == null) m.settings = new LinkedHashMap<>(); if(m.textSettings==null) m.textSettings=new LinkedHashMap<>(); m.textSettings.values().removeIf(Objects::isNull); m.textSettings.replaceAll((k,v) -> v.substring(0,Math.min(2048,v.length()))); m.key = Math.max(-1, Math.min(348, m.key)); });
        if (hud == null) hud = new LinkedHashMap<>();
        hud.values().removeIf(Objects::isNull);
        hud.values().forEach(p -> { p.x=unit(p.x); p.y=unit(p.y); });
        if (waypoints == null) waypoints = new ArrayList<>();
        waypoints.removeIf(w -> w == null || w.name == null || w.dimension == null || !Double.isFinite(w.x) || !Double.isFinite(w.y) || !Double.isFinite(w.z));
        if (waypoints.size() > 1000) waypoints = new ArrayList<>(waypoints.subList(0,1000));
    }
    private static double unit(double v) { return Double.isFinite(v) ? Math.max(0,Math.min(1,v)) : 0; }
}
