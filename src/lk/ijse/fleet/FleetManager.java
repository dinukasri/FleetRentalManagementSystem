package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetManager {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<RentalAgreement> agreements = new ArrayList<>();
    private RentalEventListener rentalEventListener;

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public void addCustomer(Customer c) {
        customers.add(c);
    }

    public Vehicle findVehicleById(String id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equals(id)) {
                return vehicle;
            }
        }
        return null;
    }

    public Customer findCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getCustomerId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    public void setRentalEventListener(RentalEventListener listener) {
        rentalEventListener = listener;
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(v -> v.getStatus() == RentalStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public RentalAgreement createRentalAgreement(String vehicleId, String customerId, int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return null;
        }
        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("Vehicle " + vehicleId + " is not available.");
        }

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double cost = customer.applyDiscount(subtotal);
        if (customer instanceof CorporateCustomer corporateCustomer
                && !corporateCustomer.hasSufficientCredit(cost)) {
            throw new InsufficientCreditException("Corporate customer has insufficient credit.");
        }

        RentalAgreement agreement = new RentalAgreement(vehicle, customer, rentalDays);
        vehicle.setStatus(RentalStatus.RENTED);
        agreements.add(agreement);
        if (rentalEventListener != null) {
            rentalEventListener.onRentalCreated(agreement);
        }
        return agreement;
    }

    public void returnVehicle(String agreementId) {
        for (RentalAgreement agreement : agreements) {
            if (agreement.getAgreementId().equals(agreementId)) {
                agreement.getVehicle().setStatus(RentalStatus.AVAILABLE);
                System.out.println("Vehicle returned successfully.");
                return;
            }
        }
        System.out.println("Rental agreement not found.");
    }

    public FleetSnapshot generateSnapshot() {
        return new FleetSnapshot();
    }

    public synchronized void printFleetStatus() {
        generateSnapshot().printSummary();
    }

    public synchronized void checkOverdueAgreements() {
        for (RentalAgreement agreement : agreements) {
            agreement.setDaysElapsed(agreement.getDaysElapsed() + 1);
            if (agreement.getDaysElapsed() > agreement.getRentalDays()) {
                System.out.println("[OVERDUE ALERT] Agreement " + agreement.getAgreementId()
                        + " has exceeded its " + agreement.getRentalDays() + "-day rental period.");
            }
        }
    }

    public List<Vehicle> getVehiclesByFuelType(FuelType type) {
        return vehicles.stream()
                .filter(v -> v.getFuelType() == type)
                .collect(Collectors.toList());
    }

    public List<Customer> getCustomersSortedByName() {
        return customers.stream()
                .sorted(Comparator.comparing(c -> c.getFullName()))
                .collect(Collectors.toList());
    }

    public double generateRevenueReport() {
        return agreements.stream()
                .mapToDouble(a -> a.getTotalCost())
                .sum();
    }

    public void printAllReportables() {
        List<Reportable> reportables = new ArrayList<>();
        reportables.addAll(vehicles);
        reportables.addAll(customers);
        for (Reportable item : reportables) {
            System.out.println(item.getReportCategory() + ": " + item.getSummary());
        }
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public List<RentalAgreement> getAllAgreements() {
        return new ArrayList<>(agreements);
    }

    public class FleetSnapshot {
        private int availableCount;
        private int rentedCount;
        private double totalRevenue;

        public FleetSnapshot() {
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getStatus() == RentalStatus.AVAILABLE) {
                    availableCount++;
                } else {
                    rentedCount++;
                }
            }
            for (RentalAgreement agreement : agreements) {
                totalRevenue += agreement.getTotalCost();
            }
        }

        public void printSummary() {
            System.out.printf("[FLEET STATUS] Available: %d  Rented: %d  Total Revenue So Far: %,.2f%n",
                    availableCount, rentedCount, totalRevenue);
        }
    }
}
