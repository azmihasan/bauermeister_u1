package edu.sb.skat.service;

import static edu.sb.skat.service.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
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
import edu.sb.skat.persistence.SkatTable;
import edu.sb.skat.persistence.Type;
import edu.sb.skat.util.RestJpaLifecycleProvider;

@Path("hands")
public class HandService {
	
	static private final Random RANDOMIZER = new SecureRandom();
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
	public void negotiate (
			@PathParam("id") @Positive final long handIdentity,
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			final long bid
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = hand.getGame();
		
		hand.setBid((short) bid);
		
		int foldedHands = 0;
		
		for (Hand currentHand: game.getHands()) {
			if (currentHand.getBid() < 0) {
				foldedHands++;
			}
		}
		if (foldedHands == 3) {
			game.setState(State.DONE);
		} else if (foldedHands == 2) {
			game.setState(State.ACTIVE);
		}
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
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
	public Set<Card> exchangeCard (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		final Set<Card> cards
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		if (hand.getGame().getState() == State.ACTIVE) throw new ClientErrorException(CONFLICT);
		if (hand.getGame().getType() == null) throw new ClientErrorException(CONFLICT);
		
		
		final Game game = hand.getGame();
		
		Set<Hand> hands = game.getHands();
		
		for ( Card c : cards) {
			hand.getCards().remove(c);
		}
		
		
		
		for (Hand h : hands) {
			
			if (h.getPlayer() == null) {
				List<Card> cardstoDraw = new ArrayList<>();
				cardstoDraw.addAll(h.getCards());
				
				for (int loop = 0; loop <cards.size() ; ++loop) {
					final int cardIndex = RANDOMIZER.nextInt(cards.size());
					Card card = cardstoDraw.remove(cardIndex);
					hand.getCards().add(card);
				}
				
			}
		}
		
		
		return hand.getCards();
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

