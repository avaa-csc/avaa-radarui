/**
 * 
 */
package fi.csc.pathway;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import org.vaadin.addon.vol3.client.OLCoordinate;

/**
 * @author pj
 *
 */
public class Result implements Serializable /*Comparable<Result>*/ {
	private static final long serialVersionUID = -9028060857053534742L;
	
	public LocalDateTime datet;
	public String dbz_limits;
	public String rain_type;
	public double area;
	public ArrayList<OLCoordinate> polygon;
	public String polygonDigest;
	
	public Result(LocalDateTime dt, String voimakkuus, String tyyppi, double pintala, ArrayList<OLCoordinate> p) {
	  this.datet = dt;
	  this.dbz_limits = voimakkuus;
	  this.rain_type = tyyppi;
	  this.area = pintala;
	  this.polygon = p;
	}
	
	public Result(LocalDateTime dt, String voimakkuus, String tyyppi,
			double pintala, ArrayList<OLCoordinate> p, String polygonDigest) {
		this.datet = dt;
		this.dbz_limits = voimakkuus;
		this.rain_type = tyyppi;
		this.area = pintala;
		this.polygon = p;
		this.polygonDigest = polygonDigest;
	}
	
	public static Comparator<Result> ResultComparator = new Comparator<Result>() {
	
	public int compare(Result t1, Result t2) {
		LocalDateTime dt1 = t1.getDatetime();
		LocalDateTime dt2 = t2.getDatetime();
		if (dt1.isEqual(dt2)) {
			return 0;
		} else if (dt1.isBefore(dt2)) {
			return -1;
		}
		return 1;
	}
	};
	
	public static Comparator<Result> ResultTIComparator = new Comparator<Result>() {
		
		public int compare(Result t1, Result t2) {
			LocalDateTime dt1 = t1.getDatetime();
			LocalDateTime dt2 = t2.getDatetime();
			int i1 = t1.getIntensity();
			int i2 = t2.getIntensity();
			if (dt1.isEqual(dt2)) {
				if (i1 == i2)
					return 0;
				if (i1 > i2) {
					return 1;
				}
				else if (i1 < i2) {
					return -1;
				}
			} else if (dt1.isBefore(dt2)) {
				return -1;
			}
			return 1;
		}
	};

	public LocalDateTime getDatetime() {
		return this.datet;
	}
	
	public int getIntensity() {
		int intensity = 0;
		switch (this.dbz_limits) {
			case "0_90":
				intensity =  0;
				break;
			case "10_90":
				intensity = 10;
				break;
			case "30_90":
				intensity = 30;
				break;
			case "45_90":
				intensity = 45;
				break;
			default:
				intensity = 0;
				break;
		}
		return intensity;
	}

	/*@Override
	public int compareTo(Result o) {
		return o.datet.isBefore(this.datet)
		return 0;
	}*/

}
