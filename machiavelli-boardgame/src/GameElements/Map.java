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
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Exceptions.AdjacencyException;
import GameEngine.Player;

public class Map {
	
	private final static Logger log = Logger.getLogger("Map.class");
	
	private HashMap<String, Territory> territories;

	/**
	 * Class constructor
	 * @param xmlString an XML representation of the Map to build
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 * @throws SAXException 
	 */
	public Map(File f) throws ParserConfigurationException, IOException, SAXException {
		// Class constructor
		// TODO
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		NodeList l = doc.getElementsByTagName("Territory");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			Territory t;
									
			/* Province or sea? */
			if (at.getNamedItem("type").getNodeValue().equals("Province")) {
				t = new Province(at.getNamedItem("name").getNodeValue());
				log.info("province " + at.getNamedItem("name").getNodeValue());
			}
			else { // "Sea"
				t = new Sea(at.getNamedItem("name").getNodeValue());
				log.info("sea " + at.getNamedItem("name").getNodeValue());
			}
			
			/* Look for controller */
			t.setController(parseController(l.item(i)));
			
			if (t instanceof Province) {
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
			t.setUnit(parseUnit(l.item(i)));
		}
	}
	
	/**
	 * @param item XML element reprensenting a Territory
	 * @return the Unit controllict the Territory, null if the Territory is empty
	 */
	private Unit parseUnit(Node item) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param item XML element reprensenting a Territory
	 * @return the Vector of adjacent Territories
	 */
	private Vector<String> parseAdjacents(Node item) {
		
		Vector<String> v = new Vector<String>();
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Adjacency")) {
				v.add(l.item(i).getTextContent());
				log.info("adjacency: " + l.item(i).getTextContent());
			}
		}
		return v;
	}

	/**
	 * @param item XML element reprensenting a Province
	 * @return true if Province is under unrest, false otherwise
	 */
	private boolean parseUnrest(Node item) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param item XML element reprensenting a Province
	 * @return true if Province is under famine, false otherwise
	 */
	private boolean parseFamine(Node item) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param item XML element reprensenting a Province
	 * @param the Province object
	 * @return the City object corresponding to the Province or nothing 
	 */
	private City parseCity(Node item, Province t) {
		// TODO
		
		/* Look for port */
		return null;
	}

	/**
	 * @param item XML element reprensenting a Territory
	 * @return the controller name or null if the Territory is controled by no player
	 */
	private String parseController(Node item) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Render the map to text
	 */
	public String toString() {
		// TODO
		return "";
	}
	
	/**
	 * Render the map to XML
	 */
	public String toXml() {
		
		return "";
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
	 * @throws AdjacencyException if the maps contains some adjacency problem, i.e
	 * Territory A in B's adjacency list, but B is not in A's adjacency list
	 */
	public void checkAdjacencies() throws AdjacencyException {
		// TODO
	}

	public HashMap<String,Territory> getTerritories() {
		return territories;
	}
}
