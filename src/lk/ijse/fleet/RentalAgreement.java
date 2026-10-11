package lk.ijse.fleet;

public class RentalAgreement {
    private static int counter = 1000;
    private String agreementId;
    private Vehicle vehicle;
    private Customer customer;
    private int rentalDays;
    private double totalCost;
    private int daysElapsed;

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {
        agreementId = "AGR-" + ++counter;
        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;
        double subtotal = vehicle.calculateRentalCost(rentalDays);
        totalCost = customer.applyDiscount(subtotal);
        daysElapsed = 0;
    }

    public String getAgreementId() { return agreementId; }
    public Vehicle getVehicle() { return vehicle; }
    public Customer getCustomer() { return customer; }
    public int getRentalDays() { return rentalDays; }
    public double getTotalCost() { return totalCost; }
    public int getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(int daysElapsed) { this.daysElapsed = daysElapsed; }

    @Override
    public String toString() {
        return "================================================================\n"
                + "RENTAL AGREEMENT RECEIPT\n"
                + "================================================================\n"
                + "Agreement ID : " + agreementId + "\n"
                + "Vehicle      : " + vehicle.getVehicleId() + " - " + vehicle.getBrand() + " "
                + vehicle.getModel() + " (" + vehicle.getClass().getSimpleName() + ")\n"
                + "Customer     : " + customer.getCustomerId() + " - " + customer.getFullName() + " ("
                + customer.getClass().getSimpleName().replace("Customer", "") + ")\n"
                + "Rental Days  : " + rentalDays + "\n"
                + String.format("Total Cost   : %,.2f%n", totalCost)
                + "================================================================";
    }
}
