package edu.sb.skat.service;

import static edu.sb.skat.service.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response.Status;

import edu.sb.skat.persistence.Card;
import edu.sb.skat.persistence.Game;
import edu.sb.skat.persistence.Game.Modifier;
import edu.sb.skat.persistence.Game.State;
import edu.sb.skat.persistence.Hand;
import edu.sb.skat.persistence.Person;
import edu.sb.skat.persistence.Person.Group;
import edu.sb.skat.persistence.Type;
import edu.sb.skat.util.RestJpaLifecycleProvider;

@Path("hands")
public class HandService {
	
	static private final String FIND_PASS_TYPE = "select t.identity from GameType as t where t.variety = edu.sb.skat.persistence.Variety.PASS";
	
	@GET
	@Path("{id}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Hand getHand (
		@PathParam("id") @Positive final long handIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = hand.getGame();

		final Person player = hand.getPlayer();
		if (player == null && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (player != null && player.getIdentity() != requester.getIdentity() && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		
		return hand;
	}
	
	@PATCH
	@Path("{id}/negotiate")
	@Produces(TEXT_PLAIN)
	public long negotiate (
			@PathParam("id") @Positive final long handIdentity,
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			final long bid
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		//TODO implement method
		
		
		return 0;
	}
	
	
	@GET
	@Path("{id}/cards")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Card[] findCards (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Person player = hand.getPlayer();
		final Hand playerHand = hand.getGame().getHands().stream().filter(h -> h.getSolo()).findFirst().orElseThrow(() -> new ClientErrorException(FORBIDDEN));

		if (player == null && requester.getGroup() != Group.ADMIN && playerHand.getPlayer().getIdentity() != requester.getIdentity()) throw new ClientErrorException(FORBIDDEN);
		if (player != null && (player.getIdentity() != requester.getIdentity() && requester.getGroup() != Group.ADMIN)) throw new ClientErrorException(FORBIDDEN);
		
		return hand.getCards().stream().sorted().toArray(Card[]::new);
	}
	
	
	@PATCH
	@Path("{id}/cards")
	@Produces({APPLICATION_JSON, APPLICATION_XML})
	public Card playCard (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		@QueryParam("cardReference") @Positive long cardReference
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		if (hand.getGame().getState() != State.ACTIVE) throw new ClientErrorException(CONFLICT);
		if (hand.getGame().getType() == null) throw new ClientErrorException(CONFLICT);
		
		
		//TODO implement method
		 
		return card;
	}
	
	
	@PATCH
	@Path("{id}/gameType")
	@Produces(TEXT_PLAIN)
	public long setGameType (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity
		// Spielart als Parameter?
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
						
		//TODO Implement method
		
		return 0;
		
	}
	
	
	
}

