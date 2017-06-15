/**
 *  Pathway project funded by Academy of Finland. 
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

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import org.vaadin.addon.vol3.OLMap;
import org.vaadin.addon.vol3.OLMapOptions;
import org.vaadin.addon.vol3.OLView;
import org.vaadin.addon.vol3.OLViewOptions;
import org.vaadin.addon.vol3.client.OLCoordinate;
import org.vaadin.addon.vol3.client.control.OLAttributionControl;
import org.vaadin.addon.vol3.client.style.OLFillStyle;
import org.vaadin.addon.vol3.client.style.OLIconStyle;
import org.vaadin.addon.vol3.client.style.OLStrokeStyle;
import org.vaadin.addon.vol3.client.style.OLStyle;
import org.vaadin.addon.vol3.feature.OLFeature;
import org.vaadin.addon.vol3.feature.OLPoint;
import org.vaadin.addon.vol3.feature.OLPolygon;
import org.vaadin.addon.vol3.layer.OLTileLayer;
import org.vaadin.addon.vol3.layer.OLVectorLayer;
import org.vaadin.addon.vol3.source.*;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;

/**
 *
 */
public class OLmap implements Serializable /*, Runnable */ {
	
	private static final long serialVersionUID = 3953325456264644582L;
	private static Log log = LogFactoryUtil.getLog(OLmap.class);
	static final float MAPLEVEYS = 720; //px
	static final float MAPKORKEUS = 650; //px
	public static final int CLICKZOOM = 8;
	private static final int MINZOOM = 5;
	private static final String intens_0 = "0_90";
	private static final String intens_10 = "10_90";
	private	static final String intens_30 = "30_90";
	private static final String intens_45 = "45_90";

	final Tutkat tutkat = new Tutkat();
	private OLMap olMap;
	private Label time;
	//private Slider lkm;
	private ArrayList<Result> resultList;
	protected OptionGroup t;
	private int lastIndex;
	private int index;
	/* | ...... | ...... | ............ | ......... | ... | ....... |
	   | is a timestamp border which separates records with same timestamps
	   . is a result with a timestamp
	 */
	private Stack timeserieBorders;
	private int lastSuunta = 0;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private OptionGroup kulma;
	private OptionGroup tyyppi;
	private OptionGroup minArea;
	private OptionGroup typeOrIntensity;
	private OptionGroup intensiteetti;
	private CheckBox salamaOption;
	private ArrayList<OLVectorLayer> layerList;
	private OLVectorLayer salamaLayer;
	private List<Salama> salamat;
	
	
	/**
	 * Constructori kopioi käyttöliittymäkomponentit olion käyttöön
	 * 
	 * @param aika Label 	aikaleima tietokannasta kartalle piirrettäville sateille
	 * @param tutkat OptionGroup	Tutkan valinta
	 * @param kulma OptionGroup		Tutkan käyttämän kulman valinta
	 * @param tyyppi OptionGroup	Sateen tyyppi, vesi tai rae
	 * @param minArea OptionGroup	Sateen pinta-ala
	 */
	OLmap(Label aika, OptionGroup tutkat, OptionGroup kulma, OptionGroup tyyppi, OptionGroup minArea, OptionGroup intensiteetti, CheckBox salamaOption, OptionGroup typeOrIntensity) {	
		this.time = aika;
		this.t = tutkat;
		this.kulma = kulma;
		this.tyyppi = tyyppi;
		this.minArea = minArea;
		this.intensiteetti = intensiteetti;
		this.typeOrIntensity = typeOrIntensity;
		this.salamaOption = salamaOption;
		this.layerList = new ArrayList<>();
		this.timeserieBorders = new Stack();
		this.lastIndex = 0;
		this.index = 0;
	}
	
	
private String encodeFilter(String table, Date start, Date end, OLCoordinate[] extent, Double minimumArea, List<String> intensities) {

		String starts = formatter.format(start);
		String ends = formatter.format(end);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<wfs:GetFeature xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:gml='http://www.opengis.net/gml' xmlns:wfs='http://www.opengis.net/wfs' xmlns:ogc='http://www.opengis.net/ogc' service='WFS' version='1.0.0' xsi:schemaLocation='http://www.opengis.net/wfs'>");
		sb.append("<wfs:Query typeName='pathway:" + table + "'>");
		sb.append("<ogc:Filter>");
		sb.append("<ogc:And>");
		
		sb.append("<ogc:PropertyIsGreaterThanOrEqualTo>");
		sb.append("<ogc:PropertyName>data_time</ogc:PropertyName>");
		sb.append("<ogc:Function name=\"dateParse\">");
		sb.append("<ogc:Literal>yyyy-MM-dd HH:mm Z</ogc:Literal>");
		sb.append("<ogc:Literal>" + starts + " -0300</ogc:Literal>");
		sb.append("</ogc:Function>");
		sb.append("</ogc:PropertyIsGreaterThanOrEqualTo>");
		
		sb.append("<ogc:PropertyIsLessThanOrEqualTo>");
		sb.append("<ogc:PropertyName>data_time</ogc:PropertyName>");
		sb.append("<ogc:Function name=\"dateParse\">");
		sb.append("<ogc:Literal>yyyy-MM-dd HH:mm Z</ogc:Literal>");
		sb.append("<ogc:Literal>" + ends + " -0300</ogc:Literal>");
		sb.append("</ogc:Function>");
		sb.append("</ogc:PropertyIsLessThanOrEqualTo>");
		
		sb.append("<ogc:PropertyIsGreaterThanOrEqualTo>");
		sb.append("<ogc:PropertyName>areal</ogc:PropertyName>");
		sb.append("<ogc:Literal>" + String.valueOf(minimumArea) + "</ogc:Literal>");
		sb.append("</ogc:PropertyIsGreaterThanOrEqualTo>");
		
		if (isTypeChosen() && !isIntensityChosen()) {
			Set<String> types = (Set<String>)this.tyyppi.getValue();
			log.debug("encodeFilter intensities disabled");
			if (!types.isEmpty()) {
				log.debug("encodeFilter " + types.size() + " types");
				sb.append("<ogc:Or>");
				for (String t : types) {
					sb.append("<ogc:PropertyIsEqualTo>");
					sb.append("<ogc:PropertyName>rain_type</ogc:PropertyName>");
					sb.append("<ogc:Literal>" + t + "</ogc:Literal>");
					sb.append("</ogc:PropertyIsEqualTo>");
				}
				sb.append("</ogc:Or>");
			}
		}
		
		sb.append("<ogc:BBOX>");
		sb.append("<ogc:PropertyName>GEOMETRY</ogc:PropertyName>");			
		sb.append("<gml:Box>");
		sb.append("<gml:coordinates>" + extent[0].x + "," + extent[0].y + " " + extent[1].x + "," + extent[1].y + "</gml:coordinates>");
		sb.append("</gml:Box>");
		sb.append("</ogc:BBOX>");
		
		if (!isTypeChosen() && isIntensityChosen()) {
			sb.append("<ogc:Or>");
			for (String i : intensities) {
				sb.append("<ogc:PropertyIsEqualTo>");
				sb.append("<ogc:PropertyName>dbz_limits</ogc:PropertyName>");
				sb.append("<ogc:Literal>" + i + "</ogc:Literal>");
				sb.append("</ogc:PropertyIsEqualTo>");
			}
			sb.append("</ogc:Or>");
		}
		
		sb.append("</ogc:And>");
		sb.append("</ogc:Filter>");
		sb.append("</wfs:Query>");
		sb.append("</wfs:GetFeature>");
		
		return sb.toString();
	}
	
	/**
	 * Muodostetaan kysely palvelulle, mitä varten luetaan kaikki käyttöliimäkomponentit.
	 * 
	 * @param startd Date
	 * @param endd Date
	 * @param tutka int (tai Integer )tutkan numero
	 * @return boolean onnistuiko kysely, false jos dataa ei löytynyt 
	 */
	public boolean getMap(Date startd, Date endd, Object tutka) {
		int t = (int)tutka;
		OLView olv = olMap.getView();
		if (null == olv) {
			log.fatal("View is missing! What to do?");	
			olMap = initOpenlayersMap();
			olv = olMap.getView();
		} else {
			removeAllResultLayersFromMap();
			List<String> qIntensiteetit = getQueryIntesities();
			OLCoordinate[] extent = null;
			try {
				extent = createExtent(olv);
			} catch (NullPointerException e) {
				log.fatal(e.toString() + e.getMessage());
				log.fatal(olv.getHeight());
				log.fatal(Tutkat.SIJAINNIT[t].toString());
			}
			log.debug("Extent: " + extent[0].x + "," + extent[0].y + " " + extent[1].x + "," + extent[1].y);
			XMLparser client = new XMLparser();
			
			// assuming OptionGroup single selection mode
			if ( kulma.getValue() == null ) {
				Notification.show("No angle selected.", "Please select an angle.", Notification.Type.WARNING_MESSAGE);
				return false;
			}
			
			this.index = 0;
			this.timeserieBorders = new Stack();
			this.timeserieBorders.push(this.index);
			
			String tableType = tableSelection();
			if(tableType == null) {
				return false;
			}
			
			String table = tableType + "_" + Tutkat.LYHENTEET[t] + "_" + kulmaparsinta(t);
			Double minimumArea = Double.parseDouble(minArea.getValue().toString());
			String filter = encodeFilter(table, startd, endd, extent, minimumArea, qIntensiteetit);

			this.resultList = client.createClient("http://avaa.tdata.fi/geoserver/pathway/ows?service=WFS&version=1.0.0&request=GetFeature&outputFormat=application%2Fgml%2Bxml%3B+version%3D3.2", filter);		
			this.resultList.sort(Result.ResultTIComparator);

			if (!this.resultList.isEmpty()) {

				if (this.resultList.get(0).rain_type.length() > 0) {
					addOverlay();
				} else {
					addIntensityLayers();
				}
				//lkm.setValue(Integer.toString(this.resultList.size()));
			} else {
				Notification.show("No results for angle " + kulma.getValue(), Notification.Type.WARNING_MESSAGE);
			}
		}
		return true;	
	}
	
//	public static void getDataFromGeoserver(String taulunNimi, String tyyppi, double minArea, String intensiteetti, String alkuPvm, String loppuPvm) {
//		if(taulunNimi == null || tyyppi == null || minArea <= 0 || intensiteetti == null || alkuPvm == null || loppuPvm == null) {
//			return;
//		}
//		if(!"hclass".startsWith(taulunNimi) && !"z".startsWith(taulunNimi) && !"lightnings".equals(taulunNimi)) {
//			return;
//		}
//		if(!Ilmiot.SADE.equals(tyyppi) && !Ilmiot.SNOW.equals(tyyppi) && !Ilmiot.RAE.equals(tyyppi)) {
//			return;
//		}
//		if(!intens_0.equals(intensiteetti) && !intens_10.equals(intensiteetti) && !intens_30.equals(intensiteetti) && !intens_45.equals(intensiteetti)) {
//			return;
//		}
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		try {
//			LocalDateTime.parse(alkuPvm, dtf);
//			LocalDateTime.parse(loppuPvm, dtf);
//		} catch(DateTimeParseException dtpe) {
//			return;
//		}
//		
//	}
	
	protected boolean fetchSalamaData(Date startDt, Date endDt) {
		Salamat salamatParser = null;
		String starts = formatter.format(startDt);
		String ends = formatter.format(endDt);
		OLCoordinate[] extent = createExtent(olMap.getView());
		
		try {
			salamatParser = new Salamat(starts, ends, extent[0].x.toString(), extent[0].y.toString(), extent[1].x.toString(), extent[1].y.toString());
		} catch (IOException e) {
			log.error("IOException from Salamat class occurred");
			return false;
		}
		
		if(salamatParser.salamat.isEmpty()) {
			Notification.show("No lightnings between start and end date", Notification.Type.WARNING_MESSAGE);
			salamat = null;
			salamaOption.setValue(false);
		} else {
			salamatParser.salamat.sort(Salama.salamaComparator);
			salamat = salamatParser.salamat;
		}
		return true;
	}
	
	protected void hideSalamaLayer() {
		if(salamaLayer != null) {
			salamaLayer.setVisible(false);
		}
	}
	
	private List<String> getQueryIntesities() {
		List<String> intensities = new ArrayList<>();
		
		if(intensiteetti.getValue() != null) {
			String intensiteettiId = (String) intensiteetti.getValue();
			if("0".equals(intensiteettiId)) {
				intensities.addAll(Arrays.asList(intens_0,intens_10,intens_30,intens_45));
			} else if("10".equals(intensiteettiId)) {
				intensities.addAll(Arrays.asList(intens_10,intens_30,intens_45));
			} else if("30".equals(intensiteettiId)) {
				intensities.addAll(Arrays.asList(intens_30,intens_45));
			} else if("45".equals(intensiteettiId)) {
				intensities.add(intens_45);
			} else {
				log.warn("No valid intensity was found selected. Will fetch All option");
				intensities.addAll(Arrays.asList(intens_0,intens_10,intens_30,intens_45));
			}
		}
		return intensities;
	}
	
	private void removeAllResultLayersFromMap() {
		for (OLVectorLayer vl : layerList) {
			olMap.removeLayer(vl);
		}
	}
	
	private void printStack() {
		for (int i=0; i<this.timeserieBorders.size(); i++)
			log.debug("stack: " + this.timeserieBorders.get(i));
	}

	public boolean updateMap(int suunta) {
		
		boolean loppu = false;

		removeAllResultLayersFromMap();


		if (lastSuunta != suunta && lastSuunta != 0) {
			log.debug("updateMap: direction changed, suunta: " + suunta + " lastSuunta: " + lastSuunta + " index: " + index);
			if (suunta == -1) {
				// NEXT -> BACK
				try {
					this.timeserieBorders.pop();
					this.index = (int)this.timeserieBorders.peek();
				} catch (EmptyStackException e) {
					lastSuunta = suunta;
					this.index = 0;
					return true;
				}
			} else {
				// BACK -> NEXT
				if (this.timeserieBorders.empty()) {
					this.index = 0;
				}
				if (this.lastIndex > 0) this.timeserieBorders.push(this.lastIndex);
				this.timeserieBorders.push(this.index);
			}
			log.debug("updateMap: after direction change, index: " + index);
		} else {
			if (suunta == 1) {
				// NEXT -> NEXT
				this.timeserieBorders.push(this.index);
			} else {
				// BACK -> BACK
				try {
					this.index = (int)this.timeserieBorders.pop();
					this.lastIndex = this.index;
				} catch (EmptyStackException e) {
					lastSuunta = suunta;
					return true;
				}
			}
			log.debug("updateMap: no direction change, index: " + index);
		}

		try {
			if (this.resultList.get(this.index).rain_type.length() > 0) {
				if (addOverlay()) {
					loppu = true;
				}
			} else {
				log.debug("updateMap: addIntensityLayers, suunta: " + suunta);
				if (addIntensityLayers()) {
					loppu = true;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			loppu = true;
		}

		if (salamaOption.getValue()) {
			updateSalamaLayer();
		}
		lastSuunta = suunta;

		return loppu;
	}
	
	protected void updateSalamaLayer() {
		olMap.removeLayer(salamaLayer);
		OLVectorSource salamatSrc = new OLVectorSource();
		OLVectorLayer newSalamaLayer = null;
		LocalDateTime currentTime = (LocalDateTime) time.getData();
		LocalDateTime acceptableTimeLowerLimit = currentTime.minusSeconds(150);
		LocalDateTime acceptableTimeUpperLimit = currentTime.plusSeconds(150);
		if(salamat != null && !salamat.isEmpty()) {
			for(Salama s : salamat) {
				LocalDateTime salamaTime = s.getDateTime();
				if(salamaTime.isAfter(acceptableTimeLowerLimit) && salamaTime.isBefore(acceptableTimeUpperLimit)) {
						salamatSrc.addFeature(new OLPoint(s.getLocation()));
				}
			}
			newSalamaLayer = new OLVectorLayer(salamatSrc);
			OLIconStyle salamaStyle = new OLIconStyle();
			salamaStyle.src = "/radarUI-0.9/images/black_lightning_bolt.svg";
			salamaStyle.scale = 0.05;
			salamaStyle.anchor = new double[] {1.0, 1};
			salamaStyle.anchorXUnits = "fraction";
			salamaStyle.anchorYUnits = "fraction";
			OLStyle style = new OLStyle();
			style.iconStyle = salamaStyle;
			newSalamaLayer.setStyle(style);
			newSalamaLayer.setVisible(true);
			olMap.addLayer(newSalamaLayer);
		}
		salamaLayer = newSalamaLayer;
	}
	
	
	/**	
	 * @return  boolean true in the end of record list
	 */
	private boolean addOverlay() {
		OLVectorSource olvs = new OLVectorSource();
		OLVectorLayer layer = new OLVectorLayer(olvs);
		olMap.addLayer(layer);
		layerList.add(layer);			
		
		LocalDateTime utcDateTime = this.resultList.get(this.index).datet;
		// Set date label in UTC time
		time.setValue("UTC time: " + utcDateTime.toString());	
		time.setData(utcDateTime);
		
		try {
			while ((time.getData()).equals(this.resultList.get(this.index).datet)) {
				
				OLPolygon olp = new OLPolygon(polygonToArray(this.resultList.get(this.index).polygon));
				
				/*if (! prev_rain_type.equals(this.resultList.get(this.index).rain_type))
					log.info(this.resultList.get(this.index).rain_type); */
				
				OLStyle style = getRainTypeStyle(this.resultList.get(this.index).rain_type);
				
				OLFeature f = new OLFeature();
				f.setGeometry(olp);
				
				f.setStyle(style);
				olvs.addFeature(f);
				
				this.index++;
				if (this.index > this.resultList.size()-1)
					return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}
	

	/**	
	 * @return  boolean true in the end of record list
	 */
	private boolean addIntensityLayers() {

		int prev_intensity = this.resultList.get(this.index).getIntensity(); 
		
		OLVectorSource olvs = new OLVectorSource();
		OLVectorLayer layer = new OLVectorLayer(olvs);
		olMap.addLayer(layer);
		layerList.add(layer);
		
		LocalDateTime utcDateTime = this.resultList.get(this.index).datet;
		// Set date label in UTC time
		time.setValue("UTC time: " + utcDateTime.toString());	
		time.setData(utcDateTime);
		
		// Assuming that jcr is sorted by time and intensity
		try {
			while ((time.getData()).equals(this.resultList.get(this.index).datet)) {
				
				log.debug("addOverlay: datestamp: " + this.resultList.get(this.index).datet + " index: " + this.index + " intensity: " + this.resultList.get(this.index).getIntensity());
				if (prev_intensity < this.resultList.get(this.index).getIntensity()) {
					// if intensity increases, create new source and layer
					olvs = new OLVectorSource();
					layer = new OLVectorLayer(olvs);
					olMap.addLayer(layer);
					layerList.add(layer);
				}
				
				OLPolygon olp = new OLPolygon(polygonToArray(this.resultList.get(this.index).polygon));
				OLFeature f = new OLFeature();
				f.setGeometry(olp);
				
				OLStyle style = new OLStyle();
				style.fillStyle = new OLFillStyle(getIntensityFillColor(this.resultList.get(this.index).getIntensity()));
				OLStrokeStyle strokeStyle = new OLStrokeStyle();
				new OLStrokeStyle().color = "rgb(0,0,0)";
				style.strokeStyle = strokeStyle;
				f.setStyle(style);
				
				olvs.addFeature(f);
				
				prev_intensity = this.resultList.get(this.index).getIntensity();
				
				this.index++;
				if (this.index > this.resultList.size()-1)
					return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}
		
	private String getIntensityFillColor(int intensity) {
		switch(intensity) {
			case 0:
				return "rgba(225,232,255,1)";
				//return "rgba(215,222,255,1.0)";
				//return "blue";
			case 10:
				return "rgba(163,183,255,1)";
				//return "rgba(153,173,255,1.0)";
				//return "green";
			case 30:
				return "rgba(83,119,255,1)";
				//return "rgba(73,109,255,1.0)";
				//return "yellow";
			case 45:
				return "rgba(18,67,255,1)";
				//return "rgba(8,57,255,1.0)";
				//return "red";
			default:
				return "white";
		}
	}
	
	private OLStyle getRainTypeStyle(String rain_type) {
		
		OLStyle style = new OLStyle();
		switch (rain_type) {
		case "snow":
			style.fillStyle = new OLFillStyle("rgba(0,0,100,0.8)");
			break;
		case "hail":
			style.fillStyle = new OLFillStyle("rgba(55,10,140,0.8)");
			break;
		default:
			style.fillStyle = new OLFillStyle("rgba(0,150,140,0.8)");
		}
		
		OLStrokeStyle strokeStyle = new OLStrokeStyle();
		new OLStrokeStyle().color = "rgb(0,0,0)";
		style.strokeStyle = strokeStyle;
		
		return style;
	}

	/**
	 * Sovelluksen käynnistyessä näytettävä Suomen kartta, jolla tutkasemien sijainnit
	 * @return Component OLMap
	 */
	public OLMap initOpenlayersMap() {
		OLViewOptions options=new OLViewOptions();
		options.setMinZoom(MINZOOM);
		OLView olv = new OLView(options);
		olv.setZoom(MINZOOM);
		olv.setCenter(2749591.42, 9598632.29);		
		//olv.setCenter(new OLCoordinate(24.7, 64.963));
		
		OLMapOptions mapOptions = new OLMapOptions();
		mapOptions.setShowOl3Logo(false);
		
		olMap = new OLMap(mapOptions);
		olMap.setView(olv);
		OLTileWMSSourceOptions srcOpts = new OLTileWMSSourceOptions();
		Map<String, String> params = new HashMap<>();
		params.put("LAYERS", "osm-finland");
		params.put("VERSION", "1.1.0");
		srcOpts.setParams(params);
		srcOpts.setUrl("http://avaa.tdata.fi/geoserver/osm_finland/gwc/service/wms");
		srcOpts.setAttributions(new String[]{"&copy; <a target=\"_blank\" href=\"http://avaa.tdata.fi/openstreetmap\">OKM AVAA</a>. Data: &copy; <a target=\"_blank\" href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap contributors</a>"});
		OLTileWMSSource OSM = new OLTileWMSSource(srcOpts);
		olMap.addLayer(new OLTileLayer(OSM));

		olMap.setWidth(MAPLEVEYS, Sizeable.Unit.PIXELS);
		olMap.setHeight(MAPKORKEUS, Sizeable.Unit.PIXELS);
		
		return olMap;
	}
	
	private OLCoordinate[] createExtent(OLView olv) {
		
		OLCoordinate center = olv.getCenter();
		log.debug("center " + center.toString());

		Double resolution = olv.getResolution();
		log.debug("resolution: " + String.valueOf(resolution));

		Double halfX = ((double) MAPLEVEYS * resolution) / 2;
		Double halfY = ((double) MAPKORKEUS * resolution) / 2;

		log.debug("halfX: " + String.valueOf(halfX) + " halfY: "	+ halfY);
		
		OLCoordinate p1 = new OLCoordinate(center.x-halfX, center.y-halfY);
		OLCoordinate p3 = new OLCoordinate(center.x+halfX, center.y+halfY);
		
		int zoom = olv.getZoom();
		log.debug("zoom: " + zoom);

		return extent(p1, p3);
	}
	
	private OLCoordinate[] extent(OLCoordinate p1, OLCoordinate p3) {

		OLCoordinate d_p1 = EPSG900913_WGS84Datum(p1);
		OLCoordinate d_p3 = EPSG900913_WGS84Datum(p3);

		log.debug("d p1 " + d_p1.toString());
		log.debug("d p3 " + d_p3.toString());
		
		OLCoordinate[] extent = new OLCoordinate[2];
		extent[0] = d_p1;
		extent[1] = d_p3;
		
		return extent;
	}
	
	private OLCoordinate EPSG900913_WGS84Datum(OLCoordinate c) {

		// lon = (mx / self.originShift) * 180.0
		// lat = (my / self.originShift) * 180.0
		// lat = 180 / math.pi * (2 * math.atan( math.exp( lat * math.pi /
		// 180.0)) - math.pi / 2.0)

		Double originShift = 20037508.342789244;

		Double lon = (c.x / originShift) * 180.0;
		Double lat = (c.y / originShift) * 180.0;

		lat = 180
				/ Math.PI
				* (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);

		return new OLCoordinate(lon, lat);
	}
	
	/**
	 * Käsitelee kulmanvalinnan arvon. Eri tutkilla on eri matala kulma!
	 * @param kulma2 OptionGroup kulman valintaan
	 * @return String tietokantataulun nimen osa
	 */
	/*
	private Object[] kulmaparsinta(OptionGroup kulmaValinta, int t) {
		String kulma = kulmaValinta.getValue().toString();
		if (kulma == null || kulma.isEmpty()) {
			return null;
		}
		ArrayList<String> al = new ArrayList<String>();
		if (kulma.equals(Tutkat.LOW)) {
			al.add(Tutkat.KULMAT[t]);
		}  else {
			al.add(kulma.replaceFirst("\\.", "_"));  //tietokantataulun nimessä on alaviiva
		}
		Set<String> kulmat = (Set<String>) kulmaValinta.getValue();
		if (kulmat.isEmpty()) {
			return null;
		}
		ArrayList<String> al = new ArrayList<String>();
		for (String kulma : kulmat) {
			if (kulma.equals(Tutkat.LOW)) {
				al.add(Tutkat.KULMAT[t]);
			}  else {
				al.add(kulma.replaceFirst("\\.", "_"));  //tietokantataulun nimessä on alaviiva
			}
		}
	
		return al.toArray();
	}
		*/
	
	private String kulmaparsinta(int t) {
		String kulmat = (String)this.kulma.getValue();
		
		if (kulmat == null) {
			return null;
		}
		if (kulmat.equals(Tutkat.LOW)) {
			return Tutkat.KULMAT[t];
		} 
		// tietokantataulun nimessä on alaviiva
		return kulmat.replaceFirst("\\.", "_");  
	}
	
	/**
	 * Converts (intensity) OptionGroup (single) selection to a table prefix. If selection is -1,
	 * intensity queries are disabled and hydroclass tables are queried instead.
	 *
	 * @param intensitySelection OptionGroup
	 * @return String Table prefix
	 */
	private String tableSelection() {
		String table_prefix = null;
		
		if (isIntensityChosen()) {
			table_prefix = new String("z");
		} else if (isTypeChosen()) {
			table_prefix = new String("hclass");
		} else {
			new Notification("Please choose either type or intensity",
        		    "",
        		    Notification.Type.WARNING_MESSAGE, false)
        		    .show(Page.getCurrent());
		}
		return table_prefix;
	}
	
	OLCoordinate[] polygonToArray(ArrayList<OLCoordinate> p) { 
		return p.toArray(new OLCoordinate[p.size()]);
	}
	
	private boolean isIntensityChosen() {
		if ("Intensity".equals(typeOrIntensity.getValue()) && intensiteetti.getValue() != null) {
			return true;
		}
		return false;
	}
	
	private boolean isTypeChosen() {
		if ("Type".equals(typeOrIntensity.getValue()) && !((Set<String>) tyyppi.getValue()).isEmpty()) {
			return true;
		}
		return false;
	}
	
}
