package me.ionar.salhack.core;
public final class Coordinates {
    private Coordinates() {}
    public static double convert(double value, boolean toNether) { return toNether ? value / 8 : value * 8; }
}
