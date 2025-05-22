// main game class for the Ramen Shop Simulator
// manages the game state, shop operations, and game loop

class Game {
    private static final int MAX_CUSTOMERS = 10; // Maximum number of customers per day
    private static final int MAX_DAYS = 3; // Maximum number of days to play
    private boolean continueGame = true; // Flag to continue the game
    private Money money = new Money(); // Using Money object instead of primitive
    private int currentDay = 1; // Current day in the game

    private final Shop shop; // Shop object that manages inventory and operations
    private final Suspicion suspicion; // Using the Suspicion wrapper class from Main.java

    // new game with fresh inventory
    public Game() {
        // initialize money with starting amount
        money.set_money(money.get_starting_money()); // FIXED: Set the initial amount correctly

        // initialize the shop with a new inventory
        Inventory inventory = new Inventory(4);
        inventory.add_ingredient(new Ingredient("Noodles", 10, 5), 0);
        inventory.add_ingredient(new Ingredient("Broth", 10, 4), 1);
        inventory.add_ingredient(new Ingredient("Meat", 10, 6), 2);
        inventory.add_ingredient(new Ingredient("Toppings", 10, 3), 3);
        shop = new Shop(inventory);

        // initialize suspicion with zero value
        suspicion = new Suspicion();
    }

    // resetting all game variables fo rnew game
    private void resetGameForPlayAgain() {
        // reset all game state variables
        continueGame = true;
        currentDay = 1;
        money.set_money(money.get_starting_money()); // Reset money to starting amount
        suspicion.set_suspicion(0);

        // reset the shop's inventory using resetInventory instead of creating a new inventory
        shop.getInventory().resetInventory();
    }

    // method asks user to play again after game ends
    private void askToPlayAgain(Scanner sc) {
        System.out.println("|-----------------------------------------------------------|");
        System.out.printf("| %-57s |\n", "Do you want to play again? (1: yes / 2: no)");
        System.out.println("|-----------------------------------------------------------|");

        int choice;
        try {
            choice = sc.nextInt();
            sc.nextLine();
        }
        catch (Exception e)
        {
            sc.nextLine();
            System.out.println("Invalid input. Exiting the game.");
            continueGame = false;
            return;
        }

        if (choice == 1)
        {
            // reset game state for a new game
            resetGameForPlayAgain();
            // no recursive startGame() call - we'll use a loop in the main method
        }
        else
        {
            continueGame = false;
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Game Over! Thanks for playing!");
            System.out.println("|-----------------------------------------------------------|");
        }
    }


    // the main game loop - runs through all days until the game ends

    public void startGame() {
        Scanner sc = new Scanner(System.in);

        try {
            do { // outer loop to handle playing multiple games
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "RAMEN SHOP SIMULATOR");
                System.out.println("|-----------------------------------------------------------|");

                // day loop - play through each day until MAX_DAYS is reached or game ends
                while (currentDay <= MAX_DAYS && continueGame) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "DAY " + currentDay + " OF " + MAX_DAYS);
                    System.out.println("|-----------------------------------------------------------|");

                    // check suspicion level at the start of each day
                    if (suspicion.sus_reached_full()) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }

                    // play the day
                    playDay(sc);

                    if (!continueGame) break; // stop if game ended during the day (arrest, etc.)

                    // end of day summary
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "End of day " + currentDay + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                    System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicion.get_suspicion() + "%");
                    System.out.println("|-----------------------------------------------------------|");

                    // apply suspicion increase for the next day
                    suspicion.suspicion_newday(currentDay);

                    // check if suspicion reached max after day end increase
                    if (suspicion.sus_reached_full() && continueGame) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }

                    // restock shop before next day starts if not final day
                    if (currentDay < MAX_DAYS && continueGame) {
                        restockShop(sc);
                    }

                    currentDay++; // move to the next day
                }

                // final game outcome messages
                if (continueGame && currentDay > MAX_DAYS) { // survived all days (win)
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You survived the " + MAX_DAYS + " days! Well done.");
                    System.out.println("|-----------------------------------------------------------|");
                }

                // ask if player wants to play again
                askToPlayAgain(sc);

            } while (continueGame); // continue if player chose to play again
        }
        finally {
            // close scanner when done
            sc.close();
        }
    }


    // plays through a single day of customers

    private void playDay(Scanner sc) {
        int customers = 0;

        // customer loop - handle each customer until MAX_CUSTOMERS or game ends
        while (customers < MAX_CUSTOMERS && continueGame) {
            // display current status
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "You have " + (MAX_CUSTOMERS - customers) + " customers left to send.");
            System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
            System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicion.get_suspicion() + "%");
            System.out.println("|-----------------------------------------------------------|");

            // generate a random customer
            Customer customer = CustomerGenerator.generateRandomCustomer();

            // display customer information
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "CUSTOMER " + (customers + 1));
            System.out.printf("| %-57s |\n", customer.get_name());
            System.out.printf("| %-57s |\n", customer.get_dialogue());
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Where would you like to send the customer?");
            System.out.printf("| %-57s |\n", "1. Serve Ramen | 2. Send to Gambling Room");
            System.out.println("|-----------------------------------------------------------|");

            // get player choice
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // consume newline
            } catch (Exception e) {
                sc.nextLine(); // consume invalid input
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            System.out.println();

            // handle the player's choice and customer interaction
            boolean servedSuccessfully = handleCustomerChoice(choice, customer, sc);

            // exit day immediately if the game ended during customer handling
            if (!continueGame) {
                return; // Game ended, exit playDay
            }

            customers++;

            // check for max suspicion after customer interaction
            if (suspicion.sus_reached_full())
            {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
                return; // game ended, exit playDay
            }
        }

        // final suspicion check at end of day
        if (continueGame)
        {
            // cap suspicion at MAX_SUSPICION
            if (suspicion.get_suspicion() > suspicion.get_max_suspicion())
            {
                suspicion.set_suspicion(suspicion.get_max_suspicion());
            }

            // check if caught by cop at end of day
            if (suspicion.sus_reached_full())
            {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
            }
        }
    }

    // handles the player's choice for a customer interaction
    // player's choice (1: Serve Ramen, 2: Send to Gambling Room)
    // customer -> The current customer
    // boolean indicating if the customer was served successfully

    private boolean handleCustomerChoice(int choice, Customer customer, Scanner sc)
    {
        if (choice == 2)
        { // send to gambling room
            if (customer instanceof UndercoverCop)
            {
                // trigger special event for undercover cop in gambling room
                ((UndercoverCop) customer).trigger_special_event();

                // handle bribe interaction if player has enough money
                if (money.get_money() >= money.get_starting_money())
                {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "An undercover cop caught you!");
                    System.out.printf("| %-57s |\n", "You can bribe the cop for $100 to avoid arrest.");
                    System.out.printf("| %-57s |\n", "Do you want to bribe the cop? (1: yes / 2: no)");
                    System.out.println("|-----------------------------------------------------------|");

                    int decision;
                    try{
                        decision = sc.nextInt();
                        sc.nextLine(); // Consume newline
                    }
                    catch (Exception e)
                    {
                        sc.nextLine(); // consume invalid input
                        decision = 2; // default to not bribing
                        System.out.println("Invalid input for bribe. Assuming no bribe.");
                    }

                    if (decision == 1)
                    { // player chooses to bribe
                        money.change_money(-money.get_bribe());
                        suspicion.set_suspicion(0); // Bribe resets suspicion

                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You chose to bribe the cop. You lost $100.");
                        System.out.printf("| %-57s |\n", "Amount left: $" + money.get_money());
                        System.out.println("|-----------------------------------------------------------|");

                        // check if player is now bankrupt end the game if yes
                        if (money.get_money() <= money.get_no_money())
                        {
                            System.out.println("|-----------------------------------------------------------|");
                            System.out.printf("| %-57s |\n", "You paid the bribe, but now you're bankrupt!");
                            System.out.printf("| %-57s |\n", "Game Over.");
                            System.out.println("|-----------------------------------------------------------|");
                            continueGame = false;
                        }
                    }
                    else
                    { // player chooses not to bribe the undercover cop
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You chose not to bribe the cop. You got arrested.");
                        System.out.printf("| %-57s |\n", "Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                    }
                }
                else
                { // not enough money to bribe the cop
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "An undercover cop caught you!");
                    System.out.printf("| %-57s |\n", "You don't have enough money to bribe the cop.");
                    System.out.printf("| %-57s |\n", "You got arrested. Game Over.");
                    System.out.println("|-----------------------------------------------------------|");
                    continueGame = false;
                }
                return true; // customer interaction succeeded (despite possible game over)
            } else if (customer.is_gambler()) {
                // gambler sent to gambling room (correct)
                customer.right_room(suspicion);
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a gambler to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter decreased to " + suspicion.get_suspicion() + "%.");

                money.change_money(money.get_gamblercustomermoney()); // Earn money from gambling
                System.out.printf("| %-57s |\n", "Money earned from gambling: $30");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.println("|-----------------------------------------------------------|");
            } else {
                // normal customer sent to gambling room (incorrect)
                customer.wrong_room(suspicion);
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a regular customer to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter increased to " + suspicion.get_suspicion() + "%");
                System.out.println("|-----------------------------------------------------------|");
            }
            return true; // customer interaction succeeded
        } else if (choice == 1) { // serve ramen
            if (shop.serveRamen()) { // check if enough ingredients
                double payment = 0; // initialize payment variable
                if (customer instanceof UndercoverCop)
                {
                    payment = money.get_undercovercopmoney(); // get the specific cop payment amount
                    money.change_money(payment); // add cop's payment
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You served ramen to an undercover cop.");
                    System.out.printf("| %-57s |\n", "Money earned: $" + payment);
                }
                else
                {
                    // Get payment from regular customer or gambler
                    Money customerPayment = new Money();
                    customer.get_money(customerPayment);
                    payment = customerPayment.get_money();
                    money.change_money(payment);

                    // display results for non-cop customers
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You served ramen to the customer.");
                    System.out.printf("| %-57s |\n", "Money earned: $" + payment);
                }

                // adjustint suspicion based on customer type
                if (customer.is_gambler()) {
                    customer.wrong_room(suspicion); // sending gambler to ramen is wrong
                }
                else
                { // this includes UndercoverCop and normal customers
                    customer.right_room(suspicion); // sending normal customer/cop to ramen is right
                }

                // common display for current money and suspicion for all served customers
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.printf("| %-57s |\n", "Suspicion: " + suspicion.get_suspicion() + "%");
                System.out.println("|-----------------------------------------------------------|");
                return true; // successfully served ramen
            }
            else
            {
                // not enough ingredients
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Not enough ingredients to serve ramen!");
                System.out.printf("| %-57s |\n", "The customer left disappointed.");
                money.change_money(-15); // Lose $15
                System.out.printf("| %-57s |\n", "You lost $15.");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.println("|-----------------------------------------------------------|");
                restockShop(sc); // prompt to restock after failed service
                return false; // failed to serve ramen
            }
        }
        else
        { // invalid choice
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Invalid choice. Please enter 1 or 2.");
            System.out.println("|-----------------------------------------------------------|");
            return true; // customer interaction still succeeded (just invalid choice)
        }
    }

    // shop restocking between days is handles by this method
    private void restockShop(Scanner sc) {
        while (true) {
            System.out.printf("| %-57s |\n", "Current inventory:");
            shop.displayInventory();
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Would you like to restock the shop? (1: yes / 2: no)");
            System.out.println("|-----------------------------------------------------------|");

            int choice;
            // checking for input mismatch exception on nextInt
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                sc.nextLine();
                System.out.println("Invalid input. Please enter 1 or 2.");
                continue;
            }

            // restocking
            if (choice == 1)
            {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Restocking the shop...");
                System.out.println("|-----------------------------------------------------------|");

                // display current inventory
                shop.displayInventory();
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.printf("| %-57s |\n", "Which ingredient? (1-Noodles, 2-Broth, 3-Meat, 4-Toppings, 0-Exit): ");
                System.out.println("|-----------------------------------------------------------|");

                // input mismatch exception if user entered wrong input
                int ingredientChoice;
                try {
                    ingredientChoice = sc.nextInt();
                    sc.nextLine();
                } catch (Exception e) {
                    sc.nextLine();
                    System.out.println("Invalid ingredient choice.");
                    continue;
                }

                // allow exiting restock menu
                if (ingredientChoice == 0) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Exiting restock menu.");
                    System.out.println("|-----------------------------------------------------------|");
                    break;
                }

                // validate ingredient choice
                if (ingredientChoice < 1 || ingredientChoice > 4) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Invalid ingredient choice. Please enter 1-4 or 0 to exit.");
                    System.out.println("|-----------------------------------------------------------|");
                    continue;
                }

                int index = ingredientChoice - 1; // convert to 0-based index

                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "How many units?: ");
                System.out.println("|-----------------------------------------------------------|");

                int units;
                // catching input mismatch exception which can be thrown is user entered wrong input
                try {
                    units = sc.nextInt();
                    sc.nextLine();
                } catch (Exception e) {
                    sc.nextLine();
                    System.out.println("Invalid unit amount.");
                    continue;
                }

                // Validate units
                if (units <= 0) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Please enter a positive number of units.");
                    System.out.println("|-----------------------------------------------------------|");
                    continue;
                }

                // calculating cost and checking if affordable in conditionals below
                int cost = shop.getInventory().restockIngredient(index, units, (int)money.get_money());

                if (cost == -1 || cost > money.get_money()) // user has less money to buy the stuff they want
                {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Not enough money to restock this amount.");
                    System.out.println("|-----------------------------------------------------------|");
                }
                else if (cost == 0) // wrong input from user
                {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Could not restock. Invalid ingredient or amount.");
                    System.out.println("|-----------------------------------------------------------|");
                }
                else // successful restock
                {
                    money.set_money(money.get_money() - cost); // updating money of user
                    shop.getInventory().get_ingredient(index).addQuantity(units); // updating the inventory by adding the stuff user bought

                    String ingredientName = shop.getInventory().get_ingredient(index).get_name();
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Restocked " + units + " units of " + ingredientName + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                    System.out.println("|-----------------------------------------------------------|");
                }
            }
            else if(choice == 2) // not restocking
            {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You chose not to restock the shop.");
                System.out.println("|-----------------------------------------------------------|");
                break;
            }
            else // wrong input from user
            {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Invalid choice. Please enter 1 or 2.");
                System.out.println("|-----------------------------------------------------------|");
            }
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.startGame();
    }
}
