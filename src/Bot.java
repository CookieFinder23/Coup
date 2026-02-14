public class Bot extends Player{
    public Bot(String name, int positionInTurnOrder) {
        super(name, positionInTurnOrder);
    }

    public Actions pickTurnAction() {

        if (getCoins() > 9) return Actions.COUP;

        // EACH ACTION HAS A DEFAULT WEIGHT OF 5
        int[] weightedActions = new int[Actions.values().length];
        for(int i = 0; i < weightedActions.length; i++)
            weightedActions[i] = 5;

        // MESS WITH NUMBERS

        // SET ILLEGAL ACTIONS TO A WEIGHT OF 0
        if(getCoins() < 7) {
            weightedActions[actionToArraySpot(Actions.COUP)] = 0;
            if (getCoins() < 3)
                weightedActions[actionToArraySpot(Actions.ASSASSINATE)] = 0;
        }

        int sum = 0;
        for(int value : weightedActions)
            sum += value;

        int finalChoice = (int) (Math.random() * sum);
        int index = 0;
        for(int value : weightedActions) {
            finalChoice -= value;
            if (finalChoice < 0)
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

    public Player pickTarget() {
        int indexOfTarget = getPositionInTurnOrder() + 1;
        Player target;
        do {
            indexOfTarget++;
            indexOfTarget = indexOfTarget %  Main.getPlayers().length;
            target = Main.getPlayers()[indexOfTarget];
        } while (!Main.isPlayerAlive(target) || target == this);
        return target;
    }

    public Card pickExchange() {
        Card[] hand = Main.getCardsInZone(Zones.getZone(this));
        System.out.println(this + " shuffled a card from their hand into the deck.");
        return hand[0];
    }

    public boolean wantsToChallenge(Player player, Cards card, Player target, boolean block) {
        if (Math.random() < 0.1) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card);
            return false;
        }
    }

    public boolean wantsToChallenge(Player player, Cards card, boolean block) {
        if (Math.random() < 0.1) {
            return true;
        } else {
            System.out.println(this + " declines to challenge " + player + "'s claim of " + card);
            return false;
        }
    }

    public Cards wantsToBlock(Player player, Actions action) {
        if (false) {
            return null;
        }
        switch(action) {
            case Actions.FOREIGN_AID: return Cards.DUKE;
            case Actions.ASSASSINATE: return Cards.CONTESSA;
            case Actions.STEAL:
                if (true)
                    return Cards.CAPTAIN;
                else
                    return Cards.AMBASSADOR;
            default: throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public Cards wantsToBlock(Player player, Actions action, Player target) {
        if (false) {
            return null;
        }
        switch(action) {
            case Actions.FOREIGN_AID: return Cards.DUKE;
            case Actions.ASSASSINATE: return Cards.CONTESSA;
            case Actions.STEAL:
                if (true)
                    return Cards.CAPTAIN;
                else
                    return Cards.AMBASSADOR;
            default: throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public Card resolveChallenge(Cards card) {
        Card[] hand = Main.getCardsInZone(Zones.getZone(this));
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
        Card[] hand = Main.getCardsInZone(Zones.getZone(this));
        Card chosenCard = hand[0];
        chosenCard.setZone(Zones.getZone(GlobalZones.DISCARD));
        System.out.println(this + " discards a " + chosenCard + ".");
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
