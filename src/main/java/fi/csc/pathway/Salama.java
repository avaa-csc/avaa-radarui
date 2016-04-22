/**
 * 
 */
package fi.csc.pathway;

import java.time.LocalDateTime;
import java.util.Comparator;

import org.vaadin.addon.vol3.client.OLCoordinate;

/**
 * yksi salama parsittuna
 * http://avaa.tdata.fi/geoserver/pathway/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=pathway:lightnings&maxFeatures=50&outputFormat=application%2Fgml%2Bxml%3B+version%3D3.2
 * 
 * @author pj
 *
 */
public class Salama {
	
	private LocalDateTime dt;
	private OLCoordinate piste;
	private int lkm;
	private double peak_current;
	
	
	/**
	 * Konstruktori
	 */
	 public Salama(LocalDateTime dt, double lat, double lon, String lkm, double peak_current) {
	 
		this.dt = dt;
		this.piste = new OLCoordinate(lat, lon);
		this.lkm = Integer.parseInt(lkm);
		this.peak_current = peak_current;		
	}
	
	public LocalDateTime getDateTime() {
		return this.dt;
	}
	
	public OLCoordinate getLocation() {
		return piste;
	}

	/**
	 * tätkin voinee visualisoida, jos yksi salama on keltaisella, kaksi punaisella 
	 */
	public int getCount() {
		return lkm;
	}

	/**
	 * Tämän voisi visulisoida vaikka salaman koolla
	 * @return double peak_current
	 */
	public double getPeakCurrent() {
		return peak_current;
	}
	
	public static Comparator<Salama> salamaComparator 
    = new Comparator<Salama>() {
	
		public int compare(Salama t1, Salama t2) {
			LocalDateTime dt1 = t1.getDateTime();
			LocalDateTime dt2 = t2.getDateTime();
			if (dt1.isEqual(dt2)) {
				return 0;
			} else if (dt1.isBefore(dt2)) {
				return -1;
			}
			return 1;
		}
	};
}
