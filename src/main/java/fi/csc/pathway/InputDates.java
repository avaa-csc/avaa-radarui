/**
 * Validaattorit eri tarkistetaan arvojen "laillisuus".
 */
package fi.csc.pathway;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.PopupDateField;

/**
 * Tämän min- ja maxValue riippuvat tietokannan sisällöstä. 
 * 
 * @author pj
 * Secondary functionality
 */
public class InputDates implements Serializable {
	
	private static final long serialVersionUID = 3953325456264644581L;
	private PopupDateField endDateField;
	private PopupDateField startDateField;
	private LocalDateTime minDateTime = LocalDateTime.of(2014, 6, 1, 0, 0, 0);
	private LocalDateTime maxDateTime = LocalDateTime.of(2014, 6, 30, 23, 59, 59);
	final Date minDateValue = Date.from(minDateTime.atZone(ZoneId.of("UTC+03:00")).toInstant());
	final Date maxDateValue = Date.from(maxDateTime.atZone(ZoneId.of("UTC+03:00")).toInstant());
	private static final String FORMAT = "yyyy-MM-dd HH:mm";
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(FORMAT);
	
	InputDates(PopupDateField endDate, PopupDateField startDate) {
		this.startDateField = startDate;
		this.endDateField = endDate;
		this.startDateField.setResolution(Resolution.MINUTE);
		this.endDateField.setResolution(Resolution.MINUTE);
		this.startDateField.setImmediate(true);
		this.endDateField.setImmediate(true);	
		this.startDateField.setValidationVisible(true);
		this.endDateField.setValidationVisible(true);
		this.startDateField.setDateFormat(FORMAT);
		this.endDateField.setDateFormat(FORMAT);
		this.startDateField.setValue(minDateValue);
		this.endDateField.setValue(Date.from(minDateTime.plusDays(2).atZone(ZoneId.of("UTC+03:00")).toInstant()));
		
		startDate.setRangeStart(minDateValue);
		startDate.setRangeEnd(maxDateValue);
		startDate.setParseErrorMessage("Please give date in the format YYYY-MM-dd HH:mm");
		startDate.setDateOutOfRangeMessage("Please select a date between " + minDateTime.format(dateFormatter) + " and " + maxDateTime.format(dateFormatter));
		startDate.setValidationVisible(true);
		startDate.setTextFieldEnabled(true);
		startDate.setInputPrompt("Select From date");
		startDate.setRequired(true);
		endDate.setRangeStart(minDateValue);
		endDate.setRangeEnd(maxDateValue);
		endDate.setParseErrorMessage("Please give date in the format YYYY-MM-dd HH:mm");
		endDate.setDateOutOfRangeMessage("Please select a date between " + minDateTime.format(dateFormatter) + " and " + maxDateTime.format(dateFormatter));
		endDate.setValidationVisible(true);
		endDate.setTextFieldEnabled(true);
		endDate.setInputPrompt("Select To date");
		endDate.setRequired(true);
	}
	
	public Component getStartDateField() {
		return startDateField;
	}

	public Component getEndDateField() {
		return endDateField;
	}
}
