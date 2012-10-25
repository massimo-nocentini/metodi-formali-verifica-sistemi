package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSProcessState;
import actions.TauProcessAction;

public class ParallelCompositionCCSProcess extends CCSProcess {

	private final CCSProcess firstProcess;
	private final CCSProcess secondProcess;

	public ParallelCompositionCCSProcess(CCSProcess firstProcess,
			CCSProcess secondProcess) {
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
	}

	@Override
	public List<CCSProcessState> getNextStates() {

		List<CCSProcessState> states = new LinkedList<CCSProcessState>();

		for (CCSProcessState stateOfFirstProcess : firstProcess.getNextStates()) {

			states.add(new CCSProcessState(stateOfFirstProcess.getAction(),
					new ParallelCompositionCCSProcess(stateOfFirstProcess
							.getProcess(), secondProcess)));
		}

		for (CCSProcessState stateOfSecondProcess : secondProcess
				.getNextStates()) {

			states.add(new CCSProcessState(stateOfSecondProcess.getAction(),
					new ParallelCompositionCCSProcess(firstProcess,
							stateOfSecondProcess.getProcess())));
		}

		for (CCSProcessState stateOfSecondProcess : secondProcess
				.getNextStates()) {
			for (CCSProcessState stateOfFirstProcess : firstProcess
					.getNextStates()) {

				if (stateOfFirstProcess.getAction().isComplementOf(
						stateOfSecondProcess.getAction())) {

					states.add(new CCSProcessState(new TauProcessAction(),
							new ParallelCompositionCCSProcess(
									stateOfFirstProcess.getProcess(),
									stateOfSecondProcess.getProcess())));
				}

			}
		}

		return states;
	}

	@Override
	public String toString() {
		return "(".concat(firstProcess.toString()).concat(" | ")
				.concat(secondProcess.toString()).concat(")");
	}

}
