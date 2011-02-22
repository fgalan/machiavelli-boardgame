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

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

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
	
	public static final int NO_DISASTER = 0;
	public static final int ONLY_ROW = 1;
	public static final int ONLY_COLUMN = 2;
	public static final int ROW_AND_COLUMN = 3; 
	
	public static int roll() {
		return generator.nextInt(6)+1;
	}
	
	private static Vector<String> getFamineRow(int row) {
		Vector<String> v = new Vector<String>();
		for (int i = 1 ; i < 12 ; i++) {
			if (!famineTable[row][i].isEmpty()) {
				v.add(famineTable[row][i]);
			}
		}
		return v;
	}
	
	private static Vector<String> getFamineColumn(int column) {
		Vector<String> v = new Vector<String>();
		for (int i = 1 ; i < 12 ; i++) {
			if (!famineTable[i][column].isEmpty()) {
				v.add(famineTable[i][column]);
			}
		}
		return v;
	}
	
	private static Vector<String> getPlagueRow(int row) {
		Vector<String> v = new Vector<String>();
		for (int i = 1 ; i < 12 ; i++) {
			if (!plagueTable[row][i].isEmpty()) {
				v.add(plagueTable[row][i]);
			}
		}
		return v;
	}
	
	private static Vector<String> getPlagueColumn(int column) {
		Vector<String> v = new Vector<String>();
		for (int i = 1 ; i < 12 ; i++) {
			if (!plagueTable[i][column].isEmpty()) {
				v.add(plagueTable[i][column]);
			}
		}
		return v;
	}
	
	public static int kindOfYear(int diceRoll) {
		if (diceRoll < 4) {
			return NO_DISASTER;
		}
		else if (diceRoll == 4 || diceRoll == 7) {
			return ONLY_ROW;
		}
		else if (diceRoll == 5 || diceRoll == 6) {
			return ONLY_COLUMN;
		}
		else { // diceRoll > 7
			return ROW_AND_COLUMN;
		}
	}
	
	/**
	 * Second diceRoll is only used in ROW_AND_COLUMN result
	 */
	public static Vector<String> famineResult(int kindOfYear, int diceRoll, int diceRoll2) {
		switch (kindOfYear) {
		case ONLY_ROW:
			return getFamineRow(diceRoll-1);
		case ONLY_COLUMN:
			return getFamineColumn(diceRoll-1);
		case ROW_AND_COLUMN:
			Vector<String> v = getFamineRow(diceRoll-1);
			for (Iterator<String> i = getFamineColumn(diceRoll2-1).iterator(); i.hasNext(); ) {
				String p = i.next();
				if (!v.contains(p)) {
					v.add(p);
				}
			}			
			return v;
		default:
			// NO_DISASTER
			return null;
		}
		
	}
	
	/**
	 * Second diceRoll is only used in ROW_AND_COLUMN result
	 */	
	public static Vector<String> plagueResult(int kindOfYear, int diceRoll, int diceRoll2) {
		switch (kindOfYear) {
		case ONLY_ROW:
			return getPlagueRow(diceRoll-1);
		case ONLY_COLUMN:
			return getPlagueColumn(diceRoll-1);
		case ROW_AND_COLUMN:
			Vector<String> v = getPlagueRow(diceRoll-1);
			/* This loop avoids duplications */
			for (Iterator<String> i = getPlagueColumn(diceRoll2-1).iterator(); i.hasNext(); ) {
				String p = i.next();
				if (!v.contains(p)) {
					v.add(p);
				}
			}
			return v;
		default:
			// NO_DISASTER
			return null;
		}
		
	}
	
	public static int randomIncome(String country, int diceRoll) throws UnknownCountryException {
		if (country.equals("Austria")) {
			switch (diceRoll) {
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
			switch (diceRoll) {
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
			return diceRoll;
		}
		else if (country.equals("Genoa") || country.equals("Naples")) {
			switch (diceRoll) {
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
			switch (diceRoll) {
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
			switch (diceRoll) {
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
	
	public static String kindOfYear2String(int kindOfYear) {
		switch (kindOfYear) {
		case ONLY_ROW:
			return "good year (only row)";
		case ONLY_COLUMN:
			return "good year (only column)";
		case ROW_AND_COLUMN:
			return "bad year (row and column)";
		default:
			return "no natural disaster";
		}
	}
	
	/**
	 * Some dice rolls to test the tables
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			int d1, d2, d3, d4, koy;
			Vector<String> v;
			
			/* Famine */
			d1 = roll();
			d2 = roll();
			koy = kindOfYear(d1+d2);
			System.out.println("Kind of Year ("+d1+"+"+d2+"): " + kindOfYear2String(koy));
			
			d1 = roll();
			d2 = roll();
			d3 = roll();
			d4 = roll();
			v = famineResult(koy,d1+d2,d3+d4); 
			switch (koy) {
			case ONLY_ROW:
				System.out.print("Famine ("+d1+"+"+d2+"): ");
				break;
			case ONLY_COLUMN:
				System.out.print("Famine ("+d1+"+"+d2+"): ");
				break;
			case ROW_AND_COLUMN:
				System.out.print("Famine ("+d1+"+"+d2+","+d3+"+"+d4+"): ");
				break;
			default: // NO DISASTER
				System.out.print("no famine");
				break;
			}
			
			if (v != null) {
				for (Iterator<String> i = v.iterator(); i.hasNext() ; ) {
					System.out.print(i.next() + ", ");
				}
			}
			System.out.println();
			
			/* Plague */
			d1 = roll();
			d2 = roll();
			koy = kindOfYear(d1+d2);
			System.out.println("Kind of Year ("+d1+"+"+d2+"): " + kindOfYear2String(koy));
			
			d1 = roll();
			d2 = roll();
			d3 = roll();
			d4 = roll();
			v = plagueResult(koy,d1+d2,d3+d4); 
			switch (koy) {
			case ONLY_ROW:
				System.out.print("Plague ("+d1+"+"+d2+"): ");
				break;
			case ONLY_COLUMN:
				System.out.print("Plague ("+d1+"+"+d2+"): ");
				break;
			case ROW_AND_COLUMN:
				System.out.print("Plague ("+d1+"+"+d2+","+d3+"+"+d4+"): ");
				break;
			default: // NO DISASTER
				System.out.print("no plague");
				break;
			}
			
			if (v != null) {
				for (Iterator<String> i = v.iterator(); i.hasNext() ; ) {
					System.out.print(i.next() + ", ");
				}
			}
			System.out.println();			
			
			d1 = roll();
			System.out.println("Austria rolls "+d1+", so gets " + randomIncome("Austria",d1));
			d1 = roll();
			System.out.println("Florence rolls "+d1+", so gets " + randomIncome("Florence",d1));
			d1 = roll();
			System.out.println("France rolls "+d1+", so gets " + randomIncome("France",d1));
			d1 = roll();
			System.out.println("Genoa rolls "+d1+", so gets " + randomIncome("Genoa",d1));
			d1 = roll();
			System.out.println("Milan rolls "+d1+", so gets " + randomIncome("Milan",d1));
			d1 = roll();
			System.out.println("Naples rolls "+d1+", so gets " + randomIncome("Naples",d1));
			d1 = roll();
			System.out.println("Papacy rolls "+d1+", so gets " + randomIncome("Papacy",d1));
			d1 = roll();
			System.out.println("Turks rolls "+d1+", so gets " + randomIncome("Turks",d1));
			d1 = roll();
			System.out.println("Venice rolls "+d1+", so gets " + randomIncome("Venice",d1));
		} 
		catch (UnknownCountryException e) {
			e.printStackTrace();
		}		
	}
}
