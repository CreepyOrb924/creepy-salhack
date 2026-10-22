package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(PlayerMoveC2SPacket.class)
public interface MovementPacketAccess {
    @Mutable @Accessor("onGround") void salhack$onGround(boolean value);
    @Mutable @Accessor("y") void salhack$y(double value);
}
