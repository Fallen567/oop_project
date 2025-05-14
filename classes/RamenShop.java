import java.lang.*;
import java.util.Scanner;

/* Main game class for the Ramen Shop Simulator
 * Manages the game state, shop operations, and game loop
 */

class Game {
    private static final int MAX_CUSTOMERS = 10; // Maximum number of customers per day
    private static final int MAX_DAYS = 3; // Maximum number of days to play
    private boolean continueGame = true; // Flag to continue the game
    private Money money = new Money(); // Using Money object instead of primitive
    private int currentDay = 1; // Current day in the game
    
    private final Shop shop; // Shop object that manages inventory and operations
    private final Suspicion suspicion; // Using the Suspicion wrapper class from Main.java

    /* 
    Constructor - initializes a new game with fresh inventory and suspicion
    */
    public Game() {
        // Initialize money with starting amount
        money.set_money(money.get_starting_money()); // FIXED: Set the initial amount correctly
        
        // Initialize the shop with a new inventory
        Inventory inventory = new Inventory(4);
        inventory.add_ingredient(new Ingredient("Noodles", 10, 5), 0);
        inventory.add_ingredient(new Ingredient("Broth", 10, 4), 1);
        inventory.add_ingredient(new Ingredient("Meat", 10, 6), 2);
        inventory.add_ingredient(new Ingredient("Toppings", 10, 3), 3);
        shop = new Shop(inventory);
        
        // Initialize suspicion with zero value
        suspicion = new Suspicion();
    }

    /*
     * Reset the game state for playing again without creating a new Game instance
     */
    private void resetGameForPlayAgain() {
        // Reset all game state variables
        continueGame = true;
        currentDay = 1;
        money.set_money(money.get_starting_money()); // Reset money to starting amount
        suspicion.set_suspicion(0);
        
        // Reset the shop's inventory using resetInventory instead of creating a new inventory
        shop.getInventory().resetInventory();
    }

    /*
     * Asks the player if they want to play again and handles the response
     * @param sc Scanner for user input
     */
    private void askToPlayAgain(Scanner sc) {
        System.out.println("|-----------------------------------------------------------|");
        System.out.printf("| %-57s |\n", "Do you want to play again? (1: yes / 2: no)");
        System.out.println("|-----------------------------------------------------------|");
        
        int choice;
        try {
            choice = sc.nextInt();
            sc.nextLine(); // Consume the newline left after nextInt()
        } catch (Exception e) {
            sc.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Exiting the game.");
            continueGame = false;
            return;
        }
        
        if (choice == 1) {
            // Reset game state for a new game
            resetGameForPlayAgain();
            // No recursive startGame() call - we'll use a loop in the main method
        } else {
            continueGame = false;
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Game Over! Thanks for playing!");
            System.out.println("|-----------------------------------------------------------|");
        }
    }

    /*
     * The main game loop - runs through all days until the game ends
     */
    public void startGame() {
        Scanner sc = new Scanner(System.in);
        
        try {
            do { // Outer loop to handle playing multiple games
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "RAMEN SHOP SIMULATOR");
                System.out.println("|-----------------------------------------------------------|");
                
                // Day loop - play through each day until MAX_DAYS is reached or game ends
                while (currentDay <= MAX_DAYS && continueGame) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "DAY " + currentDay + " OF " + MAX_DAYS);
                    System.out.println("|-----------------------------------------------------------|");
                    
                    // Check suspicion level at the start of each day
                    if (suspicion.sus_reached_full()) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }
                    
                    // Play the day
                    playDay(sc);
                    
                    if (!continueGame) break; // Stop if game ended during the day (arrest, etc.)
                    
                    // End of day summary
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "End of day " + currentDay + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                    System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicion.get_suspicion() + "%");
                    System.out.println("|-----------------------------------------------------------|");
                    
                    // Apply suspicion increase for the next day
                    suspicion.suspicion_newday(currentDay);
                    
                    // Check if suspicion reached max after day end increase
                    if (suspicion.sus_reached_full() && continueGame) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }
                    
                    // Restock shop before next day starts if not final day
                    if (currentDay < MAX_DAYS && continueGame) {
                        restockShop(sc);
                    }
                    
                    currentDay++; // Move to the next day
                }
                
                // Final game outcome messages
                if (continueGame && currentDay > MAX_DAYS) { // Survived all days (win)
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You survived the " + MAX_DAYS + " days! Well done.");
                    System.out.println("|-----------------------------------------------------------|");
                }
                
                // Ask if player wants to play again
                askToPlayAgain(sc);
                
            } while (continueGame); // Continue if player chose to play again
        } 
        finally {
            // Close scanner when done
            sc.close();
        }
    }
    
    /*
     * Plays through a single day of customers
     */
    private void playDay(Scanner sc) {
        int customers = 0;

        // Customer loop - handle each customer until MAX_CUSTOMERS or game ends
        while (customers < MAX_CUSTOMERS && continueGame) {
            // Display current status
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "You have " + (MAX_CUSTOMERS - customers) + " customers left to send.");
            System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
            System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicion.get_suspicion() + "%");
            System.out.println("|-----------------------------------------------------------|");

            // Generate a random customer
            Customer customer = CustomerGenerator.generateRandomCustomer();

            // Display customer information
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "CUSTOMER " + (customers + 1));
            System.out.printf("| %-57s |\n", customer.get_name());
            System.out.printf("| %-57s |\n", customer.get_dialogue());
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Where would you like to send the customer?");
            System.out.printf("| %-57s |\n", "1. Serve Ramen | 2. Send to Gambling Room");
            System.out.println("|-----------------------------------------------------------|");

            // Get player choice
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (Exception e) {
                sc.nextLine(); // Consume invalid input
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            System.out.println();

            // Handle the player's choice and customer interaction
            boolean servedSuccessfully = handleCustomerChoice(choice, customer, sc);

            // Exit day immediately if the game ended during customer handling
            if (!continueGame) {
                return; // Game ended, exit playDay
            }
            
            customers++;

            // Check for max suspicion after customer interaction
            if (suspicion.sus_reached_full()) {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
                return; // Game ended, exit playDay
            }
        }
        
        // Final suspicion check at end of day
        if (continueGame) {
            // Cap suspicion at MAX_SUSPICION
            if (suspicion.get_suspicion() > suspicion.get_max_suspicion()) {
                suspicion.set_suspicion(suspicion.get_max_suspicion());
            }

            // Check if caught by cop at end of day
            if (suspicion.sus_reached_full()) {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
            }
        }
    }

    /* Handles the player's choice for a customer interaction
     * Player's choice (1: Serve Ramen, 2: Send to Gambling Room)
     * customer -> The current customer
     * boolean indicating if the customer was served successfully
     */

    private boolean handleCustomerChoice(int choice, Customer customer, Scanner sc) {
        if (choice == 2) { // Send to gambling room
            if (customer instanceof UndercoverCop) {
                // Trigger special event for undercover cop in gambling room
                ((UndercoverCop) customer).trigger_special_event();
                
                // Handle bribe interaction if player has enough money
                if (money.get_money() >= money.get_starting_money()) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "An undercover cop caught you!");
                    System.out.printf("| %-57s |\n", "You can bribe the cop for $100 to avoid arrest.");
                    System.out.printf("| %-57s |\n", "Do you want to bribe the cop? (1: yes / 2: no)");
                    System.out.println("|-----------------------------------------------------------|");
                    
                    int decision;
                    try {
                        decision = sc.nextInt();
                        sc.nextLine(); // Consume newline
                    } 
                    catch (Exception e) {
                        sc.nextLine(); // Consume invalid input
                        decision = 2; // Default to not bribing
                        System.out.println("Invalid input for bribe. Assuming no bribe.");
                    }

                    if (decision == 1) { // Player chooses to bribe
                        money.change_money(-money.get_bribe());
                        suspicion.set_suspicion(0); // Bribe resets suspicion

                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You chose to bribe the cop. You lost $100.");
                        System.out.printf("| %-57s |\n", "Amount left: $" + money.get_money());
                        System.out.println("|-----------------------------------------------------------|");

                        // Check if player is now bankrupt
                        if (money.get_money() <= money.get_no_money()) {
                            System.out.println("|-----------------------------------------------------------|");
                            System.out.printf("| %-57s |\n", "You paid the bribe, but now you're bankrupt!");
                            System.out.printf("| %-57s |\n", "Game Over.");
                            System.out.println("|-----------------------------------------------------------|");
                            continueGame = false;
                        }
                    } else { // Player chooses not to bribe
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You chose not to bribe the cop. You got arrested.");
                        System.out.printf("| %-57s |\n", "Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                    }
                } else { // Not enough money to bribe
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "An undercover cop caught you!");
                    System.out.printf("| %-57s |\n", "You don't have enough money to bribe the cop.");
                    System.out.printf("| %-57s |\n", "You got arrested. Game Over.");
                    System.out.println("|-----------------------------------------------------------|");
                    continueGame = false;
                }
                return true; // Customer interaction succeeded (despite possible game over)
            } else if (customer.is_gambler()) { 
                // Gambler sent to gambling room (correct)
                customer.right_room(suspicion);
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a gambler to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter decreased to " + suspicion.get_suspicion() + "%.");
                
                money.change_money(money.get_gamblercustomermoney()); // Earn money from gambling
                System.out.printf("| %-57s |\n", "Money earned from gambling: $30");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.println("|-----------------------------------------------------------|");
            } else { 
                // Normal customer sent to gambling room (incorrect)
                customer.wrong_room(suspicion);
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a regular customer to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter increased to " + suspicion.get_suspicion() + "%");
                System.out.println("|-----------------------------------------------------------|");
            }
            return true; // Customer interaction succeeded
        } else if (choice == 1) { // Serve ramen
            if (shop.serveRamen()) { // Check if enough ingredients
                double payment = 0; // Initialize payment variable
                if (customer instanceof UndercoverCop) {
                    payment = money.get_undercovercopmoney(); // Get the specific cop payment amount
                    money.change_money(payment); // Add cop's payment
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You served ramen to an undercover cop.");
                    System.out.printf("| %-57s |\n", "Money earned: $" + payment);
                } else {
                    // Get payment from regular customer or gambler
                    Money customerPayment = new Money();
                    customer.get_money(customerPayment);
                    payment = customerPayment.get_money();
                    money.change_money(payment);

                    // Display results for non-cop customers
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "You served ramen to the customer.");
                    System.out.printf("| %-57s |\n", "Money earned: $" + payment);
                }
 
                // Adjust suspicion based on customer type
                if (customer.is_gambler()) {
                    customer.wrong_room(suspicion); // Sending gambler to ramen is wrong
                } else { // This includes UndercoverCop and normal customers
                    customer.right_room(suspicion); // Sending normal customer/cop to ramen is right
                }
                
                // Common display for current money and suspicion for all served customers
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.printf("| %-57s |\n", "Suspicion: " + suspicion.get_suspicion() + "%");
                System.out.println("|-----------------------------------------------------------|");
                return true; // Successfully served ramen
            } else { 
                // Not enough ingredients
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Not enough ingredients to serve ramen!");
                System.out.printf("| %-57s |\n", "The customer left disappointed.");
                money.change_money(-15); // Lose $15
                System.out.printf("| %-57s |\n", "You lost $15.");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.println("|-----------------------------------------------------------|");
                restockShop(sc); // Prompt to restock after failed service
                return false; // Failed to serve ramen
            }
        } else { // Invalid choice
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Invalid choice. Please enter 1 or 2.");
            System.out.println("|-----------------------------------------------------------|");
            return true; // Customer interaction still succeeded (just invalid choice)
        }
    }

    /*
     * Handles the shop restocking process between days
     */
    private void restockShop(Scanner sc) {
        while (true) {
            System.out.printf("| %-57s |\n", "Current inventory:");
            shop.displayInventory();
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Would you like to restock the shop? (1: yes / 2: no)");
            System.out.println("|-----------------------------------------------------------|");
            
            int choice;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (Exception e) {
                sc.nextLine(); // Consume invalid input
                System.out.println("Invalid input. Please enter 1 or 2.");
                continue;
            }

            if (choice == 1) { // Player wants to restock
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Restocking the shop...");
                System.out.println("|-----------------------------------------------------------|");
                
                // Display current inventory
                shop.displayInventory();
                
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                System.out.printf("| %-57s |\n", "Which ingredient? (1-Noodles, 2-Broth, 3-Meat, 4-Toppings, 0-Exit): ");
                System.out.println("|-----------------------------------------------------------|");
                
                int ingredientChoice;
                try {
                    ingredientChoice = sc.nextInt();
                    sc.nextLine(); // Consume newline
                } catch (Exception e) {
                    sc.nextLine(); // Consume invalid input
                    System.out.println("Invalid ingredient choice.");
                    continue;
                }
                
                // Allow exiting restock menu
                if (ingredientChoice == 0) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Exiting restock menu.");
                    System.out.println("|-----------------------------------------------------------|");
                    break;
                }
                
                // Validate ingredient choice
                if (ingredientChoice < 1 || ingredientChoice > 4) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Invalid ingredient choice. Please enter 1-4 or 0 to exit.");
                    System.out.println("|-----------------------------------------------------------|");
                    continue;
                }
                
                int index = ingredientChoice - 1; // Convert to 0-based index
                
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "How many units?: ");
                System.out.println("|-----------------------------------------------------------|");
                
                int units;
                try {
                    units = sc.nextInt();
                    sc.nextLine(); // Consume newline
                } catch (Exception e) {
                    sc.nextLine(); // Consume invalid input
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
                
                // Calculate cost and check if affordable
                int cost = shop.getInventory().restockIngredient(index, units, (int)money.get_money());
                
                if (cost == -1 || cost > money.get_money()) { // Not enough money
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Not enough money to restock this amount.");
                    System.out.println("|-----------------------------------------------------------|");
                } else if (cost == 0) { // Error or invalid ingredient
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Could not restock. Invalid ingredient or amount.");
                    System.out.println("|-----------------------------------------------------------|");
                } else { // Successful restock
                    money.set_money(money.get_money() - cost);
                    shop.getInventory().get_ingredient(index).addQuantity(units); // Add units to inventory
                    
                    String ingredientName = shop.getInventory().get_ingredient(index).get_name();
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Restocked " + units + " units of " + ingredientName + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money.get_money());
                    System.out.println("|-----------------------------------------------------------|");
                }
            } else if (choice == 2) { // Player doesn't want to restock
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You chose not to restock the shop.");
                System.out.println("|-----------------------------------------------------------|");
                break;
            } else { // Invalid choice
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "Invalid choice. Please enter 1 or 2.");
                System.out.println("|-----------------------------------------------------------|");
            }
        }
    }
}

/*
 * Main class with entry point for the Ramen Shop Simulator
 */
public class RamenShop {
    public static void main(String[] args) {
        Game game = new Game();
        game.startGame();
    }
}
