package jaxrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * The class is instantiated per parameter that requires a value from the {@link Request}
 * It looks for Jax RS annotations as clues as to where it can retrieve the values it needs and using what name
 * It handles {@link QueryParam} {@link PathParam} and {@link DefaultValue}
 */
class ParameterExtractor {
  // these are the types this class can handle
  private static final Map<Class<?>, Function<String, Object>> parsers = new HashMap<Class<?>, Function<String, Object>>() {{
    put(String.class, s -> s);
    put(Integer.class, Integer::parseInt);
    put(Double.class, Double::parseDouble);
    put(Long.class, Long::parseLong);
    put(LocalDateTime.class, LocalDateTime::parse);
    put(Boolean.class, Boolean::parseBoolean);
  }};

  // the following two are curried functions that simplify the logic
  private static final Function<String, Function<Request, Optional<String>>> queryParamFn =
    (String string) -> (Request request)  -> ofNullable(request.queryParam(string));

  private static final Function<String, Function<Request, Optional<String>>> pathParamFn =
    (String string) -> (Request request)  -> ofNullable(request.pathParam(string));

  // this is the actual function that retrieves the value to be passed to the argument
  private final Function<Request, Object> fn;

  public ParameterExtractor(Parameter parameter) {
    this.fn = createExtractorFunction(parameter);
  }

  @SuppressWarnings("unchecked")
  public Object valueFromRequest(Request request) {
    return fn.apply(request);
  }

  private static Function<Request, Object> createExtractorFunction(Parameter parameter) {
    // so we need to create an extractor function
    // such that given a jaxrs.Request, returns the right value of the right type for this parameter

    // this is how we do it

    // see if we have default (string) value
    String defaultValue = getAnnotation(parameter, DefaultValue.class).map(DefaultValue::value).get();

    // try to get hold of the annotations - no annotations is ok
    Optional<QueryParam> queryAnnotation = getAnnotation(parameter, QueryParam.class);
    Optional<PathParam> pathAnnotation = getAnnotation(parameter, PathParam.class);

    // figure out the parameter 'name' -
    // this can be either the value of the QueryParam annotation, if it exists and it's value is not null
    // or the value of the PathParam annotation, if it exists and it's value is not null
    // or the parameter name itself
    // this slightly messy bit of logic can be elegantly expressed as follows
    String paramName = getParameterName(parameter, queryAnnotation, pathAnnotation);

    // now that we have the parameter name, we want to return the resolution function
    return firstPresent(queryAnnotation.map(a -> queryParamFn), of(pathParamFn)) // resolve if we are query or a path extractor
      .get() // get the function that we can use
      .apply(paramName) // apply the name that we can use, to get a function that can work with requests
      .andThen(result -> result.orElse(defaultValue))
      .andThen(value -> parsers.get(parameter.getType()).apply(value));
  }

  private static String getParameterName(Parameter parameter, Optional<QueryParam> queryAnnotation, Optional<PathParam> pathAnnotation) {
    return firstPresent(
      queryAnnotation.map(QueryParam::value),
      pathAnnotation.map(PathParam::value))
      .orElse(parameter.getName());
  }

  private static <T extends Annotation> Optional<T> getAnnotation(Parameter parameter, Class<T> annotationClazz) {
    return ofNullable(parameter.getAnnotation(annotationClazz));
  }

  // This should be available on Optional but alas
  @SafeVarargs
  private static <T> Optional<T> firstPresent(Optional<T>... list) {
    return stream(list)
      .filter(Optional::isPresent)
      .findFirst()
      .orElse(empty());
  }
}
