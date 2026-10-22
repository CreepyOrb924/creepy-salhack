package me.ionar.salhack.render.expansion;
import me.ionar.salhack.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.math.Vec3d;

/** Detached view: the real player is never teleported or made noclip. */
public final class FreeCamera extends ClientModule {
    public static FreeCamera instance;
    private OtherClientPlayerEntity camera;
    private ClientWorld world;
    private Entity previous; private net.minecraft.client.network.ClientPlayerEntity body;
    public FreeCamera(){super("Freecam",Category.RENDER,"Detached noclip camera with body input and interaction isolation.");number("Speed",1,0,10,0.1);mode("Mode","Normal","Camera");flag("Cancel Packets",true);instance=this;}
    public static boolean active(){return instance!=null&&instance.enabled()&&instance.camera!=null;}
    public static boolean normal(){return active()&&instance.value("Mode")==0;}
    protected void onEnable(){var mc=MinecraftClient.getInstance();if(mc.player==null||mc.world==null)return;body=mc.player;mc.player.input.pressingForward=false;mc.player.input.pressingBack=false;mc.player.input.pressingLeft=false;mc.player.input.pressingRight=false;mc.player.input.movementForward=0;mc.player.input.movementSideways=0;mc.player.input.jumping=false;mc.player.input.sneaking=false;world=mc.world;previous=mc.getCameraEntity();camera=new OtherClientPlayerEntity(world,mc.player.getGameProfile());camera.copyPositionAndRotation(mc.player);camera.noClip=true;camera.prevX=camera.getX();camera.prevY=camera.getY();camera.prevZ=camera.getZ();mc.setCameraEntity(camera);}
    protected void onDisable(){var mc=MinecraftClient.getInstance();if(mc.getCameraEntity()==camera)mc.setCameraEntity(previous!=null&&!previous.isRemoved()&&previous.getWorld()==mc.world&&(previous!=body||body==mc.player)?previous:mc.player);camera=null;world=null;previous=null;body=null;}
    public static void validate(){if(instance==null||!instance.enabled())return;var mc=MinecraftClient.getInstance();if(mc.world==null||mc.player==null||mc.player!=instance.body||mc.world!=instance.world||!mc.player.isAlive()||mc.getCameraEntity()!=instance.camera)instance.setEnabled(false);}
    public void tick(){if(!active())return;var mc=MinecraftClient.getInstance();camera.prevX=camera.getX();camera.prevY=camera.getY();camera.prevZ=camera.getZ();camera.prevYaw=camera.getYaw();camera.prevPitch=camera.getPitch();if(mc.currentScreen!=null)return;
        double forward=(mc.options.forwardKey.isPressed()?1:0)-(mc.options.backKey.isPressed()?1:0),side=(mc.options.leftKey.isPressed()?1:0)-(mc.options.rightKey.isPressed()?1:0),up=(mc.options.jumpKey.isPressed()?1:0)-(mc.options.sneakKey.isPressed()?1:0);
        Vec3d v=Vec3d.fromPolar(0,camera.getYaw()).multiply(forward).add(Vec3d.fromPolar(0,camera.getYaw()-90).multiply(side)).add(0,up,0);if(v.lengthSquared()>1)v=v.normalize();camera.setPosition(camera.getPos().add(v.multiply(value("Speed"))));
    }
    public static boolean look(Entity e,double x,double y){var mc=MinecraftClient.getInstance();if(!active()||e!=mc.player)return false;instance.camera.changeLookDirection(x,y);return true;}
    public static boolean blockPacket(Packet<?> p){return normal()&&instance.flag("Cancel Packets")&&(p instanceof PlayerMoveC2SPacket||p instanceof VehicleMoveC2SPacket||p instanceof PlayerInteractEntityC2SPacket||p instanceof PlayerInteractBlockC2SPacket||p instanceof PlayerInteractItemC2SPacket||p instanceof PlayerActionC2SPacket||p instanceof ClientCommandC2SPacket||p instanceof HandSwingC2SPacket);}
}


