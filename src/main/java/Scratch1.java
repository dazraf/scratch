import jaxrs.ArgumentExtractor;
import jaxrs.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Scratch1 {

  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    GreetingResource resource = new GreetingResource();

    // this would be bound differently in the real world
    Method method = resource.getClass().getMethod("greeting", String.class, String.class, String.class);

    // setup a request, particularly with partial requests
    Request request = new Request();
    // request.addPathParam("greeting", "hello");
    request.addQueryParam("place", "earth");

    ArgumentExtractor extractor = new ArgumentExtractor(method, "scratch1");
    Object[] arguments = extractor.arguments(request);
    method.invoke(resource, arguments);
  }
}
