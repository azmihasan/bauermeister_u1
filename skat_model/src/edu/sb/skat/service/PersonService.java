package edu.sb.skat.service;

import static edu.sb.skat.service.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.util.Comparator;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import edu.sb.skat.persistence.Document;
import edu.sb.skat.persistence.Person;
import edu.sb.skat.persistence.Person.Group;
import edu.sb.skat.util.HashCodes;
import edu.sb.skat.util.RestJpaLifecycleProvider;

@Path("people")
public class PersonService {
	static private final String FILTER_PEOPLE_QUERY = "select p.identity from Person as p where "
		+ "(:email is null or p.email = :email) AND "
		+ "(:group is null or p.group = :group) AND "
		+ "(:firstname is null or p.name.given = :firstname) AND "
		+ "(:lastname is null or p.name.family = :lastname) AND "
		+ "(:title is null or p.name.title = :title) AND "
		+ "(:country is null or p.address.country = :country) AND "
		+ "(:postcode is null or p.address.postcode = :postcode) AND "
		+ "(:street is null or p.address.street = :street) AND "
		+ "(:city is null or p.address.city = :city)"
		+ "(:creationTimestamp is null or p.creationTimestamp = :creationTimestamp)"
		+ "(:modificationTimestamp is null or p.modificationTimestamp = :modificationTimestamp)";
	
	static private final Comparator<Person> PERSON_COMPARATOR = Comparator
		.comparing(Person::getName)
		.thenComparing(Person::getEmail);
	
	@GET   
	@Produces({APPLICATION_JSON, APPLICATION_XML})
    public Person[] queryPeople(
		@QueryParam("resultOffset") @PositiveOrZero Integer resultOffset,
		@QueryParam("resultLimit") @PositiveOrZero Integer resultLimit,
		@QueryParam("email") String email,
		@QueryParam("group") Person.Group group,
		@QueryParam("firstname") String firstname, 
		@QueryParam("lastname") String lastname,
		@QueryParam("title") String title, 
		@QueryParam("country") String country,
		@QueryParam("postcode") String postcode, 
		@QueryParam("street") String street, 
		@QueryParam("city") String city,
		@QueryParam("creationTimestamp") String creationTimestamp,
		@QueryParam("modificationTimestamp") String modificationTimestamp
    ) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final TypedQuery<Long> query = entityManager.createQuery(FILTER_PEOPLE_QUERY, Long.class);
		if(resultOffset != null) query.setFirstResult(resultOffset);
		if(resultLimit != null) query.setMaxResults(resultLimit);
		
		final Person[] people = query
			.setParameter("email", email)
			.setParameter("group", group)
			.setParameter("firstname", firstname)
			.setParameter("lastname", lastname)
			.setParameter("title", title)
			.setParameter("country", country)
			.setParameter("postcode", postcode)
			.setParameter("street", street)
			.setParameter("city", city)
			.setParameter("creationTimestamp", creationTimestamp)
			.setParameter("modificationTimestamp", modificationTimestamp)
	        .getResultList()
	        .stream()
	        .map(identity -> entityManager.find(Person.class, identity))
	        .filter(person -> person != null)
	        .sorted(PERSON_COMPARATOR)
	        .toArray(Person[]::new);
		
        return people;  
    }
	
	
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	@Produces(TEXT_PLAIN)
	public long changePerson(
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam("X-Set-Password") String password,
		@QueryParam("avatarReference") Long avatarReference,
		@NotNull @Valid Person personTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final boolean insertEnabled = personTemplate.getIdentity() == 0;
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN && requesterIdentity != personTemplate.getIdentity()) throw new ClientErrorException(FORBIDDEN);

		final Person person;
		if (insertEnabled) {
			if (avatarReference == null) avatarReference = 1L;
			person = new Person();
		} else {
			person = entityManager.find(Person.class, personTemplate.getIdentity());
			if (person == null) throw new ClientErrorException(NOT_FOUND);
		}
		
		person.setEmail(personTemplate.getEmail());
		
		person.getName().setTitle(personTemplate.getName().getTitle());
		person.getName().setFamily(personTemplate.getName().getFamily());
		person.getName().setGiven(personTemplate.getName().getGiven());

		person.getAddress().setCity(personTemplate.getAddress().getCity());
		person.getAddress().setCountry(personTemplate.getAddress().getCountry());
		person.getAddress().setPostcode(personTemplate.getAddress().getPostcode());
		person.getAddress().setStreet(personTemplate.getAddress().getStreet());
		
		person.getPhones().retainAll(personTemplate.getPhones());
		person.getPhones().addAll(personTemplate.getPhones());
		
		if (requester.getGroup() == Group.ADMIN) person.setGroup(personTemplate.getGroup());
		if (password != null) person.setPasswordHash(HashCodes.sha2HashText(256, password));
		
		if (avatarReference != null) {
			final Document avatar = entityManager.find(Document.class, avatarReference);
			if (avatar == null) throw new ClientErrorException(NOT_FOUND);
			person.setAvatar(avatar);			
		}
		
		if (insertEnabled)
			entityManager.persist(person);
		else
			entityManager.flush();

		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		return person.getIdentity();
	}
	
	
	@GET
	@Path("{id}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person findPerson (
		@PathParam("id") @PositiveOrZero final long personIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final long identity = personIdentity == 0 ? requesterIdentity : personIdentity;
		final Person person = entityManager.find(Person.class, identity);
		if (person == null) throw new ClientErrorException(NOT_FOUND);

		return person;	
	}
}