package Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import GameElements.Map;
import GameEngine.Engine;

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
			FileWriter f;
			
			m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//initial-scenario-1.xml"));
			
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}
