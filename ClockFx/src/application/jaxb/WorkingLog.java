package application.jaxb;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

	/* Helper */
	public void addWorkingDate(WorkingDayTime value) {
		if (log == null) {
			log = new HashMap<String, WorkingDayTime>();
		}
		log.put(value.getDate().toString(), value);
	}

	public Map<String, Double> getHoursByMonth() {

		if (log != null) {
			Map<String, Double> map = new HashMap<String, Double>();
			log.forEach((k, v) -> {
				String month = LocalDate.parse(k).getMonth().toString();
				if (map.get(month) != null) {
					map.put(month, map.get(month) + 2 * v.getTotalHours());
				} else {
					map.put(month, 2 * v.getTotalHours());
				}

			});
			return map;
		}
		return null;
	}
}