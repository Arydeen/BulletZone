package edu.unh.cs.cs619.bulletzone.datalayer.core;

public class Entity {
    protected EntityRecord record;
    protected int entityID;
    protected int statusID;
    protected final EntityType entityType;

    public Entity(EntityType et) { entityType = et; }

    public Entity(EntityRecord rec) {
        entityType = rec.entityType;
        entityID = rec.getID();
        statusID = rec.getStatusID();
    }

    public EntityType getEntityType() { return entityType; }

    public int getId() { return entityID; }

    @Override
    public String toString() { return entityType.name() + " (ID: " + entityID + ")"; }

}
