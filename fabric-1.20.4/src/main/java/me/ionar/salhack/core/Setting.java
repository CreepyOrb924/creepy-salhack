package me.ionar.salhack.core;

public final class Setting {
    public final String name;
    public final double min, max, step;
    private double value;
    private String[] options;
    private String text;
    public Setting(String name, double value, double min, double max, double step) {
        this.name = name; this.min = min; this.max = max; this.step = step; set(value);
    }
    public double get() { return value; }
    public static Setting mode(String name,String... options) {
        if(options.length==0) throw new IllegalArgumentException("Mode needs choices");
        var s=new Setting(name,0,0,options.length-1,1); s.options=options.clone(); return s;
    }
    public static Setting text(String name,String value) { var s=new Setting(name,0,0,0,0); s.text=""; s.setText(value); return s; }
    public boolean isText() { return text!=null; }
    public boolean isMode() { return options!=null; }
    public String text() { return text!=null?text:options!=null?options[Math.max(0,Math.min(options.length-1,integer()))]:display(); }
    public void setText(String next) {
        if(next==null) return;
        if(text!=null) text=next.substring(0,Math.min(2048,next.length())).replace("\u0000","");
        else if(options!=null) for(int i=0;i<options.length;i++) if(options[i].equalsIgnoreCase(next)) { value=i; return; }
    }
    public int integer() { return (int)Math.round(value); }
    public boolean bool() { return value > 0.5; }
    public void set(double v) { if (Double.isFinite(v)) value = Math.max(min, Math.min(max, v)); }
    public void cycle(boolean back) {
        double next = value + (back ? -step : step);
        set(next > max + 0.0001 ? min : next < min - 0.0001 ? max : next);
    }
    public String display() {
        if(text!=null || options!=null) return text();
        return min == 0 && max == 1 && step == 1 ? (bool() ? "On" : "Off") : String.format(java.util.Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
