package com.llm.membershipadmin;

import java.time.LocalDateTime;

public class Member {
    private int memberId;
    private String ssn;
    private String firstName;
    private String lastName;
    private String password;
    private MemberStatus memberStatus;
    private MemberRole role;
    private LocalDateTime dateCreated;

    public Member()
    {}
    public Member(String ssn,String fName,String lName, MemberRole role)
    {
        this.ssn=ssn;
        this.firstName=fName;
        this.lastName=lName;
        this.role=role;
        this.dateCreated=LocalDateTime.now();
        generateMemberId();
    }
    public Member(int memberid, String ssn,String fName,String lName,MemberRole role, String pwd, LocalDateTime dateCreated)
    {
        this(ssn,fName,lName,role);
        this.memberId=memberid;
        this.password=pwd;
        this.dateCreated=dateCreated;
    }
    private void generateMemberId()
    {

    }

    public int getMemberId() {
        return memberId;
    }

    public String getSsn() {
        return ssn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public MemberRole getRole() {
        return role;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public MemberStatus getMemberStatus() {
        return memberStatus;
    }

}
