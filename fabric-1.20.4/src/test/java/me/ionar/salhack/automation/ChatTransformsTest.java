package me.ionar.salhack.automation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChatTransformsTest {
 @Test void commandPrefixesRemainUntouched() { for(char c : "/*-#$^;.&,!".toCharArray()) assertEquals(c+"test", ChatTransforms.modify(c+"test",true,1,true,0,true,0,true,true)); }
 @Test void decorationsCompose() { assertEquals(">♿hello™ » ᴄʀᴇᴇᴘʏ-ѕᴀʟʜᴀᴄᴋ", ChatTransforms.modify("hello",true,1,true,0,false,0,true,true)); }
 @Test void fancyWorksWithoutOtherFlags() { assertEquals("Ａ",ChatTransforms.modify("A",false,0,false,0,true,2,false,false)); }
 @Test void limitDoesNotSplitSurrogates() { String s=ChatTransforms.modify("x".repeat(255)+"😀",false,0,false,0,false,0,false,false); assertEquals(255,s.length()); }
}
