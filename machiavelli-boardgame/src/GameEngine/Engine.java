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

import Exceptions.ProcessAdjustmentsException;
import Exceptions.ProcessCommandsException;
import Exceptions.UnknownCountryException;
import GameElements.Army;
import GameElements.Fleet;
import GameElements.Garrison;
import GameElements.City;
import GameElements.Map;
import GameElements.Province;
import GameElements.Territory;
import GameElements.Unit;

public class Engine {
	
	private final static Logger log = Logger.getLogger("Engine.class");
	
	/**
	 * Processes a set of Command orders (supposed each one from a different player) on the
	 * map, in the context of a given game status.
	 * @param gs
	 * @param m
	 * @param cmds
	 * @return
	 * @throws ProcessCommandsException 
	 */
	public static Result processCommands(GameStatus gs, Map m, Vector<Commands> cmds) throws ProcessCommandsException {
		
		ComposedResult r = new ComposedResult();
		
		/* Header */
		GenericResult r2 = new GenericResult();
		r2.addResult("------ Turn "+gs.campaing2Text() + " "+gs.getYear()+" ------");
		r.addResult(r2);
		
		/* Famine phase and income phase only in Spring */
		if (gs.getCampaign() == GameStatus.SPRING) {
			r.addResult(doFamine(m));
			try {
				r.addResult(doIncome(gs,m));
			} 
			catch (UnknownCountryException e) {
				throw new ProcessCommandsException(e.toString());
			}
		}
		
		/* Famine markers removal phase and plague only in Summer */
		if (gs.getCampaign() == GameStatus.SUMMER) {
			r.addResult(clearFamine(m));
			r.addResult(doPlague(m));
		}
		
		/* Process actions, except at the beginning of the game (in that case cmd is null) */
		if (cmds != null) {
		
			/* Process Assassinations */
			for (Iterator<String> i = gs.getPlayers().iterator(); i.hasNext() ; i.next()) {
				// TODO
			}
			
			/* Process other Expenses */
			for (Iterator<String> i = gs.getPlayers().iterator(); i.hasNext() ; i.next()) {
				// TODO
			}
			
			/* Process Actions */
			for (Iterator<String> i = gs.getPlayers().iterator(); i.hasNext() ; i.next()) {
				// TODO
			}
			
			/* Resolve conflicts */
			// TODO
		
			/* Check victory conditions */
			// TODO
			
			/* Check home country changes */
			// TODO
			
			/* Set the date of the new turn */
			gs.incCampaign();
			if (gs.getCampaign() == GameStatus.SPRING) {
				gs.incYear();
			}			
			
		}
		
		return r;
	}
	

	/**
	 * Processes a set of Adjustments orders (supposed each one from a different player) on the
	 * map, in the context of a given game status. 
	 * @param m
	 * @return
	 * @throws ProcessAdjustmentsException 
	 */
	public static Result processAdjustments(GameStatus gs, Map m, Vector<Adjustments> adj) throws ProcessAdjustmentsException {
		
		GenericResult r = new GenericResult();
		
		/* This process only can be done in spring, so other campaign will throw an Exception */
		if (gs.getCampaign() != GameStatus.SPRING) {
			throw new ProcessAdjustmentsException("wrong campaing " + gs.getCampaign());
		}
		
		for (Iterator<Adjustments> i = adj.iterator(); i.hasNext() ; ) {
			Adjustments adr = i.next();
			r.addResult("player " + adr.getPlayer() + " adjustment:");
			
			/* Gather all the units belonging to player */
			Vector<Unit> v = m.getUnitBelongingToPlayer(adr.getPlayer(),null);
			
			/* Process payments */
			for (Iterator<Payment> j = adr.getPayments().iterator(); j.hasNext(); ) {
				Payment p = j.next();
				String type = p.getType();
				int id = p.getId();
				
				/* Does that Unit belong to the player? */
				Unit u = null;
				for (int k = 0; k < v.size(); k++) {
					if (v.elementAt(k).getId() == id) {
						if (type.equals("Army")) {
							if (v.elementAt(k) instanceof Army) {
								u = v.elementAt(k);
							}
						}
						else if (type.equals("Fleet")) {
							if (v.elementAt(k) instanceof Fleet) {
								u = v.elementAt(k);
							}
						}
						else { // Garrison
							if (v.elementAt(k) instanceof Garrison) {
								u = v.elementAt(k);
							}
						}
						
					}
				}
				if (u == null) {
					r.addResult("- can not process payment order <>: player has no " + type.substring(0, 1) + id + "unit");
					continue;
				}
				
				/* Check money and pay */
				if (u.cost() > gs.getMoney(adr.getPlayer())) {
					r.addResult("- can not process payment order <>: not enough money");
				}
				else {
					/* Pay */
					gs.decMoney(adr.getPlayer(), u.cost());
					r.addResult("- payment: "+u+" (-"+u.cost()+")");
					
					/* Remove Unit for the vector*/
					v.remove(u);
				}
				
			}
			
			/* All the unit remaining in the vector has to be removed. A Province vector is built
			 * based on this, that is used in the next sub-phase (Purchase) */
			Vector<Province> vp = new Vector<Province>();
			for (Iterator<Unit> j = v.iterator() ; j.hasNext(); ) {
				Unit u = j.next();
				Territory t = u.getLocation();
				r.addResult("- unit "+ u +" removed due to unpayment");
				
				/* Actually removing Unit from map */
				if (u instanceof Army || u instanceof Fleet) {
					u.getLocation().clearUnit();
				}
				else { // Garrison
					((Province)u.getLocation()).getCity().clearUnit();
				}
				
				if (t instanceof Province) {
					vp.add((Province)t);
				}
			}
			
			/* Process purchases */
			for (Iterator<Purchase> j = adr.getPurchases().iterator(); j.hasNext(); ) {
				Purchase p = j.next();
				String type = p.getType();
				Province pr = (Province) m.getTerritoryByName(p.getProvince());
				
				/* The player has not reach unit limit (A < 12, F < 8, G < 6) */
				int freeId = m.GetFreeId(adr.getPlayer(), type);
				if ( (type.equals("Army") && freeId == Army.MAX) || 
				     (type.equals("Fleet") && freeId == Fleet.MAX) ||
				     (type.equals("Garrison") && freeId == Garrison.MAX)
				   ) {
					r.addResult("- can not process purchase order <>: maximum units limit reached for " + type);
					continue;
				}
				
				/* Is the province valid?
				 * a) Exists */
				if (pr == null) {
					r.addResult("- can not process purchase order <>: Province " +p.getProvince()+ "doesn't exists");
					continue;
				}
				/* a) Belong to player */
				if (pr.getController() == null || !pr.getController().equals(adr.getPlayer())) {
					r.addResult("- can not process purchase order <>: Province " +p.getProvince()+ "doesn't belong to player");
					continue;
				}
				/* b) Has a city (no matter if fortified or not, no matter city controller) */
				if (pr.getCity() == null) {
					r.addResult("- can not process purchase order <>: Province " +p.getProvince()+ "has no city");
					continue;
				}
				/* c) For fleets, the city has port */
				if (type.equals("Fleet") && !pr.getCity().isPort()) {
					r.addResult("- can not process purchase order <>: Province " +p.getProvince()+ "has no city");
					continue;
				}				
				/* d) No unpaid unit was removed in the province in the same adjustment phase */
				if (vp.contains(pr)) {
					r.addResult("- can not process purchase order <>: a unit was unpayed in the same province this turn, " +p.getProvince());
					continue;
				}
				
				/* For Army/Fleet, no other Army/Fleet is in the same province */
				if ( (type.equals("Army") || type.equals("Fleet")) && pr.getUnit() != null) {
					r.addResult("- can not process purchase order <>: existing unit in " +p.getProvince() + ", " + pr.getUnit());
					continue;
				}
				
				/* For Garrison, no other Garrison is in the same city */
				if ( type.equals("Garrison") && pr.getCity().getUnit() != null) {
					r.addResult("- can not process purchase order <>: existing unit in city in " +p.getProvince() + ", " + pr.getCity().getUnit());
					continue;
				}
				
				/* Pre-create the Unit (note that creation could fail beyond this point) */
				Unit u;
				if (type.equals("Army")) {
					u = new Army(m.GetFreeId(adr.getPlayer(), type), adr.getPlayer(), null, p.getElite());
				}
				else if (type.equals("Fleet")) {
					u = new Fleet(m.GetFreeId(adr.getPlayer(), type), adr.getPlayer(), null, p.getElite());
				}
				else { // garrison
					u = new Garrison(m.GetFreeId(adr.getPlayer(), type), adr.getPlayer(), (City)null, p.getElite());
				}
				
				/* Check money and buy */
				if (u.cost() > gs.getMoney(adr.getPlayer())) {
					r.addResult("- can not process buy order <>: not enough money");
				}
				else {
					/* Pay */
					gs.decMoney(adr.getPlayer(), u.cost());
					r.addResult("- payment: "+u+" (-"+u.cost()+")");
					
					/* Attach unit to location in the map */
					u.setLocation(pr);
					if (u instanceof Army || u instanceof Fleet) {
						pr.setUnit(u);
					}
					else { // garrison
						pr.getCity().setUnit(u);
					}
				}
			}
		}
		
		return r;
	}
	
	private static Result doFamine(Map m) {
		
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
	
	private static Result clearFamine(Map m) {
		
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
	
	private static Result doPlague(Map m) {
		
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
	
	/**
	 * Increase the players income in the gs, considering the game map
	 * @param gs
	 * @param m
	 * @return
	 * @throws UnknownCountryException 
	 */
	private static Result doIncome(GameStatus gs, Map m) throws UnknownCountryException {
		
		GenericResult r = new GenericResult();
		
		/* Calculate income, player by player */
		for (Iterator<String> i = gs.getPlayers().iterator(); i.hasNext() ; ) {
			String player = i.next();
			
			/* Fixed amount */
			int fixed = m.calculateIncome(player);
			
			/* Variable, rolling dices */
			int variable = 0;
			for (int j = 0; j < gs.getIncomeRolls(player); j++) {
				variable += RandomProcesses.randomIncome(player);
			}
			
			/* If player controls Genoa city, then dice extra roll */
			boolean extraDices = false;
			Province genoa = (Province) m.getTerritoryByName("Genoa");
			City genoaCity = genoa.getCity();
			if (!genoaCity.hasAutonomousGarrison()) {
				if (genoaCity.getUnit() != null) {
					/* Genoa city has controlling unit, so the unit controller is the one with gets extra rolls */
					if (genoaCity.getUnit().getOwner().equals(player)) {
						extraDices = true;
					}
				}
				else {
					/* Genoa city has no controlling unit, so the province controller (if any) 
					 * is the one which gets the extra roll */
					if (genoa.getController().equals(player)) {
						extraDices = true;
					}
				}
			}
			if (extraDices) {
				for (int j = 0; j < gs.getGenoaControllerRolls(); j++) {
					variable += RandomProcesses.randomIncome("Genoa");
				}
			}
			
			r.addResult("player "+player+" gets " + fixed + "d fixed and " + variable +"d variable (total "+(fixed+variable)+")");
			
			/* Update money */
			gs.incMoney(player, fixed + variable);
		}
		
		return r;
	}

}
