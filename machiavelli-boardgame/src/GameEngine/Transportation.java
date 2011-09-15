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

import GameElements.Sea;

/**
 * Instances of this class represent a 3-uple ArmyId-Player as reulst of Transport orders. They live
 * in a HashMap with key is the Sea name where the Tranportation is applied.
 * @author fermin
 *
 */
public class Transportation {

	private int armyId;
	private String player;
	
	public Transportation(int a, String p) {
		armyId = a;
		player = p;
	}

	public int getArmyId() {
		return armyId;
	}

	public String getPlayer() {
		return player;
	}
	
}
