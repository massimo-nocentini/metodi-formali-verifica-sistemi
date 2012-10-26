package processes;

import java.util.List;

import actions.CCSProcessState;

public class RecursionCSSProcess extends CCSProcess {

	private final CCSProcess bodyOfRecursion;
	private final RecursionPlaceHolderCCSProcess placeHolder;

	public RecursionCSSProcess(CCSProcess bodyOfRecursion,
			RecursionPlaceHolderCCSProcess placeHolder) {
		this.bodyOfRecursion = bodyOfRecursion;
		this.placeHolder = placeHolder;
		this.placeHolder.setRecurringProcess(this);
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		return bodyOfRecursion.getNextStates();
	}

	@Override
	public String toString() {
		return "rec".concat(placeHolder.getPlaceHolderName()).concat(":(")
				.concat(bodyOfRecursion.toString()).concat(")");
	}

}
