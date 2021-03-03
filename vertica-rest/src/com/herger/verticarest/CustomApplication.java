package com.herger.verticarest;

import org.glassfish.jersey.server.ResourceConfig;
 
public class CustomApplication extends ResourceConfig 
{
    public CustomApplication() 
    {
        packages("com.herger.verticarest");
        register(VerticaDemo.class);
 
        //Register Auth Filter here
        register(PasswordAuthenticationFilter.class);
    }
}