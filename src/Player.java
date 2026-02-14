public abstract class Player {
    private int coins;
    private final int positionInTurnOrder;
    private final String name;
    private int turnNumber;
    private Cards lastPlayedCard;

    public Player(String name, int positionInTurnOrder) {
        this.coins = 2;
        this.positionInTurnOrder = positionInTurnOrder;
        this.name = name;
        this.turnNumber = 0;
    }

    public void setLastPlayedCard(Cards card) {
        lastPlayedCard = card;
    }

    public Cards getLastPlayedCard() {
        return lastPlayedCard;
    }

    public int getPositionInTurnOrder() {
        return positionInTurnOrder;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String toString() {
       return name;
    }

    public String getName() {
        return name;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void takeTurn() {
        turnNumber++;
        System.out.println("\nTURN #" + turnNumber + " OF " + this + ":");
        switch(pickTurnAction()){
            case Actions.INCOME:
                income();
                break;
            case Actions.FOREIGN_AID:
                foreign_aid();
                break;
            case Actions.COUP:
                coup();
                break;
            case Actions.TAX:
                tax();
                break;
            case Actions.ASSASSINATE:
                assassinate();
                break;
            case Actions.EXCHANGE:
                exchange();
                break;
            case Actions.STEAL:
                steal();
                break;
        }
    }

    public void attemptingToUseAction(Actions action) {
        System.out.println(this + " is attempting to use " + action);
    }

    public void successfulUseOfAction(Actions action) {
        System.out.println(this + " used " + action);
    }

    public void attemptingToUseAction(Actions action, Player target) {
        System.out.println(this + " is attempting to use " + action + " on " + target);
    }

    public void successfulUseOfAction(Actions action, Player target) {
        System.out.println(this + " used " + action + " on " + target);
    }

    public void income() {
        coins++;
        successfulUseOfAction(Actions.INCOME);
    }

    public void foreign_aid() {
        attemptingToUseAction(Actions.FOREIGN_AID);
        if(!Main.offerBlock(this, Actions.FOREIGN_AID)) {
            successfulUseOfAction(Actions.FOREIGN_AID);
            coins += 2;

        }

    }

    public void coup() {

        coins -= 7;
        Player target = pickTarget(Actions.COUP);
        successfulUseOfAction(Actions.COUP, target);
        target.discard();
    }

    public void tax() {
        attemptingToUseAction(Actions.TAX);
        if(!Main.offerChallenge(this, Cards.DUKE)) {
            successfulUseOfAction(Actions.TAX);
            coins += 3;
        }
    }

    public void assassinate() {
        coins -= 3;
        Player target = pickTarget(Actions.ASSASSINATE);
        attemptingToUseAction(Actions.ASSASSINATE, target);
        if (!Main.offerChallenge(this, Cards.ASSASSIN, target))
        {
            if (!Main.offerBlock(this, Actions.ASSASSINATE, target)) {
                successfulUseOfAction(Actions.ASSASSINATE, target);
                target.discard();
            }
        }
    }

    public void exchange() {
        attemptingToUseAction(Actions.EXCHANGE);
        if (!Main.offerChallenge(this, Cards.AMBASSADOR)) {
            successfulUseOfAction(Actions.EXCHANGE);
            drawCard();
            drawCard();
            pickExchange().setZone(Zones.getZone(GlobalZones.DECK));
            pickExchange().setZone(Zones.getZone(GlobalZones.DECK));
        }
    }

    public void steal() {
        Player target = pickTarget(Actions.STEAL);
        attemptingToUseAction(Actions.STEAL, target);
        if (!Main.offerChallenge(this, Cards.CAPTAIN, target)) {
            if (!Main.offerBlock(this, Actions.STEAL, target)) {
                successfulUseOfAction(Actions.STEAL, target);
                int stolenCoins = Math.min(2, target.getCoins());
                target.setCoins(target.getCoins() - stolenCoins);
                coins += stolenCoins;
            }
        }
    }



    public abstract void drawCard();
    public abstract Card pickExchange();
    public abstract Player pickTarget(Actions action);
    public abstract Actions pickTurnAction();
    public abstract boolean wantsToChallenge(Player player, Cards card, Player target, boolean block);
    public abstract boolean wantsToChallenge(Player player, Cards card, boolean block);
    public abstract Cards wantsToBlock(Player player, Actions action, Player target);
    public abstract Cards wantsToBlock(Player player, Actions action);
    public abstract Card resolveChallenge(Cards card);
    public abstract void discard();
}
