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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Exceptions.ParseGameStatusException;
import Exceptions.PlayerNotFountException;


/**
 * This class contains the information structures that model the game status
 * 
 * @author fermin
 */
public class GameStatus {
	
	public final static int SPRING = 0;
	public final static int SUMMER = 1;
	public final static int FALL = 2;

	private int year;
	private int campaign;
	
	/* The number of variable income rolls for the player controlling Genoa */
	private int genoaControllerRolls;
	
	/* The victory conditions */ 
	private int citiesVictory;
	private int homeCountriesVictory;
	
	private HashMap<String,Vector<String>> homeCountries;
	private HashMap<String,Vector<String>> assassinTokens;
	private HashMap<String,Integer> money;
	private HashMap<String,Integer> incomeRolls;
	private HashMap<String,Integer> conqueredHomeCountries;

	public GameStatus(File f) throws ParserConfigurationException, SAXException, IOException, ParseGameStatusException {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		homeCountries = new HashMap<String,Vector<String>>();
		assassinTokens = new HashMap<String,Vector<String>>();
		money = new HashMap<String,Integer>();
		incomeRolls = new HashMap<String,Integer>();
		conqueredHomeCountries = new HashMap<String,Integer>(); 
		
		/* Get year and campaign */
		NodeList l = doc.getElementsByTagName("GameStatus");
		year = (Integer.parseInt(l.item(0).getAttributes().getNamedItem("year").getNodeValue()));
		String campaignString = l.item(0).getAttributes().getNamedItem("campaign").getNodeValue();
		if (campaignString.equals("spring")) {
			campaign = SPRING;
		}
		else if (campaignString.equals("summer")) {
			campaign = SUMMER;
		}
		else if (campaignString.equals("fall")) {
			campaign = (FALL);
		}
		else {
			throw new ParseGameStatusException("campaing value not valid: " + campaignString);
		}
		
		/* Set the Genoa rolls */
		l = doc.getElementsByTagName("GenoaControllerRolls");
		genoaControllerRolls = Integer.parseInt(l.item(0).getChildNodes().item(0).getTextContent());
		
		/* Set the victory conditions */
		l = doc.getElementsByTagName("CitiesForVictory");
		citiesVictory = Integer.parseInt(l.item(0).getChildNodes().item(0).getTextContent());
		l = doc.getElementsByTagName("HomeCountriesForVictory");
		homeCountriesVictory = Integer.parseInt(l.item(0).getChildNodes().item(0).getTextContent());		
		
		/* Process Player by Player */
		l = doc.getElementsByTagName("Player");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			String player = at.getNamedItem("name").getNodeValue();
			int ir = Integer.parseInt(at.getNamedItem("incomeRolls").getNodeValue());
			int chc = Integer.parseInt(at.getNamedItem("conqueredHomeCountries").getNodeValue());
			
			money.put(player, parseMoney(l.item(i)));
			incomeRolls.put(player, new Integer(ir));
			conqueredHomeCountries.put(player, new Integer(chc));
			homeCountries.put(player, parseHomeCountry(l.item(i)));
			assassinTokens.put(player, parseAssasins(l.item(i)));
		}
	}

	private Vector<String> parseAssasins(Node item) {
		Vector<String> v = new Vector<String>();		
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Assasin")) {
				v.add(l.item(i).getTextContent());
			}
		}
		return v;
	}

	private Vector<String> parseHomeCountry(Node item) {
		Vector<String> v = new Vector<String>();
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("HomeProvince")) {
				v.add(l.item(i).getTextContent());
			}
		}
		return v;
	}

	private Integer parseMoney(Node item) throws ParseGameStatusException {
		NodeList l = item.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (l.item(i).getNodeName().equals("Money")) {
				return new Integer(l.item(i).getTextContent());
			}
		}
		throw new ParseGameStatusException("money element not found"); 
	}
	
	public String toXml() {
		
		/* We could use DOM to render the XML, but we prefer using text directly so we can
		 * have complete control (e.g. pretty-printing format)
		 */
		String s = "<?xml version='1.0' encoding='UTF-8'?>\n";
		s = s + "<GameStatus year='"+year+"' campaign='"+campaing2Text()+"'>\n";
		
		/* Configuration element */
		s = s + "   <Configuration>\n";
		s = s + "      <GenoaControllerRolls>"+genoaControllerRolls+"</GenoaControllerRolls>\n";
		s = s + "      <CitiesForVictory>"+citiesVictory+"</CitiesForVictory>\n";
		s = s + "      <HomeCountriesForVictory>"+homeCountriesVictory+"</HomeCountriesForVictory>\n";		
		s = s + "   </Configuration>\n";
		
		for (Iterator<String> i = getPlayers().iterator(); i.hasNext(); ) {
			String player = i.next();
			s = s + "   <Player name='"+player+"' incomeRolls='"+incomeRolls.get(player)+"' conqueredHomeCountries='"+conqueredHomeCountries.get(player)+"'>\n";
			s = s + "      <Money>"+money.get(player)+"</Money>\n";
			for (Iterator<String> l = homeCountries.get(player).iterator(); l.hasNext(); ) {
				s = s + "      <HomeProvince>"+l.next()+"</HomeProvince>\n";
			}
			for (Iterator<String> l = assassinTokens.get(player).iterator(); l.hasNext(); ) {
				s = s + "      <Assasin>"+l.next()+"</Assasin>\n";
			}			
			s = s + "   </Player>\n";
		}
		
		s = s + "</GameStatus>\n";
		return s;
	}
	
	public String campaing2Text() {
		if (campaign == SPRING) {
			return "spring";
		}
		else if (campaign == SUMMER) {
			return "summer";
		}
		else { // FALL
			return "fall";
		}
	}

	public String getStatus(String p, int controlledCities) throws PlayerNotFountException {
		
		try {
			String s = "Status for player " + p + " in " + year + " " + campaing2Text() + ":\n";
			s = s + "--------------------------------------------------------------\n";
			s = s + "Treasury: " + money.get(p) + "\n";
			
			s = s + "Conquered home countries: " + conqueredHomeCountries.get(p) + "\n";
			s = s + "Controlled cities: " + controlledCities + "\n";
			s = s + "Home country provinces: ";
			Vector<String> hp = homeCountries.get(p);
			Collections.sort(hp);
			for (Iterator<String> i = hp.iterator(); i.hasNext(); ) {
				s = s + i.next() + ", ";
			}
			s = s + "\n";
			
			s = s + "Assasins: ";
			Vector<String> as = assassinTokens.get(p);
			Collections.sort(as);
			for (Iterator<String> i = as.iterator(); i.hasNext(); ) {
				s = s + i.next() + ", ";
			}
			s = s + "\n";
			
			return s;
		} 
		catch (NullPointerException e) {
			throw new PlayerNotFountException(p);
		}
	}

	public void incYear() {
		year++;
	}

	public int getYear() {
		return year;
	}

	public void incCampaign() {
		campaign++;
		if (campaign > FALL) {
			campaign = SPRING;
		}
	}

	public int getCampaign() {
		return campaign;
	}
	
	public String getCampaignString() {
		switch (campaign) {
		case SPRING:
			return "spring";
		case SUMMER:
			return "summer";
		case FALL:
			return "fall";
		default:
			return "UNKNOWN";
		}
	}
	
	public int getMoney(String player) {
		return money.get(player);
	}
	
	public void incMoney(String player, int inc) {
		money.put(player, money.get(player) + inc);
	}
	
	public void decMoney(String player, int dec) {
		money.put(player, money.get(player) - dec);
	}
	
	public Vector<String> getPlayers() {
		/* By construction, all the hashmaps has the same keys. We are arbitrarily using money
		 * for getting them */
		Vector<String> players = new Vector<String>(money.keySet());
		Collections.sort(players);
		return players;
	}

	public int getGenoaControllerRolls() {
		return genoaControllerRolls;
	}

	public int getIncomeRolls(String p) {
		return incomeRolls.get(p);
	}

}
