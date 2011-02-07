package Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import GameElements.Map;

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
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-1.xml");
			f.write(m.toXml());
			f.close();
			
			m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//test-1.xml"));
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-2.xml");
			f.write(m.toXml());
			f.close();			
			
			m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//test-2.xml"));
			f = new FileWriter("D://eclipse-my-projects//machiavelli-gameboard//xml//test-3.xml");
			f.write(m.toXml());
			f.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}
