package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import processes.ActionPrefixingCCSProcess;
import processes.CCSProcess;
import processes.TauCCSProcess;
import processes.VisibleChoiceCCSProcess;
import actions.CCSChannel;
import actions.CCSProcessState;
import actions.InputAction;
import actions.OutputAction;

public class ChoiceProcessUnitTest extends CCSProcessUnitTest {

	@Test
	public void check_toString_with_two_simple_action_prefixed_processes() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.toString(), is("(a!.tau + b?.a?.tau)"));
	}

	@Test
	public void check_next_states_with_two_simple_action_prefixed_processes() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.getNextStates().size(), is(2));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(), is("tau"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(), is("a?.tau"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));

		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("a?"));
		Assert.assertThat(secondState.getProcess().toString(), is("tau"));
		Assert.assertThat(secondState.getNextStates().size(), is(0));

	}

	@Test
	public void check_toString_with_a_mixture_processes() {
		CCSProcess process = makeComplexProcess();

		Assert.assertThat(
				process.toString(),
				is("((a!.tau + b?.a?.tau) + (((tau + tau) + d?.tau) + c?.tau))"));
	}

	@Test
	public void check_next_states_with_a_mixture_processes() {
		CCSProcess process = makeComplexProcess();

		Assert.assertThat(process.getNextStates().size(), is(4));

		CCSProcessState firstState = process.getNextStates().get(0);
		Assert.assertThat(firstState.getAction().toString(), is("a!"));
		Assert.assertThat(firstState.getProcess().toString(), is("tau"));
		Assert.assertThat(firstState.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(), is("a?.tau"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("a?"));
		Assert.assertThat(secondState.getProcess().toString(), is("tau"));
		Assert.assertThat(secondState.getNextStates().size(), is(0));

		CCSProcessState thirdState = process.getNextStates().get(2);
		Assert.assertThat(thirdState.getAction().toString(), is("d?"));
		Assert.assertThat(thirdState.getProcess().toString(), is("tau"));
		Assert.assertThat(thirdState.getNextStates().size(), is(0));

		CCSProcessState fourthState = process.getNextStates().get(3);
		Assert.assertThat(fourthState.getAction().toString(), is("c?"));
		Assert.assertThat(fourthState.getProcess().toString(), is("tau"));
		Assert.assertThat(fourthState.getNextStates().size(), is(0));

	}

	private CCSProcess makeComplexProcess() {
		CCSProcess process = new VisibleChoiceCCSProcess(
				makeProcessUnderTest(), new VisibleChoiceCCSProcess(
						new VisibleChoiceCCSProcess(
								new VisibleChoiceCCSProcess(
										new TauCCSProcess(),
										new TauCCSProcess()),
								new ActionPrefixingCCSProcess(new InputAction(
										new CCSChannel("d")),
										new TauCCSProcess())),
						new ActionPrefixingCCSProcess(new InputAction(
								new CCSChannel("c")), new TauCCSProcess())));
		return process;
	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		return new VisibleChoiceCCSProcess(new ActionPrefixingCCSProcess(
				new OutputAction(new CCSChannel("a")), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						"b")), new ActionPrefixingCCSProcess(new InputAction(
						new CCSChannel("a")), new TauCCSProcess())));
	}
}
