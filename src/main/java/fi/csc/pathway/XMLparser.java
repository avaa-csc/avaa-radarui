/**
 * Stax-parser
 * 
 * TutkaUI -portlet by CSC - IT Center for Science Ltd. is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 *  Permissions beyond the scope of this license may be available at http://www.csc.fi/english/contacts.
 *  
 * <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a><br />
 * <span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">TutkaUI-portlet</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="http://avaa.tdata.fi/" property="cc:attributionName" rel="cc:attributionURL">CSC - IT Center for Science Ltd.</a> is licensed under a
 *  <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.<br />
 * Permissions beyond the scope of this license may be available at <a xmlns:cc="http://creativecommons.org/ns#" href="http://www.csc.fi/english/contacts" rel="cc:morePermissions">http://www.csc.fi/english/contacts</a>.
 */
package fi.csc.pathway;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

//import java.util.Arrays;
//import java.security.MessageDigest;

/**
 * <XmlRootElement>
<datetime>2013-07-01 08:10:00.0</datetime>
<polygon>
POLYGON((24.1747 67.4622,24.186 67.461,24.2113 67.478,24.2261 67.4962,24.2307 67.5157,24.2473 67.5337,24.2757 67.55,24.2473 67.5337,24.2307 67.5157,24.2038 67.4989,24.1814 67.5017,24.1747 67.4622))
</polygon>
<datetime>2013-07-01 08:15:00.0</datetime>
<polygon>
POLYGON((24.3684 67.6816,24.4031 67.6965,24.4238 67.6922,24.3928 67.6987,24.3684 67.6816))
</polygon>
...
 * @author pj
 * As standard read XML content of the URL as possible 
 * 
 *  NOTE Java 8
 */
public class XMLparser {
	private static Log log = LogFactoryUtil.getLog(XMLparser.class);
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private int counter = 0;

	/**
	 * The name of the method come from JAXB, but didn't get it to unmarshal 
	 * @param url String 
	 * @return ArrayList<Result> The result contains one datetime and polugon
	 */
	public ArrayList<Result> createClient(String url, String urlParameters) {
		ArrayList<Result>cr  = new ArrayList<Result>();
		//ArrayList<ArrayList<LatLon>> polygons =  new ArrayList<ArrayList<LatLon>>();
		PolygonParser pp = new PolygonParser();
		try {
			URL u;
			try {
				u = new URL(url);
			} catch ( java.net.MalformedURLException e) {
				log.error("java.net.MalformedURLException" + url);
				return cr;
			}
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			InputStream in = con.getInputStream();
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(in);
			int eventType = parser.next();
			while((eventType == XMLStreamConstants.CHARACTERS && parser.isWhiteSpace()) // skip whitespace
					|| (eventType == XMLStreamConstants.CDATA && parser.isWhiteSpace())
					// skip whitespace
					|| eventType == XMLStreamConstants.SPACE
					|| eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
					|| eventType == XMLStreamConstants.COMMENT
					) {
				eventType = parser.next();
			}
			if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
				System.out.println("expected start or end tag"+ parser.getLocation());
			}
	    	LocalDateTime dt = null;
	    	String dbz_limits = "";
	    	String rain_type = "";
	    	Double area = 0.0;
			while (true) {
				if(!parser.hasNext()) {
					log.warn("Reached end of document abnormally which indicates fetched document format was invalid. No table for the query?");
					return new ArrayList<Result>();
				}
				int event = parser.next();
				if (event == XMLStreamConstants.END_ELEMENT) {
			       if (parser.getLocalName().equals("FeatureCollection")) {
			    	   parser.close();
			    	   break;
			       }
			    }
			    if (event == XMLStreamConstants.START_ELEMENT) {
			        if (parser.getLocalName().equals("data_time")) {
			        	//dates.add(parser.getElementText());
			        	dt = LocalDateTime.parse(parser.getElementText().trim(), dtf);
			        	int e2 = parser.nextTag();
			        	if (e2 != XMLStreamConstants.START_ELEMENT) {
			        		System.out.println("Syntax error: "+parser.getLocalName());
			        	}
			        	if  (parser.getLocalName().equals("dbz_limits")) {
			        		dbz_limits = parser.getElementText();			        		
			        	}
			        	if  (parser.getLocalName().equals("rain_type")) {
			        		rain_type = parser.getElementText();			        		
			        	}
			        	e2 = parser.nextTag();
			        	if (e2 != XMLStreamConstants.START_ELEMENT) {
			        		System.out.println("Syntax error: "+parser.getLocalName());
			        	}
			        	if  (parser.getLocalName().equals("areal")) {
			        		try {
			        			area = Double.parseDouble(parser.getElementText());
			        		} catch (NumberFormatException nfe) {
			        			log.error("Unable to parse, NumberFormatException");
			        		}
			        	}
		        	
			        } else {
			        	if  (parser.getLocalName().equals("posList")) {
			        		String elementText = parser.getElementText();
			        		//MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			        		//String polygonDigest = Arrays.toString(messageDigest.digest(elementText.getBytes()));

		        			//cr.add( new Result(dt, dbz_limits, rain_type, area, pp.parse(elementText), polygonDigest));
			        		if(dt != null) {
			        			cr.add( new Result(dt, dbz_limits, rain_type, area, pp.parse(elementText)));
				        		counter++;
			        		}
			        	}
			        }
			    }
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("Created results: " + counter);
		return  cr;
	}
}
