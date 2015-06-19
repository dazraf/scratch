package jaxrs;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple HTTP jaxrs.Request example class - just for test purposes
 */
public class Request {
  private final Map<String, String> pathParams = new HashMap<>();
  private final Map<String, String> queryParams = new HashMap<>();

  public void addPathParam(String key, String value) {
    pathParams.put(key, value);
  }
  public void addQueryParam(String key, String value) {
    queryParams.put(key, value);
  }
  public String pathParam(String key) {
    return pathParams.get(key);
  }
  public String queryParam(String key) {
    return queryParams.get(key);
  }
}
