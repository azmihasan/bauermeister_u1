package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import edu.htw.skat.persistence.Card;
import edu.htw.skat.persistence.Game;
import edu.htw.skat.persistence.Hand;
import edu.htw.skat.persistence.Hand.Position;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.persistence.Game.State;
import edu.htw.skat.persistence.Person.Group;
import edu.htw.skat.persistence.Trick;
import edu.htw.skat.util.RestJpaLifecycleProvider;

@Path("hands")
public class HandService {
	
	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public Hand getHand (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		if (requester.getGroup() != Group.ADMIN) {
			final Person player = hand.getPlayer();
			final Game game = (player == null ? requester : player).getTable().getGames().stream().max(Comparator.comparing(Game::getIdentity)).get();
			final Hand playerHand = player == null
					? (game.getForehand().getPlayer() == requester ? game.getForehand() : (game.getMiddlehand().getPlayer() == requester ? game.getMiddlehand() : game.getRearhand()))
					: hand;
			
			if (player == null && game.getState() != State.DONE || (game.getState() != State.EXCHANGE && !playerHand.isSolo())) throw new ClientErrorException(FORBIDDEN);
			if (player != null && game.getState() != State.DONE && requester != player) throw new ClientErrorException(FORBIDDEN);
		}
		
		return hand;
	}
	
	@PATCH
	@Path("{id}/bid")
	@Consumes(TEXT_PLAIN)
	public void bid (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
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
		
		final Set<Hand> otherHands = new HashSet<>();
		if (game.getForehand() != hand) otherHands.add(game.getForehand());
		if (game.getMiddlehand() != hand) otherHands.add(game.getMiddlehand());
		if (game.getRearhand() != hand) otherHands.add(game.getRearhand());
		
		if (hand.getBid() != 0 && otherHands.stream().allMatch(h -> h.getBid() == null)) throw new ClientErrorException(CONFLICT);

		if (bid != null && bid < otherHands.stream().mapToInt(Hand::getBid).max().getAsInt()) throw new ClientErrorException(CONFLICT);
		hand.setBid(bid);
		
		if (bid == null && otherHands.stream().filter(h -> h.getBid() != null).count() == 1) {
			final Hand soloHand = otherHands.stream().filter(h -> h.getBid() != null).findAny().get();
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
	
	@GET
	@Path("{id}/cards")
	@Produces(APPLICATION_JSON)
	public List<Card> getCardsById (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand || g.getSkat() == hand)
				.findFirst()
				.orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		
		final Person player = hand.getPlayer();
		
		if (requester.getGroup() != Group.ADMIN) {
			if (player != null && player != requester && game.getState() != State.DONE) throw new ClientErrorException(FORBIDDEN);
			if (player == null && game.getState() != State.DONE && (!hand.isSolo() || game.getState() != State.EXCHANGE)) throw new ClientErrorException(FORBIDDEN);
		}
		
		final List<Card> returnCards = new ArrayList<Card>(hand.getCards());
		if (requester != null && hand.isSolo() && game.getState() == State.EXCHANGE && game.getModifier() != null)
			returnCards.addAll(game.getSkat().getCards());
		
		return returnCards;
	}
	
	@PATCH
	@Path("/{id}/game/modifier")
	@Consumes(APPLICATION_JSON)
	public void setModifier (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
			final Game.Modifier modifierReference
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		
		if (!hand.isSolo() || requester != hand.getPlayer() || game.getState() != State.EXCHANGE) throw new ClientErrorException(FORBIDDEN);
		
		game.setModifier(modifierReference);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}
	}
	
	@PATCH
	@Path("{id}/cards/{cid}")
	@Consumes(TEXT_PLAIN)
	public void exchangeCard (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
			@PathParam("cid") @Positive final long cardIdentity,
			@NotNull @Valid final long skatCardIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		final Card card = entityManager.find(Card.class, cardIdentity);
		if (card == null) throw new ClientErrorException(NOT_FOUND);
		final Card exchangeCard = entityManager.find(Card.class, skatCardIdentity);
		if (exchangeCard == null) throw new ClientErrorException(NOT_FOUND);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		
		final Person player = hand.getPlayer();
		final Hand skat = game.getSkat();
		
		if (requester != player) throw new ClientErrorException(FORBIDDEN);
		if (!hand.isSolo() || game.getModifier() != null || game.getState() != State.EXCHANGE || !hand.getCards().contains(exchangeCard)) throw new ClientErrorException(FORBIDDEN);
		if (!hand.getCards().contains(card) || skat.getCards().contains(exchangeCard)) throw new ClientErrorException(FORBIDDEN);
		
		hand.getCards().remove(card);
		skat.getCards().remove(exchangeCard);
		
		hand.getCards().add(exchangeCard);
		skat.getCards().add(card);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}
	}
	
	@PATCH
	@Path("{id}/game/type")
	@Consumes(APPLICATION_JSON)
	public void setGameType (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
			@NotNull @Valid final Game.Type gameType
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		if (requester != hand.getPlayer()) throw new ClientErrorException(FORBIDDEN);
		if (!hand.isSolo()) throw new ClientErrorException(FORBIDDEN);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		
		if (game.getState() != State.EXCHANGE) throw new ClientErrorException(FORBIDDEN);
		
		game.setType(gameType);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}
	}
	
	@DELETE
	@Path("{id}/cards/{cid}")
	@Consumes(APPLICATION_JSON)
	@Produces(TEXT_PLAIN)
	public long playCard(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
			@PathParam("cid") @Positive final long cardIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		final Card card = entityManager.find(Card.class, cardIdentity);
		if (card == null) throw new ClientErrorException(NOT_FOUND);
		
		if (requester != hand.getPlayer()) throw new ClientErrorException(FORBIDDEN);

		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		if (game.getState() != State.ACTIVE) throw new ClientErrorException(FORBIDDEN);
		
		Trick gameTrick = game.getTricks().stream().max(Comparator.comparing(Trick::getIdentity)).orElse(null);
		if (gameTrick.getFirstCard() == null) {
			//TODO: check if it is the players turn to play the first card
			gameTrick.setFirstCard(card);
		} else if (gameTrick.getSecondCard() == null) {
			//TODO: check if it is the players turn to play the second card
			gameTrick.setSecondCard(card);
		} else if (gameTrick.getThirdCard() == null) {
			//TODO: check if it is the players turn to play the third card
			// check which player win the trick?
			// if the 10th trick of this game, then the game is over. The winner must be determined by amount of player point. The game state should be "DONE".
			gameTrick.setThirdCard(card);
			//gameTrick.setWinner();
			
		} else {
			//TODO: check if it is the players turn to play the first card
			final Position position = game.getForehand() == hand
					? Position.FOREHAND
					: (game.getMiddlehand() == hand ? Position.MIDDLEHAND : Position.REARHAND);
			gameTrick = new Trick(game, position);
			
			try {
				entityManager.persist(gameTrick);
				
				entityManager.getTransaction().commit();
			} finally {
				if (!entityManager.getTransaction().isActive())
					entityManager.getTransaction().begin();
			}
			
			final Cache cache = entityManager.getEntityManagerFactory().getCache();
			cache.evict(Game.class, game.getIdentity());
			
			gameTrick.setFirstCard(card);
		}
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
		}
		
		return card.getIdentity();
	}
}