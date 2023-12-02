module edu.htw.skat.server {
	requires transitive java.sql;
	requires transitive java.activation;
	requires transitive java.instrument;
	requires transitive javax.annotation.api;
	requires transitive jdk.httpserver;

	requires transitive eclipselink.minus.jpa;
	requires transitive jersey.server;
	requires transitive jersey.container.jdk.http;

	requires transitive edu.htw.skat.model;
	exports edu.htw.skat.server;
}