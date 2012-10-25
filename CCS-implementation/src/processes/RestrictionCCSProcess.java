package processes;

import java.util.LinkedList;
import java.util.List;

import actions.CCSChannel;
import actions.CCSProcessState;

public class RestrictionCCSProcess extends CCSProcess {

	private final CCSProcess process;
	private final List<CCSChannel> restrictedChannels;

	public RestrictionCCSProcess(CCSProcess actionPrefixingCCSProcess,
			CCSChannel... restrictedChannels) {

		this.process = actionPrefixingCCSProcess;
		this.restrictedChannels = new LinkedList<CCSChannel>();
		for (CCSChannel channel : restrictedChannels) {

			if (channel.toString().equals("tau")) {
				continue;
			}

			this.restrictedChannels.add(channel);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("(".concat(process.toString()).concat(")"));

		result.append("\\{");
		for (int i = 0; i < restrictedChannels.size() - 1; i = i + 1) {
			CCSChannel channel = restrictedChannels.get(i);

			result.append(channel.toString().concat(", "));
		}
		if (restrictedChannels.size() > 0) {
			result.append(restrictedChannels.get(restrictedChannels.size() - 1));
		}
		result.append("}");
		return result.toString();
	}

	@Override
	public List<CCSProcessState> getNextStates() {
		CCSChannel[] channels = new CCSChannel[restrictedChannels.size()];
		restrictedChannels.toArray(channels);

		List<CCSProcessState> states = new LinkedList<CCSProcessState>();
		for (CCSProcessState state : process.getNextStates()) {
			if (restrictedChannels.contains(state.getAction().getChannel())) {
				continue;
			}
			states.add(new CCSProcessState(state.getAction(),
					new RestrictionCCSProcess(state.getProcess(), channels)));
		}
		return states;
	}
}
