package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSProcessState;

public class TauCCSProcess extends CCSProcess {

	@Override
	public String toString() {
		return "tau";
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		return new LinkedList<CCSProcessState>();
	}

}
