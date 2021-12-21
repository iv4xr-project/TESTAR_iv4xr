package nl.ou.testar.StateModel.ActionSelection;

import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import java.util.ArrayList;
import java.util.List;

public class CompoundFactory {

    public static CompoundActionSelector getCompoundActionSelector(Settings settings) {
        // this is hardcoded for now, but can be configurable in the future.
        List<ActionSelector> selectors = new ArrayList<>();
        if (settings.get(ConfigTags.ActionSelectionAlgorithm).equals("unvisited")) {
            selectors.add(new ImprovedUnvisitedActionSelector());
        }
        if (settings.get(ConfigTags.ActionSelectionAlgorithm).equals("explore")) {
        	selectors.add(new ExploreNavigableSpaceSelector());
        }
        selectors.add(new RandomActionSelector());
        return new CompoundActionSelector(selectors);
    }

}
