package com.benayn.berkeley;

import java.io.Serializable;
import java.util.Date;

public class Person implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -1600256029283522097L;
    
    private Long id;
    private String firstName;
    private String lastName;
    private Date birthday;
    private String address;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Date getBirthday() {
        return birthday;
    }
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    @Override
    public String toString() {
        return "Person [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
                + ", birthday=" + birthday + ", address=" + address + "]";
    }
    
}
