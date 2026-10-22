package me.ionar.salhack.automation;
import java.util.Locale;
/** Pure outgoing formatting, applied before modern chat signing. */
public final class ChatTransforms {
 private ChatTransforms() {}
 public static String modify(String input,boolean suffix,int separator,boolean color,int colorMode,boolean fancy,int fancyMode,boolean wheelchair,boolean trademark) {
  if(input.isEmpty() || "/*-#$^;.&,!".indexOf(input.charAt(0))>=0) return input;
  String s=input,tail="creepy-salhack";
  if(fancy) {
   if(fancyMode==0) s=map(s.toLowerCase(Locale.ROOT),"abcdefghijklmnopqrstuvwxyz0123456789","ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏ⓪①②③④⑤⑥⑦⑧⑨");
   else if(fancyMode==1) { s=s.replace("l","w").replace("r","w").replace("R","W")+" UwU"; tail="uwu creepy-salhack"; }
   else { StringBuilder b=new StringBuilder(); for(char c:s.toCharArray()) b.append(c>=0x21 && c<=0x7e?(char)(c+0xfee0):c); s=b.toString(); }
  }
  s=(color?(colorMode==0?">":"'"):"")+(wheelchair?"♿":"")+s+(trademark?"™":"");
  if(suffix) s+=(separator==0?" ⏐ ":" » ")+map(tail,"abcdefghijklmnopqrstuvwxyz0123456789","ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ₀₁₂₃₄₅₆₇₈₉");
  int end=Math.min(256,s.length()); if(end>0 && Character.isHighSurrogate(s.charAt(end-1))) end--;
  return s.substring(0,end);
 }
 private static String map(String s,String from,String to) { StringBuilder b=new StringBuilder(); for(char c:s.toCharArray()) {int i=from.indexOf(c);b.append(i<0?c:to.charAt(i));} return b.toString(); }
}
