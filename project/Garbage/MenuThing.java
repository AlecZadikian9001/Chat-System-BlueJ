package Garbage;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import javax.swing.*;
/**
 * Write a description of class MenuThing here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MenuThing extends JFrame
{
    public MenuThing(){
        super();
        
        if (System.getProperty("os.name").contains("Mac")) {
		System.out.println("The game knows that it is running on Mac OS.");
			System.setProperty("apple.laf.useScreenMenuBar", "true"); //make JMenuBars appear at the top in Mac OS X
		}
		else if (System.getProperty("os.name").contains("Windows")){
		System.out.println("The game knows that it is running on Windows.");
		}
		else {
		 System.out.println("The game does not know what system it is running on. Unless you are running Linux or something obscure (in which case this is fine), this is a minor problem.");
		}
        
        JMenuBar menus = new JMenuBar();
        JMenu sound = new JMenu("Sound");
		JMenuItem toggleMusic = new JMenuItem("Toggle music");
		JMenuItem toggleSound = new JMenuItem("Toggle sound effects");
		JMenuItem changeMusic = new JMenuItem("Change music...");
		sound.add(toggleMusic); sound.add(toggleSound); sound.add(changeMusic);
		menus.add(sound);
		menus.validate();
		this.setJMenuBar(menus);
		this.validate();
		this.setVisible(true);
    }
}
