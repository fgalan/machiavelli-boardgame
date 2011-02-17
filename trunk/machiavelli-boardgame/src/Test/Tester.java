package Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import GameElements.Map;
import GameEngine.Adjustments;
import GameEngine.Commands;
import GameEngine.Engine;
import GameEngine.GameStatus;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Tester {

	private final static Logger log = Logger.getLogger("MAIN");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BasicConfigurator.configure();		
		try {
			Map m;
			GameStatus gs;
			FileWriter f;
			
			m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//initial-scenario-1.xml"));
			gs = new GameStatus(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//game_status.xml"));
			
			/* Just to get provinces in alphabetical order, so we can compare maps XML easily */
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-0.xml");
			f.write(m.toXml());
			f.close();
			
			System.out.println("---- Doing famine -----");
			System.out.print(Engine.doFamine(m));
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-1.xml");
			f.write(m.toXml());
			System.out.println(m.toString());
			f.close();
			
			System.out.println("---- Clearing famine -----");			
			System.out.print(Engine.clearFamine(m));
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-3.xml");
			f.write(m.toXml());
			System.out.println(m.toString());
			f.close();
			
			System.out.println("---- Doing plague -----");			
			System.out.print(Engine.doPlague(m));
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-4.xml");
			f.write(m.toXml());
			System.out.println(m.toString());
			f.close();
						
			System.out.println(m.toString());
			
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//gs-0.xml");
			f.write(gs.toXml());
			f.close();
			
			System.out.println(gs.getStatus("Austria"));
			System.out.println(gs.getStatus("France"));
			System.out.println(gs.getStatus("Papacy"));
			System.out.println(gs.getStatus("Florence"));
			System.out.println(gs.getStatus("Venice"));
			
			Commands cms = new Commands(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//Commands-example.xml"));
			Adjustments adj = new Adjustments(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//Adjustments-example.xml"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}