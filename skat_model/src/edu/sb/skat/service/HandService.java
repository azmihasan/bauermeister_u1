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
		// TODO Fall für Gamestate Showdown? Es gibt kein State.SHOWDOWN Enum
		
		return hand;
	}
	
	@PATCH
	@Path("{id}/negotiate")
	public long negotiate (
			@PathParam("id") @Positive final long handIdentity,
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			final long bid
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		//TODO finish implementation
		return 0;
	}
	
	
	
	
	@PATCH
	@Path("{id}/game/bid")
	@Produces(TEXT_PLAIN)
	public long changeHandsValue(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long handIdentity,
			@QueryParam("bid")@PositiveOrZero final short bid
			) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);

		if (hand.getPlayer() == null) throw new ClientErrorException(FORBIDDEN);
		if (bid == 0) hand.setSolo(false);
		else if ( hand.getBid() == 0 || bid >= hand.getBid()) hand.setBid(bid);
		else throw new ClientErrorException(CONFLICT);
		
		final int soloCount = (int) hand.getGame().getHands().stream().filter(h -> h.getSolo()).count();
		switch (soloCount) {
			case 0:
				final Type gameType = entityManager
					.createQuery(FIND_PASS_TYPE, Long.class)
					.getResultList()
					.stream()
			        .map(identity -> entityManager.find(Type.class, identity))
			        .filter(type -> type != null)
			        .findFirst()
			        .orElseThrow(() -> new ServerErrorException(Status.INTERNAL_SERVER_ERROR));
				hand.getGame().setType(gameType);
				hand.getGame().setState(State.DONE);
				break;
			case 1:
				if (hand.getBid() > 0) hand.getGame().setState(State.ACTIVE);
				break;
			default:
				break;
		}
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(Hand.class, hand.getIdentity());
		
		return hand.getIdentity();
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
		System.out.println("############# requester " + requester + "##########");
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		System.out.println("################ ONE ##############");
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		
		System.out.println("############# handID " + handIdentity + "############");
		System.out.println("############# cardRef " + cardReference + "############");
		System.out.println("############# hand player " + hand.getPlayer().getIdentity() + "############");
		System.out.println("############# requesterID " + requesterIdentity + "############");
		
		System.out.println("################ TWO ##############");
		if (hand.getGame().getState() != State.ACTIVE) throw new ClientErrorException(CONFLICT);
		if (hand.getGame().getGameType() == null || hand.getGame().getGameType().getVariety() == Variety.PASS) throw new ClientErrorException(CONFLICT);
		System.out.println("################ FOUR ##############");
		final Card card = hand.getCards().stream().filter(c -> c.getIdentity() == cardReference).findFirst().orElseThrow(() -> new ClientErrorException(FORBIDDEN));
		System.out.println("################ FIVE ##############");
		System.out.println("#1Card: " + card);
		
		
		final Game game = hand.getGame();
	
		// allocate tricks
		boolean alreadyReferenced = false;
		if (game.getLeftTrickCard() == null) { game.setLeftTrickCard(card); alreadyReferenced = true ;}
		if (game.getMiddleTrickCard() == null && alreadyReferenced != true) { game.setMiddleTrickCard(card); alreadyReferenced = true;}
		if (game.getRightTrickCard() == null && alreadyReferenced != true) { game.setRightTrickCard(card); alreadyReferenced = true;}
		
		hand.getCards().removeIf(c -> c.getIdentity() == cardReference);
		
		if (game.getLeftTrickCard() != null && game.getMiddleTrickCard() != null && game.getRightTrickCard() != null) {
			//TODO: stich verbuchen, welcher spieler hat stich gewonnen (left, middle, right)
			System.out.println("###### all three tricks setted #######");
			
			short points = 0;
			Person lastPlayerPlayed = game.getCurrentPlayer();
			
			short leftPoints= 0;
			short middlePoints = 0;
			short rightPoints = 0;
			
			// Augen/Wertigkeiten der jeweiligen Karten
			// 7, 8, 9 = 0 punkte
			// Bube/Jack = 2 punkt
			// Dame = 3 punkte
			// K�nig = 4 punkte
			// 10 = 10 punkte
			// Ass = 11 punkte
			
			
			/*
			final Hand[] playerHands = hand.getGame().getHands().stream().filter(h -> h.getPlayer() != null).sorted().toArray(Hand[]::new);
			System.out.println("########## playerHands" + playerHands[0].toString() + "###########");
			
			Set<Card> leftCards = playerHands[0].getCards();
			System.out.println("##### leftCards: " + leftCards + "#########");
			// TODO Warum auch immer ist leftCards null
			Card leftTrick = leftCards.stream().filter(c -> c.getIdentity() == game.getLeftTrickCard().getIdentity()).findFirst().orElse(null);
			leftPoints = leftTrick.getRank().value();
			System.out.println("########## points left" + leftPoints + "###########");
			
			Set<Card> middleCards = playerHands[1].getCards();
			Card middleTrick = middleCards.stream().filter(c -> c == game.getLeftTrickCard()).findFirst().orElse(null);
			middlePoints = middleTrick.getRank().value();
			System.out.println("########## points middle" + middlePoints + "###########");
			
			Set<Card> rightCards = playerHands[2].getCards();
			Card rightTrick = rightCards.stream().filter(c -> c == game.getLeftTrickCard()).findFirst().orElse(null);
			rightPoints = rightTrick.getRank().value();
			System.out.println("########## points right" + rightPoints + "###########");
			*/
			
			//TODO PUNKTE VERRECHNEN
			
			
			game.setLeftTrickCard(null);
			game.setMiddleTrickCard(null);
			game.setRightTrickCard(null);
		}
		
		//TODO: wenn keine karten mehr auszuspielen sind, muss abgerechnet werden wer gewonnen hat -> erg verbuchen
		
		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		 
		return card;
	}
	
	@PATCH
	@Path("{id}/hands")
	//@Consumes({APPLICATION_JSON})
	@Produces(TEXT_PLAIN)
	public long updateHandsGameType (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		@QueryParam("gameTypeReference") @Positive final long gameTypeReference,
		@QueryParam("modifier") final Modifier modifier
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
		//if (hand.getPlayer() == null || hand.getPlayer().getIdentity() != requester.getIdentity()) throw new ClientErrorException(FORBIDDEN);
		if (!hand.getSolo() || hand.getGame().getState() != State.ACTIVE) throw new ClientErrorException(CONFLICT);
		
		final GameType gameType = entityManager.find(GameType.class, gameTypeReference);
		
		if (gameType == null) throw new ClientErrorException(NOT_FOUND);
		
		hand.getGame().setGameType(gameType);
		hand.getGame().setModifier(modifier);

		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(Hand.class, hand.getIdentity());
		cache.evict(Game.class, hand.getGame().getIdentity());
		
		return hand.getIdentity();
	}
	
	@PATCH
	@Path("{id}/hands/cards")
	@Produces(TEXT_PLAIN)
	public long updateHandsCards (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long handIdentity,
		@QueryParam("cardReferenceToRemove") @PositiveOrZero long cardReferenceToRemove,
		@QueryParam("cardReferenceToAdd") @PositiveOrZero long cardReferenceToAdd
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("skat");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(FORBIDDEN);
		
		final Hand hand = entityManager.find(Hand.class, handIdentity);
		if (hand == null) throw new ClientErrorException(NOT_FOUND);
						
		final Card cardToAdd = entityManager.find(Card.class, cardReferenceToAdd);
		if(cardToAdd != null) hand.getCards().add(cardToAdd);
		
		hand.getCards().removeIf(c -> c.getIdentity() == cardReferenceToRemove);

		entityManager.flush();
		
		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(Hand.class, hand.getIdentity());
		cache.evict(Game.class, hand.getGame().getIdentity());
		
		return hand.getIdentity();
		
	}
	
}

