package com.llm.membershipadmin;

public interface IMembershipStore {
    void insertNewMember(Member member);

    void updateMemberRole(int memberId, MemberRole role);

    Member getMember(int memberId);
}
