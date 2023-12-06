package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.htw.skat.persistence.Document;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.util.HashCodes;
import edu.htw.skat.util.RestJpaLifecycleProvider;

@Path("documents")
public class DocumentService {
	private static final String QUERY_DOCUMENT = "select d from Document as d where d.hash = :hash";
	
	@GET
	@Path("{id}")
	@Produces(MediaType.WILDCARD)
	public Response findDocument (
		@PathParam("id") @Positive final long documentIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");

		final Document document = entityManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(NOT_FOUND);

		return Response.ok(document.getContent(), document.getType()).build();
	}
	
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public long createOrUpdateDocument(
		@NotNull final byte[] content,
		@HeaderParam(HttpHeaders.CONTENT_TYPE) @NotNull final String contentType,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final TypedQuery<Document> query = entityManager.createQuery(QUERY_DOCUMENT, Document.class);
		final Document document = query
				.setParameter("hash", HashCodes.sha2HashText(256, content))
				.getResultList()
				.stream()
				.findAny()
				.orElseGet(() -> new Document(content));
		
		document.setType(contentType);
		
		try {
			if (document.getIdentity() == 0)
				entityManager.persist(document);
			else
				entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RuntimeException e) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}

		return document.getIdentity();
	}
}