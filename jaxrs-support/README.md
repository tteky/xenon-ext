#JAXRS Support

Inspired by JAXRS proxy based client builder, this extension provides JAXRS annotation processing ability to <br/>
1. JAX RS annotation based request-routing primarily for stateless services (RequestRouterBuilder)
2. Jax RS Service Client to interact with Xenon services from client perspective (JaxRsServiceClient)

This extension completely uses Operation for handling request and doesn't depend on any other external http libraries.

<b>Limitation </b>
The intention of this extension is not to provide or implement the whole JAX-RS spec on top of Xenon.
1. We support only a sub-set of annotation from client perspective
2. Usage of these in server side requires some understanding of Xenon framework

Note: Test cases will serve as a usage document

Currently supported annotations in client side are 
1. GET/POST/DELETE/PUT/DELETE
2. Custom annotation PATCH as jaxrs spec doesn't have one
3. HEADER/COOKIE
4. Custom annotation @Operation to receive operation as a parameter
4. Custom annotation @OperationBody to receive body as a payload

### Server Side usage

First step is declare your *service contract* like below (This is completely optional )
```java
@Path(SELF_LINK)
public interface SampleService {

    String SELF_LINK = "/xenon-ext/sample";

    /**
    * Example of asynchronous service
    * Employee response will be send only when the CompletableFuture is complete and hence provides async capability in server side
    */
    @Path("/category/{id}")
    @GET
    CompletableFuture<Employee> fetchSample(@PathParam("id") String id, @QueryParam("tags") String tag);

    /**
    * Example of a POST operation. 
    * Employee POJO can have javax validation annotations and it will be validated before the method is invoked
    */
    @POST
    CompletableFuture<Employee> newEmployee(@OperaionBody Employee employee);
}
```

Second step is to Implement the service by extending JaxRsBridgeStatelessService. * Note the use of StatefulServiceContract*
```java
public class SampleServiceImpl extends JaxRsBridgeStatelessService implements SampleService {
    
    private StatefulServiceContract<Employee> employeeSvc;
    
     public SampleServiceImpl() {
          setContractInterface(SampleService.class); // this is required only if you implement interface containing JAX RS annotations
     }
     
      @Override
      public OperationProcessingChain getOperationProcessingChain() {
             if (employeeSvc == null) {
                 querySvc = ServiceClientUtil.newStatefulSvcContract(getHost(), 
                    EmployeeService.FACTORY_LINK, Employee.class);
             }
             return super.getOperationProcessingChain();
      }
    
    public CompletableFuture<Employee> fetchSample(@PathParam("id") String id, @QueryParam("tags") String tag) {
         // do implementation
         // do things asyncly
         return null;
    }
    //other methods
    
    // Can define additional methods other than implementing interface too
    @PUT
    @Path("/{id}")
    CompletableFuture<Employee> updateEmployee(@PathParam("id") String id,@OperaionBody Employee employee) {
         // delegate to a statefule service
         return employeeSvc.put(id,employee);
    }
}
```
Now you have a fully functional REST service using Xenon

### Client Side usage
When you want to use the proxy client builder, you need an interface with JAX-RS annotations like the one declared above.

Example usage with in Xenon host, when the sample service is running in same host
```java
       SampleService sampleService = JaxRsServiceClient.newBuilder()
                 .withHost(host)
                 .withResourceInterface(FullSampleService.class)
                 .build();
```
Example usage with in Xenon host, when the sample service is hosted externally
```java
       SampleService sampleService = JaxRsServiceClient.newBuilder()
                 .withHost(host)
                 .withBaseUri('http://someServiceHost:8000')
                 .withResourceInterface(FullSampleService.class)
                 .build();
```
Example usage outside Xenon host
```java
       SampleService sampleService = JaxRsServiceClient.newBuilder()
                 .withBaseUri('http://someServiceHost:8000')
                 .withResourceInterface(FullSampleService.class)
                 .build();
```