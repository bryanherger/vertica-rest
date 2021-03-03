package com.herger.verticarest;

import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.Set;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@DeclareRoles({"ADMIN"})
@Path("/vertica")
public class VerticaDemo {
	// https://mkyong.com/webservices/jax-rs/jax-rs-queryparam-example/
	
	VerticaJdbcAdapter vertica = new VerticaJdbcAdapter();

	public static String JDBC_DRIVER = "com.vertica.jdbc.Driver";
	public static String JDBC_URL = "jdbc:vertica://192.168.1.206:5433/d2";
	public static String JDBC_USER = "dbadmin";
	public static String JDBC_PASS = "Vertica1!";

	@Path("/license")
	@GET
	@Produces("application/json")
	public Response getLicense(@Context HttpServletRequest requestContext, @Context SecurityContext securityContext) throws JSONException {
		return vertica.select("select get_compliance_status();", securityContext);
	}

	@GET
	@Produces("application/json")
	public Response getVersion(@Context SecurityContext securityContext) throws JSONException {
		System.out.println("sc(username):"+(securityContext==null?"(null)":securityContext.getUserPrincipal()));
		return vertica.select("select version();", securityContext);
	}

	@Path("/tables")
	@GET
	@Produces("application/json")
	public Response getTables(@Context SecurityContext sc) throws JSONException {
		return vertica.select("select * from tables order by table_schema, table_name limit 10;", sc);
	}

	@Path("/tables/{t}")
	@GET
	@Produces("application/json")
	public Response getTable(@Context SecurityContext sc, @PathParam("t") String t) throws JSONException {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("table", t);
		//jsonObject.put("name", sc.getUserPrincipal().getName());

		String result = "" + jsonObject;
		return Response.status(200).entity(result).build();
	}

	@Path("/select")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response select(VerticaSql vsql, @Context SecurityContext sc) throws JSONException {
		String query = vsql.toSql();
		System.out.println(query);
		return vertica.select(query, sc);
	}

	@Path("/select/{f}")
	@GET
	@Produces("application/json")
	public Response select(@PathParam("f") String f, @Context SecurityContext sc,
			@DefaultValue("0") @QueryParam("offset") int offset, 
			@DefaultValue("10") @QueryParam("limit") int limit) throws JSONException {
		String query = "select * from "+f+" offset "+offset+" limit "+limit+";";
		return vertica.select(query, sc);
	}

	@Path("/select/{f}/{q}")
	@GET
	@Produces("application/json")
	public Response selectWhere(@Context SecurityContext sc, @PathParam("f") String f, 
			@PathParam("q") String q,
			@DefaultValue("0") @QueryParam("offset") int offset, 
			@DefaultValue("10") @QueryParam("limit") int limit) throws JSONException {
		String query = "select * from "+f+" "+q+" offset "+offset+" limit "+limit+";";
		return vertica.select(query, sc);
	}

	// https://stackoverflow.com/questions/3825084/rest-how-get-ip-address-of-caller
	@POST
	@Path("/authenticate")
	@PermitAll
	@Produces("application/json")
	@Consumes("application/json")
	public Response authenticate(@Context HttpServletRequest requestContext, @Context SecurityContext securityContext, Credential credential ) {

		JSONObject jsonObject = new JSONObject();
		int httpStatus = 200;
		String jdbcOk = "OK", e = "", jwt = "";
		try {
			if (! vertica.canLogin(credential)) {
				throw new Exception("Invalid credential");
			}

			// We need a signing key, so we'll create one just for this example. Usually
			// the key would be read from your application configuration instead.
			//Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(VerticaConfig.JWT_PRIVATE_KEY_BASE64));
			jwt = Jwts.builder().setSubject(credential.getUsername()).signWith(key).compact();
		} catch (Exception ex) {
			e = ex.getMessage();
			httpStatus = 401;
			jdbcOk = "FAIL";
		}

		jsonObject.put("auth", jdbcOk);
		jsonObject.put("jwt", jwt);
		jsonObject.put("exception", e);
		jsonObject.put("object", credential==null?"(null)":credential.toString());

		return Response.status(httpStatus).entity(jsonObject.toString()).build();		
	}
	
    @GET
	@PermitAll
	@Path("headers")
    public Response queryHeaderInfo(@Context HttpHeaders httpHeaders){
         
        /** how to get specific header info? **/
        //String cacheControl = httpHeaders.getRequestHeader("Cache-Control").get(0);
        //System.out.println("Cache-Control: "+cacheControl);
        /** get list of all header parameters from request **/
        Set<String> headerKeys = httpHeaders.getRequestHeaders().keySet();
        for(String header:headerKeys){
            System.out.println(header+":"+httpHeaders.getRequestHeader(header).get(0));
        }
        return Response.status(200).entity("successfully queried header info").build();
    } 
}