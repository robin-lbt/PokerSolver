package com.libertrobin.logic.pokergame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.libertrobin.logic.filemanager.FileManager;
import com.libertrobin.logic.filemanager.FlopHoldemFileManager;
import com.libertrobin.logic.pokergame.flop_hand_evaluator.Hand;


/**
 * Class with methods useful to work with a No Limit Flop Hold'em poker game.
 * @author Robin Libert
 *
 */
public class NoLimitFlopHoldem extends PokerGame{
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> cardValueMapping = createFlopHoldemCardValueMapping();
	private float[] betSizings;
	private float[] raiseSizings;
	private int sb;
	private int bb;
	private int startingStack;
	
	public NoLimitFlopHoldem() {
		this.setBetSizingsRelativeToPotSize(new float[] {1.0f});
		this.setRaiseSizingsRelativeToPreviousBetOrRaise(new float[] {2.0f});
		this.setSmallBlind(1);
		this.setBigBlind(2);
		this.setStartingMoney(6);
	}
	
	private Map<String, Integer> createFlopHoldemCardValueMapping() {
		Map<String, Integer> cards = new HashMap<>();
		cards.put("2c", 1);
		cards.put("3c", 2);
		cards.put("4c", 3);
		cards.put("5c", 4);
		cards.put("6c", 5);
		cards.put("7c", 6);
		cards.put("8c", 7);
		cards.put("9c", 8);
		cards.put("Tc", 9);
		cards.put("Jc", 10);
		cards.put("Qc", 11);
		cards.put("Kc", 12);
		cards.put("Ac", 13);
		cards.put("2d", 1);
		cards.put("3d", 2);
		cards.put("4d", 3);
		cards.put("5d", 4);
		cards.put("6d", 5);
		cards.put("7d", 6);
		cards.put("8d", 7);
		cards.put("9d", 8);
		cards.put("Td", 9);
		cards.put("Jd", 10);
		cards.put("Qd", 11);
		cards.put("Kd", 12);
		cards.put("Ad", 13);
		cards.put("2h", 1);
		cards.put("3h", 2);
		cards.put("4h", 3);
		cards.put("5h", 4);
		cards.put("6h", 5);
		cards.put("7h", 6);
		cards.put("8h", 7);
		cards.put("9h", 8);
		cards.put("Th", 9);
		cards.put("Jh", 10);
		cards.put("Qh", 11);
		cards.put("Kh", 12);
		cards.put("Ah", 13);
		cards.put("2s", 1);
		cards.put("3s", 2);
		cards.put("4s", 3);
		cards.put("5s", 4);
		cards.put("6s", 5);
		cards.put("7s", 6);
		cards.put("8s", 7);
		cards.put("9s", 8);
		cards.put("Ts", 9);
		cards.put("Js", 10);
		cards.put("Qs", 11);
		cards.put("Ks", 12);
		cards.put("As", 13);
		return cards;
	}
	
	private ArrayList<Integer> getValidActionsSB(int remainingMoney, int previousBet) {
		ArrayList<Integer> validActions = new ArrayList<>();
		validActions.add(getPASS());
		validActions.add(getCALL());
		if(remainingMoney > previousBet)
			validActions.add(getRAISE());
		return validActions;
	}
	
	private ArrayList<Integer> getValidActionsBB(String history, int remainingMoney, int previousBet) {
		ArrayList<Integer> validActions = new ArrayList<>();
		int s = getBigBlind()-getSmallBlind();
		int sLength = String.valueOf(s).length();
		if(history.substring(history.length() - 1, history.length()).equals("p")) {
			validActions.add(getPASS());
		}else if(history.substring(history.length() - sLength-1, history.length()).equals("c"+s)) {
			validActions.add(getPASS());
			validActions.add(getRAISE());
		}else {
			validActions.add(getPASS());
			validActions.add(getCALL());
			if(remainingMoney > previousBet)
				validActions.add(getRAISE());
		}
		return validActions;
	}
	
	private String[] sortTwoCardsDecreasing(String card1, String card2) {
		String[] sortedHoleCards = new String[2];
		if(cardValueMapping.get(card1) >= cardValueMapping.get(card2)) {
			sortedHoleCards[0] = card1;
			sortedHoleCards[1] = card2;
		}else {
			sortedHoleCards[0] = card2;
			sortedHoleCards[1] = card1;
		}
		return sortedHoleCards;
	}
	
	private String[] sortThreeCardsDecreasing(String card1, String card2, String card3) {
		String[] sortedFistTwoCards = sortTwoCardsDecreasing(card1, card2);
		if(cardValueMapping.get(card3) >= cardValueMapping.get(sortedFistTwoCards[0])) {
			return new String[]{card3, sortedFistTwoCards[0], sortedFistTwoCards[1]};
		}else if(cardValueMapping.get(card3) >= cardValueMapping.get(sortedFistTwoCards[1])) {
			return new String[]{sortedFistTwoCards[0], card3, sortedFistTwoCards[1]};
		}else {
			return new String[]{sortedFistTwoCards[0], sortedFistTwoCards[1], card3};
		}
	}
	
	private boolean isPair(String card1, String card2) {
		return card1.substring(0, 1).equals(card2.substring(0, 1));
	}
	
	private boolean isSuited(String card1, String card2) {
		return card1.substring(1).equals(card2.substring(1));
	}
	
	private boolean isSuited(String card1, String card2, String card3) {
		return card1.substring(1).equals(card2.substring(1)) && card1.substring(1).equals(card3.substring(1));
	}
	
	private String getFormattedFlopFromCards(String card1, String card2, String card3) {
		String[] sortedFlop = sortThreeCardsDecreasing(card1, card2, card3);
		if(isSuited(sortedFlop[0], sortedFlop[1], sortedFlop[2]))
			return sortedFlop[0].substring(0,1)+sortedFlop[1].substring(0,1)+sortedFlop[2].substring(0,1)+"s";
		return sortedFlop[0].substring(0,1)+sortedFlop[1].substring(0,1)+sortedFlop[2].substring(0,1);
	}
	
	private String getFormattedPreflopHoleCards(String card1, String card2) {
		String[] sortedCards = sortTwoCardsDecreasing(card1, card2);
		if(isPair(sortedCards[0], sortedCards[1])) {
			return sortedCards[0].substring(0,1) + sortedCards[1].substring(0,1);
		}else if(isSuited(sortedCards[0], sortedCards[1])) {
			return sortedCards[0].substring(0,1) + sortedCards[1].substring(0,1) + "s";
		}
		return sortedCards[0].substring(0,1) + sortedCards[1].substring(0,1) + "o";
	}
	
	private String getFormattedPostflopHoleCards(String card1, String card2, String card3, String card4, String card5) {
		String[] sortedCards = sortTwoCardsDecreasing(card1, card2);
		if(isSuited(card1, card2) && isSuited(card3, card4, card5) && card1.substring(1).equals(card3.substring(1))){
			return sortedCards[0].substring(0,1)+sortedCards[1].substring(0,1)+"s";
		}
		return sortedCards[0].substring(0,1)+sortedCards[1].substring(0,1);
	}

	@Override
	public ArrayList<String> createListOfCards() {
		ArrayList<String> cards = new ArrayList<String>();
		cards.add("2c");
		cards.add("3c");
		cards.add("4c");
		cards.add("5c");
		cards.add("6c");
		cards.add("7c");
		cards.add("8c");
		cards.add("9c");
		cards.add("Tc");
		cards.add("Jc");
		cards.add("Qc");
		cards.add("Kc");
		cards.add("Ac");
		cards.add("2d");
		cards.add("3d");
		cards.add("4d");
		cards.add("5d");
		cards.add("6d");
		cards.add("7d");
		cards.add("8d");
		cards.add("9d");
		cards.add("Td");
		cards.add("Jd");
		cards.add("Qd");
		cards.add("Kd");
		cards.add("Ad");
		cards.add("2h");
		cards.add("3h");
		cards.add("4h");
		cards.add("5h");
		cards.add("6h");
		cards.add("7h");
		cards.add("8h");
		cards.add("9h");
		cards.add("Th");
		cards.add("Jh");
		cards.add("Qh");
		cards.add("Kh");
		cards.add("Ah");
		cards.add("2s");
		cards.add("3s");
		cards.add("4s");
		cards.add("5s");
		cards.add("6s");
		cards.add("7s");
		cards.add("8s");
		cards.add("9s");
		cards.add("Ts");
		cards.add("Js");
		cards.add("Qs");
		cards.add("Ks");
		cards.add("As");
		return cards;
	}

	@Override
	public boolean checkNextRoundOrShowdown(String actionsHistory, int roundNumber) {
		if(actionsHistory.length() < 2)
			return false;
		if(roundNumber == 0 && actionsHistory.length() == 2 && actionsHistory.equals("cp")) {
			return true;
		}else if(roundNumber == 1 && actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("pp")) {
			return true;
		}
		return actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("bc") || 
				actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("rc");
	}

	@Override
	public boolean checkTerminalWithoutShowdown(String actionsHistory, int roundNumber) {
		if(roundNumber == 0 && actionsHistory.length() == 2 && actionsHistory.equals("pp")) {
			return true;
		}
		if(actionsHistory.length() >= 2) {
			return actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("bp") || 
					actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("rp");
		}
		return false;
	}

	@Override
	public ArrayList<Integer> getValidActions(String history, String actionsHistory, int remainingMoney, int previousBet,
			int roundNumber, int plays) {
		if(roundNumber == 0 && plays == 0) {
			return getValidActionsSB(remainingMoney, previousBet);
		}else if(roundNumber == 0 && plays == 1) {
			return getValidActionsBB(history, remainingMoney, previousBet);
		}
		ArrayList<Integer> validActions = new ArrayList<>();
		for (int a = 0; a < getNUM_ACTIONS(); a++) {
			if(isActionValid(a, remainingMoney, previousBet, actionsHistory)) {
				validActions.add(a);
			}
		}
		return validActions;
	}

	@Override
	public int evaluate(String hand) {
		return Hand.evaluate(Hand.fromString(hand));
	}

	@Override
	public String getHandInStringFormat(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		return listOfCards.get(playerFirstCardIndexInList) + " "+ listOfCards.get(playerFirstCardIndexInList+1) +" "+ listOfCards.get(4)+" "+ listOfCards.get(5)+" "+ listOfCards.get(6);
	}

	@Override
	public int getNumberOfPrivateCards() {
		return 2;
	}

	@Override
	public String getFormattedFlopFromDeck(ArrayList<String> listOfCards) {
		String card1 = listOfCards.get(4);
		String card2 = listOfCards.get(5);
		String card3 = listOfCards.get(6);
		return getFormattedFlopFromCards(card1, card2, card3);
	}

	@Override
	public String getFormattedPreflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		String card1 = listOfCards.get(playerFirstCardIndexInList);
		String card2 = listOfCards.get(playerFirstCardIndexInList+1);
		return getFormattedPreflopHoleCards(card1, card2);
	}

	@Override
	public String getFormattedPostflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		String card1 = listOfCards.get(playerFirstCardIndexInList);
		String card2 = listOfCards.get(playerFirstCardIndexInList+1);
		String card3 = listOfCards.get(4);
		String card4 = listOfCards.get(5);
		String card5 = listOfCards.get(6);
		return getFormattedPostflopHoleCards(card1, card2, card3, card4, card5);
	}
	

	@Override
	public ArrayList<Integer> getRaiseSizings(String actionsHistory, int previousBet, int remainingMoney,
			float[] allowedRaiseSizings, int sb, int bb, int roundNumber, int plays) {
		ArrayList<Integer> raiseSizings = new ArrayList<>();
		if(roundNumber == 0 && plays == 0) {
			remainingMoney += sb;
		}else if(roundNumber == 0 && plays == 1 && actionsHistory.substring(actionsHistory.length()-1).equals("c")) {
			remainingMoney += bb;
		}
		if(remainingMoney > 0) {
			for(int i=0; i<allowedRaiseSizings.length;i++) { // check that the bet size is not bigger than the remaining stake
				int current = (int) (previousBet*allowedRaiseSizings[i]);
				if(current + previousBet < remainingMoney) {
					if(current+previousBet > 0)
						raiseSizings.add(current);
				}else if(remainingMoney-previousBet > 0){
					raiseSizings.add(remainingMoney-previousBet);
					break; // break the loop because the betSizingTemp array is sorted in increasing order
				}
			}
		}
		return raiseSizings;
	}

	@Override
	public ArrayList<Integer> getBetSizings(int potSize, int remainingMoney, float[] allowedBetSizings) {
		ArrayList<Integer> betSizings = new ArrayList<>();
		if(remainingMoney > 0) {
			for(int i=0; i<allowedBetSizings.length;i++) { // check that the bet size is not bigger than the remaining stake
				int current = (int) (potSize*allowedBetSizings[i]);
				if(current < remainingMoney) {
					if(current > 0)
						betSizings.add(current);
				}else if(remainingMoney >= 0){
					betSizings.add(remainingMoney);
					break; // break the loop because the betSizingTemp array is sorted in increasing order
				}
			}
		}
		return betSizings;
	}
	
	@Override
	public float[] getBetSizingsRelativeToPotSize() {
		return this.betSizings;
	}

	@Override
	public float[] getRaiseSizingsRelativeToPreviousBetOrRaise() {
		return this.raiseSizings;
	}

	@Override
	public int getBigBlind() {
		return this.bb;
	}

	@Override
	public int getSmallBlind() {
		return this.sb;
	}

	@Override
	public int getStartingMoney() {
		return this.startingStack;
	}

	@Override
	public boolean changeFirstPlayerToActPostflop() {
		return true;
	}

	@Override
	public int initPreviousBet() {
		return getBigBlind();
	}

	@Override
	public boolean blindsNotEqual() {
		return true;
	}

	@Override
	public FileManager getFileManager() {
		return new FlopHoldemFileManager();
	}
	
	@Override
	public void setBetSizingsRelativeToPotSize(float[] betSizings) {
		this.betSizings = betSizings;
		
	}

	@Override
	public void setRaiseSizingsRelativeToPreviousBetOrRaise(float[] raiseSizings) {
		this.raiseSizings = raiseSizings;
		
	}

	@Override
	public void setBigBlind(int bb) {
		this.bb = bb;
		
	}

	@Override
	public void setSmallBlind(int sb) {
		this.sb = sb;
	}

	@Override
	public void setStartingMoney(int startingStack) {
		this.startingStack = startingStack;
		
	}

}
