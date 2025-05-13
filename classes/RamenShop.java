import java.lang.*;
import java.util.Scanner;

/**
 * Main game class for the Ramen Shop Simulator
 * Manages the game state, shop operations, and game loop
 */
class Game {
    private static final int MAX_CUSTOMERS = 10; // Maximum number of customers per day
    private static final int MAX_DAYS = 3; // Maximum number of days to play
    private static final int MAX_SUSPICION = 100; // Maximum suspicion meter value
    private boolean continueGame = true; // Flag to continue the game
    private double money = 100.0; // Starting money
    private int suspicionMeter = 0; // Starting suspicion meter value
    private int currentDay = 1; // Current day in the game
    
    private final Shop shop; // Shop object that manages inventory and operations
    private final Suspicion suspicion; // Using the Suspicion wrapper class from Main.java

    /* Constructor - initializes a new game with fresh inventory and suspicion */
    public Game() {
        // Initialize the shop with a new inventory
        Inventory inventory = new Inventory(4);
        inventory.add_ingredient(new Ingredient("Noodles", 10, 5), 0);
        inventory.add_ingredient(new Ingredient("Broth", 10, 4), 1);
        inventory.add_ingredient(new Ingredient("Meat", 10, 6), 2);
        inventory.add_ingredient(new Ingredient("Toppings", 10, 3), 3);
        shop = new Shop(inventory);
        suspicion = new Suspicion(); // Initialize suspicion object
    }

    /**
     * Reset the game state for playing again without creating a new Game instance
     */
    private void resetGameForPlayAgain() {
        // Reset all game state variables
        continueGame = true;
        currentDay = 1;
        money = 100.0;
        suspicionMeter = 0;
        suspicion.set_suspicion(0);
        
        // Reset the shop's inventory using resetInventory instead of creating a new inventory
        shop.getInventory().resetInventory();
    }

    /**
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

    /**
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
                    suspicionMeter = suspicion.get_suspicion();
                    if (suspicionMeter >= MAX_SUSPICION) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }
                    
                    // Play the day and see if the shop closed early due to lack of ingredients
                    boolean shopClosedEarly = playDay(sc);
                    
                    if (!continueGame) break; // Stop if game ended during the day (arrest, etc.)
                    
                    // End of day summary
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "End of day " + currentDay + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money);
                    System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicionMeter + "%");
                    System.out.println("|-----------------------------------------------------------|");
                    
                    // Apply suspicion increase for the next day
                    suspicion.suspicion_newday(currentDay);
                    suspicionMeter = suspicion.get_suspicion();
                    
                    // Check if suspicion reached max after day end increase
                    if (suspicionMeter >= MAX_SUSPICION && continueGame) {
                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                        System.out.println("|-----------------------------------------------------------|");
                        continueGame = false;
                        break;
                    }
                    
                    // Restock shop before next day starts if not final day
                    if (currentDay < MAX_DAYS && continueGame) {
                        // Show message if shop closed early
                        if (shopClosedEarly) {
                            System.out.println("|-----------------------------------------------------------|");
                            System.out.printf("| %-57s |\n", "The shop closed early today due to lack of ingredients.");
                            System.out.println("|-----------------------------------------------------------|");
                        }
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
        } finally {
            // Close scanner when done
            sc.close();
        }
    }
    
    /**
     * Plays through a single day of customers
     * @param sc Scanner for user input
     * @return boolean indicating if the shop closed early due to lack of ingredients
     */
    private boolean playDay(Scanner sc) {
        int customers = 0;
        boolean shopClosedEarly = false; // Track if shop closes early due to lack of ingredients

        // Customer loop - handle each customer until MAX_CUSTOMERS or game ends
        while (customers < MAX_CUSTOMERS && continueGame) {
            // Display current status
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "You have " + (MAX_CUSTOMERS - customers) + " customers left to send.");
            System.out.printf("| %-57s |\n", "Current money: $" + money);
            System.out.printf("| %-57s |\n", "Suspicion meter: " + suspicionMeter + "%");
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
            
            // If player tried to serve ramen but ran out of ingredients, close the shop early
            if (choice == 1 && !servedSuccessfully) {
                shopClosedEarly = true; // Set the flag
                break; // Exit customer loop
            }

            // Exit day immediately if the game ended during customer handling
            if (!continueGame) {
                return shopClosedEarly;
            }
            
            customers++;

            // Check for max suspicion after customer interaction
            suspicionMeter = suspicion.get_suspicion();
            if (suspicionMeter >= MAX_SUSPICION) {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
                return shopClosedEarly;
            }
        }

        // Display message if shop closed early
        if (shopClosedEarly) {
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Shop closed for the day due to lack of ingredients!");
            System.out.println("|-----------------------------------------------------------|");
        }
        
        // Final suspicion check at end of day (if shop didn't close early)
        if (continueGame && !shopClosedEarly) {
            suspicionMeter = suspicion.get_suspicion();
            
            // Cap suspicion at MAX_SUSPICION
            if (suspicionMeter > MAX_SUSPICION) {
                suspicionMeter = MAX_SUSPICION;
            }
            suspicion.set_suspicion(suspicionMeter);

            // Check if caught by cop at end of day
            if (suspicionMeter >= MAX_SUSPICION) {
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You got caught by undercover cop. Game Over.");
                System.out.println("|-----------------------------------------------------------|");
                continueGame = false;
            }
        }
        
        return shopClosedEarly;
    }

    /**
     * Handles the player's choice for a customer interaction
     * @param choice Player's choice (1: Serve Ramen, 2: Send to Gambling Room)
     * @param customer The current customer
     * @param sc Scanner for user input
     * @return boolean indicating if the customer was served successfully
     */
    private boolean handleCustomerChoice(int choice, Customer customer, Scanner sc) {
        if (choice == 2) { // Send to gambling room
            if (customer instanceof UndercoverCop) {
                // Trigger special event for undercover cop in gambling room
                ((UndercoverCop) customer).trigger_special_event();
                
                // Handle bribe interaction if player has enough money
                if (money >= 100) {
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "An undercover cop caught you!");
                    System.out.printf("| %-57s |\n", "You can bribe the cop for $100 to avoid arrest.");
                    System.out.printf("| %-57s |\n", "Do you want to bribe the cop? (1: yes / 2: no)");
                    System.out.println("|-----------------------------------------------------------|");
                    
                    int decision;
                    try {
                        decision = sc.nextInt();
                        sc.nextLine(); // Consume newline
                    } catch (Exception e) {
                        sc.nextLine(); // Consume invalid input
                        decision = 2; // Default to not bribing
                        System.out.println("Invalid input for bribe. Assuming no bribe.");
                    }

                    if (decision == 1) { // Player chooses to bribe
                        money -= 100;
                        suspicion.set_suspicion(0); // Bribe resets suspicion
                        suspicionMeter = suspicion.get_suspicion();

                        System.out.println("|-----------------------------------------------------------|");
                        System.out.printf("| %-57s |\n", "You chose to bribe the cop. You lost $100.");
                        System.out.printf("| %-57s |\n", "Amount left: $" + money);
                        System.out.println("|-----------------------------------------------------------|");

                        // Check if player is now bankrupt
                        if (money <= 0) {
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
            } else if (customer.is_gambler()) { // Gambler sent to gambling room (correct)
                customer.right_room(suspicion);
                suspicionMeter = suspicion.get_suspicion();
                
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a gambler to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter increased to " + suspicionMeter + "%.");
                
                money += 30; // Earn money from gambling
                System.out.printf("| %-57s |\n", "Money earned from gambling: $30");
                System.out.printf("| %-57s |\n", "Current money: $" + money);
                System.out.println("|-----------------------------------------------------------|");
            } else { // Normal customer sent to gambling room (incorrect)
                customer.wrong_room(suspicion);
                suspicionMeter = suspicion.get_suspicion();
                
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You sent a regular customer to the gambling room.");
                System.out.printf("| %-57s |\n", "Suspicion meter increased to " + suspicionMeter + "%");
                System.out.println("|-----------------------------------------------------------|");
            }
            return true; // Customer interaction succeeded
        } else if (choice == 1) { // Serve ramen
            if (shop.serveRamen()) { // Check if enough ingredients
                // Get payment from customer
                Money customerPayment = new Money();
                customer.get_money(customerPayment);
                double payment = customerPayment.get_money();
                money += payment;
                
                // Adjust suspicion based on customer type
                if (customer.is_gambler()) {
                    customer.wrong_room(suspicion); // Sending gambler to ramen is wrong
                } else {
                    customer.right_room(suspicion); // Sending normal customer to ramen is right
                }
                suspicionMeter = suspicion.get_suspicion();
                
                // Display results
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You served ramen to the customer.");
                System.out.printf("| %-57s |\n", "Money earned: $" + payment);
                System.out.printf("| %-57s |\n", "Current money: $" + money);
                System.out.printf("| %-57s |\n", "Suspicion: " + suspicionMeter + "%");
                System.out.println("|-----------------------------------------------------------|");
                return true; // Successfully served ramen
            } else { // Not enough ingredients
                System.out.println("|-----------------------------------------------------------|");
                System.out.printf("| %-57s |\n", "You ran out of ingredients!");
                System.out.println("|-----------------------------------------------------------|");
                
                return false; // Failed to serve ramen (shop will close early)
            }
        } else { // Invalid choice
            System.out.println("|-----------------------------------------------------------|");
            System.out.printf("| %-57s |\n", "Invalid choice. Please enter 1 or 2.");
            System.out.println("|-----------------------------------------------------------|");
            return true; // Customer interaction still succeeded (just invalid choice)
        }
    }

    /**
     * Handles the shop restocking process between days
     * @param sc Scanner for user input
     */
    private void restockShop(Scanner sc) {
        while (true) {
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
                System.out.printf("| %-57s |\n", "Current money: $" + money);
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
                int cost = shop.getInventory().restockIngredient(index, units, (int)money);
                
                if (cost == -1 || cost > money) { // Not enough money
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Not enough money to restock this amount.");
                    System.out.println("|-----------------------------------------------------------|");
                } else if (cost == 0) { // Error or invalid ingredient
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Could not restock. Invalid ingredient or amount.");
                    System.out.println("|-----------------------------------------------------------|");
                } else { // Successful restock
                    money -= cost;
                    shop.getInventory().get_ingredient(index).addQuantity(units); // Add units to inventory
                    
                    String ingredientName = shop.getInventory().get_ingredient(index).get_name();
                    System.out.println("|-----------------------------------------------------------|");
                    System.out.printf("| %-57s |\n", "Restocked " + units + " units of " + ingredientName + ".");
                    System.out.printf("| %-57s |\n", "Current money: $" + money);
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

/**
 * Main class with entry point for the Ramen Shop Simulator
 */
public class RamenShop {
    public static void main(String[] args) {
        Game game = new Game();
        game.startGame();
    }
}
