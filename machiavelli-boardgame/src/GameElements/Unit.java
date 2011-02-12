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

import GameEngine.Player;

public abstract class Unit {
	
	private final static Logger log = Logger.getLogger("Unit.class");
	
	private String name;
	private String owner;
	private int elite;
	private Territory location;
	
	public static final int NO_ELITE = 0;
	public static final int ELITE_TYPE_1 = 1;
	public static final int ELITE_TYPE_2 = 2;
	public static final int ELITE_TYPE_3 = 3;
	
	public Unit (String n, String p, int e) {
		name = n;
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

	public String getName() {
		return name;
	}
	
	public String toString() {
		return (name + eliteString() + " ["+owner+"]");
	}
	
	private String eliteString() {
		String s = "";
		if (elite == Unit.ELITE_TYPE_1) {
			s ="*";;
		}
		else if (elite == Unit.ELITE_TYPE_2) {
			s = "**";
		}
		else if (elite == Unit.ELITE_TYPE_3) {
			s = "***";
		}
		return s;
	}	

}
