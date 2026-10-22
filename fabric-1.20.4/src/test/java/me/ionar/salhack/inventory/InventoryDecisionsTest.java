package me.ionar.salhack.inventory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static me.ionar.salhack.inventory.InventoryDecisions.Offhand.*;
class InventoryDecisionsTest {
    @Test void swordOverridesFollowOriginalPrecedenceButNeverOverrideLowHealth() {
        assertEquals(Strength,InventoryDecisions.desired(Totem,16,16,30,15,true,true,true,false,true,true,false));
        assertEquals(Gap,InventoryDecisions.desired(Crystal,20,16,0,15,false,true,true,true,true,true,true));
        assertEquals(Totem,InventoryDecisions.desired(Crystal,15.5,16,0,15,false,true,true,false,true,true,true));
    }
    @Test void crystalRequiresNearbyEnemyAndFallingElytraGateIsSeparate() {
        assertEquals(Totem,InventoryDecisions.desired(Crystal,20,16,0,15,false,true,false,false,false,false,false));
        assertEquals(Crystal,InventoryDecisions.desired(Crystal,20,16,100,15,true,false,false,false,false,false,true));
        assertEquals(Totem,InventoryDecisions.desired(Pearl,20,16,15,15,false,false,false,false,false,false,true));
    }
    @Test void slotPriorityNeverTouchesArmorOrCraftingOutputAndHotbarReallyComesFirst() {
        int[] normal=InventoryDecisions.candidateSlots(false),hotbar=InventoryDecisions.candidateSlots(true);
        assertEquals(1,normal[0]); assertEquals(44,hotbar[0]); assertEquals(45,hotbar[40]);
        assertEquals(41,java.util.Arrays.stream(normal).distinct().count());
        assertTrue(java.util.Arrays.stream(normal).noneMatch(i -> i==0 || i>=5 && i<=8));
    }
}
