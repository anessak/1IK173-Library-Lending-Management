package com.llm.membershipadmin;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class MembershipManager {
    IMembershipStore membershipStore;
    private Logger logger;

    public MembershipManager(IMembershipStore store, Logger logger){

        this.membershipStore=store;
        this.logger =logger;
        logger.info("Finished constructor of MembershipManager");
    }
    public void registerNewLibraryMember(Member member)
    {
        //publish new message that member is created
        // publish(MemberCreated(member.memberId))
    }
    public ArrayList<Member> searchMembers(int memberIdWildCard){
        return null;
    }
    public void removeMember(int memberId){

    }
    public void cancelMemberShip(int memberId){

    }

    public void suspendRegistrationHandler(int memberId){
        //receive message from Lending and then update user status to suspended.
    }
}
