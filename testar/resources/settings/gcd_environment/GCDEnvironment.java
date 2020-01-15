package gcd_environment;

import java.util.List;

import org.fruit.Pair;
import org.fruit.alayer.AutomationCache;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tag;
import org.fruit.alayer.exceptions.NoSuchTagException;
import org.fruit.alayer.exceptions.SystemStopException;

import nl.uu.cs.aplib.mainConcepts.Environment;

import gcd_environment.GCDGame;

public class GCDEnvironment extends Environment implements SUT {

	protected GCDGame gameUnderTest;

	public GCDGame getGameUnderTest() {
		refreshWorker();
		return gameUnderTest;
	}

	public void newGameUnderTest() {
		this.gameUnderTest = new GCDGame();
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
		refreshWorker();
		return x;
	}

	public int getY() {
		refreshWorker();
		return y;
	}

	public int getGcd() {
		refreshWorker();
		return gcd;
	}

	public boolean isWin() {
		refreshWorker();
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
