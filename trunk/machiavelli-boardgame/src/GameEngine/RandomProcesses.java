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
	
	private static int kindOfYear() {
		int diceRoll =  generator.nextInt(6)+generator.nextInt(6)+1;
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
	
	public static Vector<String> famineResult() {
		switch (kindOfYear()) {
		case ONLY_ROW:
			return getFamineRow(generator.nextInt(6)+generator.nextInt(6)+1);
		case ONLY_COLUMN:
			return getFamineColumn(generator.nextInt(6)+generator.nextInt(6)+1);
		case ROW_AND_COLUMN:
			Vector<String> v = getFamineRow(generator.nextInt(6)+generator.nextInt(6)+1);
			v.addAll(getFamineColumn(generator.nextInt(6)+generator.nextInt(6)+1));
			return v;
		default:
			// NO_DISASTER
			return null;
		}
		
	}
	
	public static Vector<String> plagueResult() {
		switch (kindOfYear()) {
		case ONLY_ROW:
			return getPlagueRow(generator.nextInt(6)+generator.nextInt(6)+1);
		case ONLY_COLUMN:
			return getPlagueColumn(generator.nextInt(6)+generator.nextInt(6)+1);
		case ROW_AND_COLUMN:
			Vector<String> v = getPlagueRow(generator.nextInt(6)+generator.nextInt(6)+1);
			v.addAll(getPlagueColumn(generator.nextInt(6)+generator.nextInt(6)+1));
			return v;
		default:
			// NO_DISASTER
			return null;
		}
		
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
			Vector<String> v = famineResult();
			System.out.print("Famine: ");
			if (v != null) {
				for (Iterator<String> i = v.iterator(); i.hasNext() ; ) {
					System.out.print(i.next() + ", ");
				}
			}
			else {
				System.out.print("none");
			}
			System.out.println();
			
			v = plagueResult();
			if (v != null ) {
			System.out.print("Plague: ");
			for (Iterator<String> i = v.iterator(); i.hasNext() ; ) {
				System.out.print(i.next() + ", ");
			}
			}
			else {
				System.out.print("none");
			}
			System.out.println();			
			
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
