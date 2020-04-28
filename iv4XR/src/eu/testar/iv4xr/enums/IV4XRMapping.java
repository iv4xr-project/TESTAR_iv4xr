package eu.testar.iv4xr.enums;

import static es.upv.staq.testar.StateManagementTags.*;
import java.util.HashMap;
import java.util.Map;

import org.fruit.alayer.Tag;

public class IV4XRMapping {

	// a mapping from the state management tags to iv4XR tags
    private static Map<Tag<?>, Tag<?>> stateTagMappingIV4XR = new HashMap<Tag<?>, Tag<?>>()
    {
        {
            put(WidgetPath, IV4XRtags.entityPositionRepresentation);
            put(WidgetControlType, IV4XRtags.entityType);
            put(WidgetTitle, IV4XRtags.entityId);
            put(WidgetIsEnabled, IV4XRtags.entityIsActive);
            put(WidgetValueValue, IV4XRtags.entityTag);
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
