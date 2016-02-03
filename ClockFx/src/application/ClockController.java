package application;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import application.jaxb.WorkingDayTime;
import application.jaxb.WorkingLog;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClockController {

	@FXML
	Button bStartStop;

	@FXML
	Button bSave;

	@FXML
	TextField tfSeconds;

	@FXML
	TextField tfMinutes;

	@FXML
	TextField tfHours;

	@FXML
	TitledPane tpRecords;

	/*
	 * The state of the Clock
	 */
	STATE currentState;

	/* The time of the chronometer */
	Duration mlastedTime = Duration.ZERO;

	/* the time when the action starts */
	LocalDateTime startTime;

	/* table view */
	@FXML
	TableView<WorkingDayTime> tvWorkingDay = new TableView<WorkingDayTime>();

	/* Logger */
	Logger log = Logger.getLogger(ClockController.class.getName());

	private Timeline timeFieldTimeLine;

	private Timeline saveTimeLine;

	private List<WorkingDayTime> workingDateList;

	public void initialize() {

		currentState = STATE.ZERO;
		bStartStop.setOnAction(event -> {
			manageStartStopAction();
		});
		tpRecords.expandedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					ClockController.this.bSave.getScene().getWindow().setHeight(360);
				} else {
					ClockController.this.bSave.getScene().getWindow().setHeight(180);
				}

			}
		});

		bStartStop.setText("Start!");

		tfSeconds.setText("00");
		tfMinutes.setText("00");
		tfHours.setText("00");

		loadInitialTime();

		// data section
		TableColumn<WorkingDayTime, LocalDate> day = new TableColumn<WorkingDayTime, LocalDate>();
		TableColumn<WorkingDayTime, Long> hours = new TableColumn<WorkingDayTime, Long>();
		TableColumn<WorkingDayTime, Long> minutes = new TableColumn<WorkingDayTime, Long>();
		TableColumn<WorkingDayTime, Long> seconds = new TableColumn<WorkingDayTime, Long>();
		day.setText("Day");
		hours.setText("Hours");
		minutes.setText("Minutes");
		seconds.setText("Seconds");
		tvWorkingDay.getColumns().add(day);
		tvWorkingDay.getColumns().add(hours);
		tvWorkingDay.getColumns().add(minutes);
		tvWorkingDay.getColumns().add(seconds);
		day.setCellValueFactory(new PropertyValueFactory<WorkingDayTime, LocalDate>("date"));
		hours.setCellValueFactory(new PropertyValueFactory<WorkingDayTime, Long>("hours"));
		minutes.setCellValueFactory(new PropertyValueFactory<WorkingDayTime, Long>("minutes"));
		seconds.setCellValueFactory(new PropertyValueFactory<WorkingDayTime, Long>("seconds"));
		// order the table view by day
		day.setSortType(TableColumn.SortType.DESCENDING);

	}

	private void manageStartStopAction() {

		startTime = LocalDateTime.now();

		if (currentState.equals(STATE.ZERO)) {
			bStartStop.setText("Stop!");
			bStartStop.setId("buttonStop");
			currentState = STATE.STARTED;
			// loadInitialTime();

			Task<Long> task = new Task<Long>() {
				@Override
				protected Long call() throws Exception {
					updateGUI();
					return null;
				}
			};
			Thread t = new Thread(task);
			t.start();

		} else if (currentState.equals(STATE.PAUSED)) {
			loadInitialTime();
			// mlastedTime = Duration.between(startTime, LocalDateTime.now());
			bStartStop.setText("Stop!");
			bStartStop.setId("buttonStop");
			currentState = STATE.STARTED;
			Task<Long> task = new Task<Long>() {

				@Override
				protected Long call() throws Exception {
					updateGUI();
					return null;
				}
			};

			Thread t = new Thread(task);
			t.start();

		} else if (currentState.equals(STATE.STARTED)) {
			timeFieldTimeLine.stop();
			saveTimeLine.stop();
			bStartStop.setText("Continue!");
			bStartStop.setId("buttonContinue");
			saveTime();
			currentState = STATE.PAUSED;

		}
	}

	private void updateGUI() {
		timeFieldTimeLine = new Timeline(new KeyFrame(javafx.util.Duration.millis(1000), ae -> updateTimeFields()));
		timeFieldTimeLine.setCycleCount(Animation.INDEFINITE);
		timeFieldTimeLine.play();

		saveTimeLine = new Timeline(new KeyFrame(javafx.util.Duration.millis(60000), ae -> saveTime()));
		saveTimeLine.setCycleCount(Animation.INDEFINITE);
		saveTimeLine.play();

	}

	private Object updateTimeFields() {
		if (mlastedTime.isZero()) {
			mlastedTime = Duration.between(startTime, LocalDateTime.now());
			startTime = LocalDateTime.now();
		} else {
			mlastedTime = mlastedTime.plus(Duration.between(startTime, LocalDateTime.now()));
			startTime = LocalDateTime.now();
		}
		tfSeconds.setText(padTimeDuration(mlastedTime.getSeconds() % 60));
		tfMinutes.setText(padTimeDuration(mlastedTime.toMinutes() % 60));
		tfHours.setText(padTimeDuration(mlastedTime.toHours()));
		return null;
	}

	private void loadInitialTime() {

		Unmarshaller u;
		try {
			File f = new File("working.xml");
			if (f.exists()) {
				JAXBContext jc = JAXBContext.newInstance(WorkingLog.class);
				u = jc.createUnmarshaller();
				WorkingLog workingLog = (WorkingLog) u.unmarshal(f);

				//////
				ReadworkingLog(workingLog);
				//////
				if (workingLog.getLog().containsKey(LocalDate.now().toString())) {
					WorkingDayTime t = workingLog.getLog().get(LocalDate.now().toString());
					mlastedTime = Duration.ofSeconds(t.getHours() * 3600 + t.getMinutes() * 60 + t.getSeconds());
				}
			} else {
				mlastedTime = Duration.ZERO;
			}

		} catch (JAXBException e) {
			log.log(Level.SEVERE, e.getMessage());
		}
	}

	public void ReadworkingLog(WorkingLog workingLog) {

		if (!workingLog.getLog().isEmpty()) {
			tvWorkingDay.getItems().clear();
			tvWorkingDay.getItems().addAll(workingLog.getLog().values());
		}

	}

	// todo: review logic
	private Object saveTime() {
		// if (mlastedTime.isZero()) {
		// mlastedTime = Duration.between(startTime, LocalDateTime.now());
		// startTime = LocalDateTime.now();
		// } else {
		// mlastedTime = mlastedTime.plus(Duration.between(startTime,
		// LocalDateTime.now()));
		// startTime = LocalDateTime.now();
		// }
		// unmarshal from foo.xml
		Unmarshaller u;
		WorkingLog workingLog;
		WorkingDayTime workingDayTime = null;// = new WorkingDayTime();
		// WorkingDayTime workingDayTimeTest;

		try {
			JAXBContext jc = JAXBContext.newInstance(WorkingLog.class);
			File f = new File("working.xml");
			if (!f.exists()) {
				f.createNewFile();
				workingLog = new WorkingLog();
				// Initialize a record for today
				workingDayTime = new WorkingDayTime();
				workingDayTime.setDate(LocalDate.now());
			} else {
				u = jc.createUnmarshaller();
				workingLog = (WorkingLog) u.unmarshal(f);

				// look for today record
				workingDayTime = workingLog.getLog().get(LocalDate.now().toString());

				// if today's records does not exist create a new one
				if (workingDayTime == null) {
					workingDayTime = new WorkingDayTime();
					workingDayTime.setDate(LocalDate.now());
					workingLog.addWorkingDate(workingDayTime);
				}
			}

			workingDayTime.setHours(mlastedTime.toHours());
			workingDayTime.setMinutes(mlastedTime.toMinutes() % 60);
			workingDayTime.setSeconds(mlastedTime.getSeconds() % 60);

			// marshal to System.out
			Marshaller m = jc.createMarshaller();

			// workingDayTimeTest = workingDayTime;
			// workingDayTimeTest.setDate(LocalDate.now().plusDays(1));
			// workingLog.addWorkingDate(workingDayTimeTest);
			// m.marshal(fooObj, System.out);
			m.marshal(workingLog, f);
		} catch (JAXBException | IOException e1) {
			log.log(Level.SEVERE, e1.getMessage());
		}
		return null;
	}

	private String padTimeDuration(long duration) {
		return String.format("%2s", String.valueOf(duration)).replace(' ', '0');
	}

}

enum STATE {
	ZERO, STARTED, PAUSED
}
