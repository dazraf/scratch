import javax.ws.rs.*;

@Path("/greetings")
public class GreetingResource {
  @GET
  @Path("/resource")
  public void greeting(
    String customContext,

    @DefaultValue("bonjour")
    @PathParam("greeting")
    String greeting,

    @DefaultValue("world")
    @QueryParam("place")
    String place) {

    System.out.format("%s: %s, %s!", customContext, greeting, place);
  }
}
