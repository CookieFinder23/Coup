public abstract class Player {
    private int coins;
    private final int positionInTurnOrder;
    private final String name;

    public Player(String name, int positionInTurnOrder) {
        this.coins = 2;
        this.positionInTurnOrder = positionInTurnOrder;
        this.name = name;
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
       return "Name: " + name + "\n"
               + "Coins: " + coins + "\n"
               + "Position in turn order: " + positionInTurnOrder;
    }

    public String getName() {
        return name;
    }

    public void takeTurn() {
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

    public void income() {
        coins++;
    }

    public void foreign_aid() {
        if(!Main.offerBlock(this, Actions.FOREIGN_AID, null))
            coins += 2;
    }

    public void coup() {
        coins -= 7;
        pickTarget().discard();
    }

    public void tax() {
        if(!Main.offerChallenge(this, Cards.DUKE, null))
            coins += 3;
    }

    public void assassinate() {
        coins -= 3;
        Player target = pickTarget();
        if (!Main.offerChallenge(this, Cards.ASSASSIN, target))
        {
            if (!Main.offerBlock(this, Actions.ASSASSINATE, target))
                target.discard();
        }
    }

    public void exchange() {
        if (!Main.offerChallenge(this, Cards.AMBASSADOR, null)) {
            Main.drawCard(this);
            Main.drawCard(this);
            pickExchange().setZone(Zones.getZone(GlobalZones.DECK));
            pickExchange().setZone(Zones.getZone(GlobalZones.DECK));
        }
    }

    public void steal() {
        Player target = pickTarget();
        if (!Main.offerChallenge(this, Cards.CAPTAIN, target)) {
            if (!Main.offerBlock(this, Actions.STEAL, target)) {
                int stolenCoins = Math.min(2, target.getCoins());
                target.setCoins(target.getCoins() - stolenCoins);
                coins += stolenCoins;
            }
        }
    }

    public abstract Card pickExchange();
    public abstract Player pickTarget();
    public abstract Actions pickTurnAction();
    public abstract boolean wantsToChallenge(Player player, Cards card, Player target);
    public abstract Cards wantsToBlock(Player player, Actions action, Player target);
    public abstract Card resolveChallenge(Cards card);
    public abstract void discard();
}
