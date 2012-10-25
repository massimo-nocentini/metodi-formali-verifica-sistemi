package actions;

public abstract class ProcessAction {

	protected final CCSChannel ccsChannel;

	public ProcessAction(CCSChannel ccsChannel) {
		this.ccsChannel = ccsChannel;
	}

	public abstract boolean isComplementOf(ProcessAction action);

	public abstract boolean isComplementOfInputAction(InputAction inputAction);

	public abstract boolean isComplementOfOutputAction(OutputAction outputAction);

	public CCSChannel getChannel() {
		return ccsChannel;
	}

}