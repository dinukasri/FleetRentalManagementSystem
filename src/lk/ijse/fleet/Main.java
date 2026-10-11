package lk.ijse.fleet;

import java.util.List;
import java.util.Scanner;

public class Main {
    private FleetManager fleetManager;
    private UserManager userManager;
    private Scanner scanner;
    private SystemUser loggedInUser;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        fleetManager = new FleetManager();
        userManager = new UserManager();
        scanner = new Scanner(System.in);

        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId()));

        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();

        while (true) {
            showAuthMenu();
            if (loggedInUser == null) {
                return;
            }
            showMainMenu();
        }
    }

    public void showAuthMenu() {
        while (loggedInUser == null) {
            System.out.println("\n========================================================");
            System.out.println("FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println("========================================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            String choice = readLine("Enter choice > ").trim();

            switch (choice) {
                case "1" -> {
                    String username = readLine("Username: ");
                    String password = readLine("Password: ");
                    loggedInUser = userManager.login(username, password);
                    if (loggedInUser == null) {
                        System.out.println("Invalid username or password");
                    }
                }
                case "2" -> {
                    String username = readLine("Username: ");
                    String password = readLine("Password: ");
                    String fullName = readLine("Full name: ");
                    userManager.registerUser(username, password, fullName);
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void showMainMenu() {
        System.out.println("\nWelcome, " + loggedInUser.getFullName() + "!");
        System.out.println("Loading fleet dashboard...");

        while (loggedInUser != null) {
            System.out.println("\n========================================================");
            System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
            System.out.println("========================================================");
            System.out.println("[1] Add Vehicle");
            System.out.println("[2] Add Customer");
            System.out.println("[3] View Available Vehicles");
            System.out.println("[4] Create Rental Agreement");
            System.out.println("[5] Return Vehicle");
            System.out.println("[6] View Revenue Report");
            System.out.println("[7] View Vehicles by Fuel Type");
            System.out.println("[8] View Combined Fleet & Customer Report");
            System.out.println("[9] View All Customers");
            System.out.println("[10] View All Rental Agreements");
            System.out.println("[0] Logout");
            String choice = readLine("Enter choice > ").trim();

            try {
                switch (choice) {
                    case "1" -> addVehicle();
                    case "2" -> addCustomer();
                    case "3" -> printItems(fleetManager.getAvailableVehicles());
                    case "4" -> createRentalAgreement();
                    case "5" -> {
                        fleetManager.returnVehicle(readLine("Agreement ID: "));
                    }
                    case "6" -> {
                        System.out.println("================================================================");
                        System.out.println("FLEET REVENUE REPORT");
                        System.out.println("================================================================");
                        System.out.printf("Total Revenue: %,.2f%n", fleetManager.generateRevenueReport());
                        System.out.println("----------------------------------------------------------------");
                    }
                    case "7" -> printVehiclesByFuelType();
                    case "8" -> fleetManager.printAllReportables();
                    case "9" -> printItems(fleetManager.getAllCustomers());
                    case "10" -> printItems(fleetManager.getAllAgreements());
                    case "0" -> loggedInUser = null;
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private void addVehicle() {
        int vehicleType = readMenuChoice("Vehicle Type", "CAR", "VAN", "TRUCK");
        String registrationNumber = readLine("Registration number: ");
        String brand = readLine("Brand: ");
        String model = readLine("Model: ");
        double dailyRate = readDecimalNumber("Daily rate: ");
        FuelType fuelType = readFuelType();

        Vehicle vehicle;
        switch (vehicleType) {
            case 1 -> vehicle = new Car(registrationNumber, brand, model, dailyRate, fuelType);
            case 2 -> {
                vehicle = new Van(registrationNumber, brand, model, dailyRate, fuelType,
                        readWholeNumber("Passenger capacity: "));
            }
            case 3 -> {
                vehicle = new Truck(registrationNumber, brand, model, dailyRate, fuelType,
                        readDecimalNumber("Cargo capacity in tons: "));
            }
            default -> {
                System.out.println("Invalid vehicle type.");
                return;
            }
        }
        fleetManager.addVehicle(vehicle);
        System.out.println("Vehicle added: " + vehicle.getVehicleId());
    }

    private void addCustomer() {
        int customerType = readMenuChoice("Customer Type", "INDIVIDUAL", "CORPORATE");
        String fullName = readLine("Full name: ");
        String contactNumber = readLine("Contact number: ");

        Customer customer;
        switch (customerType) {
            case 1 -> {
                customer = new IndividualCustomer(fullName, contactNumber,
                        readWholeNumber("Loyalty points: "));
            }
            case 2 -> {
                customer = new CorporateCustomer(fullName, contactNumber,
                        readDecimalNumber("Credit limit: "));
            }
            default -> {
                System.out.println("Invalid customer type.");
                return;
            }
        }
        fleetManager.addCustomer(customer);
        System.out.println("Customer added: " + customer.getCustomerId());
    }

    private void createRentalAgreement() {
        String vehicleId = readLine("Vehicle ID: ");
        String customerId = readLine("Customer ID: ");
        int rentalDays = readWholeNumber("Rental days: ");

        try {
            RentalAgreement agreement = fleetManager.createRentalAgreement(vehicleId, customerId, rentalDays);
            if (agreement != null) {
                System.out.println(agreement);
            }
        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
    }

    private void printVehiclesByFuelType() {
        printItems(fleetManager.getVehiclesByFuelType(readFuelType()));
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return scanner.nextLine();
    }

    private int readMenuChoice(String menuTitle, String... options) {
        while (true) {
            System.out.println("\n" + menuTitle + ":");
            for (int i = 0; i < options.length; i++) {
                System.out.println("[" + (i + 1) + "] " + options[i]);
            }

            try {
                int choice = Integer.parseInt(readLine("Enter choice > ").trim());
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // The same friendly message below applies to non-numeric input.
            }
            System.out.println("Invalid choice. Please enter a number from 1 to " + options.length + ".");
        }
    }

    private FuelType readFuelType() {
        return switch (readMenuChoice("Fuel Type", "PETROL", "DIESEL", "ELECTRIC", "HYBRID")) {
            case 1 -> FuelType.PETROL;
            case 2 -> FuelType.DIESEL;
            case 3 -> FuelType.ELECTRIC;
            case 4 -> FuelType.HYBRID;
            default -> throw new IllegalStateException("Unexpected validated menu choice.");
        };
    }

    private int readWholeNumber(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    private double readDecimalNumber(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid decimal number.");
            }
        }
    }

    private void printItems(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Object item : items) {
            System.out.println(item);
        }
    }
}
