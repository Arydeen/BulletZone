package edu.unh.cs.cs619.bulletzone.datalayer.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

public class BankAccountRecord extends EntityRecord {
    double credits;

    BankAccountRecord() {
        super(EntityType.BankAccount);
        credits = 1000.0; // Set initial balance in constructor
    }

    BankAccountRecord(ResultSet itemResult) {
        super(itemResult);
        try {
            credits = itemResult.getDouble("Credits");
            if (credits == 0) {
                // If loaded from DB with 0 credits, set initial balance
                credits = 1000.0;
            }
        } catch (SQLException e) {
            credits = 1000.0; // Set default balance even if DB read fails
            throw new IllegalStateException("Unable to extract data from bank account result set", e);
        }
    }

    @Override
    public void insertInto(Connection dataConnection) throws SQLException {
        super.insertInto(dataConnection);
        String insertQuery = "INSERT INTO BankAccount (EntityID, Credits) VALUES (?, ?)";
        try (PreparedStatement accountStatement = dataConnection.prepareStatement(insertQuery)) {
            accountStatement.setInt(1, getID());
            accountStatement.setDouble(2, credits);
            int affectedRows = accountStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating BankAccount record failed.");
            }
        }
    }

    static boolean update(Connection dataConnection, int accountID, double amount) throws SQLException {
        String updateQuery = "UPDATE BankAccount SET Credits=? WHERE EntityID=?";
        try (PreparedStatement updateStatement = dataConnection.prepareStatement(updateQuery)) {
            updateStatement.setDouble(1, amount);
            updateStatement.setInt(2, accountID);
            return updateStatement.executeUpdate() > 0;
        }
    }
}