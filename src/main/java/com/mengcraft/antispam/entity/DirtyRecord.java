package com.mengcraft.antispam.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created on 16-11-17.
 */
@Data
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "antispam_log")
public class DirtyRecord {

    @Id
    private int id;

    @Column
    private String player;

    @Column
    private String chat;

    @Column
    private String ip;

    @Column
    private String server;

    @CreatedTimestamp
    private Timestamp time;
}
