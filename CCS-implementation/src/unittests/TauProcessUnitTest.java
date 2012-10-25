package unittests;

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

import processes.CCSProcess;
import processes.TauCCSProcess;

public class TauProcessUnitTest extends CCSProcessUnitTest {

	@Test
	public void tau_process_print_itself_as_it_is() {
		CCSProcess tau = makeProcessUnderTest();
		Assert.assertThat(tau.toString(), is("tau"));
	}

	@Override
	protected CCSProcess makeProcessUnderTest() {
		return new TauCCSProcess();
	}

	@Test
	public void tau_process_hasnt_any_state() {
		CCSProcess tau = makeProcessUnderTest();
		Assert.assertThat(tau.getNextStates().size(), is(0));
	}

}
