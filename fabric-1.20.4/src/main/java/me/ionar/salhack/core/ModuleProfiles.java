package me.ionar.salhack.core;
import java.util.*;

/** Applies each profile over immutable startup defaults, never over the previous profile. */
public final class ModuleProfiles {
    private final List<ClientModule> modules;
    private final Map<String,Profile.ModuleState> defaults=new LinkedHashMap<>();
    public ModuleProfiles(List<ClientModule> modules) {
        this.modules=List.copyOf(modules);
        for(var m:modules) {
            var state=new Profile.ModuleState(); state.enabled=m.enabled(); state.key=m.key;
            for(var s:m.settings) { state.settings.put(s.name,s.get()); if(s.isText()) state.textSettings.put(s.name,s.text()); } defaults.put(m.name,state);
        }
    }
    public void apply(Profile profile) {
        for(var m:modules) m.setEnabled(false);
        for(var m:modules) {
            var base=defaults.get(m.name); var state=profile.modules.get(m.name);
            m.key=state==null?base.key:state.key;
            for(var s:m.settings) {
                if(s.isText()) { s.setText(base.textSettings.get(s.name)); if(state!=null && state.textSettings!=null && state.textSettings.containsKey(s.name)) s.setText(state.textSettings.get(s.name)); continue; }
                s.set(base.settings.get(s.name));
                if(state!=null && state.settings!=null) { Double v=state.settings.get(s.name); if(v!=null) s.set(v); }
            }
        }
        for(var m:modules) { var state=profile.modules.get(m.name); m.setEnabled(state==null?defaults.get(m.name).enabled:state.enabled); }
    }
}
