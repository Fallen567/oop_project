## Underground Ramen Shop Manager (Console-Based)

A Java console simulation game where you manage a secret underground ramen shop over a 3-day period. Serve various customers while managing money, suspicion, and inventory. This repository contains only the console version; the GUI files are not included.

---

## Overview

You play as a ramen shop manager operating underground. Each day, 10 randomized customers visit your shop, including NormalCustomers, GamblerCustomers, and UndercoverCops. Your goal is to keep your shop running by managing money and inventory effectively, while keeping suspicion levels low to avoid getting caught. The game emphasizes decision-making, resource management, and risk assessment.

---

## Game Features

- Three-day gameplay loop with 10 customers per day.
- Multiple customer types with distinct behaviors:
  - **NormalCustomer**: Pays regular prices.
  - **GamblerCustomer**: Pays variably based on chance.
  - **UndercoverCop**: Get caught if sent to Gambling Room.
- Suspicion mechanic that can end the game if too high.
- Inventory management for ingredients required to serve customers.
- Names and dialogues loaded dynamically from external text files.
- Console-based gameplay (no GUI in this repo).

---

## Object-Oriented Concepts Applied

### Encapsulation  
All class fields are private, accessed only through getters and setters to protect data integrity.

### Delegation  
`Game` delegates shop-related tasks to `Shop`, which delegates inventory management to `Inventory`, ensuring each class handles its own responsibilities.

### Abstraction  
Abstract classes `Customer` and `SpecialCustomer` define shared behavior and enforce subclass implementations. A marker interface tags undercover cops for specialized logic.

### Inheritance  
Customer types (`NormalCustomer`, `GamblerCustomer`, `UndercoverCop`) inherit from `Customer`. `UndercoverCop` further inherits from `SpecialCustomer` for event-specific behavior.

### Polymorphism  
Methods like `getMoney()` are overridden in subclasses for custom behavior. The `Money` class uses overloaded constructors. Superclass references hold subclass instances to allow flexible customer handling.

### Typecasting  
Downcasting is used when calling subclass-specific methods on objects referenced by their superclass type.

### File Handling  
Dialogue and names are loaded from text files at runtime using the `FileReader` class, separating data from logic. The game robustly handles invalid user input using multiple try-catch blocks to catch `InputMismatchException` and prevent the program from crashing. This ensures smooth gameplay by prompting users to re-enter valid choices instead of terminating unexpectedly.

### Exception Handling  
File I/O operations use try-catch blocks to handle exceptions and prevent runtime crashes.

### ArrayLists  
Dynamic lists store customer names and dialogues, assigned during object construction.

### Static Initializers  
Static blocks load data immediately when classes are loaded to ensure availability during execution.

### Wrapper Classes  
Custom wrapper classes for money and suspicion enable pass-by-reference semantics and encapsulate arithmetic operations.

### Aggregation  
The game is composed of modular components: `Game` has a `Shop`, `Shop` has an `Inventory`, and `Inventory` manages multiple `Ingredient` objects.

### Constants  
Static final variables are used throughout the code to define constant values, improving code readability and maintainability.

---
