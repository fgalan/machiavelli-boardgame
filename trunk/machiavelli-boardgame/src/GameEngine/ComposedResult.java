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

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This is a Result composed by other results
 * 
 * @author fermin
 *
 */
public class ComposedResult extends Result {
	
	private final static Logger log = Logger.getLogger("ComposedResult.class");
	
	private Vector<Result> results;
	
	public ComposedResult(){
		results = new Vector<Result>();
	}
	
	public void addResult(Result r) {
		results.add(r);
	}
	
	public String toString() {
		String s = "";
		for (Iterator<Result> i = results.iterator() ; i.hasNext() ; ) {
			s = s + i.next() + "\n";
		}
		return s;	
	}	
	
}
