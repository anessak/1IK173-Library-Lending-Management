package com.llm.membershipadmin;

import com.librarylendingmanagement.infrastructure.events.OnMemberBreachedRegulation;
import com.librarylendingmanagement.infrastructure.events.OnMemberCreated;
import com.librarylendingmanagement.infrastructure.events.OnMemberSuspended;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class MembershipManager {
    IMembershipStore membershipStore;
    private Logger logger;
    private EventBus Bus;
    public MembershipManager(IMembershipStore store, Logger log, EventBus b){
        this.membershipStore=store;
        this.logger = log;
        this.Bus=b;
        this.Bus.register(this);

        logger.info("Finished constructor of MembershipManager");
    }
    public void registerNewLibraryMember(Member member)
    {
        var findmember=this.membershipStore.getMember(member.getMemberId());
        if(findmember==null) {
            //publish new message that member is created
            // publish(MemberCreated(member.memberId))
            var maxNumberOfItems = 0;
            if (member.getRole() == MemberRole.Undergraduate)
                maxNumberOfItems = 3;
            else if (member.getRole() == MemberRole.Postgraduate)
                maxNumberOfItems = 5;
            else if (member.getRole() == MemberRole.PhD)
                maxNumberOfItems = 7;
            else if (member.getRole() == MemberRole.Teacher)
                maxNumberOfItems = 10;

            membershipStore.insertNewMember(member);
            logger.info("Add new member");
            this.Bus.post(new OnMemberCreated(member.getMemberId(), maxNumberOfItems));
        }
    }
    public Member getMemberById(int memberId){
        return this.membershipStore.getMember(memberId);
    }
    public ArrayList<Member> searchMembers(int memberIdWildCard){
        return this.membershipStore.searchMembers(memberIdWildCard);
    }
    public void removeMember(int memberId){
        var member = this.membershipStore.getMember(memberId);
        if(member!=null)
            this.membershipStore.deleteMember(member);

    }

    public boolean login(int memberId, String password){

        var member = this.membershipStore.getMember(memberId);
        return (memberId==member.getMemberId() && password.equals(member.getPassword()));

    }
    public void suspendMember(int memberId){
        var member = this.membershipStore.getMember(memberId);
        if(member!=null)
            this.membershipStore.changeMemberStatus(memberId,MemberStatus.Suspended);
    }
    @SuppressWarnings("unused")
    @Subscribe
    public void suspendRegistrationHandler(OnMemberSuspended memberSuspended){
        //receive message from Lending and then update user status to suspended.
        var member = this.membershipStore.getMember(memberSuspended.getMemberId());
        if(member!=null)
            this.membershipStore.changeMemberStatus(memberSuspended.getMemberId(),MemberStatus.Suspended);
    }
    @SuppressWarnings("unused")
    @Subscribe
    public void removeMemberDueToRegulationBreach(OnMemberBreachedRegulation memberToDelete) {
        //receive message from Lending and then update user status to breach-deleted.
        var member = this.membershipStore.getMember(memberToDelete.getMemberId());
        if(member!=null)
            this.membershipStore.deleteMember(member);
    }
}
