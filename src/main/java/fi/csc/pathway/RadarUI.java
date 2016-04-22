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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin.addon.vol3.OLMap;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
@SuppressWarnings("serial")
@Title("RadarUI")
public class RadarUI extends UI {

	protected static final String SADE = "rain";
	protected static final String LUMI = "snow";
	protected static final String RAE = "hail";
//	protected static final String SALAMA = "Lightnings";
	
 	private static final long serialVersionUID = 2872281444404886611L;
	private static Log log = LogFactoryUtil.getLog(RadarUI.class);
	static final int NEXT = 1;
	static final int BACK = -1;
	private final Button playVideoBtn = new Button();
	private final Button rewindVideoBtn = new Button();
			
    @WebServlet(value = "/VAADIN/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = RadarUI.class)
    public static class Servlet extends VaadinServlet {
    }
     
    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.UI#refresh(com.vaadin.server.VaadinRequest)
     * 
     * On every page refresh, re-initialize the application so that the map is also displayed
     */
    protected void refresh(VaadinRequest request) {
        init(request);
    }
    
    /**
     * Pääohjelma, joka luo käyttöliittymän.
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void init(VaadinRequest request) {
    	final HorizontalLayout mainLayout  = new HorizontalLayout(); //main
    	final VerticalLayout parametriPalkki = new VerticalLayout();   
        parametriPalkki.setWidth(500, Unit.PIXELS);
        final HorizontalLayout karttaPalkki = new HorizontalLayout();
        final GridLayout parametrit = new GridLayout(4,3);    
    	
    	this.playVideoBtn.setStyleName("play-video-btn");
    	this.playVideoBtn.setImmediate(true);
    	this.playVideoBtn.setVisible(false);
    	setPlayVideoBtnToPlay();
    	
    	this.rewindVideoBtn.setStyleName("rewind-video-btn");
    	this.rewindVideoBtn.setImmediate(true);
    	this.rewindVideoBtn.setVisible(false);
    	setRewindVideoBtnToPlay();
    	
    	final CheckBox salamaOption = new CheckBox();
    	salamaOption.setImmediate(true);
    	salamaOption.setValue(false);
    	salamaOption.setStyleName("salama-control-checkbox");
    	salamaOption.setIcon(FontAwesome.FLASH);
    	salamaOption.setVisible(false);
    	salamaOption.setCaption("Show lightnings on the visible part of the map");
    	
    	final OptionGroup typeOrIntensityGroup = new OptionGroup();
    	typeOrIntensityGroup.addItem("Type");
    	typeOrIntensityGroup.addItem("Intensity");
    	typeOrIntensityGroup.setNullSelectionAllowed(false);
    	
    	final OptionGroup tyyppi = new OptionGroup("Type");	
    	tyyppi.setMultiSelect(true);
    	tyyppi.addStyleName("type-option-container");
    	tyyppi.addItem(SADE);
		tyyppi.addItem(LUMI);
		tyyppi.addItem(RAE);
		tyyppi.setItemIcon(SADE, FontAwesome.SQUARE);
		tyyppi.setItemIcon(LUMI, FontAwesome.SQUARE);
		tyyppi.setItemIcon(RAE, FontAwesome.SQUARE);
    	
    	final OptionGroup intensiteettiGroup = new OptionGroup("Intensity (dBZ)");
    	intensiteettiGroup.setStyleName("intensity-option-container");
    	intensiteettiGroup.addItems("0", "10", "30", "45");
    	intensiteettiGroup.setItemCaption("0", "All");
    	intensiteettiGroup.setItemCaption("10", "> 10");
    	intensiteettiGroup.setItemCaption("30", "> 30");
    	intensiteettiGroup.setItemCaption("45", "> 45");
    	intensiteettiGroup.setItemIcon("0", FontAwesome.SQUARE);
    	intensiteettiGroup.setItemIcon("10", FontAwesome.SQUARE);
    	intensiteettiGroup.setItemIcon("30", FontAwesome.SQUARE);
    	intensiteettiGroup.setItemIcon("45", FontAwesome.SQUARE);
    	intensiteettiGroup.setNullSelectionAllowed(false);
    	
        final HorizontalLayout resultLayout = new HorizontalLayout();
        resultLayout.setStyleName("result-container");
        final Label hr = new Label("  ________________________________________________", ContentMode.HTML);
        final Label aika = new Label("Time", ContentMode.HTML);
        
        final Button stepFwdBtn = new Button("Step fwd");
        stepFwdBtn.addStyleName("step-forward-btn");
        stepFwdBtn.setIcon(FontAwesome.STEP_FORWARD);
        final Button stepBackBtn = new Button("Step back");
        stepBackBtn.addStyleName("step-back-btn");
        stepBackBtn.setIcon(FontAwesome.STEP_BACKWARD);
        
		final OptionGroup kulma = new OptionGroup("Angle");
		kulma.setIcon(FontAwesome.QUESTION_CIRCLE);
		kulma.setDescription("Greyed out angles are not available for selected radar");
		kulma.addItem("1.5");
		kulma.addItem("0.7");
		kulma.addItem(Tutkat.LOW); 
		kulma.setValue("0.7");

		final OptionGroup tutka = new OptionGroup("Radar");
		final Tutkat tutkat = new Tutkat();
		
		final OptionGroup minArea =  new OptionGroup("Area (sq. km)");
		minArea.addItem(0.5);
		minArea.addItem(50);
		minArea.addItem(500);
		minArea.setValue(0.5);
		minArea.setItemCaption(0.5, "> 0.5");
		minArea.setItemCaption(50, "> 50");
		minArea.setItemCaption(500, "> 500");
		
		final Button queryBtn = new Button("Query");
		queryBtn.setStyleName("query-button");
		
        final OLmap osm = new OLmap(aika, tutka, kulma, tyyppi, minArea, intensiteettiGroup, salamaOption, typeOrIntensityGroup);
        final OLMap openlayersMap = osm.initOpenlayersMap();
        tutkat.alusta(tutka, openlayersMap, kulma, typeOrIntensityGroup);

        final PopupDateField startDate = new PopupDateField("From (UTC)");	
		startDate.setStyleName("query-startdate");
		final PopupDateField endDate = new PopupDateField("To (UTC)");
		endDate.setStyleName("query-enddate");
		 
	    parametrit.addComponent(tutka, 0, 0);
		
	    final VerticalLayout mParams = new VerticalLayout();
	    mParams.setSizeUndefined();
	    mParams.addStyleName("parameter-middle-column");
	    mParams.addComponent(typeOrIntensityGroup);
	    mParams.addComponent(tyyppi);
	    mParams.addComponent(intensiteettiGroup);
		parametrit.addComponent(mParams, 1, 0);	
		
	    final VerticalLayout rParams = new VerticalLayout();
	    rParams.setSizeUndefined();
	    rParams.addStyleName("parameter-right-column");
	    rParams.addComponent(kulma);
	    rParams.addComponent(minArea);
	    parametrit.addComponent(rParams, 2, 0);
	    
	    InputDates tarkista = new InputDates(endDate, startDate);
	    
	    parametrit.addComponent(tarkista.getStartDateField(), 0, 1, 1, 1);
	    parametrit.addComponent(tarkista.getEndDateField(), 2, 1, 3, 1);
	    parametrit.addComponent(queryBtn, 0, 2);
	    
		parametriPalkki.addComponent(parametrit);
		
		tyyppi.addValueChangeListener(e -> {
			if(((Set<String>) e.getProperty().getValue()).isEmpty()) {
				new Notification("Choose at least one precipitation type",
	        		    "",
	        		    Notification.Type.WARNING_MESSAGE, false)
	        		    .show(Page.getCurrent());
				Set<String> set = new HashSet<>();
				set.add(SADE);
				tyyppi.setValue(set);
			}
		});
		
		typeOrIntensityGroup.addValueChangeListener(e -> {
			if(e.getProperty().getValue() != null) {
				if("Type".equals(typeOrIntensityGroup.getValue())) {
					tyyppi.setVisible(true);
					intensiteettiGroup.setVisible(false);
					Set<String> set = new HashSet<>();
					set.add(SADE);
					tyyppi.setValue(set);
				} else if("Intensity".equals(typeOrIntensityGroup.getValue())) {
					tyyppi.setVisible(false);
					intensiteettiGroup.setVisible(true);
					intensiteettiGroup.setValue("0");
				}
				tutkat.setKulmaItemsState((int)tutka.getValue(), typeOrIntensityGroup, kulma);
			}
		});
		typeOrIntensityGroup.setValue("Type");
		
		stepFwdBtn.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8146345475859196611L;

			public void buttonClick(ClickEvent event) {            	
            	//gma.play(vertslider.getValue());
				if (osm.updateMap(NEXT)) {
					setPlayVideoBtnToPlay();
					playVideoBtn.setEnabled(false);
					stepFwdBtn.setEnabled(false);
					stepBackBtn.setEnabled(true);
	    			salamaOption.setEnabled(true);
	    			queryBtn.setEnabled(true);
				}
				if("play".equals(playVideoBtn.getData())) {
					rewindVideoBtn.setEnabled(true);
					stepBackBtn.setEnabled(true);
				}
            }
         });
		stepBackBtn.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8146345475859196610L;

			public void buttonClick(ClickEvent event) {            	
            	//gma.play(vertslider.getValue());
				if (osm.updateMap(BACK)) {
					setRewindVideoBtnToPlay();
					rewindVideoBtn.setEnabled(false);
					stepFwdBtn.setEnabled(true);
					stepBackBtn.setEnabled(false);
					salamaOption.setEnabled(true);
	    			queryBtn.setEnabled(true);
					
				}
				if("play".equals(rewindVideoBtn.getData())) {
					playVideoBtn.setEnabled(true);
					stepFwdBtn.setEnabled(true);
				}
            }
         });
		
		// When video button is clicked, depending on its state as indicated by getData() method
		// A style is added to prev and next buttons to show/hide them, appearance of play button is changed
		// and piece of javascript is executed by calling js methods defined custom.js found in webapp/js folder
		this.playVideoBtn.addClickListener(e -> {
    		if("play".equals(e.getButton().getData())) {
    			stepBackBtn.setEnabled(false);
    			salamaOption.setEnabled(false);
    			queryBtn.setEnabled(false);
    			rewindVideoBtn.setEnabled(false);
    			setPlayVideoBtnToPause();
    			JavaScript.getCurrent().execute("playResultMovie();");
    		} else if("pause".equals(e.getButton().getData())) {
    			stepFwdBtn.setEnabled(true);
    			stepBackBtn.setEnabled(true);
    			salamaOption.setEnabled(true);
    			queryBtn.setEnabled(true);
    			rewindVideoBtn.setEnabled(true);
    			setPlayVideoBtnToPlay();
    			JavaScript.getCurrent().execute("pauseResultMovie();");
    		}
    	});
		
		this.rewindVideoBtn.addClickListener(e -> {
    		if("play".equals(e.getButton().getData())) {
    			stepFwdBtn.setEnabled(false);
    			salamaOption.setEnabled(false);
    			queryBtn.setEnabled(false);
    			playVideoBtn.setEnabled(false);
    			setRewindVideoBtnToPause();
    			System.out.println("!!");
    			JavaScript.getCurrent().execute("rewindResultMovie();");
    		} else if("pause".equals(e.getButton().getData())) {
    			stepFwdBtn.setEnabled(true);
    			stepBackBtn.setEnabled(true);
    			salamaOption.setEnabled(true);
    			queryBtn.setEnabled(true);
    			playVideoBtn.setEnabled(true);
    			setRewindVideoBtnToPlay();
    			JavaScript.getCurrent().execute("pauseRewindResultMovie();");
    		}
    	});
		
		salamaOption.addValueChangeListener(e -> {
			boolean show = (Boolean) e.getProperty().getValue();
			if(show) {
				if(osm.fetchSalamaData(startDate.getValue(), endDate.getValue())) {
					osm.updateSalamaLayer();
				} else {
					log.error("Unable to fetch salama data");
				}
			} else {
				osm.hideSalamaLayer();
			}
		});
		
		queryBtn.addClickListener(new Button.ClickListener() {
			//Component oldC = oldComponent;
			private static final long serialVersionUID = -8146345475859196612L;

			public void buttonClick(ClickEvent event) {
				startDate.validate();
				endDate.validate();
				if(startDate.getValue() != null && endDate.getValue() != null && 
						endDate.getValue().before(startDate.getValue())) {
					new Notification("Invalid date selelection",
	            		    "From date must be less or equal to To date",
	            		    Notification.Type.WARNING_MESSAGE, false)
	            		    .show(Page.getCurrent());
				} else if(kulma.getValue() == null) {
					new Notification("Please select radar angle",
		        		    "",
		        		    Notification.Type.WARNING_MESSAGE, false)
		        		    .show(Page.getCurrent());
	            } else { 
					if (osm.getMap(startDate.getValue(),endDate.getValue(), tutka.getValue())) { 
						stepBackBtn.setEnabled(false);
						stepFwdBtn.setEnabled(true);
						setPlayVideoBtnToPlay();
						playVideoBtn.setVisible(true);
						playVideoBtn.setEnabled(true);
						setRewindVideoBtnToPlay();
						rewindVideoBtn.setVisible(true);
						rewindVideoBtn.setEnabled(false);
						
						parametriPalkki.addComponent(hr);
						parametriPalkki.addComponent(aika);
						
						GridLayout mapControlLayout = new GridLayout(2, 2);
						mapControlLayout.addStyleName("map-control-container");
						mapControlLayout.addComponent(stepBackBtn, 0, 0);
						mapControlLayout.addComponent(stepFwdBtn, 1, 0);
						mapControlLayout.addComponent(rewindVideoBtn, 0, 1);
						mapControlLayout.addComponent(playVideoBtn, 1, 1);
						resultLayout.addComponent(mapControlLayout);
						parametriPalkki.addComponent(resultLayout);
						
						salamaOption.setValue(false);
						salamaOption.setVisible(true);
						parametriPalkki.addComponent(salamaOption);
					}
	            }
			}
		});
		
        mainLayout.addComponent(parametriPalkki);        
        karttaPalkki.addComponent(openlayersMap);
        mainLayout.addComponent(karttaPalkki);
        mainLayout.setExpandRatio(parametriPalkki, 1.0f);
        mainLayout.setExpandRatio(karttaPalkki, 2.0f);
	    mainLayout.setMargin(true);
	    setContent(mainLayout);
    }
    
    /**
     * Set video play button icon and text to play mode
     */
    private void setPlayVideoBtnToPlay() {
    	playVideoBtn.setIcon(FontAwesome.FORWARD);
    	playVideoBtn.setCaption("Play");
    	playVideoBtn.setData("play");
    }
    
    /**
     * Set video play button icon and text to pause mode
     */
    private void setPlayVideoBtnToPause() {
    	playVideoBtn.setIcon(FontAwesome.PAUSE);
    	playVideoBtn.setCaption("Pause");
    	playVideoBtn.setData("pause");
    }
    
    /**
     * Set video rewind button icon and text to play mode
     */
    private void setRewindVideoBtnToPlay() {
    	rewindVideoBtn.setIcon(FontAwesome.BACKWARD);
    	rewindVideoBtn.setCaption("Rewind");
    	rewindVideoBtn.setData("play");
    }
    
    /**
     * Set video rewind button icon and text to pause mode
     */
    private void setRewindVideoBtnToPause() {
    	rewindVideoBtn.setIcon(FontAwesome.PAUSE);
    	rewindVideoBtn.setCaption("Pause");
    	rewindVideoBtn.setData("pause");
    }
    
}
