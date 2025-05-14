import java.io.IOException;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

// normally if we pass money (type int or double) into a function its passed by value so any changes made to it are made to the copy
// to solve this we can treat int money as an OBJECT , this way it will be passed to functions by reference and any changes
// made to money inside function will also take effect outside the function
// for this reason this class is used JUST so that money can be treated as an object instead of integer
class Money {
    private double money;
    private static final double no_money = 0;
    private static final double normalcustomermoney = 10;
    private static final double gamblercustomermoney = 20;
    private static final double undercovercopmoney = 40;
    private static final double bribe = 100;
    private static final double starting_money = 100;

    public Money() {
        this.money = 0;
    }

    public Money(double initialAmount) { // If you have this constructor
        this.money = initialAmount;
    }

    public double get_money() {
        return this.money;
    }

    public void set_money(double amount) {
        this.money = amount;
    }

    // This is the crucial method to check and correct:
    public void change_money(double amount_to_change) {
        this.money += amount_to_change; // Ensure it adds the parameter 'amount_to_change'
        
        // Ensure money doesnt go below zero during restocking
        if (this.money < 0) {
            this.money = 0; // Or handle as per your game's logic
        }
    }

    public double get_normalcustomermoney() {
        return normalcustomermoney;
    }

    public double get_gamblercustomermoney() {
        return gamblercustomermoney;
    }

    public double get_undercovercopmoney() {
        return undercovercopmoney; // Should return 40
    }

    public double get_bribe() {
        return bribe;
    }

    public double get_starting_money() {
        return starting_money;
    }
    public double get_no_money() {
        return no_money;
    }
}

// wrapper class for suspicion so we can change suspicion in methods
class Suspicion{
    private int suspicion;
    private static final int no_suspicion = 0;
    private static final int max_suspicion = 100;
    private static final int normalcustomersusincrease = 20;
    private static final int gamblercustomersusincrease = 20;
    private static final int normalcustomersusdecrease = -10;
    private static final int gamblercustomersusdecrease = -10;
    private static final int undercovercopsusdecrease = -15;
    private static final int undercovercopsusincrease = 100;
    private static final int customerleftsusincrease = 15;


    public Suspicion(){
        this.suspicion = 0;
    }

    public int get_no_suspicion(){
        return no_suspicion;
    }

    public int get_max_suspicion(){
        return max_suspicion;
    }

    public int get_normalcustomersusincrease(){
        return normalcustomersusincrease;
    }
    public int get_gamblercustomersusincrease(){
        return gamblercustomersusincrease;
    }
    public int get_normalcustomersusdecrease(){
        return normalcustomersusdecrease;
    }
    public int get_gamblercustomersusdecrease(){return gamblercustomersusdecrease; }
    public int get_undercovercopsusdecrease(){return undercovercopsusdecrease; }
    public int get_undercovercopsusincrease(){return undercovercopsusincrease;}
    public int get_customerleftsusincrease(){return customerleftsusincrease;}

    public Suspicion(int s){
        this.suspicion = s;
    }
    public int get_suspicion(){
        return this.suspicion;
    }
    public void set_suspicion(int suspicion){
        this.suspicion = suspicion;
    }
    public void change_suspicion(int change){
        this.suspicion = this.suspicion + change;
        if (this.suspicion >= 100){
            this.suspicion = 100;
        }
        if (this.suspicion < 0){
            this.suspicion = 0;
        }
    }
    public boolean sus_reached_full(){
        if(this.suspicion >= 100){
            return true;
        }
        else{
            return false;
        }
    }
    // after a day ends we can call this function with the day number as argument
    // 'dayJustFinished' is the day number that has just concluded.
    public void suspicion_newday(int dayJustFinished) {
        if (dayJustFinished == 1) { // After day 1 ends (for the start of day 2)
            this.change_suspicion(20); // Add 20 to current suspicion
        } else if (dayJustFinished == 2) { // After day 2 ends (for the start of day 3)
            this.change_suspicion(30); // Add 30 to current suspicion
        }
        // No action for other days, or if dayJustFinished is 0 or >= 3
    }
}

abstract class Customer {
    private String name;
    private String dialogue;

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
    public abstract void get_money(Money m); // each subclass will have separate implementation using separate amount of money, this method will change money variable\
    public abstract void right_room(Suspicion s); // each customer has own implementation of when the customer is sent to the right room
    public abstract void wrong_room(Suspicion s); // each customer has own implementation of when sent to wrong room
}

class NormalCustomer extends Customer {
    private final static ArrayList<String> NormalCustomerNames; // array list of strings of customer names
    private final static ArrayList<String> NormalCustomerDialogues; // array list of strings of customer dialogues
    private final static Random random = new Random(); // object for creating random numbers

    // this method will be run automatically  when this class is loaded all of the names , dialogues from txt file will be loaded into the array list at once
    static {
        NormalCustomerNames = FileReader.read_from_file("classes/names.txt"); // Adjusted path
        NormalCustomerDialogues = FileReader.read_from_file("classes/customer_dialogues.txt"); // Adjusted path
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
    @Override public void get_money(Money m){
        // increment money by 10
        m.change_money(m.get_normalcustomermoney());
    }

    // two suspicion methods , 1 for sending normalcustomer to right room and 1 for wrong room
    @Override public void right_room(Suspicion s){
        s.change_suspicion(s.get_normalcustomersusdecrease());
    }
    @Override public void wrong_room(Suspicion s){
        s.change_suspicion(s.get_normalcustomersusincrease());
    }
}

class GamblerCustomer extends Customer {
    private final static ArrayList<String> gambler_names; // array list storing gambler names
    private final static ArrayList<String> gambler_dialogues; // array list storing gambler dialogues
    private final static Random random = new Random(); // random object to create random numbers

    // this is a method that will be run automatically  when this class is loaded, so all of the names , dialogues from txt file will be loaded into the array list at once
    static {
        gambler_names = FileReader.read_from_file("classes/names.txt"); // Adjusted path
        gambler_dialogues = FileReader.read_from_file("classes/gambler_dialogues.txt"); // Adjusted path
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
    @Override public void get_money(Money m){
        m.change_money(m.get_gamblercustomermoney());
    }

    // 2 suspicion methods , 1 for right room 1 for wrong room
    @Override public void right_room(Suspicion s){
        s.change_suspicion(s.get_gamblercustomersusdecrease());
    }
    @Override public void wrong_room(Suspicion s){
        s.change_suspicion(s.get_gamblercustomersusincrease());
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
        cop_names = FileReader.read_from_file("classes/names.txt"); // Adjusted path
        cop_dialogues = FileReader.read_from_file("classes/cop_dialogues.txt"); // Adjusted path
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
        //handled in the main game class
    }
    // arbitrary value for now , alot bonus for correct answer on cop
    @Override public void get_money(Money m){
        m.change_money(m.get_undercovercopmoney());
    }

    // 2 functions for suspicion 1 for right room 1 for wrong , no need to use wrong one if we will end the game right away
    @Override public void right_room(Suspicion s){
        s.change_suspicion(s.get_undercovercopsusdecrease());
    }
    @Override public void wrong_room(Suspicion s){
        s.change_suspicion(s.get_undercovercopsusincrease());
    }

    // bribing if getting caught method will decrement required amount from money
    public void bribing(Money m){
        m.change_money(m.get_bribe());
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

class Ingredient {
    private final String name;
    private int quantity;
    private final int ppu; // price per unit for restocking
    private final int initialQuantity; // Store the initial quantity

    public Ingredient(String name, int quantity, int restockPricePerUnit) {
        this.name = name;
        this.quantity = quantity;
        this.initialQuantity = quantity; // Set initial quantity upon creation
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
        quantity = quantity + amount;
    }
    public void useQuantity(int amount) {
        if (quantity >= amount) {
            quantity = quantity - amount;
        } else {
            quantity = 0; // Prevent negative quantity
        }
    }
    // Method to reset quantity to its initial value
    public void resetQuantity() {
        this.quantity = this.initialQuantity;
    }
}

class Inventory {
    private final Ingredient[] ingredients;
    private final int size; 

    public Inventory(int size) {
        this.size = size;
        ingredients = new Ingredient[size];
    }
    public void add_ingredient(Ingredient ingredient, int index) {
        if (index >= 0 && index < size) {
            ingredients[index] = ingredient;
        }
    }
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
    public boolean has_enough_ingredients() {
        for (int i = 0; i < size; i++) {
            if (ingredients[i] == null || ingredients[i].get_quantity() <= 0) {
                return false;
            }
        }
        return true;
    }
    public void use_ingredients() {
        for (int i = 0; i < size; i++) {
            if (ingredients[i] != null) {
                ingredients[i].useQuantity(1);
            }
        }
    }
    public int restockIngredient(int index, int amount, int playerMoney) {
        Ingredient ingredient = get_ingredient(index); 
        if (ingredient != null && amount > 0) { // Ensure amount is positive
            int totalCost = amount * ingredient.get_ppu();
            if (playerMoney >= totalCost) {
                // The actual addition to quantity will be handled by the Game class after deducting money
                return totalCost; 
            } else {
                return -1; // Indicate not enough money
            }
        }
        return 0; 
    }

    // Method to reset all ingredients to their initial quantities
    public void resetInventory() {
        for (int i = 0; i < size; i++) {
            if (ingredients[i] != null) {
                ingredients[i].resetQuantity();
            }
        }
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
    public void decreaseInventory() {
        Inventory inventory = getInventory(); // `getInventory()` returns the inventory object
        for (int i = 0; i < 4; i++) { // Hardcoded there are 4 types of ingredients
            Ingredient ingredient = inventory.get_ingredient(i);
            if (ingredient != null) {
                ingredient.useQuantity(1); // Decrease 1 unit of each ingredient
            }
        }
        System.out.println("Ingredients used for serving ramen have been deducted from inventory.");
    }
}

