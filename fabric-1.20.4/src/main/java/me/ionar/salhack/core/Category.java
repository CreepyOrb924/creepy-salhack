package me.ionar.salhack.core;
public enum Category {
    COMBAT("Combat"), EXPLOIT("Exploit"), MISC("Misc"), MOVEMENT("Movement"), RENDER("Render"), UI("UI"), WORLD("World"), SCHEMATICA("Schematica"), CHAT("Chat");
    public final String label;
    Category(String label) { this.label=label; }
}
