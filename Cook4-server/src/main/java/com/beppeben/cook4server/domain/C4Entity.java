package com.beppeben.cook4server.domain;

public class C4Entity {

    protected Long id;

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(getClass().equals(other.getClass()))) {
            return false;
        }
        C4Entity otherTrans = (C4Entity) other;
        if (id == null || otherTrans.id == null) {
            return false;
        }
        return id.equals(otherTrans.id);
    }

}
