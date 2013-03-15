Providing provenance for a RESTful service
==========================================

This is an example of how a a RESTful service, implemented using [JAX-RS and CXF](http://cxf.apache.org/docs/jax-rs-basics.html), can expose [provenance](http://www.w3.org/TR/prov-overview/) of the resources it exposes. This code is based on the [cxf-jaxrs-service](http://cxf.apache.org/docs/jax-rs-maven-plugins.html#JAX-RSMavenPlugins-Archetypes).

There are two branches in this project on https://github.com/stain/paq:
* *master* - REST service that can greet and return provenance of greeting
* *paq* - REST service that also provides link between greet and its provenance (TODO)


To compile/run, you will need Java and [Maven](http://maven.apache.org/download.cgi):

    stain@ralph-ubuntu:~/src/paq$ mvn clean install tomcat:run-war-only
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Example PROV-AQ usage 0.0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    (..)
    Mar 15, 2013 3:59:33 PM org.apache.coyote.http11.Http11Protocol start
    INFO: Starting Coyote HTTP/1.1 on http-8080
    
Check the HelloWorld REST service is working (in a different shell):

    stain@ralph-ubuntu:~/src/paq$ curl http://localhost:8080/paq/hello/greet/Alice
    Hello, Alice

You may replace *Alice* with any name, as long as it is URI escaped:

    stain@ralph-ubuntu:~/src/paq$ curl http://localhost:8080/paq/hello/greet/Joe%20Bloggs
    Hello, Joe Bloggs


This example service provides [provenance](http://www.w3.org/TR/prov-overview/), using the [PROV-N](http://www.w3.org/TR/prov-n/) format for readability:

    stain@ralph-ubuntu:~/src/paq$ curl -i http://localhost:8080/paq/hello/provenance/greet/Alice 
    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Date: Fri, 15 Mar 2013 16:06:38 GMT
    Content-Type: text/provenance-notation
    Content-Length: 312

    document
     prefix greet <http://localhost:8080/paq/hello/greet/>
     prefix app <http://localhost:8080/paq/hello/>
     entity(greet:Alice)
     wasDerivedFrom(greet:Alice, name)
     entity(name, [ prov:value="Alice" ])
     agent(app:hello, [ prov:type='prov:SoftwareAgent' ])
     wasAttributedTo(greet:Alice, app:hello)
    endDocument

Note that we used the {{-i}} parameter above to verify that the correct media-type [text/provenance-notation](http://www.iana.org/assignments/media-types/text/provenance-notation) was returned.

The question arises, how can someone accessing the greeting service at http://localhost:8080/paq/hello/greet/Alice (greet:Alice) know that they can find the provenance at http://localhost:8080/paq/hello/provenance/greet/Alice? This is where the [PROV-AQ](http://www.w3.org/TR/prov-aq/) note comes handy.

It says that a [resource accessed by HTTP](http://www.w3.org/TR/2013/WD-prov-aq-20130312/#resource-accessed-by-http) can 
be accessed by adding a {{Link}} header with the relation "http://www.w3.org/ns/prov#has_provenance". So in our case, this can be achived with:

    Link: <http://localhost:8080/paq/hello/provenance/greet/Alice>; rel="http://www.w3.org/ns/prov#has_provenance"

We do not need to provide the anchor, as the *target-URI* for
greet:Alice is its own URI - that is, you can find the same URI you
accessed within the PROV-N document. 

If on the other hand our web service used different 
resources for different representations, like 
<http://localhost:8080/paq/hello/greet/Alice.html>, then 
we might want to keep <http://localhost:8080/paq/hello/greet/Alice>
as the entity URI in the PROV trace, at least as long as its provenance
would be the same. This we can indicate by adding the *anchor*
attribute in the Link headers returned for Alice.html:

    Link: <http://localhost:8080/paq/hello/provenance/greet/Alice>; rel="http://www.w3.org/ns/prov#has_provenance";
        anchor="http://localhost:8080/paq/hello/greet/Alice"

OK, so how do we provide this Link header? Our existing greeting is
quite simple thanks to [JAX-RS and CXF](http://cxf.apache.org/docs/jax-rs-basics.html):

    @GET
    @Path("/greet/{name}")
    @Produces("text/plain")
    public String greet(@PathParam("name") String name) {
        return "Hello, " + name + "\n";
    }

Our provenance method is a bit more complicated as it needs to [generate 
the absolute URIs](http://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-URIcalculationusingUriInfoandUriBuilder) for the greeting resource and then build the PROV-N trace - here using naive string contatination.

    @GET
    @Produces("text/provenance-notation")
    @Path("/provenance/greet/{name}")
    public String greetProvenance(@PathParam("name") String name,
    		@Context UriInfo ui) {
    	UriBuilder appUri = ui.getBaseUriBuilder().path(getClass());
		UriBuilder greetUri = appUri.path(getClass(), "greet");
    	
    	URI greetURI = greetUri.build(name);
    	URI appURI = appUri.build("").resolve("../");
    	
    	StringBuilder prov = new StringBuilder();
    	prov.append("document\n");
        // ...
    	prov.append(String.format(" wasAttributedTo(%s, app:hello)\n", greetEntity));
    	prov.append("endDocument\n");
    	return prov.toString();
    	
    }

So in order to provide the RESTful links we will need to insert *Link:*
headers in the greet() response.

TODO: How to do this?
