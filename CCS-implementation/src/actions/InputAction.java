package actions;

public class InputAction extends ProcessAction {

	public InputAction(CCSChannel ccsChannel) {
		super(ccsChannel);
	}

	@Override
	public String toString() {
		return this.ccsChannel.toString().concat("?");
	}

	@Override
	public boolean isComplementOf(ProcessAction action) {
		return action.isComplementOfInputAction(this);
	}

	@Override
	public boolean isComplementOfInputAction(InputAction inputAction) {
		// because this object is an input action, hance if the argument is
		// itself an input action too, they cannot be complement of each other.
		return false;
	}

	@Override
	public boolean isComplementOfOutputAction(OutputAction outputAction) {
		return this.ccsChannel.toString().equals(
				outputAction.ccsChannel.toString());
	}

}
