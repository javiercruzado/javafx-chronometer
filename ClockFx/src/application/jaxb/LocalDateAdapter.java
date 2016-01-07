package application.jaxb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
	@Override
	public LocalDate unmarshal(String v) throws Exception {
		return LocalDate.parse(v, formatter);
	}

	@Override
	public String marshal(LocalDate date) throws Exception {
		return date.format(formatter);
	}

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

}
