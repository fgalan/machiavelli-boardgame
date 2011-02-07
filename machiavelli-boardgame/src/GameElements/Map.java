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
import java.net.PortUnreachableException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
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

import Exceptions.AdjacencyException;
import Exceptions.ParseMapException;
import GameEngine.Player;

public class Map {
	
	private final static Logger log = Logger.getLogger("Map.class");
	
	private HashMap<String, Territory> territories;
	private HashMap<String, String> players;

	/**
	 * Class constructor
	 * @param xmlString an XML representation of the Map to build
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParseMapException 
	 * @throws DOMException 
	 * @throws AdjacencyException 
	 */
	public Map(File f) throws ParserConfigurationException, IOException, SAXException, DOMException, ParseMapException, AdjacencyException {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		territories = new HashMap<String, Territory>();
		players = new HashMap<String, String>();
		
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
					players.put(ctl, ctl);
				}
				
				/* Look for a city */
				City c = parseCity(l.item(i), (Province)t);
				((Province)t).setCity(c);
				
				/* Look for famine */
				((Province)t).setFamine(parseFamine(l.item(i)));
				
				/* Look for unrest */
				((Province)t).setFamine(parseUnrest(l.item(i)));
			}
						
			/* Add adjacent territories */
			t.setAdjacents(parseAdjacents(l.item(i)));
			
			/* Look for unit */
			t.setUnit(parseUnit(l.item(i),t));
			
			/* Store the Territory in the hashmap */
			territories.put(t.getName(), t);

		}
				
		/* Check map consistency */
		String s = checkAdjacencies();
		if (s != null) {
			throw new AdjacencyException(s);
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
				
				Unit u;
				if (type.equals("Army")) {
					u = new Army(owner, (Province)t, elite);
				}
				else if (type.equals("Fleet")) {
					u = new Fleet(owner, (Sea)t, elite);
				}
				else if (type.equals("Garrison")) {
					u = new Garrison(owner, (Province)t, elite);
				}
				else {
					throw new ParseMapException("unknown Unit type: " + type);
				}
				
				return u;
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
	 * @return true if Province is under unrest, false otherwise
	 */
	private boolean parseUnrest(Node item) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param item XML element representing a Province
	 * @return true if Province is under famine, false otherwise
	 */
	private boolean parseFamine(Node item) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param item XML element representing a Province
	 * @param the Province object
	 * @return the City object corresponding to the Province or nothing 
	 */
	private City parseCity(Node item, Province p) {
		
		NodeList l = item.getChildNodes();
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
				
				City c = new City(p, size, fortified, port, ng);
				log.info("city: size="+size+" fortified="+fortified+" port="+port+" ng="+ng);
				
				return c;
			}
		}
		
		/* No city found */
		return null;

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
	 * Render the map to text
	 */
	public String toString() {
		String s = "";
		
		// TODO: put the actual campaign and year
		s = s + "------------ TURN Spring 1457 ------------\n";
		s = s + "\n";
		s = s + "PLAYERS: ";
		for (Iterator i = players.keySet().iterator(); i.hasNext(); ) {
			s = s + i.next() + ", ";
		}
		s = s + "\n";
		
		s = s + "------------ CONTROLLED TERRITORIES ------------\n";
		// TODO
		
		s = s + "------------ MILITARY UNITS ------------\n";
		// TODO
		
		s = s + "------------ TEMPORARY MARKERS ------------\n";
		// TODO Famile
		// TODO Unrest
		
		return s;
	}
	
	/**
	 * Render the map to XML
	 */
	public String toXml() {
		
		/* We could use DOM to render the XML, but we prefer using text direclty so we can
		 * have complete control (e.g. pretty-printing format)
		 */
		String s = "<?xml version='1.0' encoding='UTF-8'?>\n";
		s = s + "<Map>\n";
		
		/* Process territories one by one */
		for (Iterator<String> i = territories.keySet().iterator(); i.hasNext() ; ) {
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
			for (Iterator<String> l = t.getAdjacents().iterator(); l.hasNext() ; ) {
				s = s + "      <Adj>"+l.next()+"</Adj>\n";
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
			
			/* Generate unit*/
			// name, owner, type, elite (optional)
			
			/* Generate famine */
			// TODO
			
			/* Generate unrest */
			// TODO
			
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
	 * 
	 * @return a list of adjacency inconsistencies, e.g.
	 * Territory A in B's adjacency list, but B is not in A's adjacency list
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
		if (s.equals("")) {
			return null;
		}
		else {
			return s;
		}
	}

	public HashMap<String,Territory> getTerritories() {
		return territories;
	}
}
