/**
 * Pathway project funded by Academy of Finland. 
 *  
 *  TutkaUI -portlet by CSC - IT Center for Science Ltd. is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 *  Permissions beyond the scope of this license may be available at http://www.csc.fi/english/contacts.
 *  
 * <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License"
 *  style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a><br />
 * <span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">TutkaUI-portlet</span> by <a xmlns:cc="http://creativecommons.org/ns#"
 *  href="http://avaa.tdata.fi/web/" property="cc:attributionName" rel="cc:attributionURL">CSC - IT Center for Science Ltd.</a>
 *  is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.<br />
 *  Permissions beyond the scope of this license may be available at <a xmlns:cc="http://creativecommons.org/ns#"
 *   href="http://www.csc.fi/english/contacts" rel="cc:morePermissions">http://www.csc.fi/english/contacts</a>.
 */
package fi.csc.pathway;


import java.util.ArrayList;

import org.vaadin.addon.vol3.client.OLCoordinate;

//import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * @author pj
 *  24.7725 59.1321 24.7711 59.1141  ...
 */
public class PolygonParser {
	
	
	/**
	 * Parsii postgis polygonin vaadin-kirjaston googlemaps addon:lle.
	 * @param text String 24.7725 59.1321 24.7711 59.1141 ...
	 * @return ArrayList<OLCoordinate> 
	 */
	public ArrayList<OLCoordinate> parse(String text) {
		ArrayList<OLCoordinate> points = new ArrayList<OLCoordinate>();		
		try {
			String[] pisteet = text.split(" ");
			for (int i=0; i < pisteet.length; i = i + 2) {
				double lat, lon;
				try {
					lat = Double.parseDouble(pisteet[i]); 
					lon = Double.parseDouble(pisteet[i+1]);
					points.add(new OLCoordinate(lat, lon)); //(x, y)
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					System.err.println(e.getMessage());
					System.err.println(text);
				}
			}
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			System.err.println(e.getMessage());
			System.err.println(text);	
		}
		return points;
	}
}
