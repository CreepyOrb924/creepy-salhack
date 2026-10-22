package me.ionar.salhack.render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
public final class OverlayGeometry {
    private OverlayGeometry() {}
    public static Box entityBox(Entity entity,float delta,Vec3d camera){
        Vec3d position=entity.getLerpedPos(delta);
        return entity.getBoundingBox().offset(position.subtract(entity.getPos())).offset(-camera.x,-camera.y,-camera.z);
    }
    /** Invert the actual view transform, including view bob, so the origin stays on the crosshair. */
    public static Vec3d tracerOrigin(Matrix4f view){
        Vector3f point=new Matrix4f(view).invert().transformPosition(new Vector3f(0,0,-.15f));
        return new Vec3d(point.x,point.y,point.z);
    }
}
