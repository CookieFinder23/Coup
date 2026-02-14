public class Bot extends Player{

    private static Cards bluff;
    public Bot(String name, int positionInTurnOrder) {
        super(name, positionInTurnOrder);
    }

    public void generateBluffCard() {
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
        3.  If bluffing, subtract fear of challenge penalty for each copy of the card in discard
        4.  Actions which require bluffing a card with three copies in discord are set to zero and cannot be changed
        5.  Add each Action's net coin value (Ambassador = 2) (Add lose influence bonus if applicable)
        6.  If you have the card required to use the action, add honesty bonus
        7.  Subtract fear of block penalty for each copy of a blocking card not seen
        8.  Subtract better option penalty for strictly better abilities of cards you have
        9.  Set all Action weightages below 1 to 1
        10. Whenever a bot draws a card, there is a 1/3 chance that they start bluffing a card they don't have. When picking
            an action, the bot acts as though the bluff card is in their hand. Whenever the bot discards a card, they
            stop bluffing.
         */

        final int honestyBonus = 3;
        final int loseInfluenceBonus = getCoins() * 2;
        final int fearOfBlockPenalty = 1;
        final int fearOfChallengePenalty = 1;
        final int betterOptionPenalty = 3;
        for(int i = 0; i < weightedActions.length; i++) {
            if (Actions.values()[i].getCard() != null) {
                Cards bluffingCard = Actions.values()[i].getCard();
                if (!handContainsCard(bluffingCard)) {
                    int amountOfCopiesInDiscard = copiesOfCardSeen(bluffingCard);
                    if (amountOfCopiesInDiscard == 3) continue;
                    weightedActions[i] -= amountOfCopiesInDiscard * fearOfChallengePenalty;
                }
            }
            weightedActions[i]++;
            switch (Actions.values()[i]) {
                case Actions.INCOME:
                    weightedActions[i]++;
                    if (handContainsCard(Cards.DUKE)) weightedActions[i] -= betterOptionPenalty;
                    break;
                case Actions.FOREIGN_AID:
                    weightedActions[i] += 2;
                    weightedActions[i] -= (3 - copiesOfCardSeen(Cards.DUKE)) * fearOfBlockPenalty;
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
                    weightedActions[i] -= (3 - copiesOfCardSeen(Cards.CONTESSA)) * fearOfBlockPenalty;
                    break;
                case Actions.EXCHANGE:
                    weightedActions[i] += 2;
                    if (handContainsCard(Cards.AMBASSADOR)) weightedActions[i] += honestyBonus;
                    break;
                case Actions.STEAL:
                    weightedActions[i] += 4;
                    if (handContainsCard(Cards.CAPTAIN)) weightedActions[i] += honestyBonus;
                    weightedActions[i] -= (3 - copiesOfCardSeen(Cards.CAPTAIN)) * fearOfBlockPenalty;
                    weightedActions[i] -= (3 - copiesOfCardSeen(Cards.AMBASSADOR)) * fearOfBlockPenalty;
                    break;
            }
            weightedActions[i] = Math.max(1, weightedActions[i]);
        }

        int sum = 0;
        for(int value : weightedActions)
            sum += value;

        int finalChoice = (int) (Math.random() * sum);
        int index = 0;
        for(int value : weightedActions) {
            finalChoice -= value;
            if (finalChoice <= 0)
                return Actions.values()[index];
            index++;
        }
        throw new IllegalStateException("Weighted random action generation failed");

    }

    public int actionToArraySpot(Actions action) {
        for(int i = 0; i < Actions.values().length; i++) {
            if (Actions.values()[i] == action)
                return i;
        }
        throw new IllegalStateException("Didn't find Action");
    }

    public Player pickTarget(Actions action) {
        // GIVE A WEIGHT TO EACH ALIVE PLAYER OTHER THAN YOU
        Player[] players = Main.getPlayers();
        int[] weightedPlayers = new int[players.length];
        for(int i = 0; i < players.length; i++) {
            if (players[i] == this || !Main.isPlayerAlive(players[i]))
                continue;
            if (action == Actions.STEAL && players[i].getCoins() > 1)
                weightedPlayers[i] += 5;
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
        throw new IllegalStateException("Weighted random action generation failed");
    }

    public Card pickExchange() {
        Card[] hand = Main.getCardsInZone(this);
        System.out.println(this + " shuffles a card from their hand into the deck.");
        return pickWorstCardInHand();
    }

    public boolean wantsToChallenge(Player player, Cards card, Player target, boolean block) {
        if ((((Math.random() < 0.1 && copiesOfCardSeen(card) == 0)
                || (Math.random() < 0.2 && copiesOfCardSeen(card) == 1)
                || (Math.random() < 0.3 && copiesOfCardSeen(card) == 2))
                && Math.random() * Main.getPlayers().length < 3)
                || copiesOfCardSeen(card) == 3
                || (target == this && Math.random() < 0.4)) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card);
            return false;
        }
    }

    public boolean wantsToChallenge(Player player, Cards card, boolean block) {
        if ((((Math.random() < 0.1 && copiesOfCardSeen(card) == 0)
        || (Math.random() < 0.2 && copiesOfCardSeen(card) == 1)
        || (Math.random() < 0.3 && copiesOfCardSeen(card) == 2))
        && Math.random() * Main.getPlayers().length < 3)
        || copiesOfCardSeen(card) == 3) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card);
            return false;
        }
    }

    public Cards wantsToBlock(Player player, Actions action) {
        if (handContainsCard(Cards.DUKE)) {
            return Cards.DUKE;
        } else {
            System.out.println(this + " declines to block.");
            return null;
        }
    }

    public Cards wantsToBlock(Player player, Actions action, Player target) {
        if(target != this && ((evaluatePlayerThreatLevel(target) > evaluatePlayerThreatLevel(player) || action == Actions.ASSASSINATE) && Math.random() < 0.96)) {
            System.out.println(this + " declines to block.");
            return null;
        }
        if (action == Actions.ASSASSINATE && ((handContainsCard(Cards.CONTESSA)) || Main.getCardsInZone(this).length == 1))
            return Cards.CONTESSA;
        if ((action == Actions.STEAL && handContainsCard(Cards.CAPTAIN)) && !(handContainsCard(Cards.AMBASSADOR) && (Math.random() < 0.5 || bluff == Cards.CAPTAIN)))
                return Cards.CAPTAIN;
        if (action == Actions.STEAL && handContainsCard(Cards.AMBASSADOR))
                return Cards.AMBASSADOR;
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
        for(Card card1 : Main.getCardsInZone(this))
            output++;
        for(Card card1 : Main.getCardsInZone(GlobalZones.DISCARD))
            output++;
        return output;
    }

    public void drawCard() {
        Card[] cardsInDeck = Main.getCardsInZone(GlobalZones.DECK);
        Card drawnCard = cardsInDeck[(int) (Math.random() * cardsInDeck.length)];
        drawnCard.setZone(Zones.getZone(this));
        System.out.println(this + " draws a card");
        if(Math.random() < 1/3.0)
            generateBluffCard();
    }

    public Card pickWorstCardInHand() {
        Card[] hand = Main.getCardsInZone(this);
        int[] weightedCards = new int[hand.length];

        for (int i = 0; i < hand.length; i++) {
            if (Main.getPlayers().length > 3) {
                switch (hand[i].getName()) {
                    case Cards.DUKE:
                        weightedCards[i] = 5;
                        break;
                    case Cards.ASSASSIN:
                        weightedCards[i] = 4;
                        break;
                    case Cards.AMBASSADOR:
                        weightedCards[i] = 1;
                        break;
                    case Cards.CAPTAIN:
                        weightedCards[i] = 2;
                        break;
                    case Cards.CONTESSA:
                        weightedCards[i] = 3;
                        break;
                }
            } else {
                switch (hand[i].getName()) {
                    case Cards.DUKE:
                        weightedCards[i] = 3;
                        break;
                    case Cards.ASSASSIN:
                        weightedCards[i] = 4;
                        break;
                    case Cards.AMBASSADOR:
                        weightedCards[i] = 1;
                        break;
                    case Cards.CAPTAIN:
                        weightedCards[i] = 5;
                        break;
                    case Cards.CONTESSA:
                        weightedCards[i] = 2;
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
        throw new IllegalStateException("Weighted random action generation failed");
    }

    /*
    public abstract Card pickExchange();
    public abstract Player pickTarget();
    public abstract Actions pickTurnAction();
    public abstract boolean wantsToChallenge(Player player, Cards card, Player target);
    public abstract Cards wantsToBlock(Player player, Actions action, Player target);
    public abstract Card resolveChallenge(Cards card);
    public abstract void discard();
     */
}
