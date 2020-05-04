package com.llm.membershipadmin;

public class Member {
    private int memberId;
    private String ssn;
    private String firstName;
    private String lastName;
    private String password;
    private MemberStatus memberStatus;
    private MemberRole role;
    public Member()
    {}
    public Member(String ssn,String fName,String lName, MemberRole role)
    {
        this.ssn=ssn;
        this.firstName=fName;
        this.lastName=lName;
        this.role=role;
        generateMemberId();
    }
    public Member(String ssn,String fName,String lName,MemberRole role, String pwd)
    {
        this(ssn,fName,lName,role);
        this.password=pwd;
    }
    private void generateMemberId()
    {

    }

}
