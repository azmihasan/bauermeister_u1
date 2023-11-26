package edu.sb.skat.service;

import static edu.sb.skat.service.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import edu.sb.skat.persistence.Card;
import edu.sb.skat.persistence.Document;
import edu.sb.skat.persistence.Game;
import edu.sb.skat.persistence.Game.State;
import edu.sb.skat.persistence.Hand;
import edu.sb.skat.persistence.Person;
import edu.sb.skat.persistence.Person.Group;
import edu.sb.skat.persistence.SkatTable;
import edu.sb.skat.util.RestJpaLifecycleProvider;

@Path("tables")
public class TableService {
	static private final Random RANDOMIZER = new SecureRandom();
	
	static private final String QUERY_SKAT_TABLE = "select t.identity from SkatTable as t where "
			+ "(:alias is null or t.alias = :alias) and "
			+ "(:valuation is null or t.valuation = :valuation) and"
			+ "(:upperCreationTimestamp is null or t.creationTimestamp <= :upperCreationTimestamp) and "
			+ "(:lowerCreationTimestamp is null or t.creationTimestamp >= :lowerCreationTimestamp) and "
			+ "(:upperModificationTimestamp is null or t.modificationTimestamp <= :upperModificationTimestamp) and "
			+ "(:lowerModificationTimestamp is null or t.modificationTimestamp >= :lowerModificationTimestamp)"; 
	
	
	static private final String QUERY_CARDS = "select c.identity from Card as c";
	
	static private final Comparator<SkatTable> SKAT_TABLE_COMPARATOR = Comparator
			.comparing(SkatTable::getAlias);
	
	@GET   
	@Produces({APPLICATION_JSON, APPLICATION_XML})
    public SkatTable[] queryTables(
		@QueryParam("resultOffset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("resultLimit") @PositiveOrZero final Integer resultLimit,
		@QueryParam("alias") final String alias,
		@QueryParam("valuation") @Positive final Integer valuation,
		@QueryParam("upperCreationTimestamp") final Long upperCreationTimestamp,	
    	@QueryParam("lowerCreationTimestamp") final Long lowerCreationTimestamp,	
    	@QueryParam("upperModificationTimestamp") final Long upperModificationTimestamp,	
    	@QueryParam("lowerModificationTimestamp") final Long lowerModificationTimestamp 
    ) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		
		final TypedQuery<Long> query = entityManager.createQuery(QUERY_SKAT_TABLE, Long.class);
		if(resultOffset != null) query.setFirstResult(resultOffset);
		if(resultLimit != null) query.setMaxResults(resultLimit);
		
		final SkatTable[] skatTables = query
				.setParameter("alias", alias)
				.setParameter("valuation", valuation)
				.setParameter("upperCreationTimestamp", upperCreationTimestamp)
	        	.setParameter("lowerCreationTimestamp", lowerCreationTimestamp)
	        	.setParameter("upperModificationTimestamp", upperModificationTimestamp)
	        	.setParameter("lowerModificationTimestamp", lowerModificationTimestamp)
		        .getResultList()
		        .stream()
		        .map(identity -> entityManager.find(SkatTable.class, identity))
		        .filter(skatTable -> skatTable != null)
		        .sorted(SKAT_TABLE_COMPARATOR)
		        .toArray(SkatTable[]::new);
		
        return skatTables;
    }
	
	
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	@Produces(TEXT_PLAIN)
	public long changeSkatTable (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@QueryParam("avatarReference") Long avatarReference,
			@NotNull @Valid SkatTable skatTableTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		final boolean insertEnabled = skatTableTemplate.getIdentity() == 0;

		final SkatTable skatTable;
		if (insertEnabled) {
			if (avatarReference == null) avatarReference = 1L;
			skatTable = new SkatTable();
		} else {
			skatTable = entityManager.find(SkatTable.class, skatTableTemplate.getIdentity());
			if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
		}
		
		skatTable.setAlias(skatTableTemplate.getAlias());
		skatTable.setBaseValuation(skatTableTemplate.getBaseValuation());
		
		if (avatarReference != null) {
			final Document avatar = entityManager.find(Document.class, avatarReference);
			if (avatar == null) throw new ClientErrorException(NOT_FOUND);
			skatTable.setAvatar(avatar);
		}
		
		if (insertEnabled)
			entityManager.persist(skatTable);
		else
			entityManager.flush();

		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		return skatTable.getIdentity();
	}
	
	
	@GET
	@Path("{id}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public SkatTable findSkatTable (
		@PathParam("id") @Positive final long skatTableIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final SkatTable skatTable = entityManager.find(SkatTable.class, skatTableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);

		return skatTable;
	}
	
	
	@PUT
	@Path("{id}/players/{pos}")
	@Produces({ TEXT_PLAIN })
	public byte addPlayerToTable(
		@PathParam("id") @Positive final long skatTableIdentity,
		@PathParam("pos") @Min(0) @Max(2) final byte position,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final SkatTable skatTable = entityManager.find(SkatTable.class, skatTableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
		
		if (skatTable.getPlayers().contains(requester)) throw new ClientErrorException(BAD_REQUEST);
		for (final Person player : skatTable.getPlayers()) 
			if (player.getPosition() == position) throw new ClientErrorException(BAD_REQUEST);
		
		requester.setPosition(position);
		requester.setTable(skatTable);
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(SkatTable.class, skatTable.getIdentity());
		
		return position;
	}
	

	@DELETE
	@Path("{id}/players/{pos}")
	@Produces(TEXT_PLAIN)
	public byte deletePlayer (
		@PathParam("id") @Positive final long skatTableIdentity,
		@PathParam("position") @Min(0) @Max(2) final byte position,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);

		final SkatTable skatTable = entityManager.find(SkatTable.class, skatTableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
			
		final Person player = skatTable.getPlayers().stream().filter(p -> Objects.equals(p.getPosition(), position)).findFirst().orElse(null);
		if (player == null || (player != requester && requester.getGroup() != Group.ADMIN)) throw new ClientErrorException(FORBIDDEN);
		
		player.setPosition(null);
		player.setTable(null);
		
		entityManager.flush();
	
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		 final Cache cache = entityManager.getEntityManagerFactory().getCache();
		 cache.evict(SkatTable.class, skatTable.getIdentity());
		 
		 return position;
	}
	
	
	@POST
	@Path("{id}/games")
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	@Produces(TEXT_PLAIN)
	public long addGameToTable (
		@PathParam("id") @Positive final long tableIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final SkatTable skatTable = entityManager.find(SkatTable.class, tableIdentity);
		System.out.println("TableService creates game" + skatTable);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
		if (!skatTable.getPlayers().contains(requester) || skatTable.getPlayers().size() < 3) throw new ClientErrorException(FORBIDDEN);
		if (skatTable.getGames().stream().anyMatch(game -> game.getState() != State.DONE)) throw new ClientErrorException(CONFLICT);
				
		final Game game = new Game(skatTable);
		entityManager.persist(game);
		//Done Karten an die H�nde verteilen -> 4 H�nde erzeugen und Karten an sie verteilen (zuf�llig) | 3 Spieler + 1 Skat Hand -> 3*10(spieler) + 2(skat)
		
		final List<Hand> hands = new ArrayList<>();
		for (final Person player: skatTable.getPlayers()) {
			final Hand hand = new Hand(game, player);
			entityManager.persist(hand);
			hands.add(hand);
		}
		
		final Hand skat = new Hand(game, null);
		entityManager.persist(skat);
		hands.add(skat);
		
		final List<Card> cards = entityManager
			.createQuery(QUERY_CARDS, Long.class)
	        .getResultList()
	        .stream()
	        .map(identity -> entityManager.find(Card.class, identity))
	        .filter(card -> card != null)
	        .collect(Collectors.toList());
		
		for (int playerIndex = 0; playerIndex < 3; ++playerIndex) {
			final Hand hand = hands.get(playerIndex);
			
			for (int loop = 0; loop < 10; ++loop) {
				final int cardIndex = RANDOMIZER.nextInt(cards.size());
				final Card card = cards.remove(cardIndex);
				hand.getCards().add(card);
			}
		}
		
		hands.get(hands.size() - 1).getCards().addAll(cards);
		game.setState(State.NEGOTIATE);
		
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(SkatTable.class, skatTable.getIdentity());
		cache.evict(Game.class, game.getIdentity());
		for (Person player : skatTable.getPlayers()) cache.evict(Person.class, player.getIdentity());
		for (Hand hand : hands) cache.evict(Hand.class, hand.getIdentity());
	
		return game.getIdentity();
	}
}
