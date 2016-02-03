package application.jaxb;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workinglog")
public class WorkingLog {

	private HashMap<String, WorkingDayTime> log;

	public HashMap<String, WorkingDayTime> getLog() {
		return log;
	}

	public void setLog(HashMap<String, WorkingDayTime> log) {
		this.log = log;
	}
	
	/*Helper*/
	public void addWorkingDate(WorkingDayTime value) {
		if (log == null) {
			log = new HashMap<String, WorkingDayTime>();
		}
		log.put(value.getDate().toString(), value);
	}
}