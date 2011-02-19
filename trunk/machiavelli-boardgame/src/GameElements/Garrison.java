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

public class Garrison extends Unit {

	private final static Logger log = Logger.getLogger("Garrison.class");
	
	public final static int MAX = 6;
	
	public Garrison(int id, String owner, City c, int elite) {
		super(id, owner, elite);
		setLocation(c.getProvince());
	}
	
	public Garrison(int id, String owner, Province p, int elite) {
		super(id, owner, elite);
		setLocation(p);
	}
	
	public String toString() {
		return ("G" + super.toString() + " (Garrison)");
	}	
	
}
