package com.llm.membershipadmin;

import java.util.ArrayList;

public interface IMembershipStore {
    int insertNewMember(Member member);

    void reActivateUser(int memberId);

    void suspendUser(int memberId);

    Member getMember(int memberId);

    int deleteMember(Member member);

    void updateMember(Member memberToUpdate);

    ArrayList<Member> searchMembers(int memberIdWildCard);
}
