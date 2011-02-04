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

public class City {

	private final static Logger log = Logger.getLogger("City.class");
	
	private int size;
	
	private Player controller;
	private Province province;
	private Unit unit;
	private boolean underSiege;
	private boolean neutralGarrison;
	private boolean fortified;
	
	public City(Province p, int s, boolean f, boolean ng) {
		province = p;
		size = s;
		fortified = f;
		neutralGarrison = ng;
	}
	
	public void setController(Player controller) {
		this.controller = controller;
	}
	public Player getController() {
		return controller;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnderSiege(boolean underSiege) {
		this.underSiege = underSiege;
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

	public boolean isNeutralGarrison() {
		return neutralGarrison;
	}

	public boolean isFortified() {
		return fortified;
	}
	
}
