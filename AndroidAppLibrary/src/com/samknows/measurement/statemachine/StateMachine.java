/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.statemachine;


import com.samknows.measurement.AppSettings;
import com.samknows.measurement.Logger;
import com.samknows.measurement.MainService;
import com.samknows.measurement.activity.components.UIUpdate;
import com.samknows.measurement.statemachine.state.ActivateState;
import com.samknows.measurement.statemachine.state.AssociateState;
import com.samknows.measurement.statemachine.state.BaseState;
import com.samknows.measurement.statemachine.state.CheckConfigVersionState;
import com.samknows.measurement.statemachine.state.DownloadConfigAnonymousState;
import com.samknows.measurement.statemachine.state.DownloadConfigState;
import com.samknows.measurement.statemachine.state.ExecuteQueueState;
import com.samknows.measurement.statemachine.state.InitialiseAnonymousState;
import com.samknows.measurement.statemachine.state.InitialiseState;
import com.samknows.measurement.statemachine.state.NoneState;
import com.samknows.measurement.statemachine.state.RunInitTestsState;
import com.samknows.measurement.statemachine.state.SubmitResultsAnonymousState;
import com.samknows.measurement.statemachine.state.SubmitResultsState;
import com.samknows.measurement.util.OtherUtils;

public class StateMachine {
	private MainService ctx;
	
	public StateMachine(MainService ctx) {
		super();
		this.ctx = ctx;
	}
	

	public void executeRoutine() { 
		AppSettings appSettings = AppSettings.getInstance();
		Transition t = Transition.create(appSettings);
		State state = appSettings.getState();
		Logger.d(this, "starting routine from state: " + state);
		ctx.publish(UIUpdate.machineState(state));
		while (state != State.SHUTDOWN) {
			Logger.d(this, "executing state: " + state);
			StateResponseCode code = createState(state).executeState();
			Logger.d(this, "finished state, code: " + code); 
			if (code == StateResponseCode.FAIL) {
				appSettings.stateMachineFailure();
				Logger.e(this, "fail to execute state: " + state + ", reschedule");
				OtherUtils.rescheduleRTC(ctx, appSettings.rescheduleTime);
				ctx.publish(UIUpdate.stateFailure());
				return;
			} else {
				appSettings.stateMachineSuccess();
				state = t.getNextState(state, code);
				appSettings.saveState(state);
				Logger.d(this, "change service state to: " + state);
				activation(state);
			}
			ctx.publish(UIUpdate.progress(state));
			ctx.publish(UIUpdate.machineState(state));
		}
		
		state = t.getNextState(state, StateResponseCode.OK);
		appSettings.saveState(state);
		Logger.d(this, "shutdown state, stop execution and setup state for next time: " + state);
		
	}
	
	public BaseState createState(State state) {
		switch (state) {
		case NONE: return new NoneState(ctx);
		case INITIALISE : return new InitialiseState(ctx);
		case INITIALISE_ANONYMOUS: return new InitialiseAnonymousState(ctx);
		case ACTIVATE : return new ActivateState(ctx);
		case ASSOCIATE : return new AssociateState(ctx);
		case CHECK_CONFIG_VERSION : return new CheckConfigVersionState(ctx);
		case DOWNLOAD_CONFIG : return new DownloadConfigState(ctx);
		case DOWNLOAD_CONFIG_ANONYMOUS: return new DownloadConfigAnonymousState(ctx);
		case RUN_INIT_TESTS : return new RunInitTestsState(ctx);
		case EXECUTE_QUEUE : return new ExecuteQueueState(ctx);
		case SUBMIT_RESULTS : return new SubmitResultsState(ctx);
		case SUBMIT_RESULTS_ANONYMOUS : return new SubmitResultsAnonymousState(ctx);
		case SHUTDOWN:
		}
		throw new RuntimeException("unimplemented state: " + state);
	}
	
	//used to set the service activate according to the state
	private void activation(State state){
		switch(state){
		case RUN_INIT_TESTS:
			AppSettings.getInstance().setServiceActivated(true);
			default:
		}
	}
	
	
}
