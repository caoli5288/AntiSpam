package com.mengcraft.antispam.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created on 17-5-6.
 */
@Entity
@Table(name = "antispam_command_wl")
public class DWhitelist {

    @Id
    private int id;

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
