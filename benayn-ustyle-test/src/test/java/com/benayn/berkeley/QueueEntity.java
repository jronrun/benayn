package com.benayn.berkeley;

import java.io.Serializable;
import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version=1)
public class QueueEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2286000447336994504L;
    
    @PrimaryKey
    private Long id;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String name;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Date date;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String address;
    
    private String test;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getTest() {
        return test;
    }
    public void setTest(String test) {
        this.test = test;
    }

}
