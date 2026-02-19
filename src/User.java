import java.util.Scanner;
public class User extends Player{
    private static Scanner keyboard;
    public User(String name, int positionInTurnOrder) {
        super(name, positionInTurnOrder);
        keyboard = new Scanner(System.in);

    }

    public Actions pickTurnAction() {
        if (getCoins() > 9) {
            System.out.println("Since you have 10+ coins, you must Coup.");
            return Actions.COUP;
        }
        for(int i = 0; i < Actions.values().length; i++)
            System.out.println((i + 1) + ": " + Actions.values()[i]);
        Actions action = Actions.values()[pickOption("What action would you like to perform?", Actions.values().length) - 1];
        if ((action == Actions.COUP && getCoins() < 7 ) || (action == Actions.ASSASSINATE && getCoins() < 3)) {
            System.out.println("You don't have the necessary coins to perform that action.");
            return pickTurnAction();
        }
        return action;
    }

    public Player pickTarget(Actions action) {
        int amountOfchoices = 0;
        for(Player player : Main.getPlayers()) {
            if (Main.isPlayerAlive(player) && player != this)
                amountOfchoices++;
        }
        Player[] choices = new Player[amountOfchoices];
        int index = 0;
        for(Player player : Main.getPlayers()) {
            if (Main.isPlayerAlive(player) && player != this) {
                choices[index] = player;
                System.out.println((index + 1) + ": " + player.getName());
                index++;
            }
        }
        return choices[pickOption("Which player would you like to target?", choices.length) - 1];
    }

    public Card[] listCardsInHand() {
        Card[] hand = Main.getCardsInZone(this);
        for(int i = 0; i < hand.length; i++)
            System.out.println((i + 1) + ": " + hand[i]);
        return hand;
    }

    public Card pickExchange() {
        Card[] hand = listCardsInHand();
        return hand[pickOption("Which card would you like to shuffle into the deck?", hand.length) - 1];
    }

    public boolean wantsToChallenge(Player player, Cards card, Player target, boolean block) {

        if (block) {
            return pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + card
                    + " to block an action, would you like to challenge their claim of " + card + "?", 2) == 1;
        } else {
            return pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + card.getAction() + " on " + target
                    + ", would you like to challenge their claim of " + card + "?", 2) == 1;
        }
    }

    public boolean wantsToChallenge(Player player, Cards card, boolean block) {
        if (block) {
            return pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + card
                    + " to block an action, would you like to challenge their claim of " + card + "?", 2) == 1;
        } else {
            return pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + card.getAction()
                    + ", would you like to challenge their claim of " + card + "?", 2) == 1;
        }
    }

    public Cards wantsToBlock(Player player, Actions action) {
        if (pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + action
                + ", would you like to block their action?", 2) == 2) {
            return null;
        }
        setLastPlayedCard(Cards.DUKE);
        return Cards.DUKE;
    }

    public Cards wantsToBlock(Player player, Actions action, Player target) {
        if (pickOption("1: Yes\n2: No\nGiven that " + player + " is attempting to use " + action
                + " on " + target + ", would you like to block their action?", 2) == 2) {
            return null;
        }
        switch(action) {
            case Actions.ASSASSINATE:
                setLastPlayedCard(Cards.CONTESSA);
                return Cards.CONTESSA;
            case Actions.STEAL:
                if (pickOption("1: Captain\n2: Ambassador\nWhich card do you want to claim to block Steal with?", 2) == 1) {
                    setLastPlayedCard(Cards.CAPTAIN);
                    return Cards.CAPTAIN;
                }
                else {
                    setLastPlayedCard(Cards.AMBASSADOR);
                    return Cards.AMBASSADOR;
                }
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
        if(pickOption("Would you like to reveal your " + cardToReveal + " to win the challenge?\n1: Yes\n2: No", 2) == 2) return null;
        return cardToReveal;
    }

    public void discard() {
        Card[] hand = listCardsInHand();
        hand[pickOption("What card would you like to discard?", hand.length) - 1].setZone(Zones.getZone(GlobalZones.DISCARD));
        if(!Main.isPlayerAlive(this))
            System.out.println("You are out of the game!");
    }

    public static int pickOption(String message, int max)
    {
        System.out.println(message + " (Input \"0\" to see the gamestate.)");
        int output;
        try {
            output = keyboard.nextInt();
            if (output == 0) {
                Main.printGamestate();
                return pickOption(message, max);
            }
            if (output < 0 || output > max) {
                System.out.println("Input must be between 0-" + max + ".");
                return pickOption(message, max);
            }
            return output;
        } catch (Exception e) {
            keyboard.nextLine();
            System.out.println("Input must be an integer.");
            return pickOption(message, max);
        }
    }

    public void drawCard() {
        Card[] cardsInDeck = Main.getCardsInZone(GlobalZones.DECK);
        Card drawnCard = cardsInDeck[(int) (Math.random() * cardsInDeck.length)];
        drawnCard.setZone(Zones.getZone(this));
        System.out.println("You drew a " + drawnCard + ".");
        for(Player player : Main.getPlayers()) {
            if(player != this)
                player.opponentDrewCard();
        }
    }

    public void opponentDrewCard() {
        ;
    }
}
