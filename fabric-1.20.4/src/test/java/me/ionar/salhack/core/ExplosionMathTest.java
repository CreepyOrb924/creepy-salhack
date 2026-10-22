package me.ionar.salhack.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExplosionMathTest {
    @Test void crystalDamageUsesExposureAndTwelveBlockRadius() {
        assertEquals(85,ExplosionMath.raw(0,1),0.0001); assertEquals(1,ExplosionMath.raw(0,0),0.0001); assertEquals(0,ExplosionMath.raw(12.1,1),0.0001);
        assertTrue(ExplosionMath.raw(4,1)>ExplosionMath.raw(8,1)); assertTrue(ExplosionMath.raw(4,0.3)<ExplosionMath.raw(4,1));
    }
    @Test void difficultyAndResistanceScaleWithoutNegativeDamage() {
        assertEquals(0,ExplosionMath.difficulty(20,0)); assertEquals(11,ExplosionMath.difficulty(20,1)); assertEquals(30,ExplosionMath.difficulty(20,3));
        assertEquals(12,ExplosionMath.resistance(20,1)); assertEquals(0,ExplosionMath.resistance(20,9));
    }
}
