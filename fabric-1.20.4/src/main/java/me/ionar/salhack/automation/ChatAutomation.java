package me.ionar.salhack.automation;
import me.ionar.salhack.ModuleManager;
import me.ionar.salhack.core.*;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public final class ChatAutomation {
 private static ModuleManager manager;
 public static void register(ModuleManager m){manager=m;
  m.add(new ClientModule("MessageModifier",Category.CHAT,"Formats outgoing chat before it is signed; command prefixes are preserved."){{flag("Suffix",false);mode("Separator","Classic","Arrow");mode("SuffixFont","SuperScript");flag("ColorChat",false);mode("ColorChatMode","Green","Blue");flag("FancyChat",false);mode("FancyChatMode","Circle","UwU","Classic");flag("Wheelchair",false);flag("TradeMark",false);}});
  m.add(new ClientModule("ChatModifications",Category.CHAT,"Local chat timestamps, filters, name highlighting and bounded history length."){{mode("TimeMode","NA","EU");flag("AntiEZ",true);flag("NoDiscord",true);flag("NameHighlight",true);number("ChatLength",100,0,100000,100);}});
  ClientSendMessageEvents.MODIFY_CHAT.register(message->{var s=m.get("MessageModifier");return !s.enabled()?message:ChatTransforms.modify(message,s.flag("Suffix"),(int)s.value("Separator"),s.flag("ColorChat"),(int)s.value("ColorChatMode"),s.flag("FancyChat"),(int)s.value("FancyChatMode"),s.flag("Wheelchair"),s.flag("TradeMark"));});
 }
 public static boolean filtered(Text original){if(manager==null||!manager.on("ChatModifications"))return false;var s=manager.get("ChatModifications");String lower=original.getString().toLowerCase(Locale.ROOT);int marker=lower.indexOf("> ");if(marker<0)return false;String body=lower.substring(marker);return s.flag("AntiEZ")&&body.contains("ez")||s.flag("NoDiscord")&&body.contains("discord");}
 public static Text incoming(Text original){if(manager==null||!manager.on("ChatModifications"))return original;if(filtered(original))return null;var s=manager.get("ChatModifications");String plain=original.getString(),lower=plain.toLowerCase(Locale.ROOT);
  MutableText body=original.copy();var mc=MinecraftClient.getInstance();if(s.flag("NameHighlight")&&mc.player!=null){String name=mc.player.getName().getString();if(lower.contains(name.toLowerCase(Locale.ROOT))){body=Text.empty();int from=0,index;while((index=lower.indexOf(name.toLowerCase(Locale.ROOT),from))>=0){body.append(Text.literal(plain.substring(from,index)));body.append(Text.literal(plain.substring(index,index+name.length())).formatted(Formatting.GOLD));from=index+name.length();}body.append(Text.literal(plain.substring(from)));}}
  String time=LocalTime.now().format(DateTimeFormatter.ofPattern(s.value("TimeMode")==0?"h:mm a":"HH:mm"));return Text.literal("["+time+"] ").formatted(Formatting.GRAY).append(body);
 }
 public static int historyLimit(){return manager!=null&&manager.on("ChatModifications")?(int)manager.get("ChatModifications").value("ChatLength"):100;}
}
