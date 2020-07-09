package com.everis.ws.integrator;

import com.sun.jersey.spi.container.*;

/** Clients may override the HTTP method by setting either the X-HTTP-Method-Override header or
 * the _method form or query parameter in a POST request.  If both the X-HTTP-Method-Override
 * header and _method parameter are present in the request then the X-HTTP-Method-Override header
 * will be used.
 *
 * Inspired by https://jersey.dev.java.net/nonav/apidocs/1.1.0-ea/jersey/com/sun/jersey/api/container/filter/PostReplaceFilter.html
 */

public class OverrideHttpMethodFilter implements ContainerRequestFilter {
	private static final String HEADER = "X-HTTP-Method-Override";
    /** The name of the form or query parameter that overrides the HTTP method. */
    public static final String METHOD = "_method";
 
    @Override
    public ContainerRequest filter(ContainerRequest request)
    {
        if (request.getMethod().equalsIgnoreCase("POST"))
            if (!override(request.getRequestHeaders().getFirst(HEADER), request))
                if (!override(request.getFormParameters().getFirst(METHOD), request))
                    override(request.getQueryParameters().getFirst(METHOD), request);
 
        return request;
    }
 
    private boolean override(String method, ContainerRequest request)
    {
    	if(null!=method && !method.isEmpty())
        {
    		request.setMethod(method.toUpperCase());
            return true;
        }
 
        return false;
    }
}
