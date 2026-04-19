package org.example.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ID id;

    public BaseEntity() {}

    public BaseEntity(ID id) {
        this.id = id;
    }

    public ID getId() { return id; }
    public void setId(ID id) { this.id = id; }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BaseEntity)) return false;
        BaseEntity<?> other = (BaseEntity<?>) obj;
        return Objects.equals(id, other.id);
    }
}

