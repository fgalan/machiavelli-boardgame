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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import Exceptions.ProcessCommandsException;
import GameElements.Garrison;
import GameElements.Map;
import GameElements.Territory;
import GameElements.Unit;

/**
 * Each instance of this class is associated to a single territory and "accumulates" all the advance and
 * support operations  It is used as a kind of "accumulators" that, upon processing of player commands
 * store all the units which advance or support into the territory. Note that, in this case, a Garrison
 * converting to Army/Fleet is considered a "movement" into the territory.  
 *  
 * @author fermin
 *
 */
public class TerritoryUnderMovement {

	private Territory territory;
	private Vector<Unit> advancingUnits;
	
	/* The supportingUnits and supportingPlayer vector are related in the following way: 
	 * supportingUnits[i] point to the Unit and inFavorOf[i] stores the player in favor of which
	 * the Unit is supporting (note that the owner of supportingUnits[i] doesn't necessarily match
	 * with inFavorOf[i] when player are supporting units of other players due to agreements, etc.)
	 */
	private Vector<Unit> supportingUnits;
	private Vector<String> inFavorOf;
	
	/* These flags are used to indicate if the Garrison in the territory unit (if any) has ordered
	 * a conversion operation. It could be seen as an special case of advancingUnit element */
	private Garrison garrisonConvertToArmy;
	private Garrison garrisonConvertToFleet;

	public TerritoryUnderMovement(String t, Map m) throws ProcessCommandsException {

		territory = m.getTerritoryByName(t);
		if (territory == null) {
			throw new ProcessCommandsException("cannot built TerritoryUnderMovement for territory '" + t + "', it doesn't exist");
		}

		advancingUnits = new Vector<Unit>();
		supportingUnits = new Vector<Unit>();
		inFavorOf = new Vector<String>();
		
		garrisonConvertToArmy = null;
		garrisonConvertToFleet = null;
	}
	
	public void addAdvancingUnit(Unit u) {
		advancingUnits.add(u);
	}
	
	public void addSupportingUnit(Unit u, String p) {
		supportingUnits.add(u);
		inFavorOf.add(p);
	}
	
	public Territory getTerritory() {
		return territory;
	}
	
	public void setGarrisonConvertToArmy(Garrison g) {
		garrisonConvertToArmy = g;
	}
	
	public void setGarrisonConvertToFleet(Garrison g) {
		garrisonConvertToFleet = g;
	}
	
	public Garrison getGarrisonConvertToArmy() {
		return garrisonConvertToArmy;
	}
	
	public Garrison getGarrisonConvertToFleet() {
		return garrisonConvertToFleet;
	}
	
	public Vector<Unit> getAdvancingUnits() {
		return advancingUnits;
	}
	
	public HashMap<String,Integer> getSides() throws ProcessCommandsException {
		
		HashMap<String,Integer> sides = new HashMap<String, Integer>();
		
		/* TODO: detect exceptional situations:
		 * - two units belonging to the same player advancing/holding into the same place
		 * - garrison converting in 
		 */
		
		/* Process advancing units vector */
		for (Iterator<Unit> i = advancingUnits.iterator(); i.hasNext(); ) {
			Unit u = i.next();
			if (sides.get(u.getOwner()) == null) {
				sides.put(u.getOwner(), new Integer(u.getStrength()));
			}
			else {
				throw new ProcessCommandsException("player " + u.getOwner() + " self-conflict at " + territory.getName());
			}
		}

		/* Process unit currently occupying the territory */
		if (territory.getUnit() != null) {
			Unit u = territory.getUnit();
			if (sides.get(u.getOwner()) == null) {
				sides.put(u.getOwner(), new Integer(u.getStrength()));
			}
			else {
				throw new ProcessCommandsException("player " + u.getOwner() + " self-conflict at " + territory.getName());
			}
		}
		
		/* Process garrisonConvert */
		if (garrisonConvertToArmy != null) {
			Garrison g = garrisonConvertToArmy;
			if (sides.get(g.getOwner()) == null) {
				sides.put(g.getOwner(), new Integer(g.getStrength()));
			}
			else {
				throw new ProcessCommandsException("player " + g.getOwner() + " self-conflict at " + territory.getName());
			}
		}
		if (garrisonConvertToFleet != null) {
			Garrison g = garrisonConvertToFleet;
			if (sides.get(g.getOwner()) == null) {
				sides.put(g.getOwner(), new Integer(g.getStrength()));
			}
			else {
				throw new ProcessCommandsException("player " + g.getOwner() + " self-conflict at " + territory.getName());
			}
		}		
		
		/* Process supporting unit vector */
		for (int i = 0 ; i < supportingUnits.size(); i ++) {
			if (sides.get(inFavorOf.elementAt(i)) != null) {
				int currentStrength = sides.get(inFavorOf.elementAt(i)).intValue();
				sides.put(inFavorOf.elementAt(i), new Integer(currentStrength + supportingUnits.elementAt(i).getStrength()));
			}
			else {
				throw new ProcessCommandsException("support of unit " + supportingUnits.elementAt(i) + "not acting at terrotory " + territory.getName());
			}
		}
		
		return sides;
		
	}
	
	public String toString() {
		String s = "following units are moving to " + territory.getName() + ":\n";
		for (Iterator<Unit> i = advancingUnits.iterator(); i.hasNext(); ) {
			Unit u = i.next();
			s = s + "   " + u + "\n";
		}
		if (garrisonConvertToArmy != null) {
			s = s + "garrison " + garrisonConvertToArmy + " is converting to Army\n";
		}
		if (garrisonConvertToFleet != null) {
			s = s + "garrison " + garrisonConvertToFleet + " is converting to Fleet\n";
		}		
		s = s + "supported by\n";
		/* In this case, we are using a loop based on numerical index, due we have to advance two
		 * Vectors "in parallel" */
		for (int i = 0 ; i < supportingUnits.size(); i ++) {
			s = s + "   " + supportingUnits.get(i) + " in favor of " + inFavorOf.get(i) + "\n";
		}
		Unit currentUnit = territory.getUnit();
		if (currentUnit == null) {
			s = s + "currently not occupied";
		}
		else {
			s = s + "currently occupied by unit '" + currentUnit + "'";
		}
		return s;
	}
}
