package com.herger.verticarest;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import com.sun.security.auth.UserPrincipal;

public class VerticaSecurityContext implements SecurityContext {

	private String username = "nobody";
	
	public VerticaSecurityContext() { }

	public VerticaSecurityContext(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		Principal p = new UserPrincipal(username);
		return p;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAuthenticationScheme() {
		// TODO Auto-generated method stub
		return null;
	}

}
