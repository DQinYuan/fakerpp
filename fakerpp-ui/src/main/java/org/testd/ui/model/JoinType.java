package org.testd.ui.model;

public enum JoinType {

    LEFT("Left"), RIGHT("Right");

    private String str;

    JoinType(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
