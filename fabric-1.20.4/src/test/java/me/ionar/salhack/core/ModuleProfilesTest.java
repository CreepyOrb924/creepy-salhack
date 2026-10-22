package me.ionar.salhack.core;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ModuleProfilesTest {
    @Test void sparseProfileResetsEnabledKeysAndSettingsToDefaults() {
        var m=new ClientModule("Sprint",Category.MOVEMENT,"test"); m.number("Speed",0.3,0.1,1,0.1);
        var profiles=new ModuleProfiles(List.of(m));
        m.setEnabled(true); m.key=65; m.setting("Speed").set(0.9);
        profiles.apply(new Profile());
        assertFalse(m.enabled()); assertEquals(-1,m.key); assertEquals(0.3,m.value("Speed"));
    }
    @Test void missingSettingUsesDefaultWhilePresentOverridesApply() {
        var m=new ClientModule("HUD",Category.UI,"test"); m.flag("Shadow",true); m.number("Scale",1,0.5,2,0.1); m.setEnabled(true);
        var profiles=new ModuleProfiles(List.of(m));
        m.setting("Shadow").set(0); m.setting("Scale").set(2);
        var p=new Profile(); var state=new Profile.ModuleState(); state.settings.put("Scale",1.5); p.modules.put("HUD",state);
        profiles.apply(p);
        assertTrue(m.flag("Shadow")); assertEquals(1.5,m.value("Scale")); assertFalse(m.enabled());
    }
}
