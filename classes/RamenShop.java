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
    private Money money = new Money(); // start from 0
    private Suspicion suspicion = new Suspicion();
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
            money.set_money(money.get_starting_money()); // Reset the money
            suspicion.set_suspicion(0); // Reset the suspicion meter
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
            System.out.println("Current money: " + money.get_money());
            System.out.println("Suspicion meter: " + suspicion.get_suspicion());
            System.out.println();
            restockShop(sc); // Restock the shop at the end of each day
            System.out.println();
        }
        // suspicion level check
        if ((suspicion.sus_reached_full() == true) || (continueGame == false)) {
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

        while ((customers < MAX_CUSTOMERS) && continueGame) {
            System.out.println();
            System.out.println("You have " + (MAX_CUSTOMERS - customers) + " customers left to send.");
            System.out.println("Current money: " + money.get_money());
            System.out.println("Suspicion meter: " + suspicion.get_suspicion());
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

            // Check for game-over condition
            if (suspicion.get_suspicion() >= MAX_SUSPICION) {
                System.out.println("Suspicion meter is full. You got caught and arrested. Game Over.");
                continueGame = false;
                return;
            }
        }
        currentDay++; // Move to the next day
        suspicion.suspicion_newday(currentDay); // Increase suspicion meter at the end of the day

    }

    private void handleCustomerChoice(int choice, Customer customer, Scanner sc) {
        if (choice == 2) // sent to gambler room
        {
            if (customer instanceof UndercovercopMarker) // if undercover cop start sequence of bribing or no bribing // using marker
            {
                ((UndercoverCop) customer).trigger_special_event();
                if (money.get_money() >= money.get_bribe()) // if user has enough money to bribe
                {
                    System.out.println("An undercover cop caught you! You can bribe the cop for $100 to avoid arrest.");
                    System.out.println("Do you want to bribe the cop? (1: yes / 2: no)");
                    int decision = sc.nextInt();
                    if (decision == 1)
                    {
                        System.out.println("You chose to bribe the cop. You lost $100.");
                        ((UndercoverCop) customer).bribing(money);
                        System.out.println("Amount left: " + money.get_money());
                        suspicion.set_suspicion(0); // Reset suspicion meter after bribing
                    }
                    else
                    {
                        System.out.println("You chose not to bribe the cop. You got arrested. Game Over.");
                        continueGame = false;
                        return;
                    }
                }
                else
                {
                    System.out.println("An undercover cop caught you! You don't have enough money to bribe the cop.");
                    System.out.println("You got arrested. Game Over.");
                    continueGame = false;
                    return; // End the game
                }
            } // if gambler sent then right room
            else if (customer.is_gambler())
            {
                System.out.println("You sent a gambler to the gambling room. Suspicion meter decreased.");
                    ((GamblerCustomer) customer).right_room(suspicion);
                    ((GamblerCustomer) customer).get_money(money); // Add extra money when a gambler is sent to the gambling room
                    System.out.println("Money earned from gambling: " + money.get_gamblercustomermoney());
                    System.out.println("Current money: " + money.get_money());
                }
            else // normal customer sent to gambling room
                {
                    System.out.println("You sent a regular customer to the gambling room. Suspicion meter increased.");
                    ((NormalCustomer) customer).wrong_room(suspicion);
                }
        }
        else // if choice 1 (sent to ramen room)
        {
            if (customer.is_gambler()) // if gambler sent to ramen room
            {
                System.out.println("You sent a gambler to the ramen bar. Suspicion meter increased.");
                ((GamblerCustomer) customer).wrong_room(suspicion);
                return; // gambler will not be served ramen only increase suspicion
            }
            if (shop.serveRamen()) // serveRamen method also decreases ingredients
            {
                if(customer instanceof NormalCustomer) // if normal customer sent to ramen room
                {
                    ((NormalCustomer) customer).get_money(money);
                    System.out.println("You served ramen to the customer. Money earned: " + money.get_normalcustomermoney());
                    System.out.println("Current money: " + money.get_money());
                    ((NormalCustomer)customer).right_room(suspicion);
                }
                else if(customer instanceof UndercovercopMarker) // if cop sent to ramen room // using marker class
                {
                    ((UndercoverCop) customer).get_money(money);
                    System.out.println("You served ramen to an UNDERCOVER COP. Money earned: " + money.get_normalcustomermoney());
                    ((UndercoverCop)customer).right_room(suspicion);
                }
            }
            else
            {
                System.out.println("You ran out of ingredients.");
                System.out.println("You need to restock the shop.");
                restockShop(sc); // Restock the shop if ingredients are low

                // handling bug where user can not serve a customer and not be penalized
                // after shop is restocked we again check if the user can now serve the customer , if he still cannot we penalize
                if (shop.serveRamen()) {
                    if(customer instanceof NormalCustomer) {
                        ((NormalCustomer) customer).get_money(money);
                        System.out.println("You served ramen to the customer. Money earned: " + money.get_normalcustomermoney());
                        System.out.println("Current money: " + money.get_money());
                        ((NormalCustomer) customer).right_room(suspicion);
                    }
                    else if(customer instanceof UndercovercopMarker) {
                        ((UndercoverCop) customer).get_money(money);
                        System.out.println("You served ramen to an UNDERCOVER COP. Money earned: " + money.get_normalcustomermoney());
                        ((UndercoverCop) customer).right_room(suspicion);
                    }
                } else {
                    // Player didn't restock or still can't serve
                    System.out.println("Ingredients still not enough.");
                    System.out.println("Customer left disappointed. Suspicion increased.");
                    suspicion.change_suspicion(suspicion.get_customerleftsusincrease());
                }
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
                System.out.println("Current money: " + money.get_money());
                System.out.println("Which ingredient? (1-Noodles, 2-Broth, 3-Meat, 4-Toppings): ");
                int index = sc.nextInt() - 1;
                System.out.println("How many units?: ");
                int units = sc.nextInt();
                int cost = shop.getInventory().restockIngredient(index, units, (int) money.get_money());
                if (cost > money.get_money()) {
                    System.out.println("Not enough money to restock this amount.");
                } else {
                    money.change_money(-cost);
                    System.out.println("Restocked the shop. Current money: " + money.get_money());
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

public class Main{
    //click on Run instead of Run main
    public static void main(String[] args) {
        Game game = new Game(); // Create a new game instance
        game.startGame(); // Start the game
    }
}
