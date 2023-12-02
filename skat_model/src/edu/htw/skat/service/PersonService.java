package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

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

import edu.htw.skat.persistence.Document;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.persistence.Person.Group;
import edu.htw.skat.util.HashCodes;
import edu.htw.skat.util.RestJpaLifecycleProvider;

@Path("people")
public class PersonService {
	static private final String FILTER_PEOPLE_QUERY = "select p.identity from Person as p where "
		+ "(:email is null or p.email = :email) and "
		+ "(:group is null or p.group = :group) and "
		+ "(:title is null or p.name.title = :title) and "
		+ "(:surname is null or p.name.family = :surname) and "
		+ "(:forename is null or p.name.given = :forename) and "
		+ "(:postcode is null or p.address.postcode = :postcode) and "
		+ "(:street is null or p.address.street = :street) and "
		+ "(:city is null or p.address.city = :city) and "
		+ "(:country is null or p.address.country = :country)";
	
	static private final Comparator<Person> PERSON_COMPARATOR = Comparator
		.comparing(Person::getName)
		.thenComparing(Person::getEmail);
	
	@GET   
	@Produces(APPLICATION_JSON)
    public Person[] queryPeople(
		@QueryParam("result-offset") @PositiveOrZero Integer resultOffset,
		@QueryParam("result-limit") @PositiveOrZero Integer resultLimit,
		@QueryParam("email") String email,
		@QueryParam("group") Person.Group group,
		@QueryParam("forename") String forename, 
		@QueryParam("surname") String surname,
		@QueryParam("title") String title, 
		@QueryParam("country") String country,
		@QueryParam("postcode") String postcode, 
		@QueryParam("street") String street, 
		@QueryParam("city") String city
    ) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final TypedQuery<Long> query = entityManager.createQuery(FILTER_PEOPLE_QUERY, Long.class);
		if(resultOffset != null) query.setFirstResult(resultOffset);
		if(resultLimit != null) query.setMaxResults(resultLimit);
		
		final Person[] people = query
			.setParameter("email", email)
			.setParameter("group", group)
			.setParameter("surname", surname)
			.setParameter("forename", forename)
			.setParameter("title", title)
			.setParameter("postcode", postcode)
			.setParameter("street", street)
			.setParameter("city", city)
			.setParameter("country", country)
	        .getResultList()
	        .stream()
	        .map(identity -> entityManager.find(Person.class, identity))
	        .filter(person -> person != null)
	        .sorted(PERSON_COMPARATOR)
	        .toArray(Person[]::new);
		
        return people;  
    }
	
	
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces(TEXT_PLAIN)
	public long changePerson(
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam("X-Set-Password") final String password,
		@NotNull @Valid final Person personTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final boolean insertEnabled = personTemplate.getIdentity() == 0;
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN && requesterIdentity != personTemplate.getIdentity()) throw new ClientErrorException(FORBIDDEN);

		final Document avatar;
		final Person person;
		if (insertEnabled) {
			person = new Person();
			avatar = entityManager.find(Document.class, personTemplate.getAvatar() == null ? 1L : personTemplate.getAvatar().getIdentity());
		} else {
			person = entityManager.find(Person.class, personTemplate.getIdentity());
			if (person == null) throw new ClientErrorException(NOT_FOUND);
			avatar = personTemplate.getAvatar() == null ? null : entityManager.find(Document.class, personTemplate.getAvatar().getIdentity());
		}
		
		person.setModified(System.currentTimeMillis());
		person.setVersion(personTemplate.getVersion());
		
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
		if (avatar != null) person.setAvatar(avatar);			

		try {
			if (insertEnabled)
				entityManager.persist(person);
			else
				entityManager.flush();
		
			entityManager.getTransaction().commit();
		} catch (final RuntimeException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}

		return person.getIdentity();
	}
	
	
	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
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