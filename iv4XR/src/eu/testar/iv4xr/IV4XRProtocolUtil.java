package eu.testar.iv4xr;

import org.fruit.alayer.State;
import org.fruit.alayer.Tags;

import es.upv.staq.testar.ProtocolUtil;
import es.upv.staq.testar.serialisation.ScreenshotSerialiser;

public class IV4XRProtocolUtil extends ProtocolUtil {

	public IV4XRProtocolUtil() {}

	@Override
	public String getStateshot(State state) {
		return ScreenshotSerialiser.saveStateshot(state.get(Tags.ConcreteID, "NoConcreteIdAvailable"), getStateshotBinary(state));
	}
}
