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
    public MemberShipResultMessage registerNewLibraryMember(Member member) {
        logger.info("Entering method registerNewLibraryMember with memberId:{} Name:{} {} and role:{}",
                member.getMemberId(), member.getFirstName(), member.getLastName(),member.getRole());

        var findmember = this.membershipStore.getMember(member.getMemberId());
        if (findmember != null) {
            logger.error("Unable to find member with id:{}", member.getMemberId());
            return MemberShipResultMessage.Conflict;
        }

        var maxNumberOfItems = switch (member.getRole()) {
            case Undergraduate -> 3;
            case Postgraduate -> 5;
            case PhD -> 7;
            case Teacher -> 10;
            default -> 0;
        };

        membershipStore.insertNewMember(member);
        logger.info("Add new member id:{} Name:{} {} ", member.getMemberId(),member.getFirstName(),member.getLastName());
        this.Bus.post(new OnMemberCreated(member.getMemberId(), maxNumberOfItems));

        logger.info("Succesfully added new member id:{} Name:{} {} ", member.getMemberId(),member.getFirstName(),member.getLastName());
        return MemberShipResultMessage.Ok;
    }
    public MemberShipResultMessage updateMember(Member updateMember){
        logger.info("Entering method updateMember with memberId:{} Name:{} {} and role:{}",
                updateMember.getMemberId(), updateMember.getFirstName(), updateMember.getLastName(),updateMember.getRole());

        var member=this.membershipStore.getMember(updateMember.getMemberId());
        if(member==null){
            logger.error("Unable to find member with id:{}", member.getMemberId());
            return MemberShipResultMessage.Conflict;
        }
        this.membershipStore.updateMember(updateMember);

        logger.info("Sucessfully updated member with id:{} Name:{} {} ", member.getMemberId(),member.getFirstName(),member.getLastName());
        return MemberShipResultMessage.Ok;
    }
    public Member getMemberById(int memberId){
        logger.info("calling method getMemberById with memberId:{} ", memberId);
        return this.membershipStore.getMember(memberId);
    }
    public ArrayList<Member> searchMembers(int memberIdWildCard){
        logger.info("calling method searchMembers with searchPattern:{} ", memberIdWildCard);
        return this.membershipStore.searchMembers(memberIdWildCard);
    }
    public MemberShipResultMessage removeMember(int memberId){
        logger.info("calling method removeMember with memberId:{} ", memberId);
        var member = this.membershipStore.getMember(memberId);

        if(member==null) {
            logger.error("Unable to find member with memberId:{} ", memberId);
            return MemberShipResultMessage.NotFound;
        }
        this.membershipStore.deleteMember(member);
        logger.info("Sucesfully deleted member with memberId:{} ", memberId);

        return MemberShipResultMessage.Ok;

    }

    public boolean login(int memberId, String password){
        logger.info("Entering method login with memberid:{} and password:{}", memberId, password);
        var member = this.membershipStore.getMember(memberId);

        return (memberId==member.getMemberId() && password.equals(member.getPassword()));

    }
    public MemberShipResultMessage suspendMember(int memberId){
        try {
            logger.info("Entering method suspendMember with memberid:{}", memberId);
            var member = this.membershipStore.getMember(memberId);
            if (member == null) {
                logger.error("Unable to find memberid:{}", memberId);
                return MemberShipResultMessage.NotFound;
            }
            this.membershipStore.changeMemberStatus(memberId, MemberStatus.Suspended);
            logger.info("Sucesfully suspended member with memberid:{}", memberId);
            return MemberShipResultMessage.Ok;
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            throw ex;
        }
    }
    @SuppressWarnings("unused")
    @Subscribe
    public void suspendRegistrationHandler(OnMemberSuspended memberSuspended){
        logger.info("Received Message OnMemberSuspended with memberid:{}", memberSuspended.getMemberId());
        logger.info("Get member with memberid:{}", memberSuspended.getMemberId());
        var member = this.membershipStore.getMember(memberSuspended.getMemberId());
        if(member!=null) {
            logger.info("Member found!", memberSuspended.getMemberId());
            this.membershipStore.changeMemberStatus(memberSuspended.getMemberId(), MemberStatus.Suspended);
            logger.info("Member with id:{} suspended!", memberSuspended.getMemberId());
        }
        else
            logger.error("not found member->Get member with memberid:{} ", memberSuspended.getMemberId());
    }
    @SuppressWarnings("unused")
    @Subscribe
    public void removeMemberDueToRegulationBreach(OnMemberBreachedRegulation memberToDelete) {
        logger.info("Received Message OnMemberBreachedRegulation with memberid:{}", memberToDelete.getMemberId());
        logger.info("Get member with memberid:{}", memberToDelete.getMemberId());
        var member = this.membershipStore.getMember(memberToDelete.getMemberId());
        if(member!=null) {
            this.membershipStore.deleteMember(member);
            logger.info("Member with id:{} deleted due to regualtion breach!", memberToDelete.getMemberId());
        }
    }
}
