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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import Exceptions.MapCoherenceException;
import Exceptions.ParseCommandsException;
import Exceptions.ParseGameStatusException;
import Exceptions.ParseMapException;
import Exceptions.PlayerNotFountException;
import Exceptions.ProcessAdjustmentsException;
import Exceptions.ProcessCommandsException;
import GameElements.Map;

public class Runner {

	/**
	 * The runner search in the directory passed as first argument the following files:
	 * 
	 * - map.xml (to load the Map)
	 * - gs.xml (to load the GameStatus)
	 * - <player>.xml (to load the Ajustements/Commands)
	 * 
	 * It writes in the directory passed as second argument the following files:
	 * 
	 * - map.xml (the resulting Map after turn processing)
	 * - gs.xml (the resulting GameStatus after turn processing)
	 * - result.txt (global turn result)
	 * - map.txt (text render of the resulting map)
	 * - <player>.txt (per-player turn result)
	 * 
	 * The third argument (either "adjustements" or "commands") select between processing Adjustments or Commands
	 * 
	 * @param args see above
	 * @throws MapCoherenceException 
	 * @throws ParseMapException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws DOMException 
	 * @throws ParseGameStatusException 
	 * @throws ParseCommandsException 
	 * @throws ProcessCommandsException 
	 * @throws ProcessAdjustmentsException 
	 * @throws PlayerNotFountException 
	 */
	public static void main(String[] args) throws DOMException, ParserConfigurationException, IOException, SAXException, ParseMapException, MapCoherenceException, ParseGameStatusException, ParseCommandsException, ProcessCommandsException, ProcessAdjustmentsException, PlayerNotFountException {
		
		BasicConfigurator.configure();		
		
		/* TODO: stronger file loading (e.g. raise an exception if gs.xml or map.xml are not
		   found */
		
		/* Some quick input checkings */
		if (args.length < 3) {
			System.err.print("not enough arguments");
			return;
		}
		
		if (!args[2].equals("commands") && !args[2].equals("adjustments")) {
			System.err.print("mode '" + args[2] + "' is not valid");
			return;
		}
		
		File dirIn = new File(args[0]);
		File dirOut = new File(args[1]);
		Vector<File> turns = new Vector<File>();
		Map m = null;
		GameStatus gs = null;
		
		String[] children = dirIn.list();
		if (children == null) {
			throw new IOException("directory " + args[0] + " error");
		} 
		else {
			for (int i=0; i<children.length; i++) {
				
				if (children[i].equals("map.xml")) {
					/* Get the map */
					m = new Map(new File(args[0] + "/" + children[i]));
				}
				else if (children[i].equals("gs.xml")) {
					/* Get the game status */
					gs = new GameStatus(new File(args[0] + "/" + children[i]));
				}
				else if (children[i].endsWith(".xml")) {
					/* Get the other files */
					turns.add(new File(args[0] + "/" + children[i]));
				}
		    }
		}
		
		/* Some more checkings... */
		children = dirOut.list();
		if (children == null) {
			throw new IOException("directory " + args[1] + " error");
		}		
		if (m == null) {
			System.err.print("no map.xml file found");
			return;
		}
		if (gs == null) {
			System.err.print("no gs.xml file found");
			return;
		}		
		
		Result r;
		if (args[2].equals("commands")) {
			/* Note that the cmds vector could be empty, in the initial turn */
			Vector<Commands> cmds = new Vector<Commands>();
			for (Iterator<File> i = turns.iterator(); i.hasNext(); ) {
				cmds.add(new Commands(i.next()));
			}
			r = Engine.processCommands(gs, m, cmds);
		}
		else { // adjustments
			/* Note that the cmds vector could be empty, in the initial turn */
			Vector<Adjustments> adjs = new Vector<Adjustments>();
			for (Iterator<File> i = turns.iterator(); i.hasNext(); ) {
				adjs.add(new Adjustments(i.next()));
			}
			r = Engine.processAdjustments(gs, m, adjs);
		}
		
		/* Make output */
		FileWriter f = new FileWriter(args[1] + "/gs.xml");
		f.write(gs.toXml());
		f.close();
		
		f = new FileWriter(args[1] + "/map.xml");
		f.write(m.toXml());
		f.close();
		
		f = new FileWriter(args[1] + "/map.txt");
		f.write(m.toString());
		f.close();
		
		f = new FileWriter(args[1] + "/result.txt");
		f.write(r.toString());
		f.close();		
		
		for (Iterator<String> i = gs.getPlayers().iterator() ; i.hasNext(); ) {
			String player = i.next();
			f = new FileWriter(args[1] + "/"+player+".txt");
			f.write(gs.getStatus(player, m.getCitiesBelongingToPlayer(player).size()));
			f.close();
		}
		
	}

}
