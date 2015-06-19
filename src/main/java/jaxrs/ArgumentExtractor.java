package jaxrs;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Instances of this class can extract the right values from a {@link Request} in order to call a {@link Method}
 * The class allows the caller to defined some set of prefixed method parameters
 */
public class ArgumentExtractor {
  private final List<ParameterExtractor> parameterExtractors;
  private final Object[] prefixParameterValues;

  public ArgumentExtractor(Method method, Object ... prefixParameterValues) {
    this.prefixParameterValues = prefixParameterValues;

    this.parameterExtractors =
      stream(method.getParameters())
        .skip(prefixParameterValues.length)
        .map(ParameterExtractor::new)
        .collect(toList());
  }

  public Object[] arguments(Request request) {
    return concat(
      stream(prefixParameterValues),
      parameterExtractors.stream().map(p -> p.valueFromRequest(request))
    ).collect(toList()).toArray();
  }
}
