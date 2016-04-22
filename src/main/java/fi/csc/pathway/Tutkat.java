/**
 * perustuu ilmatieteenlaitos.fi/suomen-tutkaverkko sivuun
 */
package fi.csc.pathway;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.vaadin.addon.vol3.OLMap;
import org.vaadin.addon.vol3.client.OLCoordinate;
import org.vaadin.addon.vol3.client.style.OLIconStyle;
import org.vaadin.addon.vol3.client.style.OLStyle;
import org.vaadin.addon.vol3.feature.OLFeature;
import org.vaadin.addon.vol3.feature.OLPoint;
import org.vaadin.addon.vol3.interaction.OLSelectInteraction;
import org.vaadin.addon.vol3.interaction.OLSelectInteractionOptions;
import org.vaadin.addon.vol3.layer.OLVectorLayer;
import org.vaadin.addon.vol3.source.OLVectorSource;

import com.vaadin.ui.OptionGroup;

/**
 * @author pj
 * Nämä varmaan voisi tallentaa tietokantaan ja lukea sieltä
 */
public class Tutkat implements Serializable {
	public static final String LOW = "Low";
	static final int IKAALINEN = 0;
	static final int KORPPOO = 1;
	static final int KOUVOLA = 2;
	static final int KUOPIO = 3;
	static final int LUOSTO = 4; 
	static final int UTAJÄRVI = 5;
	static final int VANTAA = 6;
	static final int VIMPELI = 7;
	static final String[] NIMET = { "Ikaalinen", "Korppoo", "Kouvola", "Kuopio", "Luosto", "Utajärvi",
		"Vantaa", "Vimpeli" };
	static final String[] LYHENTEET = { "ika", "kor", "anj", "kuo", "luo", "uta", "van", "vim" };
	static final String[] KULMAT = { "0_3", "0_5", "0_5", "0_3", "0_7", "0_3", "0_5", "0_3" };
	static final OLCoordinate[] SIJAINNIT = {
		new OLCoordinate(23.0764, 61.7673), new OLCoordinate(21.6434, 60.1285), new OLCoordinate(27.1081, 60.9039),
		new OLCoordinate(27.3815, 62.8624), new OLCoordinate(26.8969, 67.1391), new OLCoordinate(26.3189, 64.7749),
		new OLCoordinate(24.869, 60.2706), new OLCoordinate(23.8209, 63.1048)
	};
	static final OLCoordinate[] SIJAINNIT3857 = {
		new OLCoordinate(2568853.10, 8804175.29), new OLCoordinate(2409332.27, 8428402.73),
		new OLCoordinate(3017659.89, 8603790.52), new OLCoordinate(3048094.64, 9066639.12), 
		new OLCoordinate(2994149.21, 10195802.18), new OLCoordinate(2929806.55, 9549327.42),
		new OLCoordinate(2768404.42, 8460231.99), new OLCoordinate(2651730.46, 9125994.35)
	};
	private static final long serialVersionUID = 3953325456264644583L;
	
	/**
	 * Luo tutkavalinnan ja käyttäjän valittua tutkan zoomaa kartan oikeaan kohtaan
	 * @param tutkaGroup ListSelect vaadin komponentti
	 * @param openlayersMap Openlayers map
	 */
	void alusta(OptionGroup tutkaGroup, final OLMap openlayersMap, OptionGroup kulmat, OptionGroup typesOrIntensities) { 
		for (int i = 0; i <= VIMPELI; i++) {
			tutkaGroup.addItem(i);
			tutkaGroup.setItemCaption(i, NIMET[i]);
		}
		tutkaGroup.select(VANTAA);
		
		OLVectorSource olvs = new OLVectorSource();
		
		OLIconStyle radarMarkerStyle = new OLIconStyle();
		radarMarkerStyle.src = "/radarUI-0.9/images/radar.png";
		radarMarkerStyle.scale = 0.3;
		radarMarkerStyle.anchor = new double[] {0.5, 1};
		radarMarkerStyle.anchorXUnits = "fraction";
		radarMarkerStyle.anchorYUnits = "fraction";
		OLStyle radarStyle = new OLStyle();
		radarStyle.iconStyle = radarMarkerStyle;

		OLIconStyle radarChosenMarkerStyle = new OLIconStyle();
		radarChosenMarkerStyle.src = "/radarUI-0.9/images/marker_red.png";
		radarChosenMarkerStyle.scale = 0.3;
		radarChosenMarkerStyle.anchor = new double[] {0.5, 1};
		radarChosenMarkerStyle.anchorXUnits = "fraction";
		radarChosenMarkerStyle.anchorYUnits = "fraction";
		OLStyle chosenStyle = new OLStyle();
		chosenStyle.iconStyle = radarChosenMarkerStyle;
		
		addMarkers(olvs, radarStyle, openlayersMap, tutkaGroup);

		tutkaGroup.addValueChangeListener(event -> {
			int tutkaIdx = (int)event.getProperty().getValue();
			setKulmaItemsState(tutkaIdx, typesOrIntensities, kulmat);
			openlayersMap.getView().setCenter(SIJAINNIT3857[tutkaIdx]);
			openlayersMap.getView().setZoom(OLmap.CLICKZOOM);
		});
		
		tutkaGroup.setNullSelectionAllowed(false);
	}
	
	/**
	 * Tutkien sijainnit kartalle ja määritetään myös mitä tapahtuu kun tutkaa klikataan kartalla
	 * @param olMap GoogleMap
	 */
	void addMarkers(OLVectorSource vectorSrc, OLStyle radarStyle, OLMap olMap, OptionGroup tutkaGroup) {
		for (int i = 0; i <= VIMPELI; i++) {
			OLPoint gmarker = new OLPoint(SIJAINNIT[i]);
			OLFeature olf = new OLFeature(NIMET[i]); 
			olf.setGeometry(gmarker);
			olf.setStyle(radarStyle);
			vectorSrc.addFeature(olf);
			
		}
		OLVectorLayer layer = new OLVectorLayer(vectorSrc);
		olMap.addLayer(layer);
		olMap.addInteraction(getTutkaClickInteraction(layer, tutkaGroup));
	}
	
	/**
	 * Get object for enabling clicking of the radars on the map
	 * 
	 * @param layer
	 * @param olMap
	 * @param osm
	 * @return
	 */
	private OLSelectInteraction getTutkaClickInteraction(OLVectorLayer layer, OptionGroup tutkaGroup) {
		OLSelectInteractionOptions options = new OLSelectInteractionOptions();
		options.setLayers(Arrays.asList(layer));
		OLSelectInteraction clickInteraction = new OLSelectInteraction(options);
		
		clickInteraction.addSelectionChangeListener(e -> {
			if(e != null && e.size() > 0) {
				String selectedTutkaNimi = e.get(0);
				int tutkaIdx = -1;
				for(int i=0; i < NIMET.length; i++) {
					String tutkaNimi = NIMET[i]; 
					if(tutkaNimi.equals(selectedTutkaNimi)) {
						tutkaIdx = i;
						break;
					}
				}
				if(tutkaIdx != -1) {
					tutkaGroup.select(tutkaIdx);
				}
			}
		});
		return clickInteraction;
	}
	
	protected void setKulmaItemsState(int tutkaIdx, OptionGroup typesOrIntensities, OptionGroup kulma) {
		boolean isTypeChosen = "Type".equals(typesOrIntensities.getValue());
		boolean isIntensityChosen = !isTypeChosen;
		boolean kulmaIsSelected = kulma.getValue() != null;
		Collection<String> kulmatItems = (Collection<String>) kulma.getItemIds();
		typesOrIntensities.setItemEnabled("Type", true);
		typesOrIntensities.setItemEnabled("Intensity", true);
		
		switch (tutkaIdx) {
		case 0:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("1.5".equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("1.5");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 1:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("0.7".equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("0.7");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 2:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("0.7".equals(id) || LOW.equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("0.7");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 3:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("1.5".equals(id) || LOW.equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("1.5");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 4:
			if(isTypeChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, false);
				}
				typesOrIntensities.select("Intensity");
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("0.7".equals(id) || LOW.equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("0.7");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			typesOrIntensities.setItemEnabled("Type", false);
			break;
		case 5:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("1.5".equals(id) || LOW.equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("1.5");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 6:
			if(isTypeChosen) {
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, true);
				}
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("0.7".equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("0.7");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			break;
		case 7:
			if(isTypeChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					kulma.setItemEnabled(id, false);
				}
				typesOrIntensities.select("Intensity");
			} else if(isIntensityChosen) {
				if(kulmaIsSelected) {
					kulma.unselect(kulma.getValue());
				}
				for(String id : kulmatItems) {
					if("1.5".equals(id) || LOW.equals(id)) {
						kulma.setItemEnabled(id, true);
						kulma.select("1.5");
					} else {
						kulma.setItemEnabled(id, false);
					}
				}
			}
			typesOrIntensities.setItemEnabled("Type", false);
			break;
		default:
			break;
		}
	}
}
