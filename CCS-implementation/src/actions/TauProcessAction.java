package actions;

public class TauProcessAction extends ProcessAction {

	public TauProcessAction() {
		this(null);
	}

	public TauProcessAction(CCSChannel ccsChannel) {
		super(new CCSChannel("tau"));
	}

	@Override
	public boolean isComplementOf(ProcessAction action) {
		// tau is the complement of no action!
		return false;
	}

	@Override
	public String toString() {
		return "tau";
	}

	@Override
	public boolean isComplementOfInputAction(InputAction inputAction) {
		return isComplementOf(inputAction);
	}

	@Override
	public boolean isComplementOfOutputAction(OutputAction outputAction) {
		return isComplementOf(outputAction);
	}

}
