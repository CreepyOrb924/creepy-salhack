package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Entity.class)
public interface EntityRemovalAccess {
    @Invoker("unsetRemoved") void salhack$unsetRemoved();
}
