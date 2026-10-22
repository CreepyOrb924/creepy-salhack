package me.ionar.salhack.movement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MovementMathTest {
    @Test void diagonalHasSameSpeedAndYawRotates() {
        double[] v=MovementMath.direction(1,1,0,2);
        assertEquals(2,Math.hypot(v[0],v[1]),1e-12);
        v=MovementMath.direction(1,0,90,1);
        assertEquals(-1,v[0],1e-12); assertEquals(0,v[1],1e-12);
        assertArrayEquals(new double[]{0,0},MovementMath.direction(0,0,20,4));
    }
    @Test void teleportOffsetsHandleNegativeWorldCoordinates() {
        assertEquals(-95,MovementMath.relative(5,-100,true));
        assertEquals(5,MovementMath.relative(5,-100,false));
    }
    @Test void bufferPreservesOrderCapsAndNeverReplaysAcrossSessions() {
        var q=new MovementPacketBuffer<Integer>(2); Object first=new Object(),second=new Object();
        q.begin(first); assertTrue(q.offer(first,1)); assertTrue(q.offer(first,2)); assertFalse(q.offer(first,3));
        assertEquals(java.util.List.of(1,2),q.release(first)); assertEquals(0,q.size());
        q.begin(first); q.offer(first,4); assertEquals(java.util.List.of(),q.release(second));
        q.begin(second); assertFalse(q.offer(first,5)); q.clear(); assertEquals(0,q.size());
    }
}
