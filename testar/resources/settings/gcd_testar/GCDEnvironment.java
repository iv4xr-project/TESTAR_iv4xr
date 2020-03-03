package gcd_testar;

import java.util.List;
import java.util.Map;

import org.fruit.Assert;
import org.fruit.Pair;
import org.fruit.Util;
import org.fruit.alayer.AutomationCache;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tag;
import org.fruit.alayer.exceptions.NoSuchTagException;
import org.fruit.alayer.exceptions.SystemStopException;

import nl.uu.cs.aplib.mainConcepts.Environment;

import gcd_testar.GCDGame;

public class GCDEnvironment extends Environment implements SUT {

	protected GCDGame gameUnderTest;

	public GCDGame getGameUnderTest() {
		return gameUnderTest;
	}

	public void newGameUnderTest() {
		this.gameUnderTest = new GCDGame();
		refreshWorker();
	}

	int x ; 
	int y ;
	int gcd ;
	boolean win ;

	/**
	 * Implement the method to sync this Environment's view on the program-under-test with
	 * the real state of the program-under-test:
	 */
	@Override
	public void refreshWorker() { 
		x = gameUnderTest.x ;
		y = gameUnderTest.y ;
		gcd = gameUnderTest.gcd ;
		win = gameUnderTest.win() ;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getGcd() {
		return gcd;
	}

	public boolean isWin() {
		return win;
	}

	public GCDEnvironment() { super() ; }

	/**
	 * Implement the method to let agents to send commands to the program-under-test:
	 */
	@Override
	protected Object sendCommand_(EnvOperation cmd) {
		logger.info("Command " + cmd.command);
		switch (cmd.command) {
		case "up" : gameUnderTest.up() ; break ;
		case "down" : gameUnderTest.down() ; break ;
		case "right" : gameUnderTest.right() ; break ;
		case "left" : gameUnderTest.left() ; break ;			
		}
		// we'll re-sync this Environment after the command:
		refreshWorker() ;
		return null ;
	}

	@Override
	public String toString() { return "(" + x + "," + y + "), gcd=" + gcd ; }
	
	private Map<Tag<?>, Object> tagValues = Util.newHashMap();
	
	@Override
	public <T> T get(Tag<T> tag) throws NoSuchTagException {
		T ret = get(tag, null);
		if(ret == null)
			throw new NoSuchTagException(tag);
		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T> T get(Tag<T> tag, T defaultValue) {
		Assert.notNull(tag);
		T ret = (T) tagValues.get(tag);
		if(ret == null && !tagValues.containsKey(tag))
			ret = fetch(tag);
		return ret == null ? defaultValue : ret;
	}
	protected <T> T fetch(Tag<T> tag){ return null; }

	@Override
	public Iterable<Tag<?>> tags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void set(Tag<T> tag, T value) {
		Assert.notNull(tag, value);
		Assert.isTrue(tag.type().isInstance(value), "Value not of type required by this tag!");
		tagValues.put(tag, value);
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
		refreshWorker();
		return "(" + x + "," + y + "), win?=" + win ;
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
