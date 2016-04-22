/**
 * Parsii salamat GML3.2:sta
 */
package fi.csc.pathway;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

//import org.vaadin.addon.vol3.client.OLCoordinate;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author pj
 *
 */
public class Salamat {
	private static final String URL = "http://avaa.tdata.fi/geoserver/pathway/ows?service=WFS&version=1.0.0&request=GetFeature&outputFormat=application%2Fgml%2Bxml%3B+version%3D3.2";
	public static final String SCHEMA = "<wfs:GetFeature xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
			+ "xmlns:gml='http://www.opengis.net/gml' xmlns:wfs='http://www.opengis.net/wfs' xmlns:ogc='http://www.opengis.net/ogc' service='WFS' version='1.0.0' "
			+ "xsi:schemaLocation='http://www.opengis.net/wfs'>"
			+ "<wfs:Query typeName='pathway:lightnings'><ogc:Filter><ogc:And>";
	public static final String AIKA1 = "<ogc:PropertyIsGreaterThanOrEqualTo><ogc:PropertyName>time</ogc:PropertyName><ogc:Function name='dateParse'><ogc:Literal>yyyy-MM-dd</ogc:Literal><ogc:Literal>";
	public static final String AIKA2 = "</ogc:Literal></ogc:Function></ogc:PropertyIsGreaterThanOrEqualTo><ogc:PropertyIsLessThan><ogc:PropertyName>time</ogc:PropertyName><ogc:Function name='dateParse'><ogc:Literal>yyyy-MM-dd</ogc:Literal><ogc:Literal>";
	public static final String AIKA3 = "</ogc:Literal></ogc:Function></ogc:PropertyIsLessThan>";
	public static final String BBOX_START = "<ogc:BBOX><ogc:PropertyName>GEOMETRY</ogc:PropertyName><gml:Box><gml:coordinates>";
	public static final String BBOX_COMMA = ",";
	public static final String BBOX_SPACE = " ";		
	public static final String BBOX_END = "</gml:coordinates></gml:Box></ogc:BBOX></ogc:And></ogc:Filter></wfs:Query></wfs:GetFeature>";
	DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
	ArrayList<Salama> salamat = new ArrayList<Salama>();		
	/**
	 * Konstruktori, joka myös tekee kyselyn geoserverille. Esimerkkikutsu kommenteissa lopussa pääohjelmassa
	 * 
	 * @param alkuaika String esim. "2014-06-01"
	 * @param loppuaika String esim. "2014-06-02"
	 * @param lx String esim. 22.891460968600388
	 * @param ly String esim. 59.37322296345692
	 * @param ux String esim. "26.846539093600388"
	 * @param uy String esim. "61.14402299216982"
	 * @throws IOException
	 */
	public Salamat(String alkuaika, String loppuaika, String lx, String ly, String ux, String uy) throws IOException {
		URL u = null;
		try {
			u = new URL(URL);
		} catch ( java.net.MalformedURLException e) {
			System.err.println("java.net.MalformedURLException" + URL);
		}
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			System.out.println(SCHEMA+AIKA1+alkuaika+AIKA2+loppuaika+AIKA3+BBOX_START+lx+BBOX_COMMA+ly+BBOX_SPACE+ux+BBOX_COMMA+uy+BBOX_END);
			wr.writeBytes(SCHEMA+AIKA1+alkuaika+AIKA2+loppuaika+AIKA3+BBOX_START+lx+BBOX_COMMA+ly+BBOX_SPACE+ux+BBOX_COMMA+uy+BBOX_END);
			wr.flush();
			wr.close();
			InputStream in = con.getInputStream();
			XMLStreamReader parser = factory.createXMLStreamReader(in);		
			while (true) {
				int event = parser.next();
				if (event == XMLStreamConstants.END_ELEMENT) {
			       if (parser.getLocalName().equals("FeatureCollection")) {
			    	   parser.close();
			    	   break;
			       }
			    }
			    if (event == XMLStreamConstants.START_ELEMENT) {
			    	LocalDateTime dt = null;
			    	//int lkm = 0;
			    	Double  peak_current= 0.0;
			        if (parser.getLocalName().equals("pos")) {
			        	String[] piste = parser.getElementText().split(" ");
			        	double lat = Double.parseDouble(piste[0]); 
			        	double lon = Double.parseDouble(piste[1]);
			        	
			        	while (!parser.getLocalName().equals("time")) {
			        		parser.nextTag();
			        	}
			        	dt = LocalDateTime.parse(parser.getElementText().trim(), f);
			        	while (!parser.getLocalName().equals("multiplisity")) {
			        		parser.nextTag();
			        	}
			        	String lkm = parser.getElementText();
			        	while (!parser.getLocalName().equals("peak_current")) {
			        		parser.nextTag();
			        	}
		        		try {
		        			peak_current = Double.parseDouble(parser.getElementText());
		        		} catch (NumberFormatException nfe) {
		        			//log.error("Unable to parse, NumberFormatException");
		        		}
			        	salamat.add(new Salama(dt, lat, lon, lkm, peak_current));
			        }
			    }
			}
			
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Lopputulos
	 * @return ArrayList<Salama>
	 */
	public ArrayList<Salama> getSalamat() {
		return salamat;
	}
	/*
	public static void main( String[] args ) {
	
		try {
			Salamat s = new Salamat("2014-06-01", "2014-06-03", "22.891460968600388",
					"59.37322296345692", "26.846539093600388", "61.14402299216982");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
}
