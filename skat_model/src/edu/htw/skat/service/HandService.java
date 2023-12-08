package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
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
		
		if (requester.getGroup() != Group.ADMIN) {
			final Game game = (hand.getPlayer() == null ? requester : hand.getPlayer()).getTable().getGames().stream().max(Comparator.comparing(Game::getIdentity)).get();
			final Hand playerHand = hand.getPlayer() == null
					? (game.getForehand().getPlayer() == requester ? game.getForehand() : (game.getMiddlehand().getPlayer() == requester ? game.getMiddlehand() : game.getRearhand()))
					: hand;
			
			final Person player = hand.getPlayer();
			if (player == null && game.getState() != State.DONE && (game.getState() != State.EXCHANGE || !playerHand.isSolo())) throw new ClientErrorException(FORBIDDEN);
			if (player != null && game.getState() != State.DONE && requester != player) throw new ClientErrorException(FORBIDDEN);
		}
		
		return hand;
	}
	
	
	@PATCH
	@Path("{id}/bid")
	@Consumes(TEXT_PLAIN)
	public void bid (
			@PathParam("id") @Positive final long handIdentity,
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@Positive final Short bid
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		if (hand.getPlayer() != requester) throw new ClientErrorException(FORBIDDEN);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.get();
		
		if (bid != null && bid < Arrays.stream(game.getHands()).filter(h -> h.getBid() != null).mapToInt(Hand::getBid).max().getAsInt()) throw new ClientErrorException(CONFLICT);
		hand.setBid(bid);
		
		if (bid == null && Arrays.stream(game.getHands()).filter(h -> h.getPlayer() != null && h.getBid() != null).count() == 1) {
			final Hand soloHand = Arrays.stream(game.getHands()).filter(h -> h.getPlayer() != null && h.getBid() != null).findAny().get();
			soloHand.setSolo(true);
		}
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
	}
	
	// TODO implement new requirements
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

		if (player == null && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (player != null && (player.getIdentity() != requester.getIdentity() && requester.getGroup() != Group.ADMIN)) throw new ClientErrorException(FORBIDDEN);
		
		return hand.getCards().stream().sorted().toArray(Card[]::new);
	}
	
	
	
	//  TODO implement new method REMOVE hands/{id}/cards/{cid}
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
	
	// TODO implement new requirements	
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

