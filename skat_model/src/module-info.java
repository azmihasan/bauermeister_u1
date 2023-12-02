module edu.htw.skat.model {
	requires transitive java.logging;
	requires transitive java.validation;
	requires transitive javax.annotation.api;

	requires transitive java.json.bind;
	requires transitive java.ws.rs;
	requires transitive javax.persistence;
	requires transitive eclipselink.minus.jpa;

	// grants accessibility of the given packages to dependent projects
	exports edu.htw.skat.persistence;
	exports edu.htw.skat.service;
	exports edu.htw.skat.util;

	// grants reflective access to the given packages for JPA & JSON-B
	opens edu.htw.skat.persistence;
}