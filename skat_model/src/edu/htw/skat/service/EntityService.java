package edu.htw.skat.service;

import static edu.htw.skat.persistence.Person.Group.ADMIN;
import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import edu.htw.skat.persistence.BaseEntity;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.util.Copyright;
import edu.htw.skat.util.RestJpaLifecycleProvider;


/**
 * JAX-RS based REST service implementation for polymorphic entity resources, defining the following
 * path and method combinations, for use by administrators only:
 * <ul>
 * <li>GET entities/{id}: Returns the entity matching the given identity.</li>
 * <li>DELETE entities/{id}: Deletes the entity matching the given identity.</li>
 * </ul>
 */
@Path("entities")
@Copyright(year = 2013, holders = "Sascha Baumeister")
public class EntityService {

	/**
	 * Returns the entity with the given identity.
	 * @param entityIdentity the entity identity
	 * @param requesterIdentity the authenticated requester identity
	 * @return the matching entity (HTTP 200)
	 * @throws ClientErrorException (HTTP 403) if the given requester does not exist
	 * @throws ClientErrorException (HTTP 404) if the given entity cannot be found
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current thread is not open
	 */
	@GET
	@Path("{id}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public BaseEntity findEntity (
		@PathParam("id") @Positive final long entityIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);

		final BaseEntity entity = entityManager.find(BaseEntity.class, entityIdentity);
		if (entity == null) throw new ClientErrorException(NOT_FOUND);

		return entity;
	}


	/**
	 * Deletes the entity matching the given identity, or does nothing if no such entity exists.
	 * @param entityIdentity the entity identity
	 * @param requesterIdentity the authenticated requester identity
	 * @return void (HTTP 204)
	 * @throws ClientErrorException (HTTP 403) if the given requester is not an administrator
	 * @throws ClientErrorException (HTTP 404) if the given entity cannot be found
	 * @throws ClientErrorException (HTTP 409) if there is a database constraint violation (like conflicting locks)
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current thread is not open
	 */
	@DELETE
	@Path("{id}")
	public void deleteEntity (
		@PathParam("id") @Positive final long entityIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != ADMIN) throw new ClientErrorException(FORBIDDEN);

		final BaseEntity entity = entityManager.find(BaseEntity.class, entityIdentity);
		if (entity == null) throw new ClientErrorException(NOT_FOUND);
		entityManager.remove(entity);

		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
	}
}