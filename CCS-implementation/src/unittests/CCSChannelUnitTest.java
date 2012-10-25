package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import actions.CCSChannel;

public class CCSChannelUnitTest {

	@Test
	public void check_correct_creation_and_to_string_method() {
		String channelName = "myChannel";
		CCSChannel channel = new CCSChannel(channelName);

		Assert.assertThat(channel.toString(), is(channelName));
	}

}
