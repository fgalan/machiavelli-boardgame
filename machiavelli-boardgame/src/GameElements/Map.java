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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Exceptions.MapCoherenceException;
import Exceptions.ParseMapException;
import GameEngine.Transportation;

public class Map {
	
	private final static Logger log = Logger.getLogger("Map.class");
	
	private HashMap<String, Territory> territories;
	private Vector<String> players;

	/**
	 * Class constructor
	 * @param xmlString an XML representation of the Map to build
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParseMapException 
	 * @throws DOMException 
	 * @throws MapCoherenceException 
	 */
	public Map(File f) throws ParserConfigurationException, IOException, SAXException, DOMException, ParseMapException, MapCoherenceException {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		territories = new HashMap<String, Territory>();
		HashMap<String,String> playersTemp = new HashMap<String, String>();
		
		NodeList l = doc.getElementsByTagName("Territory");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			Territory t;
									
			/* Province or sea? */
			if (at.getNamedItem("type").getNodeValue().equals("Province")) {
				t = new Province(at.getNamedItem("name").getNodeValue());
			}
			else if (at.getNamedItem("type").getNodeValue().equals("Sea")) {
				t = new Sea(at.getNamedItem("name").getNodeValue());
			}
			else {
				throw new ParseMapException("Territory type "+at.getNamedItem("type").getNodeValue()+" is not valid");
			}
			
			if (t instanceof Province) {
				
				/* Look for controller */
				String ctl = parseController(l.item(i));
				((Province)t).setController(ctl);
				if (ctl != null) {
					/* We are using the players hashmap just to mark the existing players, so the 
					 * value part is not meaningful and we are just replicating the string name */
					playersTemp.put(ctl, ctl);
				}
				
				/* Look for a city */
				City c = parseCity(l.item(i), (Province)t);
				((Province)t).setCity(c);
				
				/* Look for famine */
				if (parseFamine(l.item(i))) {
					((Province)t).setFamine();
				}
				else {
					((Province)t).clearFamine();
				}
				
				/* Look for rebellion */
				((Province)t).setRebellion(parseRebellion(l.item(i)));
			}
						
			/* Add adjacent territories */
			t.setAdjacents(parseAdjacents(l.item(i)));
			
			/* Look for unit */
			t.setUnit(parseUnit(l.item(i),t));
			
			/* Store the Territory in the hashmap */
			territories.put(t.getName(), t);

		}

		/* Make the players vector */
		players = new Vector<String>(playersTemp.keySet());
		Collections.sort(players);
		
		/* Check map consistency */
		String s = checkInconsistencies();
		if (s != null) {
			throw new MapCoherenceException(s);
		}
	}
	
	/**
	 * @param item XML element representing a Territory
	 * @param t territory where the Unit is located
	 * @return the Unit controlling the Territory, null if the Territory is empty
	 * @throws ParseMapException
	 */
	private Unit parseUnit(Node item, Territory t) throws ParseMapException {
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Unit")) {
				
				NamedNodeMap at = l.item(i).getAttributes();
				
				/* name, type and owner are mandatory */				
				int id = Integer.parseInt(at.getNamedItem("id").getNodeValue());
				String type = at.getNamedItem("type").getNodeValue();
				String owner = at.getNamedItem("owner").getNodeValue();
				
				/* elite is optional */
				int elite = Unit.NO_ELITE;
				if (at.getNamedItem("elite")!=null) {
					elite = Integer.parseInt(at.getNamedItem("elite").getNodeValue());
				}
				
				if (type.equals("Army")) {
					return new Army(id, owner, (Province)t, elite);
				}
				else if (type.equals("Fleet")) {
					return new Fleet(id, owner, t, elite);
				}
				else if (type.equals("Garrison")) {
					/* We do nothing in this case, because Garrison units are processed
					 * as part of parseCity method */
				}
				else {
					throw new ParseMapException("unknown Unit type: " + type);
				}
				
			}
		}
		
		/* No unit found */
		return null;

	}

	/**
	 * @param item XML element representing a Territory
	 * @return the Vector of adjacent Territories
	 */
	private Vector<String> parseAdjacents(Node item) {
		
		Vector<String> v = new Vector<String>();
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Adj")) {
				v.add(l.item(i).getTextContent());
			}
		}
		return v;
	}

	/**
	 * @param item XML element representing a Province
	 * @return the player against the Rebellion has been produced, null if there is no rebellion 
	 */
	private String parseRebellion(Node item) {
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Rebellion")) {
				NamedNodeMap at = l.item(i).getAttributes();
				return at.getNamedItem("againts").getNodeValue();				
			}
		}
		return null;
	}

	/**
	 * @param item XML element representing a Province 
	 * @return true if Province is under rebellion, false otherwise
	 */
	private boolean parseFamine(Node item) {
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Famine")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param item XML element representing a Province
	 * @param the Province object
	 * @return the City object corresponding to the Province or nothing 
	 */
	private City parseCity(Node item, Province p) {
		
		NodeList l = item.getChildNodes();
		City c = null;
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("City")) {
				
				NamedNodeMap at = l.item(i).getAttributes();
				
				/* size attribute is mandatory */				
				int size = Integer.parseInt(at.getNamedItem("size").getNodeValue());
				
				/* fortified attribute is optional */
				boolean fortified;
				if (at.getNamedItem("fortified")!=null && at.getNamedItem("fortified").getNodeValue().equals("yes")) {
					fortified = true;
				}
				else {
					fortified = false;
				}
				
				/* port attribute is optional */
				boolean port;
				if (at.getNamedItem("port")!=null && at.getNamedItem("port").getNodeValue().equals("yes")) {
					port = true;
				}
				else {
					port = false;
				}
				
				/* neutral garrison is optional */
				boolean ng = false;
				for (int j = 0; j < l.getLength(); j++) {
					if (l.item(j).getNodeName().equals("AutonomousGarrison")) {
						ng = true;
						break;
					}
				}
				
				c = new City(p, size, fortified, port, ng);
				
				break;
			}
		}
		
		/* check for Garrison Units */
		if (c != null) {
			for (int i = 0; i < l.getLength(); i++) {
				if (l.item(i).getNodeName().equals("Unit")) {
					
					NamedNodeMap at = l.item(i).getAttributes();
					if (at.getNamedItem("type").getNodeValue().equals("Garrison")) {
						
						/* name and owner are mandatory */				
						int id = Integer.parseInt(at.getNamedItem("id").getNodeValue());
						String owner = at.getNamedItem("owner").getNodeValue();
						
						/* elite is optional */
						int elite = Unit.NO_ELITE;
						if (at.getNamedItem("elite")!=null) {
							elite = Integer.parseInt(at.getNamedItem("elite").getNodeValue());
						}
						
						Unit u = new Garrison(id, owner, c, elite);
						c.setUnit(u);
						break;
						
					}
				}
			}
		}
	
		return c;

	}

	/**
	 * @param item XML element representing a Territory
	 * @return the controller name or null if the Territory is controlled by no player
	 */
	private String parseController(Node item) {
		NamedNodeMap at = item.getAttributes();
								
		/* Province or sea? */
		if (at.getNamedItem("controller") != null) {
			return at.getNamedItem("controller").getNodeValue();
		}
		else {
			return null;
		}
	}

	/**
	 * @return a text-based map render
	 */
	public String toString() {
		String s = "";
		
		/* Check map consistency */
		String ss = checkInconsistencies();
		if (ss != null) {
			return "Inconsistent map: " + ss;
		}
		
		/* PLAYERS */
		
		s = s + "PLAYERS:\n";
		s = s + "   ";
		
		for (Iterator<String> i = players.iterator(); i.hasNext(); ) {
			s = s + i.next() + ", ";
		}
		s = s + "\n\n";
		
		/* CONTROLLED PROVINCES */
		
		/* Using a hashmap to index list by controlling player */
		HashMap<String,String> controlledProvinces = new HashMap<String,String>();

		/* Process territories one by one (in alphabetic order)*/
		Vector<String> l = new Vector<String>(territories.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			
			if (t instanceof Province) {
				
				/* The "code" is as follows:
				 * 
				 *   Swiss      -> the player controls the province and the unfortified city (if any)
				 *   Turin(+)   -> the player controls the province *and* the fortified city
				 *   Turin(-)   -> the player controls the province *but not* the fortified city (which is controlled
				 *                 by another player with a Garrison unit there or has an AutonomousGarrison)
				 *   Turin(c)   -> the player controls the city *but not* the province
				 *   
				 * Note that Seas have no controller by definition */
			 
				String provinceName = ((Province)t).getName();
				
				if (((Province)t).getController()!=null) {
					String provinceController = ((Province)t).getController();
				
					/* has city? */
					City c = ((Province)t).getCity();
					if (c == null) {
						appendToHashMap(controlledProvinces, provinceController, provinceName);
					}
					/* is fortified? */
					else if (c.isFortified()) {
					
						/* has autonomous garrison? */
						if (c.hasAutonomousGarrison()) {
							appendToHashMap(controlledProvinces, provinceController, provinceName + "(-)");
						}
						/* has occupying garrison? */
						else if (c.getUnit()!=null) {
							String cityController = c.getUnit().getOwner();
						
							/* garrison owner is the same than province controller? */
							if (cityController.equals(provinceController)) {
								appendToHashMap(controlledProvinces, provinceController, provinceName + "(+)");
							}
							/* is not the same */
							else {
								appendToHashMap(controlledProvinces, provinceController, provinceName + "(-)");
								appendToHashMap(controlledProvinces, cityController, provinceName + "(c)");								
							}
						}
						else {
							/* no garrison, so city belongs to the province controller */
							appendToHashMap(controlledProvinces, provinceController, provinceName + "(+)");
						}
					}
					else { // city but not fortified
						appendToHashMap(controlledProvinces, provinceController, provinceName);
					}
				}
				/* Even in the case the province has no controller (i.e. controller null), the city could have a controller */
				else if (((Province)t).getCity() != null && ((Province)t).getCity().getUnit()!=null) {
					String cityController = ((Province)t).getCity().getUnit().getOwner();
					appendToHashMap(controlledProvinces, cityController, provinceName + "(c)");
				}
			}
		}
			
		s = s + "CONTROLLED PROVINCES:\n";
		l = new Vector<String>(controlledProvinces.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			String player = i.next();
			s = s + "   " + player + ":\n      ";
			s = s + controlledProvinces.get(player);
			s = s + "\n";
		}
		
		s = s + " Code:\n";
		s = s + "   Swiss      -> the player controls the province and the unfortified city (if any)\n";
		s = s + "   Turin(+)   -> the player controls the province *and* the fortified city\n";
		s = s + "   Turin(-)   -> the player controls the province *but not* the fortified city (which is controlled\n";
		s = s + "                 by another player with a Garrison unit there or has an AutonomousGarrison)\n";
		s = s + "   Turin(c)   -> the player controls the city *but not* the province\n";
		s = s + "\n";
		s = s + "   (Note that Seas have no controller by definition)\n";
		s = s + "\n";
		
		/* MILITARY UNITS */
		
		/* Using a hashmap to index list by owing player and simple String for autonomous garrisons*/
		HashMap<String,String> unitsInMap = new HashMap<String,String>();
		String ags = "";
		
		/* Process territories one by one (in alphabetic order)*/
		l = new Vector<String>(territories.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t.getUnit()!=null) {
				
				String u = t.getUnit() + " in " + t.getName(); 
				appendToHashMap(unitsInMap, t.getUnit().getOwner(), u);
			}
			/* Look for unit at city */
			if (t instanceof Province && ((Province)t).getCity()!=null && ((Province)t).getCity().isFortified()) {
				if (((Province)t).getCity().getUnit() != null) {
					String u = ((Province)t).getCity().getUnit() + " in " + t.getName();
					appendToHashMap(unitsInMap, ((Province)t).getCity().getUnit().getOwner(), u);
				}
				if (((Province)t).getCity().hasAutonomousGarrison()) {
					ags = ags + t.getName() + "\n      ";
				}
			}
		}
		
		s = s + "MILLITARY UNITS:\n";
		l = new Vector<String>(unitsInMap.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			String player = i.next();
			s = s + "   " + player + ":\n      ";
			s = s + unitsInMap.get(player);
			s = s + "\n";
		}
		s = s + "   Autonomous Garrisons:\n      " + ags + "\n";
		
		/* TEMPORARY MARKERS */
		String famine = "";
		String rebellion = "";
		String siege = "";
		/* Process territories one by one (in alphabetic order)*/
		l = new Vector<String>(territories.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province) {
				if (((Province)t).getRebellion()!=null) {
					rebellion = rebellion + "   rebellion in " + t.getName() + " againts " + ((Province)t).getRebellion() + "\n"; 
				}
				if (((Province)t).hasFamine()) {
					famine = famine + "   famine in " + t.getName() + "\n";
				}
				if ( (((Province)t).getCity() != null) && ((Province)t).getCity().isUnderSiege()) {
					siege = siege + "   city in "+t.getName()+" is under siege by "+t.getUnit()+"\n";
				}
			}
		}	
		
		s = s + "TEMPORARY MARKERS:\n";
		if (famine.isEmpty() && rebellion.isEmpty() && siege.isEmpty()) {
			s = s + "   none\n";
		}
		else {
			s = s + famine + rebellion + siege;
		}
		
		return s;
	}
	
	/**
	 * Helper method, to factorize code in toString()
	 * @param h
	 * @param key
	 * @param s
	 */
	private void appendToHashMap(HashMap<String,String> h, String key, String s) {
		if (h.get(key)!=null) {
			h.put(key, h.get(key) + s + "\n      ");
		}
		else {
			h.put(key, s + "\n      ");
		}
	}
	
	/**
	 * @return an XML-based map render
	 * @throws MapCoherenceException 
	 */
	public String toXml() throws MapCoherenceException {
		
		/* We could use DOM to render the XML, but we prefer using text directly so we can
		 * have complete control (e.g. pretty-printing format)
		 */
		String s = "<?xml version='1.0' encoding='UTF-8'?>\n";
		s = s + "<Map>\n";
		
		Vector<String> l = new Vector<String>(territories.keySet());
		Collections.sort(l);
		
		/* Process territories one by one */
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			
			String territoryType;
			String controllerString ="";
			if (t instanceof Province) {
				territoryType = "Province";
				if (((Province)t).getController() != null) {
					controllerString = " controller='"+((Province)t).getController()+"'";
				}
			}
			else {
				territoryType = "Sea";
			}
						
			s = s + "   <Territory name='"+t.getName()+"' type='"+territoryType+"'"+controllerString+">\n";
			
			/* Generate adjacent territories elements*/
			for (Iterator<String> l2 = t.getAdjacents().iterator(); l2.hasNext() ; ) {
				s = s + "      <Adj>"+l2.next()+"</Adj>\n";
			}
			
			/* Generate city */
			if (territoryType.equals("Province") && ((Province)t).getCity()!=null) {
				City c = ((Province)t).getCity(); 
				
				String fortifiedString = "";
				if (c.isFortified()) {
					fortifiedString = " fortified='yes'";
				}
				String portString = "";
				if (c.isPort()) {
					portString = " port='yes'";
				}
				s = s + "      <City size='"+c.getSize()+"'"+fortifiedString + portString + "/>\n";
					
				if (c.hasAutonomousGarrison()) {
					s = s + "      <AutonomousGarrison/>\n";
				}

			}
			
			/* Generate unit (Army or Fleet) */
			if (t.getUnit()!=null) {
				int id = t.getUnit().getId();
				String owner = t.getUnit().getOwner();
				String eliteString = "";
				if (t.getUnit().getElite() != 0) {
					eliteString =" elite='"+t.getUnit().getElite()+"'";
				}
					
				if (t.getUnit() instanceof Army) {
					s = s + "      <Unit id='"+id+"' type='Army' owner='"+owner+"'"+eliteString+"/>\n";
				}
				else if (t.getUnit() instanceof Fleet) {
					s = s + "      <Unit id='"+id+"' type='Fleet' owner='"+owner+"'"+eliteString+"/>\n";
				}
				else { 
					/* Note that Garrison are forbidden as units for Territories (they are for cities) */
					throw new MapCoherenceException("rendering "+t.getName()+", unit type "+t.getUnit().getClass()+" is unkown or fobidden");
				}
			}
			
			/* Generate unit (Garrison) */
			if (t instanceof Province && ((Province)t).getCity()!=null && ((Province)t).getCity().getUnit()!=null) {
				int id = ((Province)t).getCity().getUnit().getId();
				String owner = ((Province)t).getCity().getUnit().getOwner();
				
				String eliteString = "";
				if (((Province)t).getCity().getUnit().getElite() != 0) {
					eliteString =" elite='"+((Province)t).getCity().getUnit().getElite()+"'";
				}
					
				s = s + "      <Unit id='"+id+"' type='Garrison' owner='"+owner+"'"+eliteString+"/>\n";
				
			}
			
			
			/* Generate famine */
			if (territoryType.equals("Province") && ((Province)t).hasFamine()) {
				s = s + "      <Famine/>\n";
			}
			
			/* Generate rebellion */
			if (territoryType.equals("Province") && ((Province)t).getRebellion()!=null) {
				s = s + "      <Rebellion againts='"+((Province)t).getRebellion()+"'/>\n";
			}
			
			s = s + "   </Territory>\n";
			
		}
		s = s + "</Map>\n";
		
		return s;
	}
	
	/**
	 * @param p a Player
	 * @return the income for Player p based on the map
	 */
	public int calculateIncome(String p) {
		
		/* Special rule: Venice generates income as province *and* city or only as province *or* as city.
		 * So:
		 * - no unit at the province: Venice generates as City for its controller (if any)
		 * - unit (Army, Fleet) at the province: Venice generate as province for its controller
		 */
		
		boolean veniceIncomeAsCity = true;
		if (territories.get("Venice").getUnit() != null) {
			veniceIncomeAsCity = false;
		}
		
		int income = 0;
		
		/* Process territories one by one */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province) {
				/* Provinces ... */
				if (!t.getName().equals("Venice") || !veniceIncomeAsCity) { 
					if (((Province)t).getController() != null && ((Province)t).getController().equals(p)) {
						/* Province only produces money if there is no Famine or Rebellion */
						if (!((Province)t).hasFamine() && ((Province)t).getRebellion() == null) {
							income++;
						}
					}
				}
				
				/* ... and cities */
				if (!t.getName().equals("Venice") || veniceIncomeAsCity) {
					City c = ((Province)t).getCity(); 
					if (c != null) {
						if (c.getUnit() != null) {
							/* garrison at the city, so if the garrisons belong to the player and the
							 * city is not under siege, then the player gets the money, *no matter* if
							 * famine or rebellion */
							if (c.getUnit().getOwner().equals(p) && !c.isUnderSiege()) {
								income += c.getSize();
							}
						}
						else {
							/* no garrison, so if the player controls the province and there is no autonomous garrison
							 * the player gets the money, *except* if famine or rebellion */
							if (((Province)t).getController() != null && ((Province)t).getController().equals(p) && !c.hasAutonomousGarrison()) {
								if (!((Province)t).hasFamine() && ((Province)t).getRebellion() == null) { 
									income += c.getSize();
								}
							}
						}
					}
				}
			}
			else {
				/* Seas */
				if (t.getUnit()!=null && t.getUnit().getOwner().equals(p)) {
					income ++;
				}
			}
		}
		return income;

	}

	
	/**
	 * @return a list of inconsistencies, null if no inconsistency is found
	 */
	private String checkInconsistencies() {
		String s = checkAdjacencies() + checkControlConsistency();
		if (s.equals("")) {
			return null;
		}
		else {
			return s;
		}
	}
	
	/**
	 * @return a list of adjacency inconsistencies, e.g.
	 * Territory A in B's adjacency list, but B is not in A's adjacency list, "" if no
	 * inconsistencies are found
	 */
	private String checkAdjacencies() {
		
		String s = "";
		
		/* Process territories one by one */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			
			/* Process adjacent territories */
			for (Iterator<String> l = t.getAdjacents().iterator(); l.hasNext() ; ) {
				String adj = l.next();
				boolean found = false;
				for (Iterator<String> m = territories.keySet().iterator(); m.hasNext() ; ) {
					/* Match names? */
					if (m.next().equals(adj)) {
						found = true;
						/* Search for dual adjacency */
						Territory t2 = territories.get(adj);
						if (!t2.getAdjacents().contains(t.getName())) {
							s = s + "province " + t.getName() + " defines adjacent " + adj + ", but " +t.getName()+ " is not defined as adjacent of " +adj+"\n";
							break;
						}
					}					
				}
				if (!found) {
					/* The adjacent territory name was not found in the map */
					s = s + "province " + t.getName() + " defines adjacent " + adj + " which doesn't exists in the map\n";
				}
				 
			}
			
		}
		return s;
	}
	
	/**
	 * @return a list of control inconsistencies, e.g. Army in a province belong to country X, but
	 * province controller is Y, "" if no inconsistencies are found
	 */
	private String checkControlConsistency() {
		String s = "";
		
		/* Process territories one by one */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province && t.getUnit()!=null && ((Province)t).getController()!=null) {
				/* Compare Unit's and Territory's controller */
				if (!t.getUnit().getOwner().equals(((Province)t).getController())) {
					s = s + "unit in province "+t.getName()+" is own by "+t.getUnit().getOwner()+", but province's controller is "+((Province)t).getController()+"\n";
				}
			}
				
			/* Match rebellion country with list of available countries */
			if (t instanceof Province && ((Province)t).getRebellion()!= null && !players.contains(((Province)t).getRebellion())) {
				s = s + "rebellion in province "+t.getName()+" is declared againts "+((Province)t).getRebellion()+", which is not defined as any province controller\n";
			}
					
			/* Check there aren't two Army and/or Fleets in the same territory */
			// TODO
			
			/* Check there aren't two Garrison Unit in the same province */
			// TODO
			
			/* Check there is no Garrison Unit declared in a province where a city with autonomous garrison exists */
			// TODO

		}
		return s;
	}
	
	public Territory getTerritoryByName(String n) {
		if (territories.containsKey(n)) {
			return territories.get(n);
		}
		else {
			return null;
		}
	}
	
	public Vector<Province> getProvincesWithFamine() {
		Vector<Province> v = new Vector<Province>();
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province && ((Province)t).hasFamine()) {
				v.add((Province)t);
			}
		}
		return v;
	}
	
	/**
	 * @param player
	 * @param type to return units of a given type, if null, returning all units
	 * @return
	 */
	public Vector<Unit> getUnitBelongingToPlayer(String player, String type) {
		Vector<Unit> v = new Vector<Unit>();
		
		/* Search all territories */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext(); ) {
			Territory t = territories.get(i.next());
			
			/* Check unit in the territory */
			if (t.getUnit() != null) {
				if (t.getUnit().getOwner().equals(player)) {
					if (t.getUnit() instanceof Army) {
						if (type == null || type.equals("Army")) {
							v.add(t.getUnit());
						}
					}
					else { // Fleet
						if (type == null || type.equals("Fleet")) {
							v.add(t.getUnit());
						}
					}				
				}
			}
			/* Check unit in the territory city (only for Provinces) */
			if (t instanceof Province && ((Province)t).getCity() != null && ((Province)t).getCity().isFortified() && ((Province)t).getCity().getUnit() != null) {
				if (((Province)t).getCity().getUnit().getOwner().equals(player)) {
					if (type == null || type.equals("Garrison")) {
						v.add(((Province)t).getCity().getUnit());
					}					
				}
			}
		}
		
		return v;
	}
	
	/**
	 * Return the elite unit belonging to player, null if the player has no elite unit. Note
	 * that according the rules each player can have a maximum of *one* elite unit (no matter the
	 * type) in any given moment
	 * @param p, player
	 * @return Unit
	 */
	public Unit getEliteUnitFromPlayer(String p) {
		/* Search all units */
		for (Iterator<Unit> i = getUnitBelongingToPlayer(p, null).iterator(); i.hasNext(); ) {
			Unit u = i.next();
			if (u.getElite() != Unit.NO_ELITE) {
				return u;
			}
		}
		return null;
	}
	
	/**
	 * @param player
	 * @param type
	 * @return the first free identifier, or 'max' if no free identifier is found
	 */
	public int getFreeId(String player, String type) {
				
		Vector<Unit> v;

		/* Search for the particular unit type */
		int max;
		if (type.equals("Army")) {
			max = Army.MAX;
			v = getUnitBelongingToPlayer(player, "Army"); 
		}
		else if (type.equals("Fleet")) {
			max = Fleet.MAX;
			v = getUnitBelongingToPlayer(player, "Fleet");
		}
		else { // Garrison
			max = Garrison.MAX;
			 v = getUnitBelongingToPlayer(player, "Garrison");
		}
		
		/* Search from 1 to max, returning the first free one */
		int free = -1;
		for (int i = 1; i < max; i++) {
			free = i;
			for (Iterator<Unit> j = v.iterator(); j.hasNext(); ) {
				Unit u = j.next();
				if (u.getId() == i) {
					free = max;
					break; // j
				}
			}
			if (free == i) {
				break; // i
			}
		}
		return free;
	}
	
	public Vector<City> getCitiesBelongingToPlayer(String p) {
		
		Vector<City> v = new Vector<City>();
		
		/* Search all territories */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext(); ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province && ((Province)t).getCity() != null && !((Province)t).getCity().hasAutonomousGarrison()) {
				if ( ((Province)t).getCity().getUnit() != null ) {
					/* If Garrison at the city, then the Garrison owner is the city controller */
					if ( ((Province)t).getCity().getUnit().getOwner().equals(p)) {
						v.add(((Province)t).getCity());
					}
				}
				else {
					/* If not Garrison at the city, then the Province controller (if any) is the city controller */
					if ( ((Province)t).getController() != null && ((Province)t).getController().equals(p)) {
						v.add(((Province)t).getCity());
					}					
				}
			}
		}
		
		return v;
	}
	
	/**
	 * Check that a movement action (Advance or Support) for Unit u to Territory t is legal according rules. 
	 * Return "" if the movement is legal or a String describing the problem otherwise
	 * @param u the Unit to move
	 * @param t the destination Territory
	 * @param tr a Transportation hashmap used to extend adjacenties (if null, no adjacenties extensions are used)
	 * @return
	 */
	public String isLegalMove(Unit u, Territory t, HashMap<String,Transportation> tr) {
		/* Garrison */
		if (u instanceof Garrison) {
			Garrison g = (Garrison) u;
			/* For Garrisons the only legal move (in Support actions) is the province where the city
			 * Garrison is located */
			if (!g.getLocation().getName().equals(t.getName())) {
				return "Garrison in " + g.getLocation().getName() + " to territory not holding its city: " + t.getName();
			}

		}
		/* Army */
		else if (u instanceof Army) {
			Army a = (Army) u;
			/* Is adjacent? */
			if (! areAdjacent(a.getLocation(), t, tr, a)) {
				return "Army in " + a.getLocation().getName() + " to not adjacent territory: " + t.getName();
			}
			
			/* Is a province? (i.e. not a sea) */
			if (t instanceof Sea) {
				return "Army in " + a.getLocation().getName() + " to a Sea: " + t.getName();
			}
			
			/* Is not occupied by a Fleet/Army of the same player? */
			//FIXME: this is only a problem when the unit in that place is not moving or has been blocked by
			//a conflict
			//if ((t.getUnit()!= null) && (t.getUnit().getOwner().equals(a.getOwner()))) {
			//	return "Army in " + a.getLocation().getName() + " to territory occupied by the same player: " + t.getName();
			//}
			
		}
		/* Fleet */
		else {
			Fleet f = (Fleet) u;
			/* Is adjacent? */
			if (! areAdjacent(f.getLocation(), t)) {
				return "Fleet in " + f.getLocation().getName() + " to not adjacent territory: " + t.getName();
			}
			
			/* Is a sea or coastal province sharing a coastal line? */
			// TODO: the sharing of a coastal line is not checked, so e.g. Capua -> Aquila (ilegal 
			// according rules) is not detected as illegal movement			
			if (t instanceof Province && !((Province)t).isCoast(this) ) {
				return "Fleet in " + f.getLocation().getName() + " to inland province: " + t.getName();
			}

			/* Is not occupied by a Fleet/Army of the same player? */
			//FIXME: this is only a problem when the unit in that place is not moving or has been blocked by
			//a conflict			
			//if ((t.getUnit()!= null) && (t.getUnit().getOwner().equals(f.getOwner()))) {
			//	return "Fleet in " + f.getLocation().getName() + " to territory occupied by the same player: " + t.getName();
			//}
		}
		
		return "";
	}
	
	/**
	 * @param tOrig a Territory (where the Army is, in the case of using transportation extension)
	 * @param tDest a Territory
	 * @param tr a Transportation hashmap used to extend adjacenties (if null, no adjacenties extensions are used)
	 * @param a Army (needed if transportation is used)
	 * @return
	 */
	public boolean areAdjacent(Territory tOrig, Territory tDest, HashMap<String,Transportation> tr, Army a) {
		Vector<String> extraAdjacencies = new Vector<String>(); 
		if (tr != null) {
			/* Add new elements to the Vector, based on Transportation */
			//FIXME: implement the 1-hop transport, enrich with a general (probably recursive) procedure
			for (Iterator<String> i = tOrig.getAdjacents().iterator(); i.hasNext(); ) {
				String s = i.next();
				Territory t1 = getTerritoryByName(s);
				/* If territory is a Sea and there is a transportation action in that place associated
				 * to the Unit, then add their not-Sea adjacents to the Vector */
				if (t1 instanceof Sea && tr.get(t1.getName()) != null 
						&& tr.get(t1.getName()).getArmyId() == a.getId()
						&& tr.get(t1.getName()).getPlayer().equals(a.getOwner())) {
					for (Iterator<String> j = t1.getAdjacents().iterator(); j.hasNext(); ) {
						Territory t2 = getTerritoryByName(j.next());
						if (t2 instanceof Province) {
							// FIXME (check): not sure if adding elements to a Vector "open" in the i-loop
							// is right
							extraAdjacencies.add(t2.getName());
						}
					}						
				}
			}
		}
		
		/* Add extraAdjacencies to tOrig.getAdjacents() clone (the clone is needed to avoid 
		 * modifying the "hardwire" map on the fly) */
		Vector<String> adjacencies = (Vector<String>) tOrig.getAdjacents().clone();
		for (Iterator<String> i = extraAdjacencies.iterator(); i.hasNext();) {
			adjacencies.add(i.next());
		}		
		
		for (Iterator<String> i = adjacencies.iterator(); i.hasNext(); ) {
			if (tDest.getName().equals(i.next())) {
				return true;
			}
		}
		return false;
	}
	
	/* Two-argument polymorphic method variant of the above */
	public boolean areAdjacent(Territory t1, Territory t2) {
		return  areAdjacent(t1, t2, null, null);
	}
	
}
