package me.ionar.salhack.automation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BuildPlansTest {
 @Test void portalHasFrameAndEmptyInterior(){var p=BuildPlans.portal();assertEquals(14,p.size());assertTrue(p.contains(new BuildPlans.Cell(-1,2,2)));assertFalse(p.contains(new BuildPlans.Cell(0,2,2)));}
 @Test void highwayWidthHasNoDuplicateCells(){for(int width:new int[]{3,4,5,7}){var p=BuildPlans.highway(width);assertEquals(width*3,p.size());assertEquals(p.size(),new java.util.HashSet<>(p).size());}}
 @Test void witherBuildsSoulSandBeforeSkulls(){var p=BuildPlans.wither();assertEquals(7,p.size());assertEquals(4,p.stream().filter(c->c.y()<2).count());assertEquals(3,p.stream().filter(c->c.y()==2).count());}
}
