import java.util.Scanner;
public class User extends Player{
    private static Scanner keyboard;
    public User(String name, int positionInTurnOrder) {
        super(name, positionInTurnOrder);
        keyboard = new Scanner(System.in);

    }

    public Actions pickTurnAction() {
        for(Actions action : Actions.values())
        return Actions.COUP;
    }

    public static int pickOption(String message, int max)
    {
        System.out.println(message + "\nInput \"0\" to see the gamestate.");
        int output;
        try {
            output = keyboard.nextInt();
            if (output == 0) {
                Main.printGamestate();
                return pickOption(message, max);
            }
            if (output < 0 || output > max) {
                System.out.println("Input must be between 0-" + max);
                return pickOption(message, max);
            }
            return output;
        } catch (Exception e) {
            keyboard.nextLine();
            System.out.println("Input must be an integer.");
            return pickOption(message, max);
        }
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
