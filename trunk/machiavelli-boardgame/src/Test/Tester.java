package Test;

import java.io.File;
import GameElements.Map;

import org.apache.log4j.BasicConfigurator;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BasicConfigurator.configure();		
		try {
			Map m = new Map(new File("D://eclipse-my-projects//machiavelli-boardgame//xml//base_map.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end test");
	}

}
