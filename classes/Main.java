import java.lang.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

// print statements for testing , need to remove after gui done

interface UndercovercopMarker {} // marker interface for undercovercop

// this class is used to read line by line from txt file and store the names/dialogues in a list
class FileReader {
    // the static method returns an ARRAYLIST of strings filled with lines from the txt file we provide
    public static ArrayList<String> read_from_file(String filename) {
        try {
            return new ArrayList<>(Files.readAllLines(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename);
            ArrayList<String> fallback = new ArrayList<>();
            fallback.add("Unknown");
            return fallback;
        }
    }
}

abstract class Customer {
    // protected so we can access these in subclasses without the getters
    protected String name;
    protected String dialogue;

    // each subclass constructor will randomly choose a name and dialogue from txt file and pass it into the superclass Customer constructor
    public Customer(String name, String dialogue) // no need of no arg constructor if we are not gonna call it in subclass
    {
        this.name = name;
        this.dialogue = dialogue;
    }
    public String get_name() {
        return name;
    }
    public String get_dialogue(){
        return dialogue;
    }
    public abstract boolean is_gambler(); // this will be implemented in all subclasses as it checks whether the user guessed right or wrong
    public abstract double get_money(); // each subclass will have separate implementation using separate amount of money
}

class NormalCustomer extends Customer {
    private final static ArrayList<String> NormalCustomerNames; // array list of strings of customer names
    private final static ArrayList<String> NormalCustomerDialogues; // array list of strings of customer dialogues
    private final static Random random = new Random(); // object for creating random numbers

    // this method will be run automatically  when this class is loaded all of the names , dialogues from txt file will be loaded into the array list at once
    static {
        NormalCustomerNames = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\names.txt"); // insert your own computer path of file
        NormalCustomerDialogues = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\dialogues.txt");
    }

    // the constructor will call the superconstructor and will pass in a random name and a random dialogue extracted from txt file and stored in array list
    public NormalCustomer() {
        super(NormalCustomerNames.get(random.nextInt(NormalCustomerNames.size())),
                NormalCustomerDialogues.get(random.nextInt(NormalCustomerDialogues.size())));
    }
    @Override public boolean is_gambler() {
        return false;
    }
    // arbitrary value for now we can change later
    @Override public double get_money(){
        return 10;
    }
}

class GamblerCustomer extends Customer {
    private final static ArrayList<String> gambler_names; // array list storing gambler names
    private final static ArrayList<String> gambler_dialogues; // array list storing gambler dialogues
    private final static Random random = new Random(); // random object to create random numbers

    // this is a method that will be run automatically  when this class is loaded, so all of the names , dialogues from txt file will be loaded into the array list at once
    static {
        gambler_names = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\names.txt");
        gambler_dialogues = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\dialogues.txt");
    }

    // the constructor will call the superconstructor and will pass in a random name and a random dialogue extracted from txt file and stored in array list
    public GamblerCustomer() {
        super(gambler_names.get(random.nextInt(gambler_names.size())),
                gambler_dialogues.get(random.nextInt(gambler_dialogues.size())));
    }
    @Override public boolean is_gambler() {
        return true;
    }
    // arbitrary value for now we can change later , slight bonus for correct answer on gambler
    @Override public double get_money(){
        return 20;
    }
}

// special customer can include any special event triggering customer we want bad or good
// no need to implement customer abstract method here we can do it in subclass
// separate abstract class made in case we add more special customers later
abstract class SpecialCustomer extends Customer {
    public SpecialCustomer(String name, String dialogue) {
        super(name, dialogue);
    }
    public abstract void trigger_special_event();
}

class UndercoverCop extends SpecialCustomer implements UndercovercopMarker {
    private final static ArrayList<String> cop_names; // array list storing cop names
    private final static ArrayList<String> cop_dialogues; // array list storing cop dialogues
    private final static Random random = new Random(); // random object to create random numbers

    // this is a method that will be run automatically  when this class is loaded, so all of the names , dialogues from txt file will be loaded into the array list at once
    static {
        cop_names = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\names.txt");
        cop_dialogues = FileReader.read_from_file("D:\\ideaJ projects\\testproject\\src\\dialogues.txt");
    }

    // the constructor will call the superconstructor and will pass in a random name and a random dialogue extracted from txt file and stored in array list
    // we have double constructor chain , undercovercop calls specialcustomer which calls customer
    public UndercoverCop() {
        super(cop_names.get(random.nextInt(cop_names.size())),
                cop_dialogues.get(random.nextInt(cop_dialogues.size())));
    }

    // even for cop we will use the same is_gambler method
    // but in the game loop when checking if user answered correct , we will first check if the customer was instance of SpecialCustomer/UndercoverCop
    // if yes then if the user choose "gambler" the special event is triggered , if no then the user gets the bonus money for sending undercover cop to ramen bar
    @Override public boolean is_gambler() {
        return false;
    }
    // undercover special event should end the game and so we will need to pass in the main game object of the game class
    @Override public void trigger_special_event() {
        System.out.println("The undercover cop arrested you. GAME OVER.");
    }
    // arbitrary value for now , alot bonus for correct answer on cop
    @Override public double get_money(){
        return 40;
    }
}

// this class will generate the customers coming to our shop , it doesnt need to be instantiated and is completely static
// we will need to tweak this class a bit after main game class is made if we want the chances to change according to the day
class CustomerGenerator {
    private final static Random random = new Random();
    public static Customer generateRandomCustomer() {
        int chance = random.nextInt(100); // random number 0-99 for chance of the next customer spawn
        if (chance < 10) // 10% chance for special customer (For numbers 0-9)
        {
            return new UndercoverCop();
        }
        else if (chance < 40) // 30% chance for special customer (For numbers 10-39)
        {
            return new GamblerCustomer();
        }
        else // 60% chance for normal customer (For numbers 40-99)
        {
            return new NormalCustomer();
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class Ingredient {
    private final String name;
    private int quantity;
    private final int ppu;
    public Ingredient(String name, int quantity, int restockPricePerUnit) {
        this.name = name;
        this.quantity = quantity;
        this.ppu = restockPricePerUnit;
    }
    public String get_name() {
        return name;
    }
    public int get_quantity() {
        return quantity;
    }
    public int get_ppu() {
        return ppu;
    }
    public void addQuantity(int amount) {
        quantity = quantity+amount;
    }
    public void useQuantity(int amount) {
        quantity = quantity-amount;
    }
}

class Inventory {
    private final Ingredient[] ingredients;  // array will contain an object for every TYPE of ingredient
    private final int size; // we will predefine size e.g our ramen takes 4 main ingredients that need restocking everyday

    public Inventory(int size) {
        this.size = size;
        ingredients = new Ingredient[size];
    }
    // will add 1 to type of ingredient on where that type is stored in array using index
    public void add_ingredient(Ingredient ingredient, int index) {
        if (index >= 0 && index < size) {
            ingredients[index] = ingredient;
        }
    }
    // will return us what TYPE of ingredient is stored in the array at the index , e.g at 0th index were storing noodles
    public Ingredient get_ingredient(int index) {
        if (index >= 0 && index < size) {
            return ingredients[index];
        }
        return null;
    }
    public void display_inventory() {
        System.out.println("Inventory");
        for (int i = 0; i < size; i++) {
            if (ingredients[i] != null) {
                System.out.println((i + 1) + ". " + ingredients[i].get_name() + "-Quantity: " + ingredients[i].get_quantity());
            }
        }
    }
    // if we need 1 ingredient of every type for ramen, we check if we have 1 of every type
    public boolean has_enough_ingredients() {
        for (int i = 0; i < size; i++) {
            if (ingredients[i] == null || ingredients[i].get_quantity() <= 0) {
                return false;
            }
        }
        return true;
    }
    // subtract one from every type of ingredient, should ONLY be called if has_enough_ingredients returns true
    public void use_ingredients() {
        for (int i = 0; i < size; i++) {
            if (ingredients[i] != null) {
                ingredients[i].useQuantity(1);
            }
        }
    }
    // add amount bought into the type of ingredient bought
    public int restockIngredient(int index, int amount, int playerMoney) {
        Ingredient ingredient = get_ingredient(index); // checking which type of ingredient is being bought
        if (ingredient != null) {
            int totalCost = amount * ingredient.get_ppu();
            if (playerMoney >= totalCost) {
                ingredient.addQuantity(amount);
                return totalCost; // subtraction of player money can be done in gameloop using returned cost
            }
        }
        return 0; // in case index didnt contain any ingredient
    }
}
// manages our ramen shop where we serve ramen, it requires our inventory to see if ingredients are enough e.t.c
class Shop {
    private final Inventory inventory;

    // aggregation since we are passing the reference type in constructor ourselves. Inventory can exist independantly of shop
    // since aggregation we will also provide getter for aggregated object
    public Shop(Inventory inventory) {
        this.inventory = inventory;
    }

    // serve ramen if enough ingredients
    public boolean serveRamen() {
        if (inventory.has_enough_ingredients() == true) {
            inventory.use_ingredients();
            System.out.println("Ramen served successfully!");
            return true;
        } else {
            System.out.println("Not enough ingredients.");
            return false;
        }
    }
    public void displayInventory() {
        inventory.display_inventory();
    }
    public Inventory getInventory() {
        return inventory;
    }
}
