import java.lang.*;
import java.util.Random;
import java.util.Scanner;

//Three days game loop
//No of customers fix = 10
//No of gamblers or customers in wrong room -> suspicion meter raised
//At a certain suspicion point an undercover cop will come
//If undercover cop goes to gambling room -> bribe or shut down
//Make money, restock, dishes count will decrease if we send in the right customer
//If we send in the wrong customer -> suspicion meter raised
//If we send in the right customer -> suspicion meter decreased
//If we run out of ingredients -> restock
//If we run out of suspicion meter -> game over

class Game {
    private static final int MAX_CUSTOMERS = 10; // Maximum number of customers per day
    private static final int MAX_DAYS = 3; // Maximum number of days to play
    private static final int MAX_SUSPICION = 100; // Maximum suspicion meter value
    private boolean continueGame = true; // Flag to continue the game
    private double money = 100.0; // Starting money
    private int suspicionMeter = 0; // Starting suspicion meter value
    private int currentDay = 1; // Current day in the game
    
    private final Shop shop; // Assuming Shop is a class that manages the inventory and shop operations

    public Game() {
        Inventory inventory = new Inventory(4); // Assuming Inventory is a class that manages the ingredients
        inventory.add_ingredient(new Ingredient("Noodles", 10, 5), 0);
        inventory.add_ingredient(new Ingredient("Broth", 10, 4), 1);
        inventory.add_ingredient(new Ingredient("Meat", 10, 6), 2);
        inventory.add_ingredient(new Ingredient("Toppings", 10, 3), 3);
        shop = new Shop(inventory);
    }

    private void askToPlayAgain(Scanner sc) {
        System.out.println("Do you want to play again? (1: yes / 2: no)");
        int choice = sc.nextInt(); // Get user input for playing again
        if (choice == 1) {
            continueGame = true; // Set the flag to continue the game
            currentDay = 1; // Reset the day counter
            money = 100.0; // Reset the money
            suspicionMeter = 0; // Reset the suspicion meter
            startGame(); // Start a new game
        } else {
            continueGame = false; // Set the flag to stop the game
            System.out.println("Game Over! Thanks for playing!"); // End game
        }
    }

    public void startGame() {
        Scanner sc = new Scanner(System.in);
        System.out.println();
        System.out.println("---| RAMEN SHOP SIMULATOR |---");    

        while (currentDay <= MAX_DAYS  && continueGame) { 
            System.out.println("---| DAY " + currentDay + " |---");    
            playDay(); // Play a day in the game
            if (!continueGame) {
                break; // Exit the loop if the game is over
            }
            System.out.println();
            System.out.println("End of day " + currentDay + "."); // End of day message
            System.out.println("Current money: " + money);
            System.out.println("Suspicion meter: " + suspicionMeter);
            System.out.println();
            restockShop(sc); // Restock the shop at the end of each day
            System.out.println();
        }

        if (suspicionMeter >= 100) {
            System.out.println("You got caught and arrested. Game Over.");
        } 
        else {
            System.out.println("You survived the 3 days! Well done.");
        }
        askToPlayAgain(sc); // Ask the player if they want to play again
        sc.close(); // Close the scanner to avoid resource leaks    
    }
    private void playDay() {
        int customers = 0;
        Random random = new Random(); 
        Scanner scanner = new Scanner(System.in);

        while (customers < MAX_CUSTOMERS && continueGame) {
            System.out.println();
            System.out.println("You have " + (MAX_CUSTOMERS - customers) + " customers left to send.");
            System.out.println("Current money: " + money);
            System.out.println("Suspicion meter: " + suspicionMeter);
            Customer customer = CustomerGenerator.generateRandomCustomer(); // Generate a random customer
            System.out.println();
            System.out.println("Customer " + (customers + 1) + ": "); // Display customer dialogue
            System.out.println(customer.get_name()); // Display customer name
            System.out.println(customer.get_dialogue()); // Display customer dialogue
            System.out.println();
            System.out.println("Where would you like to send the customer?");
            System.out.println("1: Ramen Shop");
            System.out.println("2: Gambling Room");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            System.out.println();
            handleCustomerChoice(choice, customer, scanner); // Handle the customer's choice
            customers++; // Increment the number of customers sent

            // Cap suspicion meter at 100
            if (suspicionMeter > MAX_SUSPICION) {
                suspicionMeter = MAX_SUSPICION;
            }

            // Check for game-over condition
            if (suspicionMeter >= MAX_SUSPICION) {
                System.out.println("Suspicion meter is full. You got caught and arrested. Game Over.");
                continueGame = false;
                return;
            }
        }
        currentDay++; // Move to the next day
        suspicionMeter += 25; // Increase suspicion meter at the end of the day

        // Cap suspicion meter at 100
        if (suspicionMeter > MAX_SUSPICION) {
            suspicionMeter = MAX_SUSPICION;
        }
    }

    private void handleCustomerChoice(int choice, Customer customer, Scanner sc) {
        if (choice == 2) {
            if (customer instanceof UndercoverCop) {
                ((UndercoverCop) customer).trigger_special_event();
                if (money >= 100) {
                    System.out.println("An undercover cop caught you! You can bribe the cop for $100 to avoid arrest.");
                    System.out.println("Do you want to bribe the cop? (1: yes / 2: no)");
                    int decision = sc.nextInt();
                    if (decision == 1) {
                        System.out.println("You chose to bribe the cop. You lost $100.");
                        money -= 100;
                        System.out.println("Amount left: " + money);
                        suspicionMeter = 0; // Reset suspicion meter after bribing
                    } else {
                        System.out.println("You chose not to bribe the cop. You got arrested. Game Over.");
                        continueGame = false;
                        return;
                    }
                } else {
                    System.out.println("An undercover cop caught you! You don't have enough money to bribe the cop.");
                    System.out.println("You got arrested. Game Over.");
                    continueGame = false;
                    return; // End the game
                }
            } else if (customer.is_gambler()) {
                System.out.println("You sent a gambler to the gambling room. Suspicion meter decreased.");
                if (suspicionMeter > 10)
                    suspicionMeter -= 10;
                money += 30; // Add 30 money when a gambler is sent to the gambling room
                System.out.println("Money earned from gambling: 30");
                System.out.println("Current money: " + money);
            } else {
                System.out.println("You sent a regular customer to the gambling room. Suspicion meter increased.");
                suspicionMeter += 20;

                // Cap suspicion meter at 100
                if (suspicionMeter > MAX_SUSPICION) {
                    suspicionMeter = MAX_SUSPICION;
                }
            }
        } else {
            if (shop.serveRamen()) {
                money += customer.get_money();
                System.out.println("You served ramen to the customer. Money earned: " + customer.get_money());
                System.out.println("Current money: " + money);

                // Decrease inventory when serving ramen
                shop.decreaseInventory();

                if (suspicionMeter > 2)
                    suspicionMeter -= 2; // Decrease suspicion meter for serving ramen
            } else {
                System.out.println("You ran out of ingredients.");
                System.out.println("You need to restock the shop.");
                restockShop(sc); // Restock the shop if ingredients are low
            }
        }
    }

    private void restockShop(Scanner sc) {
        while (true) {
            System.out.println("Would you like to restock the shop? (1: yes / 2: no)");
            int choice = sc.nextInt();
            if (choice == 1) {
                System.out.println("Restocking the shop...");
                shop.displayInventory(); // Display the current inventory
                System.out.println("Current money: " + money);
                System.out.println("Which ingredient? (1-Noodles, 2-Broth, 3-Meat, 4-Toppings): ");
                int index = sc.nextInt() - 1;
                System.out.println("How many units?: ");
                int units = sc.nextInt();
                int cost = shop.getInventory().restockIngredient(index, units, (int) money);
                if (cost > money) {
                    System.out.println("Not enough money to restock this amount.");
                } else {
                    money -= cost;
                    System.out.println("Restocked the shop. Current money: " + money);
                }
            } else if (choice == 2) {
                System.out.println("You chose not to restock the shop.");
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
}
public class RamenShop{
    //click on Run instead of Run main
    public static void main(String[] args) {
        Game game = new Game(); // Create a new game instance
        game.startGame(); // Start the game
    }
}
