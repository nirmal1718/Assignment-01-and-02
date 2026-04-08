import java.util.*;
import java.time.*;

enum SpotStatus { EMPTY, OCCUPIED, DELETED }

class Vehicle {
    String licensePlate;
    LocalDateTime entryTime;

    Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
        this.entryTime = LocalDateTime.now();
    }
}

class ParkingSpot {
    Vehicle vehicle;
    SpotStatus status = SpotStatus.EMPTY;
}

public class ParkingLotSystem {
    private final int capacity;
    private final ParkingSpot[] lot;
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int totalParkings = 0;

    public ParkingLotSystem(int capacity) {
        this.capacity = capacity;
        this.lot = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            lot[i] = new ParkingSpot();
        }
    }

    // Custom Hash Function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    /**
     * Assigns a spot using Linear Probing: (hash + i) % capacity
     */
    public String parkVehicle(String licensePlate) {
        if (occupiedCount >= capacity) return "Lot Full!";

        int preferredSpot = hash(licensePlate);
        int probes = 0;
        int currentSpot = preferredSpot;

        // Linear Probing
        while (lot[currentSpot].status == SpotStatus.OCCUPIED) {
            currentSpot = (currentSpot + 1) % capacity;
            probes++;
        }

        // Park the vehicle
        lot[currentSpot].vehicle = new Vehicle(licensePlate);
        lot[currentSpot].status = SpotStatus.OCCUPIED;
        occupiedCount++;
        totalProbes += probes;
        totalParkings++;

        return String.format("Vehicle [%s] parked at spot #%d (%d probes)",
                licensePlate, currentSpot, probes);
    }

    /**
     * Removes vehicle and calculates billing
     */
    public String exitVehicle(String licensePlate) {
        int preferredSpot = hash(licensePlate);
        int currentSpot = preferredSpot;
        int checked = 0;

        // Search for the vehicle
        while (checked < capacity) {
            if (lot[currentSpot].status == SpotStatus.EMPTY) break; // Stop if we hit an actual empty spot

            if (lot[currentSpot].status == SpotStatus.OCCUPIED &&
                    lot[currentSpot].vehicle.licensePlate.equals(licensePlate)) {

                Vehicle v = lot[currentSpot].vehicle;
                Duration duration = Duration.between(v.entryTime, LocalDateTime.now().plusHours(2)); // Mock 2h duration
                double fee = calculateFee(duration);

                // Lazy Deletion: Mark as DELETED so probing continues correctly for other vehicles
                lot[currentSpot].status = SpotStatus.DELETED;
                lot[currentSpot].vehicle = null;
                occupiedCount--;

                return String.format("Exit: %s | Duration: %dh %dm | Fee: $%.2f",
                        licensePlate, duration.toHours(), duration.toMinutesPart(), fee);
            }
            currentSpot = (currentSpot + 1) % capacity;
            checked++;
        }
        return "Vehicle not found.";
    }

    private double calculateFee(Duration duration) {
        return Math.max(5.0, duration.toHours() * 5.50); // $5 minimum, $5.50/hr
    }

    public void printStats() {
        double occupancy = (double) occupiedCount / capacity * 100;
        double avgProbes = totalParkings == 0 ? 0 : (double) totalProbes / totalParkings;
        System.out.println("\n--- Parking Statistics ---");
        System.out.printf("Occupancy: %.1f%%\n", occupancy);
        System.out.printf("Average Probes: %.2f\n", avgProbes);
        System.out.println("--------------------------\n");
    }

    public static void main(String[] args) {
        ParkingLotSystem mallParking = new ParkingLotSystem(500);

        // Simulate simultaneous arrivals (potential collisions)
        System.out.println(mallParking.parkVehicle("ABC-1234"));
        System.out.println(mallParking.parkVehicle("ABC-1235")); // Likely to hash close/same
        System.out.println(mallParking.parkVehicle("XYZ-9999"));

        mallParking.printStats();

        System.out.println(mallParking.exitVehicle("ABC-1234"));
    }
}