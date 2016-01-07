package application;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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

	/*
	 * The state of the Clock
	 */
	STATE currentState;

	/* The time of the chronometer */
	Duration mlastedTime = Duration.ZERO;

	/* the time when the action starts */
	LocalDateTime startTime;

	/* Logger */
	Logger log = Logger.getLogger(ClockController.class.getName());

	private Timeline timeFieldTimeLine;

	private Timeline saveTimeLine;

	public void initialize() {

		currentState = STATE.ZERO;
		bStartStop.setOnAction(event -> {
			manageStartStopAction();
		});

		bStartStop.setText("Start!");

		tfSeconds.setText("00");
		tfMinutes.setText("00");
		tfHours.setText("00");

		loadInitialTime();

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
		WorkingDayTime workingDayTime = new WorkingDayTime();
		// WorkingDayTime workingDayTimeTest;

		try {
			JAXBContext jc = JAXBContext.newInstance(WorkingLog.class);
			File f = new File("working.xml");
			if (!f.exists()) {
				f.createNewFile();
				workingLog = new WorkingLog();
			} else {
				u = jc.createUnmarshaller();
				workingLog = (WorkingLog) u.unmarshal(f);
			}

			workingDayTime.setDate(LocalDate.now());
			workingDayTime.setHours(mlastedTime.toHours());
			workingDayTime.setMinutes(mlastedTime.toMinutes() % 60);
			workingDayTime.setSeconds(mlastedTime.getSeconds() % 60);

			workingLog.addWorkingDate(workingDayTime);
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
