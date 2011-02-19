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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Actions.Action;
import Actions.Retreats;
import Exceptions.ParseCommandsException;
import Expenses.Expense;

public class Adjustments {

	private String player;
	private Vector<Payment> payments;
	private Vector<Purchase> purchases;
	
	/**
	 * Class constructor 
	 * @param f xmlString an XML representation of the Adjustments to build
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Adjustments(File f) throws ParserConfigurationException, SAXException, IOException  {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(f);
		
		payments = new Vector<Payment>();
		purchases = new Vector<Purchase>();
		
		/* Get player in the root node */
		player = doc.getElementsByTagName("Adjustments").item(0).getAttributes().getNamedItem("player").getNodeValue();
		
		/* Pattern: <Payment type="Army" id="1"/> */
		NodeList l = doc.getElementsByTagName("Payment");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			payments.add(new Payment(at.getNamedItem("type").getNodeValue(),Integer.parseInt(at.getNamedItem("id").getNodeValue())));
		}
		
		/* Pattern: <Purchase type="Army" elite="0" province="Treviso"/> */
		l = doc.getElementsByTagName("Purchase");
		for (int i = 0; i < l.getLength(); i++) {
			NamedNodeMap at = l.item(i).getAttributes();
			purchases.add(new Purchase(at.getNamedItem("type").getNodeValue(),Integer.parseInt(at.getNamedItem("elite").getNodeValue()),at.getNamedItem("province").getNodeValue()));
		}		
	}

	public String getPlayer() {
		return player;
	}

	public Vector<Payment> getPayments() {
		return payments;
	}

	public Vector<Purchase> getPurchases() {
		return purchases;
	}
}
