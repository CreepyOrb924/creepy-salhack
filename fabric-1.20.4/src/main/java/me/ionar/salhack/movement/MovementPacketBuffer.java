package me.ionar.salhack.movement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** A bounded, connection-scoped FIFO. Never replay across connections or worlds. */
public final class MovementPacketBuffer<T> {
    private final int capacity;
    private final ArrayDeque<T> queue=new ArrayDeque<>();
    private Object owner;
    public MovementPacketBuffer(int capacity) { if(capacity<1) throw new IllegalArgumentException("capacity"); this.capacity=capacity; }
    public void begin(Object owner) { queue.clear(); this.owner=owner; }
    public boolean offer(Object owner,T packet) {
        if(this.owner!=owner || queue.size()>=capacity) return false;
        queue.addLast(packet); return true;
    }
    public List<T> release(Object owner) {
        List<T> packets=this.owner==owner?new ArrayList<>(queue):List.of();
        clear(); return packets;
    }
    public void clear() { queue.clear(); owner=null; }
    public int size() { return queue.size(); }
}
