package labrecruits_demo;

import java.util.List;

import org.fruit.Pair;
import org.fruit.alayer.AutomationCache;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tag;
import org.fruit.alayer.exceptions.NoSuchTagException;
import org.fruit.alayer.exceptions.SystemStopException;

public class labRecruitsSUT implements SUT {

	@Override
	public <T> T get(Tag<T> tag) throws NoSuchTagException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Tag<T> tag, T defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Tag<?>> tags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void set(Tag<T> tag, T value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(Tag<?> tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws SystemStopException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<Long, String>> getRunningProcesses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNativeAutomationCache() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AutomationCache getNativeAutomationCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
