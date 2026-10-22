package me.ionar.salhack.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class TypedSettingsTest {
    @TempDir Path dir;
    @Test void modesCycleNamedValuesAndTextPreservesSpacesAcrossProfiles() throws Exception {
        var m=new ClientModule("Test",Category.UI,"test"); var mode=m.mode("Mode","Normal","Camera"); var text=m.text("Message","hello world");
        assertEquals("Normal",mode.display()); mode.cycle(false); assertEquals("Camera",mode.text()); mode.cycle(false); assertEquals("Normal",mode.text());
        var defaults=new ModuleProfiles(List.of(m)); text.setText("A saved message with spaces"); mode.setText("Camera");
        var p=new Profile(); var state=new Profile.ModuleState(); state.settings.put("Mode",mode.get()); state.textSettings.put("Message",text.text()); p.modules.put("Test",state);
        var store=new ProfileStore(dir); store.save("typed",p); defaults.apply(store.load("typed"));
        assertEquals("Camera",mode.text()); assertEquals("A saved message with spaces",text.text());
        defaults.apply(new Profile()); assertEquals("hello world",text.text()); assertEquals("Normal",mode.text());
    }
    @Test void textLengthAndUnknownModesCannotPoisonNumericState() {
        var m=new ClientModule("T",Category.UI,""); var text=m.text("Text",""); text.setText("x".repeat(5000)); assertTrue(text.text().length()<=2048);
        var mode=m.mode("Mode","One","Two"); mode.setText("unknown"); assertEquals("One",mode.text()); mode.set(Double.NaN); assertEquals("One",mode.text());
    }
}
