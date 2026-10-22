package me.ionar.salhack.core;

import java.util.*;

public class ClientModule {
    public final String name, description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public int key = -1;
    public boolean configurationOnly;
    private boolean enabled;
    public Runnable onChange = () -> {};
    public ClientModule(String name, Category category, String description) { this.name=name; this.category=category; this.description=description; }
    public Setting number(String name, double value, double min, double max, double step) { var s = new Setting(name,value,min,max,step); settings.add(s); return s; }
    public Setting flag(String name, boolean value) { return number(name,value?1:0,0,1,1); }
    public Setting mode(String name,String... choices) { var s=Setting.mode(name,choices); settings.add(s); return s; }
    public Setting text(String name,String value) { var s=Setting.text(name,value); settings.add(s); return s; }
    public double value(String name) { return setting(name).get(); }
    public boolean flag(String name) { return value(name)>0.5; }
    public Setting setting(String name) { return settings.stream().filter(s -> s.name.equals(name)).findFirst().orElseThrow(); }
    public boolean enabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean value) {
        if(configurationOnly) return;
        if (enabled==value) return;
        enabled=value;
        if (enabled) onEnable(); else onDisable();
        onChange.run();
    }
    protected void onEnable() {}
    protected void onDisable() {}
    public void tick() {}
}
