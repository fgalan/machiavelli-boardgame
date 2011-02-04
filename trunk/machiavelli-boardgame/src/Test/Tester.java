package Test;

import java.io.File;
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
			Map m = new Map(new File("D://eclipse-my-projects//machiavelli-gameboard//xml//base_map.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}
