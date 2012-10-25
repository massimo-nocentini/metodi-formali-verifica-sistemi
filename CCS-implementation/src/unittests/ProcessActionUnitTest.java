package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import actions.CCSChannel;
import actions.InputAction;
import actions.OutputAction;
import actions.ProcessAction;
import actions.TauProcessAction;

public class ProcessActionUnitTest {

	@Test
	public void input_action_should_print_the_channel_name_after_question_mark() {
		String channelName = "a";
		ProcessAction action = new InputAction(new CCSChannel(channelName));
		Assert.assertThat(action.toString(),
				is(channelName.toString().concat("?")));
	}

	@Test
	public void output_action_should_print_the_channel_name_after_exclamation_mark() {
		String channelName = "a";
		ProcessAction action = new OutputAction(new CCSChannel(channelName));
		Assert.assertThat(action.toString(),
				is(channelName.toString().concat("!")));
	}

	@Test
	public void tau_action_should_print_always_tau_without_any_marks() {
		String channelName = "a";
		ProcessAction action = new TauProcessAction(new CCSChannel(channelName));
		Assert.assertThat(action.toString(), is("tau"));

		action = new TauProcessAction();
		Assert.assertThat(action.toString(), is("tau"));
	}

	@Test
	public void tau_action_cannot_be_complemented() {
		String channelName = "a";
		ProcessAction action = new TauProcessAction(new CCSChannel(channelName));
		Assert.assertThat(action.isComplementOf(new InputAction(new CCSChannel(
				channelName))), is(false));

		ProcessAction action2 = new OutputAction(new CCSChannel(channelName));
		Assert.assertThat(action.isComplementOf(action2), is(false));
		Assert.assertThat(action2.isComplementOf(action), is(false));
	}

	@Test
	public void actions_on_different_channel_are_not_complementary() {
		String channelName = "a";
		String anotherChannelName = "b";
		ProcessAction action = new InputAction(new CCSChannel(channelName));
		ProcessAction anotherAction = new OutputAction(new CCSChannel(
				anotherChannelName));
		Assert.assertThat(action.isComplementOf(anotherAction), is(false));
		Assert.assertThat(anotherAction.isComplementOf(action), is(false));
	}

	@Test
	public void actions_on_same_channel_but_same_direction_are_not_complementary() {
		String channelName = "a";
		ProcessAction action = new InputAction(new CCSChannel(channelName));
		ProcessAction anotherAction = new InputAction(new CCSChannel(
				channelName));
		Assert.assertThat(action.isComplementOf(anotherAction), is(false));
		Assert.assertThat(anotherAction.isComplementOf(action), is(false));

		action = new OutputAction(new CCSChannel(channelName));
		anotherAction = new OutputAction(new CCSChannel(channelName));
		Assert.assertThat(action.isComplementOf(anotherAction), is(false));
		Assert.assertThat(anotherAction.isComplementOf(action), is(false));
	}

	@Test
	public void actions_on_same_channel_with_different_direction_are_not_complementary() {
		String channelName = "a";
		ProcessAction action = new InputAction(new CCSChannel(channelName));
		ProcessAction anotherAction = new OutputAction(new CCSChannel(
				channelName));
		Assert.assertThat(action.isComplementOf(anotherAction), is(true));
		Assert.assertThat(anotherAction.isComplementOf(action), is(true));
	}

}
