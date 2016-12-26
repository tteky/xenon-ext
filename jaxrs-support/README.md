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


