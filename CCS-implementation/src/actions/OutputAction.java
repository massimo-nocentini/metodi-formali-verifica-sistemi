package actions;

public class OutputAction extends ProcessAction {

	public OutputAction(CCSChannel ccsChannel) {
		super(ccsChannel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return ccsChannel.toString().concat("!");
	}

	@Override
	public boolean isComplementOf(ProcessAction action) {
		return action.isComplementOfOutputAction(this);
	}

	@Override
	public boolean isComplementOfInputAction(InputAction inputAction) {
		return this.ccsChannel.toString().equals(
				inputAction.ccsChannel.toString());
	}

	@Override
	public boolean isComplementOfOutputAction(OutputAction outputAction) {
		// because this object is an output action, hance if the argument is
		// itself an output action too, they cannot be complement of each other.
		return false;
	}

}
