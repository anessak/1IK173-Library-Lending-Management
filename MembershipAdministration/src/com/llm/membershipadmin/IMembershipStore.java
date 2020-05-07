package com.llm.membershipadmin;

import java.util.ArrayList;

public interface IMembershipStore {
    void insertNewMember(Member member);

    void updateMemberRole(int memberId, MemberRole role);

    void changeMemberStatus(int memberId, MemberStatus status);

    Member getMember(int memberId);

    void deleteMember(Member member);

    ArrayList<Member> searchMembers(int memberIdWildCard);
}
