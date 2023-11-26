package edu.sb.skat.service;

import static edu.sb.skat.service.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.sb.skat.persistence.Document;
import edu.sb.skat.persistence.Person;
import edu.sb.skat.util.RestJpaLifecycleProvider;
import edu.sb.skat.util.HashCodes;

@Path("documents")
public class DocumentService {
	private static final String QUERY_DOCUMENT = "select d from Document as d where d.hash = :hash";
	
	@GET
	@Path("{id}")
	@Produces(MediaType.WILDCARD)
	public Response findDocument (
		@PathParam("id") @Positive final long documentIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);

		final Document document = entityManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(NOT_FOUND);

		return Response.ok(document.getContent(), document.getType()).build();
	}
	
	
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(TEXT_PLAIN)
	public long createOrUpdateDocument(
		@NotEmpty byte[] content,
		@HeaderParam("Content-Type") @NotEmpty String contentType,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final String contentHash = HashCodes.sha2HashText(256, content);
		
		final TypedQuery<Document> query = entityManager.createQuery(QUERY_DOCUMENT, Document.class);
		final Document document = query
				.setParameter("hash", contentHash)
				.getResultList()
				.stream()
				.findAny()
				.orElseGet(() -> new Document(content));
		
		document.setType(contentType);
		
		if(document.getIdentity() == 0)
			entityManager.persist(document);
		else
			entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException e) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		return document.getIdentity();
	}
}