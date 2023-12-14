package edu.htw.skat.service;

import static edu.htw.skat.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
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
import javax.ws.rs.QueryParam;

import edu.htw.skat.persistence.Card;
import edu.htw.skat.persistence.Game;
import edu.htw.skat.persistence.Hand;
import edu.htw.skat.persistence.Hand.Position;
import edu.htw.skat.persistence.Person;
import edu.htw.skat.persistence.Game.Modifier;
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
		
		final Person player = hand.getPlayer();

		if (player != requester && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);
		if (game.getState() == State.EXCHANGE && game.getModifier() == null) {
			List<Card> returnCards = new ArrayList<Card>();
			returnCards.addAll(hand.getCards());
			returnCards.addAll(game.getSkat().getCards());
			return (Card[]) returnCards.toArray();
			
		}
		
		return hand.getCards().stream().sorted().toArray(Card[]::new);
	}
	
	@PATCH
	@Path("/{id}/game/modifier")
	public void setModifier (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		@QueryParam("modifierReference") Modifier modifierReference
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
		
		if (requester != hand.getPlayer()) throw new ClientErrorException(FORBIDDEN);
		if (hand.isSolo() || game.getState() != State.EXCHANGE) throw new ClientErrorException(CONFLICT);
		
		game.setModifier(modifierReference);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
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
		final Card exchangeCard
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		final Card card = entityManager.find(Card.class, cardIdentity);
		if (card == null) throw new ClientErrorException(NOT_FOUND);
		
		Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		Hand skat = game.getSkat();
		
		if (requester != hand.getPlayer()) throw new ClientErrorException(FORBIDDEN);
		if (hand.isSolo() || game.getModifier() != null) throw new ClientErrorException(FORBIDDEN);
		if (hand.getCards().contains(card) || skat.getCards().contains(exchangeCard) ) throw new ClientErrorException(NOT_FOUND);
		
		hand.getCards().remove(card);
		hand.getCards().add(exchangeCard);
		skat.getCards().remove(exchangeCard);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
	}
	
	@PATCH
	@Path("{id}/game/type")
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
		if (requester == hand.getPlayer()) throw new ClientErrorException(FORBIDDEN);
		if (hand.isSolo() == true) throw new ClientErrorException(FORBIDDEN);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		
		if (game.getState() == Game.State.EXCHANGE) throw new ClientErrorException(CONFLICT);
		game.setType(gameType);
		game.setState(Game.State.ACTIVE);
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		return gameType.value();
		
	}
	
	@DELETE
	@Path("{id}/cards/{cid}")
	public long removeCards(
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
		// how to determine the given hand's turn?
		//if (hand.getIdentity() != ) throw new ClientErrorException(CONFLICT);
		if (!hand.getCards().contains(card)) throw new ClientErrorException(CONFLICT);
		
		final Game game = requester.getTable().getGames().stream()
				.filter(g -> g.getForehand() == hand || g.getMiddlehand() == hand || g.getRearhand() == hand)
				.findFirst()
				.orElse(null);
		if (game.getState() != State.ACTIVE) throw new ClientErrorException(CONFLICT);
		
		entityManager.remove(hand.getCards());
		
		Trick gameTricks = (Trick) game.getTricks().stream().sorted();
		// creating a new trick if necessary?
		if (gameTricks == null)
			gameTricks = new Trick(game, Position.FOREHAND);
			
		hand.getCards().add(gameTricks.getFirstCard());
		hand.getCards().add(gameTricks.getSecondCard());
		hand.getCards().add(gameTricks.getThirdCard());
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		return hand.getIdentity();
	}
}