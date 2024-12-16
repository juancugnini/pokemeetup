package io.github.pokemeetup.world.model;

public enum ObjectType {
    TREE_0(true, true),
    TREE_1(true, true),
    CACTUS(true, true),
    BUSH(true, true),
    SUNFLOWER(true, false),
    APRICORN_TREE(true, true),
    DEAD_TREE(true, true),
    ;

    private final boolean permanent;
    private final boolean collidable;

    ObjectType(boolean permanent, boolean collidable) {
        this.permanent = permanent;
        this.collidable = collidable;
    }

    public boolean isPermanent() { return permanent; }
    public boolean isCollidable() { return collidable; }
}
