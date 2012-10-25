package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSProcessState;
import actions.ProcessAction;

public class ActionPrefixingCCSProcess extends CCSProcess {

	private final ProcessAction action;
	private final CCSProcess process;

	public ActionPrefixingCCSProcess(ProcessAction action, CCSProcess process) {
		this.action = action;
		this.process = process;
	}

	@Override
	public String toString() {
		return action.toString().concat(".").concat(process.toString());
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		List<CCSProcessState> states = new LinkedList<CCSProcessState>();
		states.add(new CCSProcessState(action, process));

		return states;
	}
}
