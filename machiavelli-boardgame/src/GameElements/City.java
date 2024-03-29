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

public class City {

	private final static Logger log = Logger.getLogger("City.class");
	
	private int size;
	
	private Province province;
	private boolean fortified;
	private boolean port;
	
	private Unit unit;
	private boolean underSiege;
	private boolean autonomousGarrison;
	
	public City(Province p, int s, boolean f, boolean po, boolean ag) {
		province = p;
		size = s;
		fortified = f;
		port = po;
		autonomousGarrison = ag;
	}
	
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Unit getUnit() {
		return unit;
	}
	public void clearUnit() {
		unit = null;
	}	
	public void setUnderSiege() {
		this.underSiege = true;
	}
	public void clearUnderSiege() {
		this.underSiege = false;
	}	
	public boolean isUnderSiege() {
		return underSiege;
	}

	public int getSize() {
		return size;
	}

	public Province getProvince() {
		return province;
	}

	public boolean hasAutonomousGarrison() {
		return autonomousGarrison;
	}
	
	public void clearAutonomousGarrison() {
		autonomousGarrison = false;
	}

	public boolean isFortified() {
		return fortified;
	}

	public boolean isPort() {
		return port;
	}
	
}
