package com.herger.verticarest;

import java.lang.reflect.Method;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
 
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
 
/**
 * This filter verify the access permissions for a user
 * based on username and password provided in request
 * */
@Provider
public class PasswordAuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter
{
     
    @Context
    private ResourceInfo resourceInfo;
     
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private static final String BEARER_AUTHENTICATION_SCHEME = "Bearer";
      
    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        Method method = resourceInfo.getResourceMethod();
        //Access allowed for all
        if( ! method.isAnnotationPresent(PermitAll.class))
        {
            //Access denied for all
            if(method.isAnnotationPresent(DenyAll.class))
            {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                         .entity("Access blocked for all users !!").build());
                return;
            }
              
            //Get request headers
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
              
            //Fetch authorization header
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
              
            //If no authorization information present; use API user if configured, or block access
            if(authorization == null || authorization.isEmpty())
            {
            	if (VerticaConfig.ALLOW_API_USER) {
                    SecurityContext sc = new VerticaSecurityContext("BASIC:"+VerticaConfig.JDBC_API_USER+":"+VerticaConfig.JDBC_API_PASS);
                    requestContext.setSecurityContext(sc);
                    return;
            	}
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You cannot access this resource").build());
                return;
            }
            
            // check for JWT login
            if (authorization.get(0).contains(BEARER_AUTHENTICATION_SCHEME)) {
            	final String jwt = authorization.get(0).replaceFirst(BEARER_AUTHENTICATION_SCHEME + " ", "");
            	if (verifyToken(jwt, requestContext) == false) {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Invalid JWT").build());
            	}
            	return;
            }
              
            //Get encoded username and password
            final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
              
            //Decode username and password

            String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
  
            //Split username and password tokens
            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String username = tokenizer.nextToken();
            final String password = tokenizer.nextToken();
            List<String> idList = new ArrayList<String>();
            idList.add( username );
            headers.put( "username", idList );
            SecurityContext sc = new VerticaSecurityContext(isUserAllowed(username, password));
            requestContext.setSecurityContext(sc);
            //Verifying Username and password
            System.out.println(username);
            System.out.println(password);
        }
    }
    
    private String isUserAllowed(final String username, final String password)
    {
    	VerticaJdbcAdapter vertica = new VerticaJdbcAdapter();
        if (vertica.canLogin(new Credential(username, password))) {
        	return "BASIC:"+username+":"+password;
        } else {
        	return "BASIC:"+VerticaConfig.JDBC_API_USER+":"+VerticaConfig.JDBC_API_PASS;
        }
    }
    
    private boolean verifyToken(final String jwt, ContainerRequestContext requestContext) {
		//Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(VerticaConfig.JWT_PRIVATE_KEY_BASE64));
		boolean valid = false;
    	try {
    		Jws<Claims> jws = Jwts.parserBuilder()  // (1)
    	    .setSigningKey(key)         // (2)
    	    .build()                    // (3)
    	    .parseClaimsJws(jwt); // (4)
    	    
    	    // we can safely trust the JWT
    	     valid = true;
    	     System.out.println("JWS OK:"+jws.toString());
    	     String username = "jwt";
             SecurityContext sc = new VerticaSecurityContext(username);
             requestContext.setSecurityContext(sc);
    	} catch (JwtException ex) {       // (5)
    	    System.out.println(ex.getMessage());
    	    // we *cannot* use the JWT as intended by its creator
    	}
    	return valid;
    }
}