package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import processes.ActionPrefixingCCSProcess;
import processes.CCSProcess;
import processes.ParallelCompositionCCSProcess;
import processes.TauCCSProcess;
import processes.VisibleChoiceCCSProcess;
import actions.CCSChannel;
import actions.CCSProcessState;
import actions.InputAction;
import actions.OutputAction;

public class ParallelCompositionUnitTest extends CCSProcessUnitTest {

	@Test
	public void check_toString_with_two_simple_action_prefixed_processes() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.toString(), is("(a?.tau | b?.tau)"));
	}

	@Test
	public void check_next_states_with_two_simple_action_prefixed_processes() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.getNextStates().size(), is(2));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a?"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("(tau | b?.tau)"));
		Assert.assertThat(firststate.getNextStates().size(), is(1));
		firststate = firststate.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("b?"));
		Assert.assertThat(firststate.getProcess().toString(), is("(tau | tau)"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(a?.tau | tau)"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("a?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(tau | tau)"));
		Assert.assertThat(secondState.getNextStates().size(), is(0));

	}

	@Test
	public void check_toString_with_two_complementary_action_prefixed_processes() {
		CCSProcess process = makeProcessWithComplementaryActions();

		Assert.assertThat(process.toString(), is("(a?.tau | a!.tau)"));
	}

	@Test
	public void check_next_states_with_two_complementary_action_prefixed_processes() {
		CCSProcess process = makeProcessWithComplementaryActions();

		Assert.assertThat(process.getNextStates().size(), is(3));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a?"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("(tau | a!.tau)"));
		Assert.assertThat(firststate.getNextStates().size(), is(1));
		firststate = firststate.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(), is("(tau | tau)"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("a!"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(a?.tau | tau)"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("a?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(tau | tau)"));
		Assert.assertThat(secondState.getNextStates().size(), is(0));

		CCSProcessState thirdState = process.getNextStates().get(2);
		Assert.assertThat(thirdState.getAction().toString(), is("tau"));
		Assert.assertThat(thirdState.getProcess().toString(), is("(tau | tau)"));
		Assert.assertThat(thirdState.getNextStates().size(), is(0));

	}

	@Test
	public void check_toString_with_more_complex_processes() {
		CCSProcess process = new ParallelCompositionCCSProcess(
				makeProcessUnderTest(), new VisibleChoiceCCSProcess(
						new ActionPrefixingCCSProcess(new InputAction(
								new CCSChannel("a")), new TauCCSProcess()),
						new ParallelCompositionCCSProcess(new TauCCSProcess(),
								new ActionPrefixingCCSProcess(new OutputAction(
										new CCSChannel("b")),
										new ActionPrefixingCCSProcess(
												new InputAction(new CCSChannel(
														"a")),
												new TauCCSProcess())))));

		Assert.assertThat(process.toString(),
				is("((a?.tau | b?.tau) | (a?.tau + (tau | b!.a?.tau)))"));
	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		return new ParallelCompositionCCSProcess(new ActionPrefixingCCSProcess(
				new InputAction(new CCSChannel("a")), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						"b")), new TauCCSProcess()));
	}

	protected CCSProcess makeProcessWithComplementaryActions() {
		String channel = "a";
		return new ParallelCompositionCCSProcess(new ActionPrefixingCCSProcess(
				new InputAction(new CCSChannel(channel)), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new OutputAction(new CCSChannel(
						channel)), new TauCCSProcess()));
	}

}
