package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import processes.ActionPrefixingCCSProcess;
import processes.CCSProcess;
import processes.ParallelCompositionCCSProcess;
import processes.RecursionCSSProcess;
import processes.RecursionPlaceHolderCCSProcess;
import processes.RecursionPostFixingCCSProcess;
import processes.RestrictionCCSProcess;
import processes.TauCCSProcess;
import processes.VisibleChoiceCCSProcess;
import actions.CCSChannel;
import actions.CCSProcessState;
import actions.InputAction;
import actions.OutputAction;

public class RecursionProcessUnitTest extends CCSProcessUnitTest {

	@Test
	public void test() {
		CCSProcess recProcess = makeProcessUnderTest();

		Assert.assertThat(recProcess.toString(),
				is("recX:((a!.tau + (b?.tau | c?.tau).X))"));
	}

	@Test
	public void check_next_states_with_two_complementary_action_prefixed_processes() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.getNextStates().size(), is(3));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(), is("tau"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(tau | c?.tau).X"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("c?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("recX:((a!.tau + (b?.tau | c?.tau).X))"));
		Assert.assertThat(secondState.getNextStates().size(), is(3));

		// first recursion...
		secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(tau | c?.tau).X"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("c?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("recX:((a!.tau + (b?.tau | c?.tau).X))"));
		Assert.assertThat(secondState.getNextStates().size(), is(3));

		// ...second recursion...
		secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("(tau | c?.tau).X"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("c?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("recX:((a!.tau + (b?.tau | c?.tau).X))"));
		Assert.assertThat(secondState.getNextStates().size(), is(3));

		// ...and the story repeat...

	}

	@Test
	public void mixing_static_and_dynamic_operators_produce_redundance() {
		CCSProcess process = makeProcessWithMixtureOfOperators();

		Assert.assertThat(process.toString(), is("(recX:(a!.tau.X))\\{b}"));

		Assert.assertThat(process.getNextStates().size(), is(1));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("(recX:(a!.tau.X))\\{b}"));

		// first recursion...
		Assert.assertThat(firststate.getNextStates().size(), is(1));
		firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("(recX:(a!.tau.X))\\{b}"));

		// ...second recursion...
		Assert.assertThat(firststate.getNextStates().size(), is(1));
		firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a!"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("(recX:(a!.tau.X))\\{b}"));
		Assert.assertThat(firststate.getNextStates().size(), is(1));

		// and the story repeat...

	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		String placeHolderName = "X";
		RecursionPlaceHolderCCSProcess placeHolder = new RecursionPlaceHolderCCSProcess(
				placeHolderName);
		RecursionPostFixingCCSProcess recursionPostFixingCCSProcess = new RecursionPostFixingCCSProcess(
				new ParallelCompositionCCSProcess(
						new ActionPrefixingCCSProcess(new InputAction(
								new CCSChannel("b")), new TauCCSProcess()),
						new ActionPrefixingCCSProcess(new InputAction(
								new CCSChannel("c")), new TauCCSProcess())),
				placeHolder);
		CCSProcess bodyOfRecursion = new VisibleChoiceCCSProcess(
				new ActionPrefixingCCSProcess(new OutputAction(new CCSChannel(
						"a")), new TauCCSProcess()),
				recursionPostFixingCCSProcess);
		return new RecursionCSSProcess(bodyOfRecursion, placeHolder);

	}

	protected CCSProcess makeProcessWithMixtureOfOperators() {
		String placeHolderName = "X";

		RecursionPlaceHolderCCSProcess placeHolder = new RecursionPlaceHolderCCSProcess(
				placeHolderName);

		CCSProcess bodyOfRecursion = new RecursionPostFixingCCSProcess(
				new ActionPrefixingCCSProcess(new OutputAction(new CCSChannel(
						"a")), new TauCCSProcess()), placeHolder);

		CCSProcess recursionProcess = new RecursionCSSProcess(bodyOfRecursion,
				placeHolder);

		return new RestrictionCCSProcess(recursionProcess, new CCSChannel("b"));

	}
}
