package org.testd.ui.model;

public enum JoinType {

    LEFT("Left"), RIGHT("Right");

    public static final JoinType defaultType = LEFT;

    private String str;

    JoinType(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    /**
     * handle by join type
     * @param left
     * @param right
     */
    public void handle(Runnable left, Runnable right) {
        switch (this) {
            case LEFT:
                left.run();
                break;
            case RIGHT:
                right.run();
                break;
        }
    }
}
