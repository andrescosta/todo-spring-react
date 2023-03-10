package com.coeux.todo.filters;



import java.util.List;
import java.util.Objects;

/**
 * Classes in this `impl` implementation package may change in NON backward compatible way, and should ONLY be used as
 * a "runtime" dependency.
 */
public class JwkKeys {

    private List<JwkKey> keys;

    public List<JwkKey> getKeys() {
        return keys;
    }

    public JwkKeys setKeys(List<JwkKey> keys) {
        this.keys = keys;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwkKeys jwkKeys = (JwkKeys) o;
        return Objects.equals(keys, jwkKeys.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }
}