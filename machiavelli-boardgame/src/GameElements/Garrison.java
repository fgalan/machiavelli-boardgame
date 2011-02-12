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

import org.apache.log4j.Logger;

import GameEngine.Player;

public class Garrison extends Unit {

	private final static Logger log = Logger.getLogger("Garrison.class");
	
	public Garrison(String name, String owner, City c, int elite) {
		super(name, owner, elite);
		setLocation(c.getProvince());
	}
	
	public Garrison(String name, String owner, Province p, int elite) {
		super(name, owner, elite);
		setLocation(p);
	}
	
	public String toString() {
		return (super.toString() + " (Garrison)");
	}	
	
}
