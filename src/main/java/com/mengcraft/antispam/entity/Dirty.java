package com.mengcraft.antispam.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created on 16-11-17.
 */
@Entity
@Table(name = "antispam_dirty")
public class Dirty {

    @Id
    private int id;

    @Column(unique = true)
    private String line;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

}
