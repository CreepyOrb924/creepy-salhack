package me.ionar.salhack.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ProfileStoreTest {
    @TempDir Path dir;
    @Test void roundTripRetainsModuleSettingsHudAndWaypoints() throws Exception {
        var store = new ProfileStore(dir);
        var p = new Profile();
        var m = new Profile.ModuleState(); m.enabled = true; m.key = 82; m.settings.put("Range", 4.5);
        p.modules.put("AutoCrystal", m); p.hud.put("Coordinates", new Profile.Position(0.2, 0.7)); p.searchBlock="minecraft:ancient_debris";
        p.waypoints.add(new Profile.Waypoint("Home", "minecraft:overworld", 8, 70, -16));
        store.save("test", p);
        var result = store.load("test");
        assertTrue(result.modules.get("AutoCrystal").enabled);
        assertEquals(82, result.modules.get("AutoCrystal").key);
        assertEquals(4.5, result.modules.get("AutoCrystal").settings.get("Range"));
        assertEquals(0.7, result.hud.get("Coordinates").y);
        assertEquals("Home", result.waypoints.get(0).name);
        assertEquals("minecraft:ancient_debris",result.searchBlock);
    }
    @Test void corruptFileIsPreservedAndDoesNotPreventStartup() throws Exception {
        Files.writeString(dir.resolve("bad.json"), "{broken");
        var store = new ProfileStore(dir);
        assertNotNull(store.load("bad"));
        assertEquals("{broken", Files.readString(dir.resolve("bad.json")));
        assertTrue(Files.list(dir).anyMatch(p -> p.toString().endsWith(".broken")));
        assertThrows(IllegalStateException.class, () -> store.save("bad", new Profile()));
    }
    @Test void traversalAndEmptyNamesAreRejected() {
        var store = new ProfileStore(dir);
        for (String name : new String[]{"../outside", "", "x/y", "C:evil", "a..b"})
            assertThrows(IllegalArgumentException.class, () -> store.load(name));
    }
    @Test void serverKeysAreStableAndDoNotExposeAddress() {
        assertEquals(ProfileStore.serverKey("example.org:25565"), ProfileStore.serverKey("example.org:25565"));
        assertNotEquals(ProfileStore.serverKey("a"), ProfileStore.serverKey("b"));
        assertFalse(ProfileStore.serverKey("example.org").contains("example"));
    }
    @Test void nullMembersAndOutOfBoundsHudAreSanitized() throws Exception {
        Files.writeString(dir.resolve("nulls.json"), "{\"modules\":null,\"waypoints\":null,\"hud\":{\"Coordinates\":{\"x\":5,\"y\":-3}}}");
        var p = new ProfileStore(dir).load("nulls");
        assertNotNull(p.modules); assertNotNull(p.waypoints);
        assertEquals(1, p.hud.get("Coordinates").x); assertEquals(0, p.hud.get("Coordinates").y);
    }
    @Test void numericSettingsRejectNonFiniteAndClamp() {
        var s = new Setting("Range", 4, 1, 6, 0.5);
        s.set(99); assertEquals(6, s.get());
        s.set(Double.NaN); assertEquals(6, s.get());
        s.set(-5); assertEquals(1, s.get());
    }
    @Test void netherConversionPreservesNegativeCoordinates() {
        assertEquals(-10, Coordinates.convert(-80, true));
        assertEquals(-640, Coordinates.convert(-80, false));
    }
}
