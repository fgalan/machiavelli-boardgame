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
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import Actions.Action;
import Actions.Advance;
import Actions.Besiege;
import Actions.Convert;
import Actions.Hold;
import Actions.LiftSiege;
import Actions.Support;
import Actions.Transport;
import Exceptions.ProcessAdjustmentsException;
import Exceptions.ProcessCommandsException;
import Exceptions.UnknownCountryException;
import Expenses.Assasination;
import Expenses.BuyAutonomousGarrison;
import Expenses.BuyUnit;
import Expenses.CounterBride;
import Expenses.DisbandAutonomousGarrison;
import Expenses.DisbandGarrison;
import Expenses.DisbandUnit;
import Expenses.Expense;
import Expenses.FamineRelief;
import Expenses.GarrisonToAutonomous;
import Expenses.PacifyRebellion;
import Expenses.Rebellion;
import GameElements.Army;
import GameElements.Fleet;
import GameElements.Garrison;
import GameElements.City;
import GameElements.Map;
import GameElements.Province;
import GameElements.Sea;
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
		
		/* Process actions, except at the beginning of the game (in that case cmd is null) */
		if (cmds != null) {
			
			GenericResult rExpenses = new GenericResult();
			rExpenses.addResult("expenses: ");
			GenericResult rResolv = new GenericResult();
			rResolv.addResult("turn resolution (including conflicts):");
			
			/* Process Expenses */
			
			boolean atLeatOneExpense = false;						
			for (Iterator<Commands> i = cmds.iterator(); i.hasNext() ; ) {
				
				Commands c = i.next();
				
				/* Note that we are processing differently the output for Expenses and Actions,  
				 * in rExpenses and rActions respectively (e.g. rActions is per-player while rExpenses
				 * includes all player simultaneously). This is due to Expenses are rarer than Actions
				 * (i.e. it is possible a turn without any Expense for any player, but this is almost
				 * impossible in the case of Actions) 
				 */

				for (Iterator<Expense> j = c.getExpenses().iterator(); j.hasNext();  ) {
					Expense e = j.next();
					rExpenses.addResult("- " + c.getPlayer() + " expends " + e);
					atLeatOneExpense = true;
					
					if (e instanceof Assasination) {
						// TODO: process Assassinations first, before than other expenses, no matter its position in the
						// Vector
						throw new ProcessCommandsException("'Assasination' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof BuyAutonomousGarrison) {
						BuyAutonomousGarrison bag = (BuyAutonomousGarrison)e;
						 
						/* Check that expense is legal */
						Province p = ((Province)m.getTerritoryByName(bag.getProvince()));
						if (gs.getMoney(c.getPlayer()) < bag.getAmount()) {
							rExpenses.addResult("  * INVALID: player has no enough money");
							continue; // for in j
						}
						if (p.getCity() == null) {
							rExpenses.addResult("  * INVALID: province " + bag.getProvince() + " has no city");
							continue; // for in j
						}
						if ( ! p.getCity().hasAutonomousGarrison() ) {
							rExpenses.addResult("  * INVALID: province " + bag.getProvince() + " city has no autonomous garrison");
							continue; // for in j
						}
						boolean adjacency = false;
						for (Iterator<Unit> k = m.getUnitBelongingToPlayer(c.getPlayer(), null).iterator(); k.hasNext() ;) {
							/* FIXME: rules doubt: Garrison count for checking adjacency in the case of
							 * bridges or not? */
							Unit u = k.next();
							if (m.areAdjacent(u.getLocation(), p)) {
								adjacency = true;
								break; // for in k
							}
						}
						if (! adjacency) {
							rExpenses.addResult("  * INVALID: province " + bag.getProvince() + " is not adjacent to any Unit of the player");
							continue; // for in j
						}
						if (m.getFreeId(c.getPlayer(), "Garrison") == Garrison.MAX) {
							rExpenses.addResult("  * INVALID: Garrison units limit reached");
							continue; // for in j
						}
						
						/* Sum counter-brides */
						// TODO: actually implement this
						int counterBride = 0;
						rExpenses.addResult("  * Counter brides: " + counterBride + "d");
						
						/* Check amount */
						int min = BuyAutonomousGarrison.MIN_AMMOUNT;
						if (p.getCity().getSize() > 1 ) {
							rExpenses.addResult("  * Bride in capital city (size greater than 1) doubles minimum ammount");
							min = 2 * min;
						}
						min = min + counterBride;
						
						gs.decMoney(c.getPlayer(), bag.getAmount());
						if (bag.getAmount() < min) {
							rExpenses.addResult("  * Required " + min + "d, result is FAIL. Player expends " + bag.getAmount() + "d");
						}
						else {
							rExpenses.addResult("  * Required " + min + "d, result is SUCCESS. Player expends " + bag.getAmount() + "d");
							/* Create new unit and attach it to the map */
							p.getCity().clearAutonomousGarrison();
							Garrison g = new Garrison(m.getFreeId(c.getPlayer(), "Garrison"), c.getPlayer(), p.getCity(), Unit.NO_ELITE);
							g.setLocation(p);
							p.getCity().setUnit(g);
							
							/* Create action for the new unit, inserting it in the Action vector of the player */
							bag.getAction().setId(g.getId());
							bag.getAction().setType("Garrison");
							c.addAction(bag.getAction());
							
							rExpenses.addResult("  * Created '" + g + "' with action '" + bag.getAction().toStringWithElite(m, c.getPlayer()) + "'");
						}
						
						//throw new ProcessCommandsException("'BuyAutonomousGarrison' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof BuyUnit) {
						// TODO
						throw new ProcessCommandsException("'BuyUnit' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof CounterBride) {
						// TODO
						throw new ProcessCommandsException("'CounterBride' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof DisbandAutonomousGarrison) {
						// TODO
						throw new ProcessCommandsException("'DisbandAutonomousGarrison' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof DisbandGarrison) {
						// TODO
						throw new ProcessCommandsException("'DisbandGarrison' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof DisbandUnit) {
						// TODO
						throw new ProcessCommandsException("'DisbandUnit' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof FamineRelief) {
						// TODO
						throw new ProcessCommandsException("'FamineRelief' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof GarrisonToAutonomous) {
						// TODO
						throw new ProcessCommandsException("'GarrisonToAutonomous' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof PacifyRebellion) {
						// TODO
						throw new ProcessCommandsException("'PacifyRebellion' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else if (e instanceof Rebellion) {
						// TODO
						throw new ProcessCommandsException("'Rebellion' expense not implemented for player " + c.getPlayer() + ": " + e);
					}
					else {
						// This must not happen!
						throw new ProcessCommandsException("unknown expense for player " + c.getPlayer() + ": " + e);						
					}
				}
			}
			if (!atLeatOneExpense) {
				rExpenses.addResult("  none");
			}
			r.addResult(rExpenses);
			
			HashMap<String,GenericResult> rActions = new HashMap<String,GenericResult>(); 
			/* Create HashMap with GenericResult for each player */
			for (Iterator<Commands> i = cmds.iterator(); i.hasNext() ; ) {
				Commands c = i.next();
				rActions.put(c.getPlayer(), new GenericResult());
				rActions.get(c.getPlayer()).addResult("player " + c.getPlayer() + " commands:");
			}
			
			/* Transport actions are processed first, because they need to be processed before Advance */
			// FIXME: chicken-and-egg problem with doing processing in this way: Transport-Advance-Conflict,
			// as the result of the conflict could be the retreat of a Fleet ordering transport
			HashMap<String,Transportation> transportations = new HashMap<String,Transportation>(); 
			for (Iterator<Commands> i = cmds.iterator(); i.hasNext() ; ) {
				Commands c = i.next();
				for (Iterator<Action> j = c.getActions().iterator(); j.hasNext();  ) {
					Action a = j.next();
					
					/* FIXME: I don't like the following too much, but is the easy way of splitting actions
					 * processing in the two consecutive loops */
					if (!(a instanceof Transport)) {
						continue;
					}
					Transport tr = (Transport)a;
					
					rActions.get(c.getPlayer()).addResult("- " + a.toStringWithElite(m, c.getPlayer()));
					
					/* Search for a matching unit in the map */
					Unit u = a.getAssociatedUnitInMap(c.getPlayer(), m);
					if (u == null) {
						throw new ProcessCommandsException("action '" + a.toStringWithElite(m, c.getPlayer()) + "' for player " + c.getPlayer() +" has no matching unit in the map");
					}
					
					/* Check the transported unit exist in the map */
					Vector<Unit> units = m.getUnitBelongingToPlayer(tr.getPlayer(), "Army");
					boolean found = false;
					for (Iterator<Unit> k = units.iterator(); k.hasNext() ; ) {
						Unit uu = k.next();
						if (uu.getId() == tr.getArmyId()) {
							found = true;
							break;
						}
					}
					if (!found) {
						rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal transport, A"+tr.getArmyId()+" from player "+tr.getPlayer()+" doesn't exist, unit actions becomes 'Hold'");
						continue;
					}
					
					/* Check that transport make sense (i.e. is there a "tranportation line" between the
					 * Fleet and the Army */
					//FIXME: this could be great, but is difficult :) and actually not necessary (it is up
					// to the player ensure that Transport make sense)					
					
					/* Add Transportation instance to the HashMap */
					transportations.put(tr.getAssociatedUnitInMap(c.getPlayer(), m).getLocation().getName(),new Transportation(tr.getArmyId(), tr.getPlayer()));
					
				}
			}
			
			/* Process other Actions */
			Vector <TerritoryUnderMovement> tums = new Vector<TerritoryUnderMovement>();			
			for (Iterator<Commands> i = cmds.iterator(); i.hasNext() ; ) {
				
				Commands c = i.next();				
								
				for (Iterator<Action> j = c.getActions().iterator(); j.hasNext();  ) {
					Action a = j.next();
					
					/* FIXME: I don't like the following too much, but is the easy way of splitting actions
					 * processing in the two consecutive loops */					
					if (a instanceof Transport) {
						continue;
					}
					
					rActions.get(c.getPlayer()).addResult("- " + a.toStringWithElite(m, c.getPlayer()));
					
					/* Search for a matching unit in the map */
					Unit u = a.getAssociatedUnitInMap(c.getPlayer(), m);
					if (u == null) {
						throw new ProcessCommandsException("action '" + a.toStringWithElite(m, c.getPlayer()) + "' for player " + c.getPlayer() +" has no matching unit in the map");
					}
					
					if (a instanceof Advance) {
						
						Advance ad = (Advance)a;
					
						/* To check if the territory to advance is on the map */
						if (m.getTerritoryByName(ad.getTerritory())==null) {
							throw new ProcessCommandsException("processing <"+ad+">: "+ad.getTerritory()+" is not a valid territory name on the map");
						}
					
						/* To check that advance is legally valid */
						String lm = m.isLegalMove(u, m.getTerritoryByName(ad.getTerritory()), transportations);
						if (!lm.equals("")) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal advance, unit actions becomes 'Hold': " + lm);
							continue; //for in j
						}
						
						/* To check that the Unit is not besieging */
						// TODO: not sure if this is actually a rule
						
						/* Search for a TerritoryUnderMovement associated to the same province; if doesn't
						 * exits created a new one */
						TerritoryUnderMovement tum = null;
						for (Iterator<TerritoryUnderMovement> k = tums.iterator(); k.hasNext();) {
							TerritoryUnderMovement tumAux = k.next();
							if (tumAux.getTerritory().getName().equals(ad.getTerritory())) {
								tum = tumAux;
							}
						}
						if (tum == null) {
							tum = new TerritoryUnderMovement(ad.getTerritory(), m);
							tums.add(tum);
						}
						
						/* Add Unit to the advancing units list */						
						tum.addAdvancingUnit(u);

					}
					else if (a instanceof Besiege) {
						
						/* To check if there is the unit is in a Province with city */
						if (!(u.getLocation() instanceof Province)) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal Besiegue, unit actions becomes 'Hold': can not Besiegue in the Sea");
							continue; // for in j							
						}
						Province p = ((Province)u.getLocation());
						
						if (p.getCity() == null) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal Besiegue, unit actions becomes 'Hold': can not Besiegue in a Province without city");
							continue; // for in j							
						}
						/* If the unit is a Fleet, to check if the city has a port */
						if (u instanceof Fleet && !(p.getCity().isPort())) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal Besiegue, unit actions becomes 'Hold': Fleets only can besiegue cities with port");
							continue; // for in j
						}	
						/* To check if there is actually a Garrison in the city */
						if (p.getCity().getUnit() == null && !p.getCity().hasAutonomousGarrison()) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal Besiegue, unit actions becomes 'Hold': city has no garrison inside");
							continue; // for in j
						}
							
						/* Resolve action */
						if (p.getCity().isUnderSiege()) {
							/* City under siege: resolve the siege */
							Garrison g = (Garrison)p.getCity().getUnit();
							if (g != null) {
								rResolv.addResult("- unit " + u + " in " + p.getName() + " finishes siege: unit " + g + " is destroyed");
								p.getCity().clearUnit();
								p.getCity().clearUnderSiege();
							}
							else if (p.getCity().hasAutonomousGarrison()){
								rResolv.addResult("- unit " + u + " in " + p.getName() + " finishes siege: autonomous garrision unit is destroyed");
								p.getCity().clearAutonomousGarrison();
								p.getCity().clearUnderSiege();
							}
							else {
								throw new ProcessCommandsException("siegue finishing at " + p.getName() +" but not garrision nor autonomous garrison is at the place!");
							}
						}
						else {
							/* Cite not under siege: put it under siege */
							p.getCity().setUnderSiege();
							rResolv.addResult("- unit " + u + " in " + p.getName() + " besiegues city");
						}
					}
					else if (a instanceof Convert) {
						// TODO: implement voluntary disband as convert operation
						
						Convert co = (Convert) a;
						
						/* To check that convert is legally valid */
						if (u instanceof Army && co.getNewType().equals("Fleet")) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': Army cannot be converted to Fleet");
							continue; // for in j
						}
						if (u instanceof Fleet && co.getNewType().equals("Army")) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': Fleet cannot be converted to Army");
							continue; // for in j
						}
						if ( (u instanceof Army && co.getNewType().equals("Army"))||(u instanceof Fleet && co.getNewType().equals("Fleet")) ) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': original and new types cannot be the same");
							continue; // for in j
						}						
						if (!(u.getLocation() instanceof Province)) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': unit must be in a province with a fortificed city");
							continue; // for in j
						}
						Province p = (Province) u.getLocation();
						if ( (p.getCity() == null) || (p.getCity() != null && !p.getCity().isFortified()) ) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': unit must be in a province with a fortificed city");
							continue; // for in j
						}
						if ((co.getNewType().equals("Fleet") || u instanceof Fleet) && ! p.getCity().isPort() ) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': this kind of conversion required a city with port");
							continue; // for in j
						}
						
						if (u instanceof Garrison) {
							/* Garrison -> Fleet/Army */
							/* Search for a TerritoryUnderMovement associated to the same province; if doesn't
							 * exits created a new one */
							TerritoryUnderMovement tum = null;
							for (Iterator<TerritoryUnderMovement> k = tums.iterator(); k.hasNext();) {
								TerritoryUnderMovement tumAux = k.next();
								if (tumAux.getTerritory().getName().equals(p.getName())) {
									tum = tumAux;
								}
							}
							if (tum == null) {
								tum = new TerritoryUnderMovement(p.getName(), m);
								tums.add(tum);
							}
							
							/* Set garrisonConvert* */
							if (co.getNewType().equals("Army")) {
								tum.setGarrisonConvertToArmy((Garrison)u);
							}
							else {  // Fleet
								tum.setGarrisonConvertToFleet((Garrison)u);
							}
						}
						else { /* Army/Fleet -> Garrison */
							/* Check that city is empty, otherwise the action is invalid (note that in this
							 * case it is not a conflict) */
							if (p.getCity().getUnit() != null || p.getCity().hasAutonomousGarrison()) {
								rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal convert, unit actions becomes 'Hold': city has a Garrison");
							}
							else {
								
								String player = u.getOwner();
								int elite = u.getElite();
								
								Garrison g = new Garrison(m.getFreeId(player, "Garrison"), player, p.getCity(), elite);
								p.getCity().setUnit(g);
								p.setUnit(null);
								rResolv.addResult("- unit " + u + " in " + p.getName() + " converts into " + g);
								
							}	
						}
						
					}
					else if (a instanceof Hold) {
						/* Do nothing */
					}
					else if (a instanceof LiftSiege) {
						// TODO
						throw new ProcessCommandsException("'LiftSiege' operation not implemented for player " + c.getPlayer() + " action: " + a.toStringWithElite(m, c.getPlayer()));
					}
					else if (a instanceof Support) {
						Support sp = (Support)a;
						
						/* To check if the supported territory is on the map */
						if (m.getTerritoryByName(sp.getTerritory())==null) {
							throw new ProcessCommandsException("processing <"+sp+">: "+sp.getTerritory()+" is not a valid territory name on the map");
						}
						
						/* To check that support is legally valid */
						String lm = m.isLegalMove(u, m.getTerritoryByName(sp.getTerritory()), null);
						if (!lm.equals("")) {
							rActions.get(c.getPlayer()).addResult("  * INVALID: ilegal support, unit actions becomes 'Hold': " + lm);
							continue; // for in j
						}						
						
						/* To check "support break" due to advances from provinces different of the supported one */
						// TODO
						
						/* Search for a TerritoryUnderMovement associated to the same province; if doesn't
						 * exits created a new one */
						TerritoryUnderMovement tum = null;
						for (Iterator<TerritoryUnderMovement> k = tums.iterator(); k.hasNext();) {
							TerritoryUnderMovement tumAux = k.next();
							if (tumAux.getTerritory().getName().equals(sp.getTerritory())) {
								tum = tumAux;
							}
						}
						if (tum == null) {
							tum = new TerritoryUnderMovement(sp.getTerritory(), m);
							tums.add(tum);
						}
						
						/* Add Unit to the supporting units list, along with the player in favor 
						 * of which the support action is being done */
						tum.addSupportingUnit(u,sp.getPlayer());
						
					}
					else {
						// This must not happen!
						throw new ProcessCommandsException("unknown action for player " + c.getPlayer() + ": " + a.toStringWithElite(m, c.getPlayer()));
					}
				}
				
			}
			
			/* Merging all the per-player GenericResult in the general Result objec */
			for (Iterator<String> j = rActions.keySet().iterator(); j.hasNext();) {
				r.addResult(rActions.get(j.next()));
			}
			
			Vector<String> retreatForbidden = new Vector<String>();
			HashMap<Unit, Vector<String>> retreatForbiddenPerUnit = new HashMap<Unit, Vector<String>>();
			Vector<Unit> mustRetreat = new Vector<Unit>();
			rResolv.addResult(processNoConflicts(tums, m, retreatForbidden));
			rResolv.addResult(processConflicts(tums, m, retreatForbidden, retreatForbiddenPerUnit, mustRetreat));
			rResolv.addResult(proccessRetreats(m, retreatForbidden, retreatForbiddenPerUnit, mustRetreat));
			rResolv.addResult(processSelfBlocks(tums, m));
		
			/* Remove control in provinces where an enemy (or autonomous) garrison is occupying the city
			 * and no unit is there, accordingly to: "If one player has a garrison unit in the city while 
			 * another player has a military unit in the city's province, no one controls the area"
			 * (rules section 4.3). 
			 */
			rResolv.addResult(m.removeControls());
						
			/* Check home country changes */
			// TODO

			r.addResult(rResolv);
			
			/* Check victory conditions */
			// TODO			
			
			/* Set the date of the new turn */
			gs.incCampaign();
			if (gs.getCampaign() == GameStatus.SPRING) {
				gs.incYear();
			}			
			
		}

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
		
		return r;
	}

	private static String processNoConflicts(Vector<TerritoryUnderMovement> tums, Map m, Vector<String> retreatForbidden) {
		/* We process elements in the TerritoryUnderMovement Vector that don't involve conflict: 
		 * 
		 * 1) only 1 member in the advancing units vector, no garrison conversion and no unit currently 
		 *   Occupying territory
		 * 2) 0 members in the advancing units vector and
		 *   a. garrison conversion and no unit currently occupying territory
		 *   b. other situation (e.g. a unit supporting a place where no other unit is advancing)
		 */
		
		boolean recursiveInvocation = false;
		String s = "";
		Vector<TerritoryUnderMovement> tumsRemoval = new Vector<TerritoryUnderMovement>();
		
		for (Iterator<TerritoryUnderMovement> j = tums.iterator(); j.hasNext();) {
			TerritoryUnderMovement tum = j.next();
			
			if (tum.getAdvancingUnits().size() == 1 && tum.getGarrisonConvertToArmy() == null 
					&& tum.getGarrisonConvertToFleet() == null && tum.getTerritory().getUnit() == null) {  // 1)
				
				Unit u = tum.getAdvancingUnits().firstElement();
				s = s + "- unit " + u + " advances from " + u.getLocation().getName() + " to " + tum.getTerritory().getName() + "\n";
				m.getTerritoryByName(u.getLocation().getName()).setUnit(null);
				m.getTerritoryByName(tum.getTerritory().getName()).setUnit(u);
				u.setLocation(tum.getTerritory());
				if (m.getTerritoryByName(tum.getTerritory().getName()) instanceof Province) {
					((Province)m.getTerritoryByName(tum.getTerritory().getName())).setController(u.getOwner());					
				}
				tumsRemoval.add(tum);
				retreatForbidden.add(tum.getTerritory().getName());
				recursiveInvocation = true;
			}
			else if (tum.getAdvancingUnits().size() == 0) { // 2.a)
				
				if (tum.getTerritory().getUnit() == null 
					&& (tum.getGarrisonConvertToArmy() != null || tum.getGarrisonConvertToFleet() != null) ) {
					
					/* Create the new unit */
					Unit u;
					if (tum.getGarrisonConvertToArmy() != null) {
						String player = tum.getGarrisonConvertToArmy().getOwner();
						int elite = tum.getGarrisonConvertToArmy().getElite();
						u = new Army(m.getFreeId(player, "Army"), player, (Province)tum.getTerritory(), elite);
						s = s + "- unit " + tum.getGarrisonConvertToArmy() + " in " + tum.getTerritory().getName() + " converts into " + u + "\n";
					}
					else { // tum.getCarrisonConvertToFleet() != null
						String player = tum.getGarrisonConvertToFleet().getOwner();
						int elite = tum.getGarrisonConvertToFleet().getElite();
						u = new Fleet(m.getFreeId(player, "Fleet"), player, (Province)tum.getTerritory(), elite);
						s = s + "- unit " + tum.getGarrisonConvertToFleet() + " in " + tum.getTerritory().getName() + " converts into " + u + "\n";					
					}
				
					((Province)m.getTerritoryByName(tum.getTerritory().getName())).getCity().setUnit(null);
					m.getTerritoryByName(tum.getTerritory().getName()).setUnit(u);
				}
				/* There is not an explicit clause for 2.b), it would be an empty 'else' in this place */
					
				tumsRemoval.add(tum);
				retreatForbidden.add(tum.getTerritory().getName());
				recursiveInvocation = true;
			}
		}
		
		/* Remove processed elements from tum. Note that we can not this inside the loop, because a
		 * ConcurrentModificationException will occur */
		for (Iterator<TerritoryUnderMovement> j = tumsRemoval.iterator(); j.hasNext();) {
			TerritoryUnderMovement tum = j.next();
			tums.remove(tum);
		}
		
		/* If at least one change has been done, invoke recursively the function */
		if (recursiveInvocation) {
			return s + processNoConflicts(tums, m, retreatForbidden);
		}
		
		return s;
	}
	
	private static String processConflicts(Vector<TerritoryUnderMovement> tums, Map m, Vector<String> retreatForbidden, HashMap<Unit,Vector<String>> retreatForbiddenPerUnit, Vector<Unit> mustRetreat) throws ProcessCommandsException {
		
		String s = "";
		Vector<TerritoryUnderMovement> tumsRemoval = new Vector<TerritoryUnderMovement>();
		
		for (Iterator<TerritoryUnderMovement> i = tums.iterator(); i.hasNext();) {
			
			TerritoryUnderMovement tum = i.next();
			HashMap<String,Integer> sides = tum.getSides();
			
			if (sides.size() == 1) {
				/* This is not actually a conflict, but a self-block that will be 
				 * processed in processSelfBlocks() */
				continue;
			}
			
			if (tum.getGarrisonConvertToArmy()!= null || tum.getGarrisonConvertToFleet()!=null ) {
				throw new ProcessCommandsException("at " + tum.getTerritory() + " conflict: converting units taking part in conflicts is not yet implemented");
			}
			
			/* Search the winner */
			String winnerPlayer = "";
			String sidesString = "( ";
			int winnerStrength = -1;
			boolean tie = false;
			for (Iterator<String> j = sides.keySet().iterator() ; j.hasNext() ; ) {
				String player = j.next();
				int strength = sides.get(player).intValue();
				sidesString = sidesString + player + "=" + strength + " ";
				if (strength > winnerStrength) {
					winnerPlayer = player;
					winnerStrength = strength;
					tie = false;
				}
				else if (strength == winnerStrength) {
					tie = true;
				}
			}
			sidesString = sidesString + ")";
			
			s = s + "- conflict at " + tum.getTerritory().getName() + " " + sidesString + ":\n";
			if (tie) {
				s = s + "  * tied at strength " + winnerStrength + "\n";
				s = s + "  * as result, nothing happens\n";
				retreatForbidden.add(tum.getTerritory().getName());
			}
			else {				
				s = s + "  * winner is " + winnerPlayer + " with strength " + winnerStrength + "\n";
				
				Unit winnerUnit = null;
				for (Iterator<Unit> j = tum.getAdvancingUnits().iterator(); j.hasNext(); ) {
					/* Search the advancing (or converting) unit belonging to the winning player */
					Unit u = j.next();
					if (u.getOwner().equals(winnerPlayer)) {
						winnerUnit = u;
						break;
					}
				}
				
				Unit u = tum.getTerritory().getUnit();
				if (winnerUnit == null) {
					/* This means that the winning unit belongs to the player that is holding/converting */
					s = s + "  * " + u + " at the place mantains its position\n";
				}				
				else { 
					
					/* If a not-winning unit is in the territory, make it Retreat */
					if  (tum.getTerritory().getUnit() != null) {
						s = s + "  * " + u + " at the place must retreat\n";
						mustRetreat.add(u);
						/* The place where the attacking unit came is also forbidden but only for that unit */
						if (retreatForbiddenPerUnit.get(u) == null) {
							retreatForbiddenPerUnit.put(u, new Vector<String>());
						}
					retreatForbiddenPerUnit.get(u).add(winnerUnit.getLocation().getName());					
					}
				
					/* Put the winning unit in place in the Territory, or doing nothing if the winning unit 
					 *was Holding */
					//FIXME: not implemented for converting units
					s = s + "  * unit " + winnerUnit + " advances from " + winnerUnit.getLocation().getName() + " to " + tum.getTerritory().getName() + "\n";
					m.getTerritoryByName(winnerUnit.getLocation().getName()).setUnit(null);
					m.getTerritoryByName(tum.getTerritory().getName()).setUnit(winnerUnit);
					winnerUnit.setLocation(tum.getTerritory());
					if (m.getTerritoryByName(tum.getTerritory().getName()) instanceof Province) {
						((Province)m.getTerritoryByName(tum.getTerritory().getName())).setController(winnerPlayer);					
					}
					retreatForbidden.add(tum.getTerritory().getName());
				}
			}
			tumsRemoval.add(tum);	
		}
		
		/* Remove processed elements from tum. Note that we can not this inside the loop, because a
		 * ConcurrentModificationException will occur */
		for (Iterator<TerritoryUnderMovement> j = tumsRemoval.iterator(); j.hasNext();) {
			TerritoryUnderMovement tum = j.next();
			tums.remove(tum);
		}		
		
		return s;
	}
	
	private static String processSelfBlocks(Vector<TerritoryUnderMovement> tums, Map m) {
				
		String s = "";
		
		for (Iterator<TerritoryUnderMovement> i = tums.iterator(); i.hasNext();) {
			TerritoryUnderMovement tum = i.next();
			
			Unit u1 = ((Unit)tum.getAdvancingUnits().get(0));
			Unit u2 = tum.getTerritory().getUnit();
			
			if (u2 != null) {
				s = s + "- unit " + u1 + " at " + u1.getLocation().getName() + " can not move to " + tum.getTerritory().getName() + " due to " + u2 + " (owned by the same player) is at that place\n";
			}
		}
		
		if (!s.isEmpty()) {
			s = "blocked movements:\n" + s;
		}
		
		return s;

	}
	
	private static String proccessRetreats(Map m, Vector<String> retreatForbidden, HashMap<Unit,Vector<String>> retreatForbiddenPerUnit, Vector<Unit> mustRetreat) {
		String s = "";
		
		for (Iterator<Unit> i = mustRetreat.iterator(); i.hasNext(); ) {
			Unit u = i.next();
			Vector<String> candidates = new Vector<String>();
			Vector<String> retreatForbiddenPerUnitVector = retreatForbiddenPerUnit.get(u);
			for (Iterator<String> j = u.getLocation().getAdjacents().iterator(); j.hasNext(); ) {
				String t = j.next();
				/* Check if the adjacency is a valid retreat place */
				if (retreatForbidden.indexOf(t) < 0
					&& (retreatForbiddenPerUnitVector != null && retreatForbiddenPerUnitVector.indexOf(t) < 0)
					&& (!(m.getTerritoryByName(t) instanceof Sea && u instanceof Army))
				    && m.getTerritoryByName(t).getUnit() != null) {
					candidates.add(t);
				}
			}
			
			if (candidates != null && candidates.size() != 0) {
				//FIXME: use Retreat preferred list, if available
				Random generator = new Random(System.currentTimeMillis());
				String dest = candidates.get(generator.nextInt(candidates.size()));
				m.getTerritoryByName(u.getLocation().getName()).setUnit(null);
				m.getTerritoryByName(dest).setUnit(u);
				u.setLocation(m.getTerritoryByName(dest));
				if (m.getTerritoryByName(dest) instanceof Province) {
					((Province)m.getTerritoryByName(dest)).setController(u.getOwner());
				}
				//FIXME: it seems to be a very improbable situation that two units overlap in their retirement zones */
				//retreatForbidden.add(tum.getTerritory().getName());
			}
			else {
				s = s + "- unit " + u + " has no legal place to retreat so it is DESTROYED\n";
				//FIXME: this is not needed, as the unit has been indirectly cleare
				//m.getTerritoryByName(u.getLocation().getName()).setUnit(null);
			}
		}
		
		if (!s.isEmpty()) {
			s = "retreats:\n" + s;
		}
		
		return s;
	}	
	
	/**
	 * Processes a set of Adjustments orders (supposed each one from a different player) on the
	 * map, in the context of a given game status. 
	 * @param m
	 * @return
	 * @throws ProcessAdjustmentsException 
	 */
	public static Result processAdjustments(GameStatus gs, Map m, Vector<Adjustments> adj) throws ProcessAdjustmentsException {
		
		GenericResult orderLog = new GenericResult();
		GenericResult r = new GenericResult();
		
		/* This process only can be done in spring, so other campaign will throw an Exception */
		if (gs.getCampaign() != GameStatus.SPRING) {
			throw new ProcessAdjustmentsException("wrong campaing " + gs.getCampaignString());
		}
		
		for (Iterator<Adjustments> i = adj.iterator(); i.hasNext() ; ) {
			Adjustments adr = i.next();
			orderLog.addResult("player " + adr.getPlayer() + " adjustment orders:");
			r.addResult("player " + adr.getPlayer() + " adjustment processing:");
			
			/* Gather all the units belonging to player */
			Vector<Unit> v = m.getUnitBelongingToPlayer(adr.getPlayer(),null);
			
			/* Process payments */
			for (Iterator<Payment> j = adr.getPayments().iterator(); j.hasNext(); ) {
				Payment p = j.next();
				String type = p.getType();
				int id = p.getId();
				
				orderLog.addResult("- " + p);
				
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
					r.addResult("- can not process payment order <"+p+">: player has no " + type.substring(0, 1) + id + " unit");
					continue;
				}
				
				/* Check money and pay */
				if (u.cost() > gs.getMoney(adr.getPlayer())) {
					r.addResult("- can not process payment order <"+p+">: not enough money");
				}
				else {
					/* Pay */
					gs.decMoney(adr.getPlayer(), u.cost());
					r.addResult("- payment: "+u+" (-"+u.cost()+"d)");
					
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
				
				orderLog.addResult("- " + p);				
				
				/* Is the unit type valid? */
				if (!(type.equals("Army")||type.equals("Fleet")||type.equals("Garrison"))) {
					r.addResult("- can not process purchase order <"+p+">: type " + type + " is not a valid unit type");
					continue;
				}
				
				/* The player has not reach unit limit (A < 12, F < 8, G < 6) */
				int freeId = m.getFreeId(adr.getPlayer(), type);
				if ( (type.equals("Army") && freeId == Army.MAX) || 
				     (type.equals("Fleet") && freeId == Fleet.MAX) ||
				     (type.equals("Garrison") && freeId == Garrison.MAX)
				   ) {
					r.addResult("- can not process purchase order <"+p+">: maximum units limit reached for " + type);
					continue;
				}
				
				/* The player has no more than one elite unit */
				if (p.getElite()!= Unit.NO_ELITE && m.getEliteUnitFromPlayer(adr.getPlayer()) != null) {
					r.addResult("- can not process purchase order <"+p+">: player already has a elite unit ("+m.getEliteUnitFromPlayer(adr.getPlayer())+")");
					continue;
				}				
				
				/* Is the province valid?
				 * a) Exists */
				if (pr == null) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " doesn't exists");
					continue;
				}
				/* a) Belong to player */
				if (pr.getController() == null || !pr.getController().equals(adr.getPlayer())) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " doesn't belong to player");
					continue;
				}
				
				/* b) Has no famine */
				if (pr.hasFamine()) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " has a Famine marker");
					continue;
				}
				
				/* c) Has a city (no matter if fortified or not, no matter city controller) */
				if (pr.getCity() == null) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " has no city");
					continue;
				}
				
				/* d) For fleets, the city has port */
				if (type.equals("Fleet") && !pr.getCity().isPort()) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " has no city");
					continue;
				}
				
				/* e) For Garrisons, the city has to be fortified */
				if (type.equals("Garrison") && !pr.getCity().isFortified()) {
					r.addResult("- can not process purchase order <"+p+">: Province " +p.getProvince()+ " has a city, but it is not fortified");
					continue;
				}
				
				/* f) No unpaid unit was removed in the province in the same adjustment phase */
				if (vp.contains(pr)) {
					r.addResult("- can not process purchase order <"+p+">: a unit was unpayed in the same province this turn, " +p.getProvince());
					continue;
				}
				
				/* For Army/Fleet, no other Army/Fleet is in the same province */
				if ( (type.equals("Army") || type.equals("Fleet")) && pr.getUnit() != null) {
					r.addResult("- can not process purchase order <"+p+">: existing unit in " +p.getProvince() + ", " + pr.getUnit());
					continue;
				}
				
				/* For Garrison, no other Garrison is in the same city */
				if ( type.equals("Garrison") && pr.getCity().getUnit() != null) {
					r.addResult("- can not process purchase order <"+p+">: existing unit in city in " +p.getProvince() + ", " + pr.getCity().getUnit());
					continue;
				}
				
				/* Pre-create the Unit (note that creation could fail beyond this point) */
				Unit u;
				if (type.equals("Army")) {
					u = new Army(m.getFreeId(adr.getPlayer(), type), adr.getPlayer(), null, p.getElite());
				}
				else if (type.equals("Fleet")) {
					u = new Fleet(m.getFreeId(adr.getPlayer(), type), adr.getPlayer(), null, p.getElite());
				}
				else { // garrison
					u = new Garrison(m.getFreeId(adr.getPlayer(), type), adr.getPlayer(), (City)null, p.getElite());
				}
				
				/* Check money and buy */
				if (u.cost() > gs.getMoney(adr.getPlayer())) {
					r.addResult("- can not process buy order <"+p+">: not enough money");
				}
				else {
					/* Pay */
					gs.decMoney(adr.getPlayer(), u.cost());
					r.addResult("- purchase: "+u+" at "+ pr.getName() +" (-"+u.cost()+"d)");
					
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
		
		ComposedResult cr = new ComposedResult();
		cr.addResult(orderLog);
		cr.addResult(r);
		
		return cr;
	}
	
	private static Result doFamine(Map m) {
		
		GenericResult r = new GenericResult();
		
		/* Get provinces with famine */		
		int d1, d2, d3, d4, koy;
		Vector<String> v;
		
		d1 = RandomProcesses.roll();
		koy = RandomProcesses.kindOfYear(d1);
		String prefix = "kind of year ("+d1+"): " + RandomProcesses.kindOfYear2String(koy);
		
		d1 = RandomProcesses.roll();
		d2 = RandomProcesses.roll();
		d3 = RandomProcesses.roll();
		d4 = RandomProcesses.roll();
		v = RandomProcesses.famineResult(koy,d1+d2,d3+d4); 
		switch (koy) {
		case RandomProcesses.ONLY_ROW:
			r.addResult(prefix + ", row="+d1+"+"+d2);
			break;
		case RandomProcesses.ONLY_COLUMN:
			r.addResult(prefix + ", column="+d1+"+"+d2);
			break;
		case RandomProcesses.ROW_AND_COLUMN:
			r.addResult(prefix + ", row="+d1+"+"+d2+", column="+d3+"+"+d4);
			break;
		default: // NO DISASTER
			r.addResult(prefix + ", no famine");
			break;
		}
		
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
		int d1, d2, d3, d4, koy;
		Vector<String> v;
		
		d1 = RandomProcesses.roll();
		koy = RandomProcesses.kindOfYear(d1);
		String prefix = "kind of year ("+d1+"): " + RandomProcesses.kindOfYear2String(koy);
		
		d1 = RandomProcesses.roll();
		d2 = RandomProcesses.roll();
		d3 = RandomProcesses.roll();
		d4 = RandomProcesses.roll();
		v = RandomProcesses.plagueResult(koy,d1+d2,d3+d4); 
		switch (koy) {
		case RandomProcesses.ONLY_ROW:
			r.addResult(prefix + ", row="+d1+"+"+d2);
			break;
		case RandomProcesses.ONLY_COLUMN:
			r.addResult(prefix + ", column="+d1+"+"+d2);
			break;
		case RandomProcesses.ROW_AND_COLUMN:
			r.addResult(prefix + ", row="+d1+"+"+d2+", column="+d3+"+"+d4);
			break;
		default: // NO DISASTER
			r.addResult(prefix + ", no plague");
			break;
		}
		
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
			
			r.addResult("player "+player+" gets: ");
			
			/* Fixed amount */
			int fixed = m.calculateIncome(player);
			r.addResult("   fixed: " + fixed + "d");
			
			/* Variable, rolling dices */
			int variable = 0;
			for (int j = 0; j < gs.getIncomeRolls(player); j++) {
				int d = RandomProcesses.roll();
				int partialVariable = RandomProcesses.randomIncome(player,d);
				r.addResult("   variable at home (rolling "+d+"): " + partialVariable + "d");
				variable += partialVariable;
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
					int d = RandomProcesses.roll();
					int partialVariable = RandomProcesses.randomIncome("Genoa",d);
					r.addResult("   variable as Genoa controller (rolling "+d+"): " + partialVariable + "d");
					variable += partialVariable;					
				}
			}
			
			r.addResult("   TOTAL: "+(fixed+variable)+"d");
			
			/* Update money */
			gs.incMoney(player, fixed + variable);
		}
		
		return r;
	}

}
