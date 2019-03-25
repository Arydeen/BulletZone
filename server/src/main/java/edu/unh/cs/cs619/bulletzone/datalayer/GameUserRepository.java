package edu.unh.cs.cs619.bulletzone.datalayer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class GameUserRepository {
    HashMap<Integer, GameUser> userMap = new HashMap<>();
    Connection dataConnection;
    GameItemRepository itemRepo;

    final int iterations = 65536;
    final int keySize = 128;
    final int saltSize = 16;

    GameUser getUser(int userID) { return userMap.get(userID); }

    //need create user, with login and password arguments...
    GameUser createUser(String name, String username, String password) {
        GameUserRecord newRecord = new GameUserRecord();
        GameUser newUser = null;
        newRecord.name = name;
        newRecord.username = username;
        newRecord.statusID = Status.Active.ordinal();
        //The following is adapted from https://www.baeldung.com/java-password-hashing
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltSize];
        random.nextBytes(salt);

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            newRecord.passwordHash = hash;
            newRecord.passwordSalt = salt;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to attempt password creation!", e);
        }

        try {
            // Create base item
            PreparedStatement insertStatement = dataConnection.prepareStatement(
                    " INSERT INTO User ( Name, Username, PasswordHash, PasswordSalt, StatusID )\n" +
                            "    VALUES (" + newRecord.name + ", "
                            + newRecord.username + ", "
                            + newRecord.passwordHash + ", "
                            + newRecord.passwordSalt + ", "
                            + newRecord.statusID + "); ", Statement.RETURN_GENERATED_KEYS);
            int affectedRows = insertStatement.executeUpdate();
            if (affectedRows == 0)
                throw new SQLException("Creating User " + newRecord.username + " failed.");

            ResultSet generatedKeys = insertStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newRecord.userID = generatedKeys.getInt(1);
            }
            else {
                throw new SQLException("Created user " + newRecord.username + " but failed to obtain ID.");
            }

            newUser = new GameUser(newRecord);
            userMap.put(newRecord.userID, newUser);
        } catch (SQLException e) {
            throw new IllegalStateException("Error while creating item!", e);
        }
        System.out.println("New user " + username + " added with ID " + newRecord.userID);
        return newUser;
    }

    /**
     * Returns the GameUser associated with a given username if the password matches
     * @param username  Username for the desired user
     * @param password  Password for the desired user
     * @return  GameUser corresponding to the username/password, or
     *          null if not found or wrong password
     */
    GameUser validateLogin(String username, String password) {
        GameUserRecord userRecord = null;
        try {
            Statement statement = dataConnection.createStatement();
            // Read users that aren't deleted
            ResultSet userResult = statement.executeQuery(
                    "SELECT * FROM User WHERE StatusID != " + Status.Deleted.ordinal() + " AND Username = " + username);
            if (userResult.isBeforeFirst()) //empty result list
                userRecord = makeUserRecordFromResultSet(userResult);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to access user table for password validation!", e);
        }
        if (userRecord == null)
            return null;

        //The following is adapted from https://www.baeldung.com/java-password-hashing
        try {
            byte[] salt = userRecord.passwordSalt;
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keySize);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            if (hash == userRecord.passwordHash)
                return getUser(userRecord.userID); //matches!
            //else fall through
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to attempt password validation!", e);
        }

        return null;
    }

    /**
     * Reads the database and fills the HashMaps as appropriate. Intended to be called once
     * at time of initialization.
     *
     * @param sqlDataConnection connection on which to make all future SQL queries
     */
    void refresh(Connection sqlDataConnection, GameItemRepository gameItemRepo) {
        itemRepo = gameItemRepo;
        dataConnection = sqlDataConnection;
        try {
            Statement statement = dataConnection.createStatement();
            // Read users that aren't deleted
            ResultSet userResult = statement.executeQuery(
                    "SELECT * FROM User u WHERE StatusID != " + Status.Deleted.ordinal());
            while (userResult.next()) {
                GameUserRecord rec = makeUserRecordFromResultSet(userResult);
                userMap.put(rec.userID, new GameUser(rec));
            }

            // Read mapping of users to items that they own
            ResultSet mappingResult = statement.executeQuery(
                    "SELECT * FROM ItemContainer_User_Permissions WHERE PermissionID = "
                            + Permission.Owner.ordinal());
            while (mappingResult.next()) {
                int itemID = mappingResult.getInt("ItemID");
                int userID = mappingResult.getInt("UserID");

                // not worrying about StartSlot, EndSlot, or Modifier right now...
                GameItemContainer container = itemRepo.getContainer(itemID);
                GameUser user = getUser(userID);
                user.addItem(container);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static info!", e);
        }
    }

    /**
     * Converts a ResultSet to a GameUserRecord for further processing. It assumes that the record
     * it should be getting data for was labeled with the name "u".
     * @param userResult    The ResultSet that's the result of an SQL query with item labeled "u"
     * @return GameItemRecord filled with data from the current item in the ResultSet.
     */
    private GameUserRecord makeUserRecordFromResultSet(ResultSet userResult) {
        GameUserRecord rec = new GameUserRecord();
        try {
            rec.userID = userResult.getInt("u.UserID");
            rec.name = userResult.getString("u.Name");
            rec.username = userResult.getString("u.Username");
            rec.passwordHash = userResult.getBytes("u.passwordHash");
            rec.passwordSalt = userResult.getBytes("u.passwordSalt");
            rec.statusID = userResult.getInt("u.StatusID");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from user result set", e);
        }
        return rec;
    }

}