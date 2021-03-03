package com.herger.verticarest;

public class VerticaConfig {
	// update the following to match your environment.
	// TODO: make this configurable via init params or resource
	
	public static String JDBC_DRIVER = "com.vertica.jdbc.Driver";
	public static String JDBC_URL = "jdbc:vertica://192.168.1.206:5433/d2";
	public static String JDBC_DBADMIN_USER = "dbadmin";
	public static String JDBC_DBADMIN_PASS = "Vertica1!";
	// this is insecure and provided for demo only. This user is used for anonymous access. This user must exist in the DB
	public static boolean ALLOW_API_USER = true;
	public static String JDBC_API_USER = "jwtapi";
	public static String JDBC_API_PASS = "P@ssw0rd";
	// this is also insecure, roll your own keys
	public static String JWT_PRIVATE_KEY_BASE64 = "55zu98gEpFZH08GicNfXvQGoEsjrbHbILiKiJRfEpt8=";

}
