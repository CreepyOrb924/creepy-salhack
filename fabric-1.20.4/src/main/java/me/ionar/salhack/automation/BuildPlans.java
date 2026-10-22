package me.ionar.salhack.automation;
import java.util.*;
/** Cells are right/up/forward relative to a cardinal facing. */
public final class BuildPlans {
 public record Cell(int x,int y,int z){}
 public static List<Cell> portal(){List<Cell> p=new ArrayList<>();for(int x=-1;x<=2;x++)p.add(new Cell(x,0,2));for(int y=1;y<=4;y++){p.add(new Cell(-1,y,2));p.add(new Cell(2,y,2));}p.add(new Cell(0,4,2));p.add(new Cell(1,4,2));return p;}
 public static List<Cell> highway(int width){List<Cell> p=new ArrayList<>();for(int z=0;z<3;z++)for(int x=-(width/2);x<width-width/2;x++)p.add(new Cell(x,-1,z));return p;}
 public static List<Cell> wither(){return List.of(new Cell(0,0,0),new Cell(0,1,0),new Cell(-1,1,0),new Cell(1,1,0),new Cell(-1,2,0),new Cell(0,2,0),new Cell(1,2,0));}
}
