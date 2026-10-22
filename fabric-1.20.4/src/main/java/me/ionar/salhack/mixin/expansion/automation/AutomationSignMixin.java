package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractSignEditScreen.class)
public abstract class AutomationSignMixin {
 @Shadow private int currentRow;
 @Invoker("setCurrentRowMessage") protected abstract void salhack$setLine(String message);
 @Unique private int salhack$age;
 @Inject(method="tick",at=@At("TAIL")) private void fill(CallbackInfo ci){if(!AutomationHooks.on("AutoSign"))return;var s=AutomationHooks.manager.get("AutoSign");if(salhack$age++==0){int row=currentRow;for(int i=0;i<4;i++){currentRow=i;String line=s.setting("Line"+(i+1)).text().replace("~currentdate~",java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));var p=MinecraftClient.getInstance().player;if(p!=null)line=line.replace("~playername~",p.getName().getString());salhack$setLine(line);}currentRow=row;}if(salhack$age>=60&&s.flag("AutoComplete"))((AbstractSignEditScreen)(Object)this).close();}
}
