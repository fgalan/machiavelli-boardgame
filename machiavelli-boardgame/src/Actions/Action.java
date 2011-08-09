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


package Actions;

import java.util.Iterator;
import java.util.Vector;

import GameElements.Army;
import GameElements.Fleet;
import GameElements.Garrison;
import GameElements.Map;
import GameElements.Unit;

public abstract class Action {

	private String type;
	private int id;
	
	public Action(String type, int id) {
		this.type = type;
		this.id = id;
	}
	
	/**
	 * For "anonymous actions" (in <Buy*>)
	 */
	public Action() { };
	
	/**
	 * For "anonymous actions" (in <Buy*>)
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * For "anonymous actions" (in <Buy*>)
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Search for the Unit associated to the action in the Map provides as argument. The player to which
	 * the Unit belong is also specified as argument. If a matching Unit is not found, then 'null' is
	 * returned
	 * 
	 * @param player
	 * @param m
	 */
	public Unit getAssociatedUnitInMap(String player, Map m) {
		Vector<Unit> units = m.getUnitBelongingToPlayer(player, type);
		for (Iterator<Unit> i = units.iterator(); i.hasNext() ; ) {
			Unit u = i.next();
			if (u.getId() == id) {
				return u;
			}
		}
		return null;
	}
	
	public String toString() {
		String s = "" + type.charAt(0) + id;
		return s;
	}
	
	/**
	 * The same as toString, but appending '*', '**' or '***' correctly based on the Map and player passed
	 * as arguments 
	 * @param m
	 * @param player
	 * @return
	 */
	public String toStringWithElite(Map m, String player) {
		Unit u = m.getEliteUnitFromPlayer(player);
		if (u != null) {
			if ( ( (u instanceof Army && type.equals("Army")) ||
			       (u instanceof Fleet && type.equals("Fleet")) ||
			       (u instanceof Garrison && type.equals("Garrison")) ) 
			      && (u.getId() == id) 
			    ) {
			return toString() + Unit.eliteString(u.getElite());
			}
		}
		return toString();
	}	
	
}
