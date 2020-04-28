package eu.testar.iv4xr;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.fruit.Assert;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.StateBuilder;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.StateBuildException;

public class IV4XRStateBuilder implements StateBuilder {

	private static final long serialVersionUID = 5818255982945782620L;

	private static final int defaultThreadPoolCount = 1;
	private final double timeOut;
	private transient ExecutorService executor;

	public IV4XRStateBuilder(double timeOut) {
		Assert.isTrue(timeOut > 0);
		this.timeOut = timeOut;
		
		// Needed to be able to schedule asynchronous tasks conveniently.
		executor = Executors.newFixedThreadPool(defaultThreadPoolCount);
	}

	@Override
	public IV4XRState apply(SUT system) throws StateBuildException {
		try {
			Future<IV4XRState> future = executor.submit(new IV4XRStateFetcher(system));
			IV4XRState state = future.get((long) (timeOut), TimeUnit.SECONDS);
			
			return state;
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			throw new StateBuildException(e.getMessage());
		}
		catch (TimeoutException e) {
			IV4XRRootElement iv4XRrootElement = IV4XRStateFetcher.buildRoot(system);
			IV4XRState iv4XRState = new IV4XRState(iv4XRrootElement);
			iv4XRState.set(Tags.Role, Roles.Process);
			iv4XRState.set(Tags.NotResponding, true);
			
			return iv4XRState;
		}
	}
}
