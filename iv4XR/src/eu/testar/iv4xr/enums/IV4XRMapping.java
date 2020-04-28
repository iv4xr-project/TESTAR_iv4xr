package eu.testar.iv4xr.enums;

import static es.upv.staq.testar.StateManagementTags.WidgetPath;
import java.util.HashMap;
import java.util.Map;

import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;

public class IV4XRMapping {

	// a mapping from the state management tags to iv4XR tags
    private static Map<Tag<?>, Tag<?>> stateTagMappingIV4XR = new HashMap<Tag<?>, Tag<?>>()
    {
        {
            put(WidgetPath, Tags.Path);
        }
    };
    
    /**
     * This method will return its equivalent, internal iv4XR tag, if available.
     * @param mappedTag
     * @return
     */
    public static <T> Tag<T> getMappedStateTag(Tag<T> mappedTag) {
        return (Tag<T>) stateTagMappingIV4XR.getOrDefault(mappedTag, null);
    }
}
