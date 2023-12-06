package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import edu.htw.skat.persistence.Card;
import edu.htw.skat.persistence.Game;
import edu.htw.skat.persistence.Hand;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.persistence.Game.State;
import edu.htw.skat.persistence.Person.Group;
import edu.htw.skat.util.RestJpaLifecycleProvider;

@Path("hands")
public class HandService {
	
	static private final Random RANDOMIZER = new SecureRandom();
	static private final String FIND_PASS_TYPE = "select t.identity from GameType as t where t.variety = edu.sb.skat.persistence.Variety.PASS";
	
	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public Hand getHand (
		@PathParam("id") @Positive final long handIdentity,
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		final Person player = hand.getPlayer();
		if (player == null && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (player != null && player.getIdentity() != requester.getIdentity() && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (game.getState() != State.DONE) throw new ClientErrorException(FORBIDDEN);
		
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
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		hand.setBid((short) bid);
		
		int foldedHands = 0;
		
		final List<Hand> hands = new ArrayList<>();
		hands.add(game.getForehand());
		hands.add(game.getMiddlehand());
		hands.add(game.getRearhand());
		
		for (Hand currentHand: hands) {
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
	@Produces(APPLICATION_JSON)
	public Card[] findCards (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		final List<Hand> hands = new ArrayList<>();
		hands.add(game.getForehand());
		hands.add(game.getMiddlehand());
		hands.add(game.getRearhand());
		
		final Person player = hand.getPlayer();
		//final Hand playerHand = hands.stream().filter(h -> h.isSolo()).findFirst().orElseThrow(() -> new ClientErrorException(FORBIDDEN));

		if (player == null && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (player != null && (player.getIdentity() != requester.getIdentity() && requester.getGroup() != Group.ADMIN)) throw new ClientErrorException(FORBIDDEN);
		
		return hand.getCards().stream().sorted().toArray(Card[]::new);
	}
	
	
	@PATCH
	@Path("{id}/cards")
	@Produces(APPLICATION_JSON)
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
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		if (game.getState() == State.ACTIVE) throw new ClientErrorException(CONFLICT);
		if (game.getType() == null) throw new ClientErrorException(CONFLICT);
		
		for ( Card c : cards) {
			hand.getCards().remove(c);
		}	

		List<Card> cardstoDraw = new ArrayList<>();
		cardstoDraw.addAll(game.getSkat().getCards());
				
		for (int loop = 0; loop < cards.size() ; ++loop) {
			final int cardIndex = RANDOMIZER.nextInt(cards.size());
			Card card = cardstoDraw.remove(cardIndex);
			hand.getCards().add(card);
		}
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		return hand.getCards();
	}
	
	
	@PATCH
	@Path("{id}/gameType")
	@Produces(TEXT_PLAIN)
	public long setGameType (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		final Game.Type gameType
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
						
		Set<Game>games = hand.getPlayer().getTable().getGames();
		for (Game g : games) {
			g.setType(gameType);
		}	
		return gameType.value();
		
	}
}

