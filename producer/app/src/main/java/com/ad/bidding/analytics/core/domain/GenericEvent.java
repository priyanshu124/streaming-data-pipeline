package core.domain;

import java.util.Map;

@Value
@Builder
public class GenericEvent {
    String key;
    long eventTime;
    Map<String, Object> payload;
}
