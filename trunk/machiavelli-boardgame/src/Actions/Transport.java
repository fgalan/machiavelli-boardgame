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


package Actions;

import GameElements.Map;

public class Transport extends Action {
	
	private String armyId;
	private String player;
	
	public Transport(String type, int id, String aid, String p) {
		super(type, id);
		this.armyId = aid;
		this.player = p;
	}
	
	/**
	 * For "anonymous unit" actions (in <Buy*>)
	 */
	public Transport(String aid, String p) {
		this.armyId = aid;
		this.player = p;
	}
	
	/**
	 * The same as toString, but appending '*', '**' or '***' correctly based on the Map and player passed
	 * as arguments 
	 * @param m
	 * @param player
	 * @return
	 */
	public String toStringWithElite(Map m, String player) {
		String s = super.toStringWithElite(m, player) + " transports army A" + armyId + " of player " + player;
		return s;
	}
}
