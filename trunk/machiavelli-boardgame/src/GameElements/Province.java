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

public class Province extends Territory {
	
	private final static Logger log = Logger.getLogger("Province.class");
	
	private boolean famine;
	private String rebellion;
	private City city;

	private String controller;

	public Province(String n) {
		super(n);
	}

	public void setFamine() {
		this.famine = true;
	}
	
	public void clearFamine() {
		this.famine = false;
	}

	public boolean hasFamine() {
		return famine;
	}

	public void setRebellion(String rebellion) {
		this.rebellion = rebellion;
	}

	public String getRebellion() {
		return rebellion;
	}
	
	public void clearRebellion() {
		rebellion = null;
	}
	
	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public String getController() {
		return controller;
	}
	
	public void clearController() {
		controller = null;
	}
}
