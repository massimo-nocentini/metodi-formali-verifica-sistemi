package unittests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import org.junit.Assert;
import org.junit.Test;

import processes.ActionPrefixingCCSProcess;
import processes.CCSProcess;
import processes.TauCCSProcess;
import actions.CCSChannel;
import actions.CCSProcessState;
import actions.InputAction;
import actions.OutputAction;
import actions.ProcessAction;

public class ActionPrefixingProcessUnitTest extends CCSProcessUnitTest {

	@Test
	public void check_a_prefix_of_length_one() {
		CCSProcess actionPrefixing = makeProcessUnderTest();

		Assert.assertThat(actionPrefixing.toString(), is("a?.tau"));
	}

	@Test
	public void check_a_prefix_of_length_three() {
		CCSProcess actionPrefixing = makeComplexActionPrefixingProcess();

		Assert.assertThat(actionPrefixing.toString(), is("a?.a!.a?.tau"));
	}

	@Test
	public void simple_action_prefixing_should_return_exactly_one_future_state() {
		CCSProcess actionPrefixing = makeProcessUnderTest();

		Assert.assertThat(actionPrefixing.getNextStates().size(), is(1));
		CCSProcessState state = actionPrefixing.getNextStates().get(0);
		Assert.assertThat(state.getAction().toString(), is("a?"));
		Assert.assertThat(state.getProcess().toString(), is("tau"));
		Assert.assertThat(state.getNextStates().size(), is(0));
	}

	private CCSProcess makeComplexActionPrefixingProcess() {
		CCSProcess actionPrefixing = new ActionPrefixingCCSProcess(
				new InputAction(new CCSChannel("a")),
				new ActionPrefixingCCSProcess(new OutputAction(new CCSChannel(
						"a")), new ActionPrefixingCCSProcess(new InputAction(
						new CCSChannel("a")), new TauCCSProcess())));
		return actionPrefixing;
	}

	@Test
	public void complex_action_prefixing_should_return_exactly_one_future_state() {
		CCSProcess actionPrefixing = makeComplexActionPrefixingProcess();

		Assert.assertThat(actionPrefixing.getNextStates().size(), is(1));
		CCSProcessState state = actionPrefixing.getNextStates().get(0);
		Assert.assertThat(state.getAction().toString(), is("a?"));
		Assert.assertThat(state.getProcess().toString(), is("a!.a?.tau"));
		Assert.assertThat(state.getNextStates().size(), is(1));
		state = state.getNextStates().get(0);
		Assert.assertThat(state.getAction().toString(), is("a!"));
		Assert.assertThat(state.getProcess().toString(), is("a?.tau"));
		Assert.assertThat(state.getNextStates().size(), is(1));
		state = state.getNextStates().get(0);
		Assert.assertThat(state.getAction().toString(), is("a?"));
		Assert.assertThat(state.getProcess().toString(), is("tau"));
		Assert.assertThat(state.getNextStates().size(), is(0));

	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		return new ActionPrefixingCCSProcess(new InputAction(
				new CCSChannel("a")), new TauCCSProcess());
	}

	@Test
	public void creating_a_prefix_process_getting_state_an_recreating_is_identity() {
		String channelName = "a";
		ProcessAction action = new InputAction(new CCSChannel(channelName));
		CCSProcess tauCCSProcess = new TauCCSProcess();
		CCSProcess process = new ActionPrefixingCCSProcess(action,
				tauCCSProcess);

		Assert.assertThat(process.getNextStates().size(), is(1));
		CCSProcessState ccsProcessState = process.getNextStates().get(0);
		Assert.assertThat(ccsProcessState.getAction(), is(sameInstance(action)));
		Assert.assertThat(ccsProcessState.getProcess(),
				is(sameInstance(tauCCSProcess)));

		// hence if we recreate an ActionPrefixing from the state we obtain
		// process again
	}
}
