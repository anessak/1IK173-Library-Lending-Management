package com.llm.membershipadmin;

import java.util.ArrayList;

public interface IMembershipStore {
    void insertNewMember(Member member);

    void changeMemberStatus(int memberId, MemberStatus status);

    Member getMember(int memberId);

    void deleteMember(Member member);

    void updateMember(Member memberToUpdate);

    ArrayList<Member> searchMembers(int memberIdWildCard);
}
