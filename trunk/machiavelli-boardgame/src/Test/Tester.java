package Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import GameElements.Map;
import GameEngine.Engine;
import GameEngine.GameStatus;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gnu.glade.GladeXMLException;
import org.gnu.glade.LibGlade;
import org.gnu.gtk.Gtk;

public class Tester {

	private final static Logger log = Logger.getLogger("MAIN");
	
	private LibGlade glade;
	
	public Tester() throws GladeXMLException, FileNotFoundException, IOException {
		glade = new LibGlade("data/helloworld.glade", this);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BasicConfigurator.configure();		
		try {
			
			Gtk.init(args);
			Tester t = new Tester();
			Gtk.main();
			
			Map m;
			GameStatus gs;
			FileWriter f;
			
			m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//The-Balance-Of-Power-SCENARIO.xml"));
			gs = new GameStatus(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//The-Balance-Of-Power-GAMESTATUS.xml"));
			
			/* Just to get provinces in alphabetical order, so we can compare maps XML easily */
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-0.xml");
			f.write(m.toXml());
			f.close();
									
			System.out.println(m.toString());
			
			/* Process the first turn */
			System.out.println(Engine.processCommands(gs, m, null));
			
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml/gs-new.xml");
			f.write(gs.toXml());
			f.close();
			
			/* Just to get provinces in alphabetical order, so we can compare maps XML easily */
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//map-new.xml");
			f.write(m.toXml());
			f.close();			
			
			/*System.out.println(gs.getStatus("Austria"));
			System.out.println(gs.getStatus("Florence"));			
			System.out.println(gs.getStatus("France"));
			System.out.println(gs.getStatus("Milan"));
			System.out.println(gs.getStatus("Naples"));
			System.out.println(gs.getStatus("Papacy"));
			System.out.println(gs.getStatus("Turks"));
			System.out.println(gs.getStatus("Venice"));*/
			
			//Commands cms = new Commands(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//Commands-example.xml"));
			//Adjustments adj = new Adjustments(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//Adjustments-example.xml"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}
