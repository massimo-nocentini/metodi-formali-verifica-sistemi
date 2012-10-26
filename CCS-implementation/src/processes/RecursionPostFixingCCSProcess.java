package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSProcessState;

public class RecursionPostFixingCCSProcess extends CCSProcess {

	private final CCSProcess beforeRecursionPlaceHolder;
	private final RecursionPlaceHolderCCSProcess recursionPlaceHolder;

	public RecursionPostFixingCCSProcess(CCSProcess beforeRecursionPlaceHolder,
			RecursionPlaceHolderCCSProcess recursionPlaceHolder) {
		this.beforeRecursionPlaceHolder = beforeRecursionPlaceHolder;
		this.recursionPlaceHolder = recursionPlaceHolder;
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		List<CCSProcessState> states = new LinkedList<CCSProcessState>();

		for (CCSProcessState ccsProcessState : beforeRecursionPlaceHolder
				.getNextStates()) {

			CCSProcess nextProcess = null;
			if (ccsProcessState.getNextStates().size() == 0) {
				nextProcess = recursionPlaceHolder.getRecurringProcess();
			} else {
				nextProcess = new RecursionPostFixingCCSProcess(
						ccsProcessState.getProcess(), recursionPlaceHolder);
			}

			states.add(new CCSProcessState(ccsProcessState.getAction(),
					nextProcess));
		}

		return states;
	}

	@Override
	public String toString() {
		return beforeRecursionPlaceHolder.toString().concat(".")
				.concat(recursionPlaceHolder.toString());
	}
}
