package com.example.provaq.rest;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;

@Path("/")
public class HelloWorld {

	@GET
    @Path("hello/{name}")
    @Produces("text/plain")
    public String hello(@PathParam("name") String name) {
        String greeting = "Hello, " + name + "\n";
        return greeting;
    }
	
    @GET
    @Path("provenance/hello/{name}")
    @Produces("text/provenance-notation")
    public String helloProvenance(@PathParam("name") String name,
    		@Context UriInfo ui) throws IOException {
    	// Get our absolute URI
    	// See http://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-URIcalculationusingUriInfoandUriBuilder    	
    	UriBuilder appUri = ui.getBaseUriBuilder();
    	// Absolute URIs for resources we are to give provenance about
    	URI helloURI = appUri.path(getClass(), "hello").build(name);    	
    	
    	// Prepare prefixes for PROV-N qualified names
    	URI appURI = appUri.build("").resolve("../");    	
    	URI helloPrefix = helloURI.resolve("./");
    	
    	// The PROV-N qualified name for our /hello/{name} resource
    	String helloEntity = "hello:" + helloPrefix.relativize(helloURI);
    	
    	// Simple PROV-N trace, see <http://www.w3.org/TR/prov-n/>
    	// Here this is done in a naive way by loading a template 
    	// from src/main/resources and do string-replace to insert
    	// our URIs.
    	String template = IOUtils.toString(getClass().getResourceAsStream("/provTemplate.txt"));
    	String prov = MessageFormat.format(template, 
    			helloPrefix, appURI, helloEntity, name);
    	// Note: PROV-N should be be built using say the PROV Toolbox 
    	// rather than this naive template approach!
    	return prov;
    }
}

