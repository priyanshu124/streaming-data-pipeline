package core.domain;

import java.util.Map;

public interface Event {
    String key();
    long eventTime();
    Map<String, Object> payload(); 
}
