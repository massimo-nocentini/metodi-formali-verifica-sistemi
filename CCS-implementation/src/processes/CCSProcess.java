package processes;

import java.util.List;

import actions.CCSProcessState;

public abstract class CCSProcess {

	public abstract List<CCSProcessState> getNextStates();
}
