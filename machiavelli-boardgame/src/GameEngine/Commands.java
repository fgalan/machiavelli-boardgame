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

import java.io.File;
import java.io.IOException;
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

import Actions.Action;
import Actions.Advance;
import Actions.Besiege;
import Actions.Convert;
import Actions.Hold;
import Actions.LiftSiege;
import Actions.Retreats;
import Actions.Support;
import Actions.Transport;
import Exceptions.ParseCommandsException;
import Expeditures.Assasination;
import Expeditures.BuyAutonomousGarrison;
import Expeditures.BuyUnit;
import Expeditures.CounterBride;
import Expeditures.DisbandAutonomousGarrison;
import Expeditures.DisbandGarrison;
import Expeditures.DisbandUnit;
import Expeditures.Expediture;
import Expeditures.FamineRelief;
import Expeditures.GarrisonToAutonomous;
import Expeditures.PacifyRebellion;
import Expeditures.Rebellion;

public class Commands {
	
	private final static Logger log = Logger.getLogger("Commands.class");
	
	private String player;
	private Vector<Action> actions;
	private Vector<Expediture> expeditures;
	private Vector<Retreats> retreats;
	
	/**
	 * Class constructor
	 * @param xmlString an XML representation of the Commands to build
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParseCommandsException 
	 */
	public Commands(File f) throws ParserConfigurationException, SAXException, IOException, ParseCommandsException  {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		actions = new Vector<Action>();
		expeditures = new Vector<Expediture>();
		retreats = new Vector <Retreats>();
		
		/* Get player in the root node */
		player = doc.getElementsByTagName("Commands").item(0).getAttributes().getNamedItem("player").getNodeValue();
		
		/* Pattern: <Action type="Army" id="1"> [Action] </Action> */
		NodeList l = doc.getElementsByTagName("Action");
		for (int i = 0; i < l.getLength(); i++) {
			actions.add(parseAction(l.item(i)));
		}

		/* Pattern: <FamineRelief province=""/> */
		l = doc.getElementsByTagName("FamineRelief");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			expeditures.add(new FamineRelief(at.getNamedItem("province").getNodeValue()));
		}
		
		/* Pattern: <PacifyRebellion province=""/> */
		l = doc.getElementsByTagName("PacifyRebellion");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			expeditures.add(new PacifyRebellion(at.getNamedItem("province").getNodeValue()));
		}
		
		/* Pattern: <CounterBride type="Army" id="1" player="" amount=""/> */
		l = doc.getElementsByTagName("CounterBride");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			int id = Integer.parseInt(at.getNamedItem("id").getNodeValue());
			String t = at.getNamedItem("type").getNodeValue();
			String p = at.getNamedItem("player").getNodeValue();
			expeditures.add(new CounterBride(amount, t, id, p));
		}
		
		/* Pattern: <DisbandAutonomousGarrison province="" amount="6"/> */
		l = doc.getElementsByTagName("DisbandAutonomousGarrison");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			String p = at.getNamedItem("province").getNodeValue();
			expeditures.add(new DisbandAutonomousGarrison(amount, p));
		}
		
		/* Pattern: <BuyAutonomousGarrison province="" amount="9"> [Action] </BuyAutonomousGarrison> */
		l = doc.getElementsByTagName("BuyAutonomousGarrison");
		for (int i = 0; i < l.getLength(); i++) {
			expeditures.add(parseBuyAutonomousGarrison(l.item(i)));
		}
		
		/* Pattern: <GarrisonToAutonomous province="" amount="9"/> */
		l = doc.getElementsByTagName("GarrisonToAutonomous");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			String p = at.getNamedItem("province").getNodeValue();
			expeditures.add(new GarrisonToAutonomous(amount, p));
		}
		
		/* Pattern: <DisbandGarrison province="" amount="12"/> */
		l = doc.getElementsByTagName("DisbandGarrison");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			String p = at.getNamedItem("province").getNodeValue();
			expeditures.add(new DisbandGarrison(amount, p));
		}
		
		/* Pattern: <DisbandUnit province="" amount="12"/> */
		l = doc.getElementsByTagName("DisbandUnit");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			String p = at.getNamedItem("province").getNodeValue();
			expeditures.add(new DisbandUnit(amount, p));			
		}
		
		/* Pattern: <BuyUnit province="" amount="24"> [Action] </BuyUnit> */
		l = doc.getElementsByTagName("BuyUnit");
		for (int i = 0; i < l.getLength(); i++) {
			expeditures.add(parseBuyUnit(l.item(i)));
		}
		
		/* Pattern: <Rebellion province=""/> */
		l = doc.getElementsByTagName("Rebellion");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			expeditures.add(new Rebellion(at.getNamedItem("province").getNodeValue()));
		}
		
		/* Pattern: <Assasination player="" amount=""/> */
		l = doc.getElementsByTagName("Assasination");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			int amount = Integer.parseInt(at.getNamedItem("amount").getNodeValue());
			String p = at.getNamedItem("player").getNodeValue();
			expeditures.add(new Assasination(amount, p));
		}
		
		l = doc.getElementsByTagName("Retreat");
		for (int i = 0; i < l.getLength(); i++) {
			retreats.add(parseRetreats(l.item(i)));
		}		
	}

	private Retreats parseRetreats(Node item) {
		
		/* Pattern: <Retreat type="Army" id="1"> */
		String type = item.getAttributes().getNamedItem("type").getNodeValue();
		int id = Integer.parseInt(item.getAttributes().getNamedItem("id").getNodeValue());
		Retreats r = new Retreats(type, id);
		
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Province")) {
				r.addProvince(l.item(i).getTextContent());
			}
		}
		
		return r;
	}

	private Expediture parseBuyUnit(Node item) throws ParseCommandsException {

		/* Pattern: <BuyUnit province="" amount="24"> [Action] </BuyUnit> */
		String province = item.getAttributes().getNamedItem("province").getNodeValue();
		int amount = Integer.parseInt(item.getAttributes().getNamedItem("amount").getNodeValue());
		
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String actionType = l.item(i).getNodeName();
				NamedNodeMap at = l.item(i).getAttributes();

				/* Patter: <Advance territory=""/> */			
				if (actionType.equals("Advance")) {
					return new BuyUnit(amount, province, new Advance(at.getNamedItem("territory").getNodeValue())); 
				}
				/* <Besiege/> */
				else if (actionType.equals("Besiege")) {
					return new BuyUnit(amount, province, new Besiege());
				}
				/* <Hold/> */			
				else if (actionType.equals("Hold")) {
					return new BuyUnit(amount, province, new Hold());
				}
				/* <LiftSiege/> */
				else if (actionType.equals("LiftSiege")) {
					return new BuyUnit(amount, province, new LiftSiege());
				}
				/* <Support territory="" player=""/> */
				else if (actionType.equals("Support")) {
					return new BuyUnit(amount, province, new Support(at.getNamedItem("territory").getNodeValue(), at.getNamedItem("player").getNodeValue()));
				}
				/* <Transport armyId="" player=""/> */
				else if (actionType.equals("Transport")) {
					return new BuyUnit(amount, province, new Transport(at.getNamedItem("armyId").getNodeValue(), at.getNamedItem("player").getNodeValue()));
				}
				/* <Convert newType="Fleet"/> */
				else if (actionType.equals("Convert")) {
					return new BuyUnit(amount, province, new Convert(at.getNamedItem("newType").getNodeValue()));
				}
				else {
					throw new ParseCommandsException("unknown action type: " + actionType);
				}
			}
		}
		/* No action associated to the new Unit, so Hold is assumed */
		return new BuyUnit(amount, province, new Hold());
	}

	private Expediture parseBuyAutonomousGarrison(Node item) throws ParseCommandsException {
		
		/* Pattern: <BuyAutonomousGarrison province="" amount="9"> [Action] </BuyAutonomousGarrison> */
		String province = item.getAttributes().getNamedItem("province").getNodeValue();
		int amount = Integer.parseInt(item.getAttributes().getNamedItem("amount").getNodeValue());
		
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {		
				String actionType = l.item(i).getNodeName();
				NamedNodeMap at = l.item(i).getAttributes();

				/* Patter: <Advance territory=""/> */			
				if (actionType.equals("Advance")) {
					return new BuyAutonomousGarrison(amount, province, new Advance(at.getNamedItem("territory").getNodeValue())); 
				}
				/* <Besiege/> */
				else if (actionType.equals("Besiege")) {
					return new BuyAutonomousGarrison(amount, province, new Besiege());
				}
				/* <Hold/> */			
				else if (actionType.equals("Hold")) {
					return new BuyAutonomousGarrison(amount, province, new Hold());
				}
				/* <LiftSiege/> */
				else if (actionType.equals("LiftSiege")) {
					return new BuyAutonomousGarrison(amount, province, new LiftSiege());
				}
				/* <Support territory="" player=""/> */
				else if (actionType.equals("Support")) {
					return new BuyAutonomousGarrison(amount, province, new Support(at.getNamedItem("territory").getNodeValue(), at.getNamedItem("player").getNodeValue()));
				}
				/* <Transport armyId="" player=""/> */
				else if (actionType.equals("Transport")) {
					return new BuyAutonomousGarrison(amount, province, new Transport(at.getNamedItem("armyId").getNodeValue(), at.getNamedItem("player").getNodeValue()));
				}
				/* <Convert newType="Fleet"/> */
				else if (actionType.equals("Convert")) {
					return new BuyAutonomousGarrison(amount, province, new Convert(at.getNamedItem("newType").getNodeValue()));
				}
				else {
					throw new ParseCommandsException("unknown action type: " + actionType);
				}
			}
		}
		/* No action associated to the new Unit, so Hold is assumed */
		return new BuyAutonomousGarrison(amount, province, new Hold());
	}

	private Action parseAction(Node item) throws ParseCommandsException {
		
		/* Pattern: <Action type="Army" id="1"> [Action] </Action> */
		String type = item.getAttributes().getNamedItem("type").getNodeValue();
		int id = Integer.parseInt(item.getAttributes().getNamedItem("id").getNodeValue());
		
		NodeList l = item.getChildNodes();		
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String actionType = l.item(i).getNodeName();
				NamedNodeMap at = l.item(i).getAttributes();

				/* Patter: <Advance territory=""/> */			
				if (actionType.equals("Advance")) {
					return new Advance(id, type, at.getNamedItem("territory").getNodeValue());
				}
				/* <Besiege/> */
				else if (actionType.equals("Besiege")) {
					return new Besiege(type, id);
				}
				/* <Hold/> */			
				else if (actionType.equals("Hold")) {
					return new Hold(type, id);
				}
				/* <LiftSiege/> */
				else if (actionType.equals("LiftSiege")) {
					return new LiftSiege(type, id);
				}
				/* <Support territory="" player=""/> */
				else if (actionType.equals("Support")) {
					return new Support(type, id, at.getNamedItem("territory").getNodeValue(), at.getNamedItem("player").getNodeValue());
				}
				/* <Transport armyId="" player=""/> */
				else if (actionType.equals("Transport")) {
					return new Transport(type, id, at.getNamedItem("armyId").getNodeValue(), at.getNamedItem("player").getNodeValue());
				}
				/* <Convert newType="Fleet"/> */
				else if (actionType.equals("Convert")) {
					return new Convert(type, id, at.getNamedItem("newType").getNodeValue());
				}
				else {
					throw new ParseCommandsException("unknown action type: " + actionType);
				}
			}
		}
		throw new ParseCommandsException("<Action> not found");
	}
}
