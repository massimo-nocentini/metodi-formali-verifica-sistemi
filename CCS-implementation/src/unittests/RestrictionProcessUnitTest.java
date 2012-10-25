package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import processes.ActionPrefixingCCSProcess;
import processes.CCSProcess;
import processes.ParallelCompositionCCSProcess;
import processes.RestrictionCCSProcess;
import processes.TauCCSProcess;
import actions.CCSChannel;
import actions.CCSProcessState;
import actions.InputAction;

public class RestrictionProcessUnitTest extends CCSProcessUnitTest {

	@Test
	public void check_a_prefix_of_length_one() {
		CCSProcess process = makeProcessUnderTest();

		Assert.assertThat(process.toString(), is("(a?.tau)\\{b, c}"));
	}

	@Test
	public void restricting_on_disjoined_channels_should_block_anything() {
		CCSProcess process = new ParallelCompositionCCSProcess(
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						"a")), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						"b")), new TauCCSProcess()));

		process = new RestrictionCCSProcess(process, new CCSChannel("c"));

		Assert.assertThat(process.toString(), is("((a?.tau | b?.tau))\\{c}"));

		Assert.assertThat(process.getNextStates().size(), is(2));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a?"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("((tau | b?.tau))\\{c}"));
		Assert.assertThat(firststate.getNextStates().size(), is(1));
		firststate = firststate.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("b?"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("((tau | tau))\\{c}"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

		CCSProcessState secondState = process.getNextStates().get(1);
		Assert.assertThat(secondState.getAction().toString(), is("b?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("((a?.tau | tau))\\{c}"));
		Assert.assertThat(secondState.getNextStates().size(), is(1));
		secondState = secondState.getNextStates().get(0);
		Assert.assertThat(secondState.getAction().toString(), is("a?"));
		Assert.assertThat(secondState.getProcess().toString(),
				is("((tau | tau))\\{c}"));
		Assert.assertThat(secondState.getNextStates().size(), is(0));

	}

	@Test
	public void restricting_on_used_channel_should_block_something() {
		String blockedChannel = "b";
		CCSProcess process = new ParallelCompositionCCSProcess(
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						"a")), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						blockedChannel)), new TauCCSProcess()));

		process = new RestrictionCCSProcess(process, new CCSChannel(
				blockedChannel));

		Assert.assertThat(process.toString(), is("((a?.tau | b?.tau))\\{b}"));

		Assert.assertThat(process.getNextStates().size(), is(1));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("a?"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("((tau | b?.tau))\\{b}"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));
	}

	@Test
	public void restricting_on_all_used_channel_should_produce_deadlock() {
		String blockedChannel = "b";
		String anotherBlockedChannel = "a";
		CCSProcess process = new ParallelCompositionCCSProcess(
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						anotherBlockedChannel)), new TauCCSProcess()),
				new ActionPrefixingCCSProcess(new InputAction(new CCSChannel(
						blockedChannel)), new TauCCSProcess()));

		process = new RestrictionCCSProcess(process, new CCSChannel(
				blockedChannel), new CCSChannel(anotherBlockedChannel));

		Assert.assertThat(process.toString(), is("((a?.tau | b?.tau))\\{b, a}"));

		Assert.assertThat(process.getNextStates().size(), is(0));

	}

	@Test
	public void restriction_on_two_complementary_action_prefixed_processes() {
		CCSProcess process = ParallelCompositionUnitTest
				.makeProcessWithComplementaryActions();

		process = new RestrictionCCSProcess(process, new CCSChannel("a"));

		Assert.assertThat(process.getNextStates().size(), is(1));

		CCSProcessState firststate = process.getNextStates().get(0);
		Assert.assertThat(firststate.getAction().toString(), is("tau"));
		Assert.assertThat(firststate.getProcess().toString(),
				is("((tau | tau))\\{a}"));
		Assert.assertThat(firststate.getNextStates().size(), is(0));

	}

	@Test
	public void check_a_prefix_of_doubled_restricted_process() {
		CCSProcess process = new RestrictionCCSProcess(makeProcessUnderTest(),
				new CCSChannel("a"), new CCSChannel("tau"));

		Assert.assertThat(process.toString(), is("((a?.tau)\\{b, c})\\{a}"));
	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		return new RestrictionCCSProcess(new ActionPrefixingCCSProcess(
				new InputAction(new CCSChannel("a")), new TauCCSProcess()),
				new CCSChannel("b"), new CCSChannel("c"));
	}

}
