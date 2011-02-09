/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * An online copy of the licence can be found at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (C) 2011 Fermin Galan Marquez
 *
 */

package GameEngine;

import java.util.Random;

import Exceptions.UnknownCountryException;

public class RandomProcesses {

	
	private static Random generator = new Random(System.currentTimeMillis());
	
	private static final String famineTable[][] = { 
		// Row 1 (actually it is not used)
		{"","","","","","","","","","","",""},
		// Row 2
		{"","","","Provence","Patrimony","Modena","","Corsica","Ancona","","",""},
		// Row 3
		{"","","Piomino","","","","","","Tunis","","","Palermo"},
		// Row 4
		{"","Tivoli","","Otranto","Padua","Swiss","Cremona","Pontremoli","","Herzegovina","",""},
		// Row 5
		{"","Friuli","","Bologna","Salerno","Verona","Austria","Milan","Sienna","","","Durazzo"},
		// Row 6
		{"","Marseille","Ragusa","Vicenza","Carinthia","Bergamo","Pisotia","Spoleto","","Piacenzia","Hungray",""},
		// Row 7
		{"","","Bari","Slavonia","Montferrat","Urbino","Fornova","","Como","Trent","",""},
		// Row 8
		{"","Ferrara","","Rome","Pavia","","","Arezzo","Brescia","Sluzzo","Albania","Genoa"},
		// Row 9
		{"","","","Croatia","","Florence","Turin","Mantua","Capua","Treviso","",""},
		// Row 10
		{"","Savoy","","Sardinia","","Parma","Bosnia","Tyrolea","","Naples","Romagna","Dalmatia"},
		// Row 11
		{"","","Venice","","","","","Carniola","","Messina","",""},
		// Row 12
		{"","","","","Pisa","Aquila","Avignon","Lucca","","Istria","",""},
	};
	
	private static final String plagueTable[][] = { 
		// Row 1 (actually it is not used)
		{"","","","","","","","","","","",""},
		// Row 2
		{"","Vicenza","Swiss","","","Carniola","","","","","Montferrat","Capua"},
		// Row 3
		{"","Pontremoli","Bosnia","Slavonia","","","","Croatia","","Tivoli","Bari","Tyrolea"},
		// Row 4
		{"","Savoy","","","Friuli","","Rome","","Marseille","Pavia","",""},
		// Row 5
		{"","","Salerno","Verona","","Dalmatia","Lucca","Bologna","Carinthia","Provence","",""},
		// Row 6
		{"","","","Turin","Sienna","Messina","Padua","Austria","Ferrara","","",""},
		// Row 7
		{"","Palermo","","Genoa","Albania","Pisa","Tunis","Avignon","Milan","","","Sardinia"},
		// Row 8
		{"","Durazzo","","Naples","Modena","Perugia","Cremona","Venice","Florence","","",""},
		// Row 9
		{"","","Bergamo","Ancona","Parma","","","","","Mantua","Istria",""},
		// Row 10
		{"","Romagna","Hungrary","","Urbino","","","","","Treviso","","Como"},
		// Row 11
		{"","Piacencia","Fornova","","","","","","Otranto","","Aquila","Spoleto"},
		// Row 12
		{"","Trent","Herzegovina","","Brescia","","","","Corsica","","Patrimony","Saluzzo"},
	};	
	
	public static String famineDiceRoll() {
		return famineTable[generator.nextInt(6)+generator.nextInt(6)+1][generator.nextInt(6)+generator.nextInt(6)+1];
	}
	
	public static String plageDiceRoll() {
		return plagueTable[generator.nextInt(6)+generator.nextInt(6)+1][generator.nextInt(6)+generator.nextInt(6)+1];
	}
	
	public static int randomIncome(String country) throws UnknownCountryException {
		int i = generator.nextInt(6)+1;
		if (country.equals("Austria")) {
			switch (i) {
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 3;
			case 4:
				return 3;
			case 5:
				return 4;
			case 6:
				return 4;
			}
		} 
		else if (country.equals("Florence")) {
			switch (i) {
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 3;
			case 4:
				return 3;
			case 5:
				return 4;
			case 6:
				return 5;
			}
		}
		else if (country.equals("France") || country.equals("Turks")) {
			return i;
		}
		else if (country.equals("Genoa") || country.equals("Naples")) {
			switch (i) {
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 2;
			case 4:
				return 3;
			case 5:
				return 3;
			case 6:
				return 4;
			}
		}
		else if (country.equals("Milan") || country.equals("Venice")) {
			switch (i) {
			case 1:
				return 2;
			case 2:
				return 3;
			case 3:
				return 3;
			case 4:
				return 4;
			case 5:
				return 4;
			case 6:
				return 5;
			}
		}		
		else if (country.equals("Papacy")) {
			switch (i) {
			case 1:
				return 2;
			case 2:
				return 3;
			case 3:
				return 3;
			case 4:
				return 4;
			case 5:
				return 5;
			case 6:
				return 6;
			}
		}
		else {
			throw new UnknownCountryException("country "+country+" is unknown");
		}
		/* Unreachable code */
		return -99999999;
	}
	
	/**
	 * Some dice rolls to test the tables
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Plague at " + plageDiceRoll());
			System.out.println("Plague at " + plageDiceRoll());
			System.out.println("Plague at " + plageDiceRoll());
			System.out.println("Plague at " + plageDiceRoll());
			System.out.println("Famine at " + famineDiceRoll());
			System.out.println("Famine at " + famineDiceRoll());
			System.out.println("Famine at " + famineDiceRoll());
			System.out.println("Famine at " + famineDiceRoll());
			System.out.println("Austria gets " + randomIncome("Austria"));
			System.out.println("Florence gets " + randomIncome("Florence"));
			System.out.println("France gets " + randomIncome("France"));
			System.out.println("Genoa gets " + randomIncome("Genoa"));
			System.out.println("Milan gets " + randomIncome("Milan"));
			System.out.println("Naples gets " + randomIncome("Naples"));
			System.out.println("Papacy gets " + randomIncome("Papacy"));
			System.out.println("Turks gets " + randomIncome("Turks"));
			System.out.println("Venice gets " + randomIncome("Venice"));
		} 
		catch (UnknownCountryException e) {
			e.printStackTrace();
		}		
	}
}
