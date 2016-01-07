package application.jaxb;

import java.time.LocalDate;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workingdaytime")
public class WorkingDayTime {
	private Long seconds;
	private Long minutes;
	private Long hours;
	private LocalDate date;

	public Long getSeconds() {
		return seconds;
	}

	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}

	public Long getMinutes() {
		return minutes;
	}

	public void setMinutes(Long minutes) {
		this.minutes = minutes;
	}

	public Long getHours() {
		return hours;
	}

	public void setHours(Long hours) {
		this.hours = hours;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

}