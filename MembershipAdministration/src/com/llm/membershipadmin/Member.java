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
    public Member(int mid, String ssn,String fName,String lName, MemberRole role)
    {
        this.memberId=mid;
        this.ssn=ssn;
        this.firstName=fName;
        this.lastName=lName;
        this.role=role;
        this.memberStatus=MemberStatus.Active;
        this.dateCreated=LocalDateTime.now();
    }
    public Member(int memberid, String ssn,String fName,String lName,MemberRole role, MemberStatus status, String pwd, LocalDateTime dateCreated)
    {
        this(memberid,ssn,fName,lName,role);
        this.password=pwd;
        this.memberStatus=status;
        this.dateCreated=dateCreated;
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

    public String getPassword() {
        return this.password;
    }
}
