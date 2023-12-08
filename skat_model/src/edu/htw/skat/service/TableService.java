package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

import edu.htw.skat.persistence.Card;
import edu.htw.skat.persistence.Document;
import edu.htw.skat.persistence.Game;
import edu.htw.skat.persistence.Hand;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.persistence.SkatTable;
import edu.htw.skat.persistence.Game.State;
import edu.htw.skat.persistence.Person.Group;
import edu.htw.skat.util.RestJpaLifecycleProvider;

@Path("tables")
public class TableService {
	static private final Random RANDOMIZER = new SecureRandom();
	
	static private final String QUERY_SKAT_TABLES = "select t.identity from SkatTable as t where "
			+ "(:lowerModified is null or t.modified >= :lowerModified) and "
			+ "(:upperModified is null or t.modified <= :upperModified) and "
			+ "(:alias is null or t.alias = :alias)"; 
	
	
	static private final String QUERY_CARDS = "select c.identity from Card as c";
	
	static private final Comparator<SkatTable> SKAT_TABLE_COMPARATOR = Comparator.comparing(SkatTable::getAlias);
	
	@GET   
	@Produces(APPLICATION_JSON)
    public SkatTable[] queryTables(
		@QueryParam("resultOffset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("resultLimit") @PositiveOrZero final Integer resultLimit,
		@QueryParam("lower-modified") Long lowerModified,
		@QueryParam("upper-modified") Long upperModified,
		@QueryParam("alias") final String alias
    ) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		
		final TypedQuery<Long> query = entityManager.createQuery(QUERY_SKAT_TABLES, Long.class);
		if(resultOffset != null) query.setFirstResult(resultOffset);
		if(resultLimit != null) query.setMaxResults(resultLimit);
		final SkatTable[] skatTables = query
			.setParameter("lowerModified", lowerModified)
			.setParameter("upperModified", upperModified)
			.setParameter("alias", alias)
		    .getResultList()
		    .stream()
		    .map(identity -> entityManager.find(SkatTable.class, identity))
		    .filter(skatTable -> skatTable != null)
		    .sorted(SKAT_TABLE_COMPARATOR)
		    .toArray(SkatTable[]::new);
		
        return skatTables;
    }
	
	
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces(TEXT_PLAIN)
	public long changeSkatTable (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@NotNull @Valid SkatTable skatTableTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		final boolean insertEnabled = skatTableTemplate.getIdentity() == 0;

		final SkatTable skatTable;
		final Document avatar;
		if (insertEnabled) {
			skatTable = new SkatTable();
			avatar = entityManager.find(Document.class, skatTableTemplate.getAvatar() == null ? 1L : skatTableTemplate.getAvatar().getIdentity());
			if (avatar == null) throw new ClientErrorException(NOT_FOUND);
		} else {
			skatTable = entityManager.find(SkatTable.class, skatTableTemplate.getIdentity());
			if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
			avatar = skatTableTemplate.getAvatar() == null ? null : entityManager.find(Document.class, skatTableTemplate.getAvatar().getIdentity());
		}
		
		skatTable.setModified(System.currentTimeMillis());
		skatTable.setVersion(skatTableTemplate.getVersion());
		skatTable.setAlias(skatTableTemplate.getAlias());
		skatTable.setBaseValuation(skatTableTemplate.getBaseValuation());
		if (avatar != null) skatTable.setAvatar(avatar);
		
		try {
			if (insertEnabled)
				entityManager.persist(skatTable);
			else
				entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();
		}

		return skatTable.getIdentity();
	}
	
	
	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
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
	@Produces(TEXT_PLAIN)
	public byte addPlayerToTable(
		@PathParam("id") @Positive final long skatTableIdentity,
		@PathParam("tablePosition") @Min(0) @Max(2) final byte position,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() == Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		final SkatTable skatTable = entityManager.find(SkatTable.class, skatTableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
		
		if (skatTable.getPlayers().contains(requester)) throw new ClientErrorException(CONFLICT);
		for (final Person player : skatTable.getPlayers()) 
			if (player.getTablePosition() == position) throw new ClientErrorException(CONFLICT);
		
		requester.setTablePosition(position);
		requester.setTable(skatTable);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();
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
		@PathParam("tablePosition") @Min(0) @Max(2) final byte position,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);

		final SkatTable skatTable = entityManager.find(SkatTable.class, skatTableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);
			
		final Person player = skatTable.getPlayers().stream().filter(p -> p.getTablePosition() == position).findFirst().orElse(null);
		if (player == null) throw new ClientErrorException(CONFLICT);
		if (player != requester && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		player.setTablePosition(null);
		player.setTable(null);
	
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();
		}
		
		 final Cache cache = entityManager.getEntityManagerFactory().getCache();
		 cache.evict(SkatTable.class, skatTable.getIdentity());
		 
		 return position;
	}
	
	
	@POST
	@Path("{id}/games")
	@Consumes(APPLICATION_JSON)
	@Produces(TEXT_PLAIN)
	public long addGameToTable (
		@PathParam("id") @Positive final long tableIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final SkatTable skatTable = entityManager.find(SkatTable.class, tableIdentity);
		if (skatTable == null) throw new ClientErrorException(NOT_FOUND);

		if (!skatTable.getPlayers().contains(requester)) throw new ClientErrorException(FORBIDDEN);
		if (skatTable.getPlayers().size() <= 2) throw new ClientErrorException(CONFLICT);
		if (skatTable.getGames().stream().anyMatch(game -> game.getState() != State.DONE)) throw new ClientErrorException(CONFLICT);
		
		final List<Hand> hands = new ArrayList<>();
		for (final Person player: skatTable.getPlayers()) {
			final Hand hand = new Hand(player);
			entityManager.persist(hand);
			hands.add(hand);
		}
		
		final Hand skat = new Hand(null);
		entityManager.persist(skat);
		hands.add(skat);
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
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
				final Card card = cards.remove(RANDOMIZER.nextInt(cards.size()));
				hand.getCards().add(card);
			}
		}
		skat.getCards().addAll(cards);
		
		final Game game = new Game(skatTable, hands.get(0), hands.get(1), hands.get(2), skat);
		game.setState(State.BET);

		try {
			entityManager.persist(game);
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();
		}

		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(SkatTable.class, skatTable.getIdentity());
	
		return game.getIdentity();
	}
}
