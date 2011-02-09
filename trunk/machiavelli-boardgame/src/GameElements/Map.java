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
import GameEngine.Player;

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
				log.info("province " + at.getNamedItem("name").getNodeValue());
			}
			else if (at.getNamedItem("type").getNodeValue().equals("Sea")) {
				t = new Sea(at.getNamedItem("name").getNodeValue());
				log.info("sea " + at.getNamedItem("name").getNodeValue());
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
				((Province)t).setFamine(parseFamine(l.item(i)));
				
				/* Look for unrest */
				((Province)t).setUnrest(parseUnrest(l.item(i)));
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
				String name = at.getNamedItem("name").getNodeValue();
				String type = at.getNamedItem("type").getNodeValue();
				String owner = at.getNamedItem("owner").getNodeValue();
				
				/* elite is optional */
				int elite = Unit.NO_ELITE;
				if (at.getNamedItem("elite")!=null) {
					elite = Integer.parseInt(at.getNamedItem("elite").getNodeValue());
				}
				
				if (type.equals("Army")) {
					return new Army(name, owner, (Province)t, elite);
				}
				else if (type.equals("Fleet")) {
					return new Fleet(name, owner, t, elite);
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
				log.info("adjacency: " + l.item(i).getTextContent());
			}
		}
		return v;
	}

	/**
	 * @param item XML element representing a Province
	 * @return the player against the Unrest has been produced, null if there is no rebellion 
	 */
	private String parseUnrest(Node item) {
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Unrest")) {
				NamedNodeMap at = l.item(i).getAttributes();
				return at.getNamedItem("againts").getNodeValue();				
			}
		}
		return null;
	}

	/**
	 * @param item XML element representing a Province 
	 * @return true if Province is under unrest, false otherwise
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
				log.info("city: size="+size+" fortified="+fortified+" port="+port+" ng="+ng);
				
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
						String name = at.getNamedItem("name").getNodeValue();
						String owner = at.getNamedItem("owner").getNodeValue();
						
						/* elite is optional */
						int elite = Unit.NO_ELITE;
						if (at.getNamedItem("elite")!=null) {
							elite = Integer.parseInt(at.getNamedItem("elite").getNodeValue());
						}
						
						Unit u = new Garrison(name, owner, c, elite);
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
			
			if (t instanceof Province && ((Province)t).getController()!=null) {
				
				/* The "code" is as follows:
				 * 
				 *   Swiss      -> the player controls the province and the unfortified city (if any)
				 *   Turin(+)   -> the player controls the province *and* the fortified city
				 *   Turin(-)   -> the player controls the province *but not* the fortified city (which is controlled
				 *                 by another player with a Garrison unit there
				 *   Turin(c)   -> the player controls the city *but not* the city
				 *   
				 * Note that Seas have no controller by definition */
			 
				String provinceName = ((Province)t).getName();
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
				
				String type;
				if (t.getUnit() instanceof Army) {
					type = "(Army)";
				}
				else if (t.getUnit() instanceof Fleet) {
					type = "(Fleet)";
				}
				else { 
					/* Note that Garrison are forbidden as units for Territories (they are for cities) */
					//throw new MapCoherenceException("rendering "+t.getName()+", unit type "+t.getUnit().getClass()+" is unkown or fobidden");
					// We can not throw exception because we are overriding toString(), which signature is fixed in Object class
					type = "(UNKNOWN)";
				}
				
				String u = t.getUnit().getName() + elite2String(t.getUnit()) + " " + type + " in " + t.getName(); 
				appendToHashMap(unitsInMap, t.getUnit().getOwner(), u);
			}
			/* Look for unit at city */
			if (t instanceof Province && ((Province)t).getCity()!=null && ((Province)t).getCity().isFortified()) {
				if (((Province)t).getCity().getUnit() != null) {
					String u = ((Province)t).getCity().getUnit().getName() + elite2String(((Province)t).getCity().getUnit()) + " (Garrison) in " + t.getName();
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
		String unrest = "";
		/* Process territories one by one (in alphabetic order)*/
		l = new Vector<String>(territories.keySet());
		Collections.sort(l);
		for (Iterator<String> i = l.iterator(); i.hasNext() ; ) {
			Territory t = territories.get(i.next());
			if (t instanceof Province) {
				if (((Province)t).getUnrest()!=null) {
					unrest = unrest + "   unrest in " + t.getName() + " againts " + ((Province)t).getUnrest() + "\n"; 
				}
				if (((Province)t).hasFamine()) {
					famine = famine + "   famine in " + t.getName() + "\n";
				}
			}
		}
		
		s = s + "TEMPORARY MARKERS:\n";
		if (famine.isEmpty() && unrest.isEmpty()) {
			s = s + "   none\n";
		}
		else {
			s = s + famine + unrest;
		}
		
		return s;
	}
	
	/**
	 * Helper method, to factorice code in toString()
	 * @param u
	 * @return
	 */
	private String elite2String(Unit u) {
		String s = "";
		if (u.getElite() == Unit.ELITE_TYPE_1) {
			s ="*";;
		}
		else if (u.getElite() == Unit.ELITE_TYPE_2) {
			s = "**";
		}
		else if (u.getElite() == Unit.ELITE_TYPE_3) {
			s = "***";
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
		
		/* We could use DOM to render the XML, but we prefer using text direclty so we can
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
				String name = t.getUnit().getName();
				String owner = t.getUnit().getOwner();
				String eliteString = "";
				if (t.getUnit().getElite() != 0) {
					eliteString =" elite='"+t.getUnit().getElite()+"'";
				}
					
				if (t.getUnit() instanceof Army) {
					s = s + "      <Unit name='"+name+"' type='Army' owner='"+owner+"'"+eliteString+"/>\n";
				}
				else if (t.getUnit() instanceof Fleet) {
					s = s + "      <Unit name='"+name+"' type='Fleet' owner='"+owner+"'"+eliteString+"/>\n";
				}
				else { 
					/* Note that Garrison are forbidden as units for Territories (they are for cities) */
					throw new MapCoherenceException("rendering "+t.getName()+", unit type "+t.getUnit().getClass()+" is unkown or fobidden");
				}
			}
			
			/* Generate unit (Garrison) */
			if (t instanceof Province && ((Province)t).getCity()!=null && ((Province)t).getCity().getUnit()!=null) {
				String name = ((Province)t).getCity().getUnit().getName();
				String owner = ((Province)t).getCity().getUnit().getOwner();
				
				String eliteString = "";
				if (((Province)t).getCity().getUnit().getElite() != 0) {
					eliteString =" elite='"+((Province)t).getCity().getUnit().getElite()+"'";
				}
					
				s = s + "      <Unit name='"+name+"' type='Garrison' owner='"+owner+"'"+eliteString+"/>\n";
				
			}
			
			
			/* Generate famine */
			if (territoryType.equals("Province") && ((Province)t).hasFamine()) {
				s = s + "      <Famine/>\n";
			}
			
			/* Generate unrest */
			if (territoryType.equals("Province") && ((Province)t).getUnrest()!=null) {
				s = s + "      <Unrest againts='"+((Province)t).getUnrest()+"'/>\n";
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
	public int getIncome(Player p) {
		// TODO
		return 0;
	}
	
	/** 
	 * @param p a Player
	 * @return the Provinces controlled by Player p
	 */
	public Province[] getPlayerProvinces(Player p) {
		// TODO
		return null;
	}
	
	/** 
	 * @param p a Player
	 * @return the Seas controlled by Player p
	 */
	public Sea[] getPlayerSeas(Player p) {
		// TODO
		return null;
	}
	
	/** 
	 * @param p a Player
	 * @return the Territories (both Provinces and Seas) controlled by Player p
	 */
	public Territory[] getPlayerTerritories(Player p) {
		// TODO
		return null;
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
				
			/* Match Unrest country with list of available countries */
			if (t instanceof Province && ((Province)t).getUnrest()!= null && !players.contains(((Province)t).getUnrest())) {
				s = s + "unrest in province "+t.getName()+" is declared againts "+((Province)t).getUnrest()+", which is not defined as any province controller\n";
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
	

	public HashMap<String,Territory> getTerritories() {
		return territories;
	}
}
