package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSProcessState;

public class VisibleChoiceCCSProcess extends CCSProcess {

	private final CCSProcess firstProcess;
	private final CCSProcess secondProcess;

	public VisibleChoiceCCSProcess(CCSProcess firstProcess,
			CCSProcess secondProcess) {
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		List<CCSProcessState> states = new LinkedList<CCSProcessState>();
		states.addAll(firstProcess.getNextStates());
		states.addAll(secondProcess.getNextStates());
		return states;
	}

	@Override
	public String toString() {
		return "(".concat(firstProcess.toString()).concat(" + ")
				.concat(secondProcess.toString()).concat(")");
	}

}
