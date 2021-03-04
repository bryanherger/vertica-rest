# vertica-rest

An unsupported demo showing how to build a REST service to access data in Vertica.  This project is built with Eclipse and Maven and deployed on Tomcat 9.0

There is currently minimal support for SQL SELECT with OFFSET and LIMIT for pagination, and almost no security - there is no string sanitization, and the API endpoint takes unencrypted username and password via HTTP BASIC authentication header.
