package processes;

import java.util.List;

import actions.CCSProcessState;

public class RecursionPlaceHolderCCSProcess extends CCSProcess {

	private final String placeHolderName;
	private RecursionCSSProcess recursionCSSProcess;

	public RecursionPlaceHolderCCSProcess(String placeHolderName) {
		this.placeHolderName = placeHolderName;
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPlaceHolderName() {
		return placeHolderName;
	}

	@Override
	public String toString() {
		return placeHolderName.toUpperCase();
	}

	public CCSProcess getRecurringProcess() {
		return recursionCSSProcess;
	}

	public void setRecurringProcess(RecursionCSSProcess recursionCSSProcess) {
		this.recursionCSSProcess = recursionCSSProcess;

	}

}
