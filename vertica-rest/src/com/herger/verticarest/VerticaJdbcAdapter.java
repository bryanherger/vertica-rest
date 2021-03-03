package com.herger.verticarest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONObject;

public class VerticaJdbcAdapter {
	public boolean canLogin(Credential credential) {
		boolean canLogin = false;
		try {
			Class.forName(VerticaConfig.JDBC_DRIVER);
			Connection c = DriverManager.getConnection(VerticaConfig.JDBC_URL, credential.getUsername(), credential.getPassword());
			c.close();
			canLogin = true;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return canLogin;
	}
	
	public String select(String sql) {
		System.out.println("sql:"+sql);
		JSONObject response = new JSONObject();
		String v = "", e = "";
		int httpStatus = 200;

		JSONArray rows = new JSONArray();
		try {
			Class.forName(VerticaConfig.JDBC_DRIVER);
			Connection c = DriverManager.getConnection(VerticaConfig.JDBC_URL, VerticaConfig.JDBC_DBADMIN_USER, VerticaConfig.JDBC_DBADMIN_PASS);
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			Map<String,String> row = new HashMap<>();
			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					row.put(rsmd.getColumnName(i), rs.getString(i));
				}
				rows.put(row);
			}
			rs.close();
		} catch (Exception ex) {
			e = ex.getMessage();
			httpStatus = 404;
		}

		response.put("result", rows);
		response.put("exception", e);
		response.put("status", httpStatus);
		
		return response.toString();
	}

	public Response select(String sql, SecurityContext securityContext) {
		System.out.println("sql:"+sql);
		JSONObject response = new JSONObject();
		String e = "";
		int httpStatus = 200;
		String[] scTokens = securityContext.getUserPrincipal().getName().split(":");

		JSONArray rows = new JSONArray();
		try {
			Class.forName(VerticaConfig.JDBC_DRIVER);
			Connection c = DriverManager.getConnection(VerticaConfig.JDBC_URL, scTokens[1], scTokens[2]);
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			Map<String,String> row = new HashMap<>();
			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					row.put(rsmd.getColumnName(i), rs.getString(i));
				}
				rows.put(row);
			}
			rs.close();
		} catch (Exception ex) {
			e = ex.getMessage();
			httpStatus = 404;
		}

		response.put("result", rows);
		response.put("exception", e);
		
		return Response.status(httpStatus).entity(response.toString()).build();
	}
}
