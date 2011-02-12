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
import java.util.Vector;

import org.apache.log4j.Logger;

import GameElements.Map;
import GameElements.Province;

public class Engine {
	
	private final static Logger log = Logger.getLogger("Engine.class");

	//public static TurnResult processTurnt(Player p[], Map m, Turn t) {
	//	// TODO
	//	return null;
	//}
	
	public static Result doFamine(Map m) {
		
		GenericResult r = new GenericResult();
		
		/* Get provinces with famine */
		Vector<String> v = RandomProcesses.famineResult();
		if (v != null) {
			/* Put a marker in each province */
			for (Iterator<String> i = v.iterator() ; i.hasNext() ; ) {
				Province p = (Province) m.getTerritoryByName(i.next());
				/* note that the not null checking is needed because in the famine table there could
				 * be provinces not being used in the current scenario */
				if (p !=null) {
					p.setFamine();
					r.addResult("famine strikes at " + p.getName());
				}
			}
		}
		else {
			r.addResult("no famine this year");
		}
		return r;
	}
	
	public static Result clearFamine(Map m) {
		
		GenericResult r = new GenericResult();
		
		/* Remove Famine marker and units in those provinces */
		Vector<Province> v = m.getProvincesWithFamine();
		for (Iterator<Province> i = v.iterator(); i.hasNext(); ) {
			Province p = i.next();
			
			/* note that the not null checking is needed because in the famine table there could
			 * be provinces not being used in the current scenario */
			if (p != null) {
			
				/* has army or fleet? */
				if (p.getUnit() != null) {
					r.addResult(p.getUnit() + " removed due to famine at " + p.getName());
					p.clearUnit();
				}
		
				if (p.getCity() != null) {
					/* has garrison at city? */
					if (p.getCity().getUnit() != null) {
						r.addResult(p.getCity().getUnit() + " removed due to famine at " + p.getName());
						p.getCity().clearUnit();
					}
				
					/* has autonomous garrison? */
					if (p.getCity().hasAutonomousGarrison()) {
						r.addResult("autonomous garrison removed due to famine at " + p.getName());
						p.getCity().clearAutonomousGarrison();
					}
				}
				
				/* Remove the marker itself */
				p.clearFamine();
			}
			
		}
		return r;
	}	
	
	public static Result doPlague(Map m) {
		
		GenericResult r = new GenericResult();
		
		/* Get provinces with plague */
		Vector<String> v = RandomProcesses.famineResult();
		if (v != null) {
			for (Iterator<String> i = v.iterator(); i.hasNext(); ) {
				Province p = (Province) m.getTerritoryByName(i.next());
			
				/* note that the not null checking is needed because in the famine table there could
				 * be provinces not being used in the current scenario */
				if (p != null) {
				
					/* Remove units in each province */
					/* has army or fleet? */
					if (p.getUnit() != null) {
						r.addResult(p.getUnit() + " removed due to plague at " + p.getName());
						p.clearUnit();
					}
		
					if (p.getCity() != null) {
						/* has garrison at city? */
						if (p.getCity().getUnit() != null) {
							r.addResult(p.getCity().getUnit() + " removed due to plague at " + p.getName());
							p.getCity().clearUnit();
						}
				
						/* has autonomous garrison? */
						if (p.getCity().hasAutonomousGarrison()) {
							r.addResult("autonomous garrison removed due to plague at " + p.getName());
							p.getCity().clearAutonomousGarrison();
						}
					}
				}
			}
		}
		else {
			r.addResult("no plague this year");
		}
		return r;
	}
}
