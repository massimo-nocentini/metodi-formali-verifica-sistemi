package actions;

import java.util.List;

import processes.CCSProcess;

public class CCSProcessState {

	private final ProcessAction action;
	private final CCSProcess process;

	public ProcessAction getAction() {
		return action;
	}

	public CCSProcess getProcess() {
		return process;
	}

	public CCSProcessState(ProcessAction action, CCSProcess process) {
		this.action = action;
		this.process = process;
	}

	public List<CCSProcessState> getNextStates() {
		return process.getNextStates();
	}
}
