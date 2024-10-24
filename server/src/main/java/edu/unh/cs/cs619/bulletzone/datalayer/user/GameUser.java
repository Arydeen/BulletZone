package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.util.Collection;
import java.util.HashSet;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.core.Entity;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItem;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemContainer;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntity;

public class GameUser extends Entity {
    protected String name;
    protected String username;
    //password info not stored in game
    protected HashSet<OwnableEntity> ownedEntities = new HashSet<OwnableEntity>();

    public String getName() { return name; }

    public String getUsername() { return username; }

    public Collection<GameItemContainer> getOwnedContainerItems() {
        HashSet<GameItemContainer> ownedItems = new HashSet<GameItemContainer>();
        for (OwnableEntity e : ownedEntities) {
            EntityType t = e.getEntityType();
            if (t == EntityType.ItemContainer) {
                ownedItems.add((GameItemContainer) e);
            }
        }
        return ownedItems;
    }

    public Collection<GameItem> getOwnedItems() {
        HashSet<GameItem> ownedItems = new HashSet<GameItem>();
        for (OwnableEntity e : ownedEntities) {
            EntityType t = e.getEntityType();
            if (t == EntityType.Item || t == EntityType.ItemContainer) {
                ownedItems.add((GameItem) e);
            }
        }
        return ownedItems;
    }

    public Collection<BankAccount> getOwnedAccounts() {
        HashSet<BankAccount> ownedItems = new HashSet<BankAccount>();
        for (OwnableEntity e : ownedEntities) {
            EntityType t = e.getEntityType();
            if (t == EntityType.BankAccount) {
                ownedItems.add((BankAccount) e);
            }
        }
        return ownedItems;
    }

    public Collection<OwnableEntity> getOwnedEntities() { return ownedEntities; }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    GameUser(GameUserRecord rec) {
        super(rec);
        name = rec.name;
        username = rec.username;
    }

    void addPermissionTarget(OwnableEntity pt) {
        ownedEntities.add(pt);
    }

    void removePermissionTarget(OwnableEntity pt) {
        ownedEntities.remove(pt);
    }
}