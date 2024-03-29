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

package GameElements;

import org.apache.log4j.Logger;

public abstract class Unit {
	
	private final static Logger log = Logger.getLogger("Unit.class");
	
	private int id;
	private String owner;
	private int elite;
	private Territory location;
	
	public static final int NO_ELITE = 0;
	public static final int ELITE_TYPE_1 = 1;
	public static final int ELITE_TYPE_2 = 2;
	public static final int ELITE_TYPE_3 = 3;
	
	public Unit (int i, String p, int e) {
		id = i;
		owner = p;
		elite = e;
	}
	
	public String getOwner() {
		return owner;
	}

	public int getElite() {
		return elite;
	}

	public void setLocation(Territory location) {
		this.location = location;
	}

	public Territory getLocation() {
		return location;
	}

	public int getId() {
		return id;
	}
	
	public int getStrength () {
		if (elite == ELITE_TYPE_1 || elite == ELITE_TYPE_2) {
			return 2;
		}
		else {
			return 1;
		}
	}
	
	public String toString() {
		return (id + eliteString() + " ["+owner+"]");
	}
	
	public static String eliteString(int elite) {
		String s = "";
		if (elite == ELITE_TYPE_1) {
			s ="*";;
		}
		else if (elite == ELITE_TYPE_2) {
			s = "**";
		}
		else if (elite == ELITE_TYPE_3) {
			s = "***";
		}
		return s;
		
	}
	
	private String eliteString() {
		return eliteString(elite);
	}
	
	public int cost() {
		if (elite == NO_ELITE) {
			return 3;
		}
		else if (elite == ELITE_TYPE_1) {
			return 6;
		}
		else if (elite == ELITE_TYPE_2) {
			return 6;
		}
		else { // ELITE_TYPE_3
			return 9;
		}
	}

}
