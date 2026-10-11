package lk.ijse.fleet;

public class IndividualCustomer extends Customer {
    private int loyaltyPoints;

    public IndividualCustomer(String fullName, String contactNumber, int loyaltyPoints) {
        super(fullName, contactNumber);
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    @Override
    public double applyDiscount(double subtotal) {
        double discountRate = loyaltyPoints >= 300 ? 0.10 : loyaltyPoints >= 100 ? 0.05 : 0.00;
        return subtotal * (1 - discountRate);
    }
}
