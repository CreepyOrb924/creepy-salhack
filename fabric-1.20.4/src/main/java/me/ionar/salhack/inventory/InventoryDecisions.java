package me.ionar.salhack.inventory;

/** Original offhand priority, kept independent from mutable inventory operations. */
public final class InventoryDecisions {
    private InventoryDecisions() {}
    public enum Offhand { Totem,Gap,Crystal,Pearl,Chorus,Strength,Shield }
    public static Offhand desired(Offhand selected,double health,double threshold,double fallDistance,double fallThreshold,
            boolean flying,boolean totemOnElytra,boolean sword,boolean strength,boolean swordGap,boolean strGap,boolean nearbyEnemy) {
        if(health>=threshold && sword && strGap && !strength) return Offhand.Strength;
        if(health>=threshold && sword && swordGap) return Offhand.Gap;
        if(health<threshold || selected==Offhand.Totem || flying && totemOnElytra || !flying && fallDistance>=fallThreshold || selected==Offhand.Crystal && !nearbyEnemy) return Offhand.Totem;
        return selected;
    }
    /** Original screen order includes crafting inputs and offhand; output and armor are excluded. */
    public static int[] candidateSlots(boolean hotbarFirst) {
        int[] slots=new int[41];int n=0;
        if(hotbarFirst) { for(int i=44;i>=9;i--) slots[n++]=i;for(int i=4;i>=1;i--) slots[n++]=i; }
        else { for(int i=1;i<=4;i++) slots[n++]=i;for(int i=9;i<=44;i++) slots[n++]=i; }
        slots[n]=45;return slots;
    }
}
