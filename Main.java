package kites;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class Main {
	
	private static Map<String, Customer> customers = new HashMap<>();
    @SuppressWarnings("unused")
	private static String name, type, phoneNumber;
    
    private static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:mysql://localhost:3306/EBMS";
        String username = "root";
        String password = "";

        return DriverManager.getConnection(dbUrl, username, password);
    }
    
    public static void generateElectricityBill(String phoneNumber) {
    	phoneNumber = phoneNumber.trim();
        if (customers.containsKey(phoneNumber)) {
            Customer customer = customers.get(phoneNumber);

            double rate;
            if (customer.getType().equalsIgnoreCase("p")) {
                rate = 0.10; // Private connection rate
            } else if (customer.getType().equalsIgnoreCase("c")) {
                rate = 0.15; // Commercial connection rate
            } else {
                System.out.println("Invalid connection type.");
                return;
            }

            Scanner sc = new Scanner(System.in);

            System.out.print("Enter units consumed: ");
            double units = sc.nextDouble();

            double billAmount = units * rate;

            int bill_id = 0;
			try {
				bill_id = generatebill_id(getConnection());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            kites.Main.Customer.Bill newBill = new kites.Main.Customer.Bill(bill_id, units, billAmount);
            customer.addBill(newBill);

            try (Connection connection = getConnection()) {
                String insertBillQuery = "INSERT INTO bills (bill_id, customer_id, units_consumed, totalAmount) VALUES (?, ?, ?, ?)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(insertBillQuery)) {
                    preparedStatement.setInt(1, bill_id);
                    preparedStatement.setString(2, customer.getId());
                    preparedStatement.setDouble(3, units);
                    preparedStatement.setDouble(4, billAmount);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Bill Details:");
                        System.out.println("+--------+-----------------+----------------+----------------+--------+----------------+--------------+");
                        System.out.println("| bill_id | Customer Name   | Phone Number   | Connection Type| Units  | Rate per Unit  | Total Amount |");
                        System.out.println("+--------+-----------------+----------------+----------------+--------+----------------+--------------+");
                        System.out.printf("| %-6s | %-15s | %-15s | %-14s | %-6.2f | $%-14.2f | $%-11.2f |\n",
                                bill_id, customer.getName(), customer.getPhoneNumber(),
                                (customer.getType().equalsIgnoreCase("p") ? "Private" : "Commercial"),
                                units, rate, billAmount);
                        System.out.println("+--------+-----------------+----------------+----------------+--------+----------------+--------------+");
                    } else {
                        System.out.println("Failed to generate bill.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Customer with Phone Number " + phoneNumber + " not found.");
        }
    }
    
    private static int generatebill_id(Connection connection) throws SQLException {
        String selectMaxbill_idQuery = "SELECT MAX(bill_id) FROM bills";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectMaxbill_idQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                int maxbill_id = resultSet.getInt(1);
                return maxbill_id + 1;
            } else {
                return 1000; // Start from 1000 if there are no bills yet
            }
        }
    }
    
    public static void main(String a[]) {
        int option = 0;

        try (Connection connection = getConnection()) {
            while (true) {
                Scanner sc = new Scanner(System.in);
                System.out.println("Welcome to Electricity Billing System");
                System.out.println("-------------------------------------");

                System.out.println("+----------+--------------------------------+");
                System.out.println("Input your choice from the following menu:");

                System.out.println("+----------+--------------------------------+");
                System.out.println("| Option   | Description                    |");
                System.out.println("+----------+--------------------------------+");
                System.out.println("| 1        | New Customer Registration      |");
                System.out.println("| 2        | Modify Customer Details        |");
                System.out.println("| 3        | Delete Customer                |");
                System.out.println("| 4        | Search User                    |");
                System.out.println("| 5        | Show all Customers Registered  |");
                System.out.println("| 6        | Generate New Bill              |");
                System.out.println("| 7        | Delete Bill                    |");
                System.out.println("| 8        | Show all Bills                 |");
                System.out.println("| 9        | Search Bill                    |");
                System.out.println("| 0        | Exit                           |");
                System.out.println("+----------+--------------------------------+");
               
                System.out.print("Enter your choice: ");
                option = sc.nextInt();
                sc.nextLine();

                switch (option) {
                    case 1:
                        addNewCustomer(connection);
                        break;
                    case 2:
                        System.out.print("Enter Phone Number to Modify Customer Details: ");
                        String phoneNumberToModify = sc.nextLine();
                        modifyCustomerDetails(phoneNumberToModify, connection);
                        break;
                        
                    case 3:
                        System.out.print("Enter Customer Phone Number to Delete:");
                        String phoneNumberToDelete = sc.nextLine();
                        deleteCustomer(phoneNumberToDelete, connection);
                        break;
                        
                    case 4:
                        System.out.print("Enter Customer Phone Number to Search: ");
                        String phoneNumberToSearch = sc.nextLine();
                        searchUser(phoneNumberToSearch, connection);
                        break;
           
                    case 5:
                        displayAllCustomers(connection);
                        break;
                        
                    case 6:
                        System.out.print("Enter Customer Phone Number to Generate Bill: ");
                        String phoneNumberToGenerateBill = sc.nextLine();
                        generateElectricityBill(phoneNumberToGenerateBill);
                        break;
                        
                    case 7:
                         System.out.print("Enter Bill ID to Delete: ");
                         int bill_idToDelete = sc.nextInt();
                         deleteBill(connection, bill_idToDelete);
                         break;
                        
                    case 8:
                        showAllBills(connection);
                        break;
                        
                    case 9:
                        System.out.print("Enter Bill ID to Search: ");
                        int bill_idToSearch = sc.nextInt();
                        searchBill(connection, bill_idToSearch);
                        break;
                       
                    case 0:
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    
    public static void addNewCustomer(Connection connection) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Add New Customer");
        System.out.println("-----------------");

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phoneNumber = sc.nextLine();

        System.out.print("Enter Type of connection [p: private users and c: for commercial purpose]: ");
        String type = sc.nextLine();
        String typeDisplay = type.equalsIgnoreCase("p") ? "Private" : (type.equalsIgnoreCase("c") ? "Commercial" : "Invalid");

        // Generate a unique ID for the customer
        String id = generateRandomCustomerId(connection);

        try {
            // Create a PreparedStatement for inserting customer data
        	String insertQuery = "INSERT INTO customers (id, name, phone_number, type) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setString(4, type);
            
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                customers.put(phoneNumber, new Customer(id, name, phoneNumber, type));
                System.out.println("\nCustomer added successfully!");

                System.out.println("+------------+---------------+----------------+----------------+");
                System.out.println("| CustomerID | Name          | Phone Number   | Connection Type|");
                System.out.println("+------------+---------------+----------------+----------------+");
                System.out.printf("| %-10s | %-13s | %-14s | %-14s |\n", id, name, phoneNumber, typeDisplay);
                System.out.println("+------------+---------------+----------------+----------------+");
            } else {
                System.out.println("Failed to add customer.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error adding customer to the database.");
        }
    }

    
    public static void displayAllCustomers(Connection connection) {
        System.out.println("Registered Customers:");
        System.out.println("+------------+-----------------+-----------------+----------------+");
        System.out.println("| CustomerID | Name            | Phone Number    | Connection Type|");
        System.out.println("+------------+-----------------+-----------------+----------------+");
        
        try {
            Statement statement = connection.createStatement();
            String selectQuery = "SELECT id, name, phone_number, type FROM customers";
            ResultSet resultSet = statement.executeQuery(selectQuery);

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String phoneNumber = resultSet.getString("phone_number");
                String connectionType = resultSet.getString("type");

                String typeDisplay = connectionType.equalsIgnoreCase("p") ? "Private" : (connectionType.equalsIgnoreCase("c") ? "Commercial" : "Invalid");

                System.out.printf("| %-10s | %-15s | %-15s | %-14s |\n", id, name, phoneNumber, typeDisplay);
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error fetching customer data from the database.");
        }
        
        System.out.println("+------------+-----------------+-----------------+----------------+");
    }

    public static void modifyCustomerDetails(String phoneNumber, Connection connection) {
        @SuppressWarnings("unused")
		boolean found = false;

        try {
            Statement statement = connection.createStatement();
            String selectQuery = "SELECT id, name, type FROM customers WHERE phone_number = '" + phoneNumber + "'";
            ResultSet resultSet = statement.executeQuery(selectQuery);

            if (resultSet.next()) {
                found = true;
                String customerId = resultSet.getString("id");
                String currentName = resultSet.getString("name");
                String currentType = resultSet.getString("type");

                @SuppressWarnings("resource")
				Scanner sc = new Scanner(System.in);

                System.out.println("Current Name: " + currentName);
                System.out.print("Enter New Name: ");
                String newName = sc.nextLine();

                System.out.println("Current Type: " + (currentType.equalsIgnoreCase("p") ? "Private" : "Commercial"));
                System.out.print("Enter New Type [p:private or c:commercial]: ");
                String newType = sc.nextLine();

                String updateQuery = "UPDATE customers SET name = '" + newName + "', type = '" + newType + "' WHERE id = '" + customerId + "'";
                statement.executeUpdate(updateQuery);

                System.out.println("Customer details updated successfully!");

            } else {
                System.out.println("Customer with Phone Number " + phoneNumber + " not found.");
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating customer details in the database.");
        }
    }

    public static void deleteCustomer(String phoneNumber, Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String deleteQuery = "DELETE FROM customers WHERE phone_number = '" + phoneNumber + "'";
            int rowsAffected = statement.executeUpdate(deleteQuery);

            if (rowsAffected > 0) {
                System.out.println("Customer with Phone Number " + phoneNumber + " deleted.");
            } else {
                System.out.println("Customer with Phone Number " + phoneNumber + " not found.");
            }

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting customer from the database.");
        }
    }

    public static void searchUser(String phoneNumber, Connection connection) {
        try {
            Statement statement = connection.createStatement();

            String customerQuery = "SELECT * FROM customers WHERE phone_number = '" + phoneNumber + "'";
            ResultSet customerResult = statement.executeQuery(customerQuery);

            if (customerResult.next()) {
                String customerId = customerResult.getString("id"); // Corrected to "id"
                String customerName = customerResult.getString("name");
                String customerPhoneNumber = customerResult.getString("phone_number");
                String customerType = customerResult.getString("type");

                System.out.println("Search Results:");
                System.out.println("+------------+-----------------+-----------------+----------------+");
                System.out.println("| CustomerID | Name            | Phone Number    | Connection Type|");
                System.out.println("+------------+-----------------+-----------------+----------------+");
                System.out.printf("| %-10s | %-15s | %-15s | %-14s |\n",
                        customerId, customerName, customerPhoneNumber,
                        (customerType.equalsIgnoreCase("p") ? "Private" : "Commercial"));
                System.out.println("+------------+-----------------+-----------------+----------------+");

                String billQuery = "SELECT * FROM bills WHERE customer_id = '" + customerId + "'";
                ResultSet billResult = statement.executeQuery(billQuery);

                if (!billResult.next()) {
                    System.out.println("No bills available for this customer.");
                } else {
                    System.out.println("Bills:");
                    System.out.println("+----------+--------------+--------------+");
                    System.out.println("| bill_id  | Units Consumed | Total Amount|");
                    System.out.println("+----------+--------------+--------------+");
                    do {
                        int bill_id = billResult.getInt("bill_id"); // Corrected to "billid"
                        double unitsConsumed = billResult.getDouble("units_consumed");
                        double totalAmount = billResult.getDouble("totalAmount"); // Corrected to "totalAmount"

                        System.out.printf("| %-8s | %-13.2f | $%-11.2f |\n", bill_id, unitsConsumed, totalAmount);
                    } while (billResult.next());
                    System.out.println("+----------+--------------+--------------+");
                }
            } else {
                System.out.println("Customer with Phone Number " + phoneNumber + " not found.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error searching user information from the database.");
        }
    }


    
    public static void showAllBills(Connection connection) {
        try {
            Statement statement = connection.createStatement();

            String query = "SELECT bill_id AS bill_id, c.id AS customer_id, c.name AS customer_name, c.phone_number AS customer_phone, c.type AS customer_type, b.totalAmount AS totalAmount " +
                           "FROM bills AS b " +
                           "JOIN customers AS c ON b.customer_id = c.id";

            ResultSet resultSet = statement.executeQuery(query);

            boolean foundBills = false;

            System.out.println("All Generated Bills:");
            System.out.println("+----------+----------------+-----------------+-----------------+-----------+--------------+");
            System.out.println("| Bill ID  | Customer ID    | Customer Name   | Customer Phone  | Type      | Total Amount |");
            System.out.println("+----------+----------------+-----------------+-----------------+-----------+--------------+");

            while (resultSet.next()) {
                int bill_id = resultSet.getInt("bill_id");
                String customerId = resultSet.getString("customer_id");
                String customerName = resultSet.getString("customer_name");
                String customerPhone = resultSet.getString("customer_phone");
                String customerType = resultSet.getString("customer_type");
                double totalAmount = resultSet.getDouble("totalAmount");

                foundBills = true;

                System.out.printf("| %-8s | %-14s | %-15s | %-15s | %-9s | $%-12.2f |\n",
                        bill_id, customerId, customerName, customerPhone,
                        (customerType.equalsIgnoreCase("p") ? "Private" : "Commercial"),
                        totalAmount);
            }

            System.out.println("+----------+----------------+-----------------+-----------------+-----------+--------------+");

            if (!foundBills) {
                System.out.println("No bills have been generated.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error fetching bill information from the database.");
        }
    }

    
    public static void deleteBill(Connection connection, int bill_idToDelete) {
        try {
            PreparedStatement deleteBillStatement = connection.prepareStatement("DELETE FROM bills WHERE bill_id = ?");
            deleteBillStatement.setInt(1, bill_idToDelete);
            int rowsDeleted = deleteBillStatement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Bill with ID " + bill_idToDelete + " deleted.");
            } else {
                System.out.println("Bill with ID " + bill_idToDelete + " not found.");
            }

            deleteBillStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting bill from the database.");
        }
    }

    public static void searchBill(Connection connection, int bill_idToSearch) {
        try {
            PreparedStatement searchBillStatement = connection.prepareStatement(
                    "SELECT bill_id, c.name, c.phone_number, c.type, b.units_consumed, b.totalAmount " +
                    "FROM bills b " +
                    "INNER JOIN customers c ON b.customer_id = c.id " +
                    "WHERE bill_id = ?");
            searchBillStatement.setInt(1, bill_idToSearch);
            ResultSet resultSet = searchBillStatement.executeQuery();

            boolean foundBill = false;

            System.out.println("Bill Details:");
            System.out.println("+--------+-----------------+----------------+----------------+--------+--------------+");
            System.out.println("|Bill ID | Customer Name   | Phone Number   | Connection Type| Units  | Total Amount |");
            System.out.println("+--------+-----------------+----------------+----------------+--------+--------------+");

            while (resultSet.next()) {
                foundBill = true;
                System.out.printf("| %-6s | %-15s | %-15s | %-14s | %-6.2f | $%-11.2f |\n",
                        resultSet.getInt("bill_id"), resultSet.getString("name"), resultSet.getString("phone_number"),
                        (resultSet.getString("type").equalsIgnoreCase("p") ? "Private" : "Commercial"),
                        resultSet.getDouble("units_consumed"), resultSet.getDouble("totalAmount"));
            }

            System.out.println("+--------+-----------------+----------------+----------------+--------+--------------+");

            if (!foundBill) {
                System.out.println("Bill with ID " + bill_idToSearch + " not found.");
            }

            resultSet.close();
            searchBillStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error searching for the bill in the database.");
        }
    }

    
    private static String generateRandomCustomerId(Connection connection) {
        Random random = new Random();
        int customerId = random.nextInt(900000) + 100000; // Generates a random 6-digit number

        try {
            PreparedStatement insertCustomerIdStatement = connection.prepareStatement(
                "INSERT INTO customer_ids (customer_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            insertCustomerIdStatement.setInt(1, customerId);
            insertCustomerIdStatement.executeUpdate();
            
            return String.valueOf(customerId);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error generating a new customer ID.");
        }

        return null;
    }

    private static class Customer {
        private String id;
        private String name;
        private String phoneNumber;
        private String type;
        private List<Bill> bills = new ArrayList<>();

        public Customer(String id, String name, String phoneNumber, String type) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.type = type;
        }
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getType() {
            return type;
        }
       
        @SuppressWarnings("unused")
		public void setName(String name) {
        this.name = name;
        }

        @SuppressWarnings("unused")
		public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        }

        @SuppressWarnings("unused")
		public void setType(String type) {
        this.type = type;
        }
        
         @SuppressWarnings("unused")
		public List<Bill> getBills() {
            return bills;
        }

        public void addBill(Bill bill) {
            bills.add(bill);
        }
        
        private static class Bill {
            private int id;
            private double unitsConsumed;
            private double totalAmount;

            public Bill(int id, double unitsConsumed, double totalAmount) {
                this.id = id;
                this.unitsConsumed = unitsConsumed;
                this.totalAmount = totalAmount;
            }

            @SuppressWarnings("unused")
			public int getId() {
                return id;
            }

            @SuppressWarnings("unused")
			public double getUnitsConsumed() {
                return unitsConsumed;
            }

            @SuppressWarnings("unused")
			public double getTotalAmount() {
                return totalAmount;
            }}
}}
