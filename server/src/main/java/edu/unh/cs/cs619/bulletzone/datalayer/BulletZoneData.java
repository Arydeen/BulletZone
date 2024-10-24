/**
 * This is the main interaction point for the datalayer package.
 * <p/>
 * Each instance of this class corresponds to an instance of a game database. In most cases,
 * you will just want a single instance (and it could be problematic to have more than one
 * instance). However, since it's possible that one application may need to be able to access
 * more than one game database at a time, this class is not a singleton.
 * It is important to remember to close the database connection when you're done with the
 * BulletZoneData object, so that you don't open so many connections that the server becomes
 * inaccessible (the connections will otherwise time out eventually, but it may not be as
 * early as you like).
 *
 */
package edu.unh.cs.cs619.bulletzone.datalayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccountRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemContainer;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemCategoryRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemPropertyRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemType;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemTypeRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntity;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.Permission;
import edu.unh.cs.cs619.bulletzone.datalayer.terrain.TerrainType;
import edu.unh.cs.cs619.bulletzone.datalayer.terrain.TerrainTypeRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.user.AssociationManager;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUserRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.user.PermissionManager;

public class BulletZoneData {
    private String _url;
    private String _username;
    private String _password;
    private String _commandPrefix = "";
    private String _commandSuffix = "";
    public ItemPropertyRepository properties;
    public ItemCategoryRepository categories;
    public ItemTypeRepository types;
    public GameItemRepository items = new GameItemRepository();
    public TerrainTypeRepository terrains;
    public GameUserRepository users = new GameUserRepository();
    public BankAccountRepository accounts = new BankAccountRepository();
    public PermissionManager permissions = new PermissionManager();
    public AssociationManager associations = new AssociationManager();

    /**
     * Opens the database at the specified URL and initializes internal structures
     * @param url       URL for the database, usually in the form "jdbc:mysql://server-name.com:3306/database-name"
     * @param username  Username with appropriate access to the database specified by url
     * @param password  Password for the user specified by username
     */
    public BulletZoneData(String url, String username, String password)
    {
        _url = url;
        _username = username;
        _password = password;
        _commandPrefix = "BEGIN NOT ATOMIC\n"; //needed for MairaDB, for whatever reason
        _commandSuffix = "\nEND\n";
        Connection dataConnection = getConnection();
        if (dataConnection == null)
            return;

        initialize(dataConnection);
    }

    /**
     * Opens an in-memory database (for testing purposes) and initializes internal structures
     */
    public BulletZoneData() {
        // see https://www.baeldung.com/java-in-memory-databases for other in-memory DB options
        _url = "jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1";
        //_url = "jdbc:derby:memory:testDB;create=true";
        _username = "sa";
        _password = "sa";

        try {
            Class.forName("org.h2.Driver");
            //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection dataConnection = getConnection();
        if (dataConnection == null)
            return;

        initialize(dataConnection);
    }

    /**
     * Re-initializes the internal data structures from the database (primarily for testing purposes)
     */
    public void refresh() {
        initialize(getConnection());
    }

    /**
     * Lists the names of all tables in the currently open database to System.out, one per line.
     * This is intended to be a debugging method only.
     */
    public void listTables() {
        try {
            Connection dataConnection = getConnection();
            if (dataConnection == null)
                return;
            Statement statement = dataConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            System.out.println("Database tables:");
            while (resultSet.next())
                System.out.println(resultSet.getString(1));
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot show tables!", e);
        }
    }

    /**
     * Drops all tables (WHICH DESTROYS ALL EXISTING DATA!!), then rebuilds it by calling the
     * initializeData method.
     */
    public void rebuildData() {
        executeScript("/DropTables.sql", "Cannot drop all tables!");
        initializeData();
        refreshData();
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Method for getting an appropriate database connection. Intended to be package-level,
     * but public to allow for related packages to access it.
     * @return Connection to SQL database that can be used for further queries. Note that
     *         the caller should close the connection when finished.
     */
    public Connection getConnection() {
        Connection dataConnection;
        try {
            dataConnection = DriverManager.getConnection(_url, _username, _password);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to the database!", e);
        }
        return dataConnection;
    }

    /**
     * Shared construction details for ensuring the tables for the database exist
     * and filling the in-memory data structures
     * @param dataConnection Connection to an existing database
     */
    private void initialize(Connection dataConnection)
    {
        try {
            Statement statement = dataConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            if (!resultSet.next()) //no next row means we don't have any tables
                initializeData();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot access tables!", e);
        }

        try {
            properties = new ItemPropertyRepository(dataConnection);
            categories = new ItemCategoryRepository(dataConnection, properties);
            types = new ItemTypeRepository(dataConnection, categories);
            terrains = new TerrainTypeRepository(dataConnection);
            refreshData();
        } catch (IllegalStateException e) {
            System.out.println("Unable to read initial data. Exception listed below, but continuing...");
            e.printStackTrace();
        }
    }

    /**
     * Assuming an empty database, creates all the appropriate tables and then populates
     * them with appropriate data (especially for enumeration tables).
     */
    private void initializeData() {
        executeScript("/CreateTables.sql", "Cannot create tables!");
        executeScript("/PopulateTables.sql", "Cannot populate tables!");
    }

    /**
     * Refreshes all repositories after initialization.
     */
    private void refreshData() {
        items.refresh(this, types);
        users.refresh(this, items);
        accounts.refresh(this);
        permissions.refresh(this , Arrays.asList(items, accounts), users);
        associations.refresh(this, Arrays.asList(items, users, accounts), users);
    }
    /**
     * Executes the passed SQL script on the current database (it assumes the account given
     * at construction time has the appropriate permissions to execute whatever is in the script).
     * @param resourceName  Name of the Java resource file, usually of the form "/SomeFile.sql"
     * @param errorMessage  String to be printed
     * @return  Number of rows impacted by the query.
     */
    int executeScript(String resourceName, String errorMessage)
    {
        Connection dataConnection = getConnection();
        int resultSet = 0;

        if (dataConnection == null)
            return resultSet;

        try {
            Statement statement = dataConnection.createStatement();
            InputStream in = getClass().getResourceAsStream(resourceName);
            if (in == null)
                System.err.println("missing resource file");
            else {
                String createQuery = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
                resultSet = statement.executeUpdate(_commandPrefix + createQuery + _commandSuffix);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(errorMessage, e);
        }
        return resultSet;
    }

    public static void main(String[] args) {
        //String url = "jdbc:mysql://stman1.cs.unh.edu:3306/cs6190";
        //String username = "mdp";
        //String password = "Drag56kes";

        //BulletZoneData d = new BulletZoneData(url, username, password);
        BulletZoneData d = new BulletZoneData(); //Just use in-memory database
        //d.listTables();

        // Drop all tables and rebuilds an empty database
        d.rebuildData();
        //d.listTables();

        // Create a Garage bay and a couple of tanks to go in it, then remove them and delete the first tank
        GameItemContainer bay = d.items.createContainer(d.types.GarageBay);
        GameItemContainer tank1 = d.items.createContainer(d.types.TankFrame);
        GameItemContainer tank2 = d.items.createContainer("Standard tank frame");
        d.items.addItemToContainer(tank1, bay);
        d.items.addItemToContainer(tank2, bay);
        d.items.removeAllFromContainer(bay);
        d.items.delete(tank1.getId());
        if (d.items.getItemOrContainer(tank1.getId()) == null)
            System.out.println("Successfully deleted tank1");

        // Create a user and verify their credentials, then set tank2 to be owned by them
        GameUser user = d.users.createUser("Test User", "testuser", "testPass");
        GameUser user2 = d.users.validateLogin("testuser", "testPass");
        if (user == user2 && user2 != null)
            System.out.println("User creation/validation successful");
        else
            System.out.println("Problem: user is " + user + " and user2 is " + user2);
        if (tank2 == null)
            System.out.println("Somehow tank2 is null");
        d.permissions.setOwner(tank2, user2);

        // Create a bank account for the user
        BankAccount account = d.accounts.create();
        d.permissions.setOwner(account, user2);

        // Create another user (will fail if there already, but then the validation should work)
        // Then grant this new user several permissions on tank2 (and check them). This part only prints
        // something out if something goes wrong.
        GameUser user3 = d.users.createUser("Second User", "user2", "xyzzy");
        user3 = d.users.validateLogin("user2", "xyzzy");
        if (!d.permissions.grant(tank2, user3, Permission.Add))
            System.out.println("Add permission not granted");
        if (!d.permissions.check(tank2, user3, Permission.Add))
            System.out.println("Add permission granted but doesn't check");
        if (!d.permissions.grant(tank2, user3, Permission.Remove))
            System.out.println("Remove permission not granted");
        if (!d.permissions.check(tank2, user3, Permission.Remove))
            System.out.println("Remove permission granted but doesn't check");
        if (!d.permissions.grant(tank2, user3, Permission.Use))
            System.out.println("Use permission not granted");
        if (!d.permissions.check(tank2, user3, Permission.Use))
            System.out.println("Use permission granted but doesn't check");
        if (d.permissions.grant(tank2, user3, Permission.Use))
            System.out.println("Use permission granted twice");
        if (!d.permissions.revoke(tank2, user3, Permission.Remove))
            System.out.println("Remove permission not revoked");
        if (d.permissions.check(tank2, user3, Permission.Transfer))
            System.out.println("Transfer permission never granted but somehow checks");
        if (d.permissions.revoke(tank2, user3, Permission.Transfer))
            System.out.println("Nonexistent transfer permission somehow revoked");

        //Create another user, then loop through all users to see what permissions they have
        GameUser user4 = d.users.createUser("UserF", "userF", "xyzzy");
        //d.permissions.setOwner(bay, user4);
        for (GameUser u : d.users.getUsers()) {
            PermissionManager.AccessibleTargets ip = d.permissions.getUserPermissions(u);
            for (OwnableEntity thing : ip.getItems()) {
                System.out.print(u.getName() + " has permissions for " + thing + ": ");
                for (Permission p : ip.getPermissionsOnItem(thing.getId())) {
                    System.out.print(p.name() + " ");
                }
                System.out.println();
            }
        }
        // Check what permissions our latest user has on tank2 by looping through valid permissions
        for (Permission p : Permission.values()) {
            System.out.println(user4.getName() + " Permission check for " + p.name() + ": " + d.permissions.check(tank2, user4, p));
            System.out.println(user4.getName() + " Permission ID check for " + p.name() + ": " + d.permissions.check(tank2.getId(), user4.getId(), p));
        }
        //d.permissions.removeOwner(tank2);

        // List the type names and ID's for all ItemTypes
        for (ItemType t : d.types.getAll())
        {
            System.out.println("Type " + t.getName() + " has type ID " + t.getID());
        }
        System.out.println("BattleSuit Type is " + d.types.BattleSuit);

        // List the terrain names and ID's for all TerrainTypes
        for (TerrainType t : d.terrains.getAll())
        {
            System.out.println("Terrain " + t.getName() + " has type ID " + t.getID());
        }
        System.out.println("Open Water Type is " + d.terrains.OpenWater);

    }
}