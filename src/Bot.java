public class Bot extends Player{

    private Cards bluff;
    private Actions lastAction;
    private boolean lastActionSucceeded;
    private boolean[] cardKnownToBeInDeck;
    public Bot(String name, int positionInTurnOrder) {
        super(name, positionInTurnOrder);
        lastAction = Actions.INCOME;
        lastActionSucceeded = true;
        cardKnownToBeInDeck = new boolean[15];
    }

    public void generateBluffCard() {
        bluff = null; // Challenge: Why does the code have an extremely low chance of handing indefinitely without this line?
        int indexOfCard;
        do {
            indexOfCard = (int) (Math.random() * 5);
        } while(handContainsCard(Cards.values()[indexOfCard]));
        bluff = Cards.values()[indexOfCard];
    }

    public Actions pickTurnAction() {

        if (getCoins() > 9) return Actions.COUP;

        int[] weightedActions = new int[Actions.values().length];

        /*
        ACTION WEIGHT ALGORITHM
        1.  Each Action has a default value of 1
        2.  Actions you cannot afford are set to zero and cannot be changed
        3.  If bluffing, subtract fear of challenge penalty for each copy of the card in discard, and for each time the card was played by an
            alive player last turn
        4.  Actions which require bluffing a card with three copies in discard are set to zero and cannot be changed
        5.  Add each Action's net coin value (Ambassador = 2) (Add lose influence bonus if applicable) (Add 3 or less player bonus for captain)
        6.  If you have the card required to use the action, add honesty bonus
        7.  Subtract fear of block penalty for each copy of a blocking card not seen multiplied by the amount of times the card has been claimed
        8.  Subtract better option penalty for strictly better abilities of cards you have
        9.  If the bot tried and failed to do an action last turn, subtract the repetition penalty to that action
        10. Multiply all action weightages by the consistency multiplier value
        11. Set all Action weightages below 1 to 1
        12. Whenever a bot draws a card, there is a 20% chance that they start bluffing a card they don't have. When picking
            an action, the bot acts as though the bluff card is in their hand. Whenever the bot discards a card, they
            stop bluffing.
         */

        final int honestyBonus = 3;
        final int loseInfluenceBonus = getCoins() * 2;
        final int fearOfBlockPenalty = 1;
        final int fearOfChallengePenalty = 1;
        final int betterOptionPenalty = 3;
        final int repetitionPenalty = 5;
        final int consistencyMultiplier = 2;
        final int lowPlayerBonus = 4;
        for(int i = 0; i < weightedActions.length; i++) {
            if (Actions.values()[i].getCard() != null) {
                Cards bluffingCard = Actions.values()[i].getCard();
                if (!handContainsCard(bluffingCard)) {
                    int amountOfCopiesInDiscard = copiesOfCardSeen(bluffingCard);
                    if (amountOfCopiesInDiscard == 3) continue;
                    weightedActions[i] -= (amountOfCopiesInDiscard + howManyTimesHasCardBeenPlayedRecently(bluffingCard))* fearOfChallengePenalty;
                }
            }
            weightedActions[i]++;
            switch (Actions.values()[i]) {
                case Actions.INCOME:
                    if (copiesOfCardSeen(Cards.DUKE) == 3)
                        break;
                    weightedActions[i]++;
                    if (handContainsCard(Cards.DUKE))
                        weightedActions[i] -= betterOptionPenalty;
                    break;
                case Actions.FOREIGN_AID:
                    weightedActions[i] += 2;
                    weightedActions[i] -=  calculateFearOfBlockingPenalty(Cards.DUKE, fearOfBlockPenalty);
                    if (handContainsCard(Cards.DUKE)) weightedActions[i] -= betterOptionPenalty;
                    break;
                case Actions.COUP:
                    if (getCoins() < 7) {
                        weightedActions[i] = 0;
                        continue;
                    }
                    weightedActions[i] += loseInfluenceBonus;
                    if (handContainsCard(Cards.ASSASSIN)) weightedActions[i] -= betterOptionPenalty;
                    break;
                case Actions.TAX:
                    weightedActions[i] += 3;
                    if (handContainsCard(Cards.DUKE)) weightedActions[i] += honestyBonus;
                    break;
                case Actions.ASSASSINATE:
                    if (getCoins() < 3) {
                        weightedActions[i] = 0;
                        continue;
                    }
                    weightedActions[i] += loseInfluenceBonus + 4;
                    if (handContainsCard(Cards.ASSASSIN)) weightedActions[i] += honestyBonus;
                    weightedActions[i] -=  calculateFearOfBlockingPenalty(Cards.CONTESSA, fearOfBlockPenalty) * 2;
                    break;
                case Actions.EXCHANGE:
                    weightedActions[i] += 2;
                    if (handContainsCard(Cards.AMBASSADOR)) weightedActions[i] += honestyBonus;
                    break;
                case Actions.STEAL:
                    weightedActions[i] += 4;
                    if (handContainsCard(Cards.CAPTAIN)) weightedActions[i] += honestyBonus;
                    weightedActions[i] -= calculateFearOfBlockingPenalty(Cards.CAPTAIN, fearOfBlockPenalty);
                    weightedActions[i] -= calculateFearOfBlockingPenalty(Cards.AMBASSADOR, fearOfBlockPenalty);
                    int playerCount = 0;
                    for(Player player : Main.getPlayers()) {
                        if (Main.isPlayerAlive(player))
                            playerCount++;
                    }
                    if(playerCount < 3)
                        weightedActions[i] += (4 - playerCount) * lowPlayerBonus;
                    break;
            }
            if(Actions.values()[i] == lastAction && !lastActionSucceeded)
                weightedActions[i] -= repetitionPenalty;
            weightedActions[i] *= consistencyMultiplier;
            weightedActions[i] = Math.max(1, weightedActions[i]);
        }

        int sum = 0;
        for(int value : weightedActions)
            sum += value;

//        for(int i = 0; i < weightedActions.length; i++)
//            System.out.println(Actions.values()[i] + ": " + weightedActions[i]);

        int finalChoice = (int) (Math.random() * sum);
        int index = 0;
        for(int value : weightedActions) {
            finalChoice -= value;
            if (finalChoice <= 0) {
                lastActionSucceeded = false;
                return lastAction = Actions.values()[index];
            }
            index++;
        }
        throw new IllegalStateException("invalid action " + finalChoice);
    }

    public int calculateFearOfBlockingPenalty(Cards card, int penalty) {
        return ((3 - copiesOfCardSeen(card)) * howManyTimesHasCardBeenPlayedRecently(card)) * penalty;
    }

    public int howManyTimesHasCardBeenPlayedRecently(Cards card) {
        int output = 0;
        Player[] players = Main.getPlayers();
        for(Player player : players) {
            if (player.getLastPlayedCard() == card && player != this && Main.isPlayerAlive(player))
                output++;
        }
        return output;
    }

    public void successfulUseOfAction(Actions action) {
        lastActionSucceeded = true;
        System.out.println(this + " used " + action + ".");
    }

    public void successfulUseOfAction(Actions action, Player target) {
        lastActionSucceeded = true;
        System.out.println(this + " used " + action + " on " + target + ".");
    }

    public Player pickTarget(Actions action) {
        // GIVE A WEIGHT TO EACH ALIVE PLAYER OTHER THAN YOU
        Player[] players = Main.getPlayers();
        int[] weightedPlayers = new int[players.length];
        for(int i = 0; i < players.length; i++) {
            if (players[i] == this || !Main.isPlayerAlive(players[i]))
                continue;
            if (action == Actions.STEAL && players[i].getCoins() > 1 && players[i].getLastPlayedCard() != Cards.AMBASSADOR && players[i].getLastPlayedCard() != Cards.CAPTAIN)
                weightedPlayers[i] *= 3;
            if(action == Actions.ASSASSINATE && players[i].getLastPlayedCard() != Cards.CONTESSA)
                weightedPlayers[i] *= 3;
            weightedPlayers[i] += evaluatePlayerThreatLevel(players[i]);
        }

        // PICK WEIGHTED AVERAGE
        int sum = 0;
        for(int value : weightedPlayers)
            sum += value;

        int finalChoice = (int) (Math.random() * sum);
        int index = 0;
        for(int value : weightedPlayers) {
            finalChoice -= value;
            if (finalChoice <= 0)
                return players[index];
            index++;
        }
        throw new IllegalStateException("invalid player");
    }

    public Card pickExchange() {
        System.out.println(this + " shuffles a card from their hand into the deck.");
        Card chosenCard = pickWorstCardInHand();
        changeCardKnownToBeInDeck(chosenCard, true);
        return chosenCard;
    }

    public void changeCardKnownToBeInDeck(Card card, boolean bool) {
        for(int i = 0; i < Main.getCards().length; i++) {
            if (Main.getCards()[i] == card) {
                cardKnownToBeInDeck[i] = bool;
                return;
            }
        }
    }

    public boolean wantsToChallenge(Player player, Cards card, Player target, boolean block) {
        switch(shouldIChallengeWithSolve(player, card, block)) {
            case Solved.TRUE:
                return true;
            case Solved.FALSE:
                return false;
            default:
                break;
        }
        if ((((Math.random() < 0.1 && copiesOfCardSeen(card) == 0)
                || (Math.random() < 0.2 && copiesOfCardSeen(card) == 1)
                || (Math.random() < 0.3 && copiesOfCardSeen(card) == 2))
                && Math.random() * Main.getPlayers().length < 3)
                || (target == this && Math.random() < 0.4)) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card + ".");
            return false;
        }
    }

    public boolean wantsToChallenge(Player player, Cards card, boolean block) {
        switch(shouldIChallengeWithSolve(player, card, block)) {
            case Solved.TRUE:
                return true;
            case Solved.FALSE:
                return false;
            default:
                break;
        }
        if ((((Math.random() < 0.1 && copiesOfCardSeen(card) == 0)
        || (Math.random() < 0.2 && copiesOfCardSeen(card) == 1)
        || (Math.random() < 0.3 && copiesOfCardSeen(card) == 2))
        && Math.random() * Main.getPlayers().length < 3)) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card + ".");
            return false;
        }
    }

    public Cards wantsToBlock(Player player, Actions action) {
        if (handContainsCard(Cards.DUKE) || (lastAction == Actions.TAX && lastActionSucceeded)) {
            setLastPlayedCard(Cards.DUKE);
            return Cards.DUKE;
        } else {
            System.out.println(this + " declines to block.");
            return null;
        }
    }

    public Cards wantsToBlock(Player player, Actions action, Player target) {
        if (action == Actions.ASSASSINATE && ((handContainsCard(Cards.CONTESSA)) || Main.getCardsInZone(this).length == 1)) {
            setLastPlayedCard(Cards.CONTESSA);
            return Cards.CONTESSA;
        }
        if ((action == Actions.STEAL && (handContainsCard(Cards.CAPTAIN)
                || (lastAction == Actions.STEAL && lastActionSucceeded)))
                && !(handContainsCard(Cards.AMBASSADOR) && (Math.random() < 0.5 || bluff == Cards.CAPTAIN))) {
            setLastPlayedCard(Cards.CAPTAIN);
            return Cards.CAPTAIN;
        }
        if (action == Actions.STEAL && handContainsCard(Cards.AMBASSADOR)) {
            setLastPlayedCard(Cards.AMBASSADOR);
            return Cards.AMBASSADOR;
    }
        System.out.println(this + " declines to block.");
        return null;
    }

    public int evaluatePlayerThreatLevel(Player player) {
        if(player == this) return 0;
        return Main.getCardsInZone(player).length * 7 + player.getCoins();
    }

    public Card resolveChallenge(Cards card) {
        Card[] hand = Main.getCardsInZone(this);
        Card cardToReveal = null;
        for(Card card1 : hand) {
            if (card1.getName() == card) {
                cardToReveal = card1;
                break;
            }
        }
        if (cardToReveal == null) return null;
        return cardToReveal;
    }

    public void discard() {
        Card[] hand = Main.getCardsInZone(this);
        if (hand.length == 0)
            return;
        Card chosenCard = pickWorstCardInHand();
        chosenCard.setZone(Zones.getZone(GlobalZones.DISCARD));
        System.out.println(this + " discards a " + chosenCard + ".");
        bluff = null;
        if(!Main.isPlayerAlive(this))
            System.out.println(this + " is out of the game!");
    }

    public boolean handContainsCard(Cards card) {
        for(Card card1 : Main.getCardsInZone(this)) {
            if (card1.getName() == card)
                return true;
        }
        return bluff == card;
    }

    public int copiesOfCardSeen(Cards card) {
        int output = 0;
        for(Card card1 : Main.getCardsInZone(this)) {
            if (card1.getName() == card)
                output++;
        }
        for(Card card1 : Main.getCardsInZone(GlobalZones.DISCARD)) {
            if (card1.getName() == card)
                output++;
        }
        for(int i = 0; i < Main.getCards().length; i++) {
            if(Main.getCards()[i].getName() == card && cardKnownToBeInDeck[i])
                output++;
        }
        return output;
    }

    public void drawCard() {
        Card[] cardsInDeck = Main.getCardsInZone(GlobalZones.DECK);
        Card drawnCard = cardsInDeck[(int) (Math.random() * cardsInDeck.length)];
        drawnCard.setZone(Zones.getZone(this));
        System.out.println(this + " draws a card.");
        for(Player player : Main.getPlayers()) {
            if (player != this)
                opponentDrewCard();
        }
        if(Math.random() < 0.2)
            generateBluffCard();
    }

    public void opponentDrewCard() {
        for(int i = 0; i < cardKnownToBeInDeck.length; i++)
            cardKnownToBeInDeck[i] = false;
    }

    public Card pickWorstCardInHand() {
        Card[] hand = Main.getCardsInZone(this);
        int[] weightedCards = new int[hand.length];

        for (int i = 0; i < hand.length; i++) {
            if (Main.getPlayers().length > 3) {
                switch (hand[i].getName()) {
                    case Cards.DUKE:
                        weightedCards[i] = 1;
                        break;
                    case Cards.ASSASSIN:
                        weightedCards[i] = 2;
                        break;
                    case Cards.AMBASSADOR:
                        weightedCards[i] = 5;
                        break;
                    case Cards.CAPTAIN:
                        weightedCards[i] = 4;
                        break;
                    case Cards.CONTESSA:
                        weightedCards[i] = 3;
                        break;
                }
            } else {
                switch (hand[i].getName()) {
                    case Cards.DUKE:
                        weightedCards[i] = 4;
                        break;
                    case Cards.ASSASSIN:
                        weightedCards[i] = 3;
                        break;
                    case Cards.AMBASSADOR:
                        weightedCards[i] = 2;
                        break;
                    case Cards.CAPTAIN:
                        weightedCards[i] = 1;
                        break;
                    case Cards.CONTESSA:
                        weightedCards[i] = 5;
                        break;
                }
            }
        }

        // PICK WEIGHTED AVERAGE
        int sum = 0;
        for(int value : weightedCards)
            sum += value;

        int finalChoice = (int) (Math.random() * sum);
        int index = 0;
        for(int value : weightedCards) {
            finalChoice -= value;
            if (finalChoice <= 0)
                return hand[index];
            index++;
        }
        throw new IllegalStateException("invalid card" + finalChoice);
    }

    public Solved shouldIChallengeWithSolve(Player opponent, Cards card, boolean block) {
        // IF THERE ARE THREE CARDS SEEN, THEN CHALLENGE
        if(copiesOfCardSeen(card) == 3)
            return Solved.TRUE;

        // IF A PLAYER HAS 2 CARDS, THIS DOESN'T APPLY
        if(Main.getCardsInZone(this).length == 2 || Main.getCardsInZone(opponent).length == 2)
            return Solved.UNKNOWN;

        // IF THERE ARE ANY OTHER PLAYERS, THIS DOESN'T APPLY
        for(Player player : Main.getPlayers()) {
            if(Main.isPlayerAlive(player) && player != this && player != opponent)
                return Solved.UNKNOWN;
        }
        if(block) {
            // IF I WILL DIE AFTERWARDS, THEN DO
            return opponent.getCoins() > 6 ? Solved.TRUE : Solved.UNKNOWN;
        } else {
            Cards myCard = Main.getCardsInZone(this)[0].getName();
            // IF I CAN KILL AFTERWARDS, THEN DON'T
            if (getCoins() > 6 && card != Cards.ASSASSIN && (card != Cards.CAPTAIN || getCoins() < 9)
                    || (getCoins() > 2 && myCard == Cards.ASSASSIN && (card != Cards.CAPTAIN || getCoins() < 5)))
                return Solved.FALSE;

            switch (card) {
                case Cards.DUKE:
                    if (myCard == Cards.CAPTAIN)
                        return opponent.getCoins() + 1 > 6 ? Solved.TRUE : Solved.FALSE;
                    else
                        return opponent.getCoins() + 3 > 6 ? Solved.TRUE : Solved.FALSE;
                case Cards.ASSASSIN:
                    return myCard == Cards.CONTESSA ? Solved.FALSE : Solved.TRUE;
                case Cards.CAPTAIN:
                    if (myCard == Cards.CAPTAIN || myCard == Cards.AMBASSADOR)
                        return Solved.FALSE;
                    else
                        return myCard == Cards.DUKE && (opponent.getCoins() < getCoins() - 4) ? Solved.TRUE : Solved.FALSE;
                default:
                    return Solved.FALSE;
            }
        }


    }
}
