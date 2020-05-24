package com.llm.membershipadmin;

import com.librarylendingmanagement.infrastructure.events.OnMemberBreachedRegulation;
import com.librarylendingmanagement.infrastructure.events.OnMemberCreated;
import com.librarylendingmanagement.infrastructure.events.OnMemberSuspended;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
            if(findmember.getMemberStatus()==MemberStatus.Active) {
                logger.warn("User with same id already exists ID:{} Name: {} {}", member.getMemberId(), member.getFirstName(), member.getLastName());
                return MemberShipResultMessage.Conflict;
            }
            if (findmember.getMemberStatus() == MemberStatus.Suspended) {
                logger.warn("Member is SUSPENDED ID:{} Name {} {}", findmember.getMemberId(), findmember.getFirstName(), findmember.getLastName());
                return MemberShipResultMessage.Suspended;
            }
        }

        var maxNumberOfItems = switch (member.getRole()) {
            case Undergraduate -> 3;
            case Postgraduate -> 5;
            case PhD -> 7;
            case Teacher -> 10;
            case Admin-> 0;
        };

        logger.info("Add new member to database ID:{} Name:{} {} ", member.getMemberId(),member.getFirstName(),member.getLastName());
        var result=membershipStore.insertNewMember(member);
        if(result==0) {
            logger.info("Sending Message OnMemberCreated(memberid:{}, maxNumberOfItems:{}) ", member.getMemberId(), maxNumberOfItems);
            this.Bus.post(new OnMemberCreated(member.getMemberId(), maxNumberOfItems));
            logger.info("Succesfully added new member id:{} Name:{} {} ", member.getMemberId(), member.getFirstName(), member.getLastName());
            return MemberShipResultMessage.Ok;
        }
        else
            return MemberShipResultMessage.Error;

    }
    public MemberShipResultMessage updateMember(Member updateMember){
        if(updateMember==null){
            logger.error("updateMember method called with NULL argument");
            return MemberShipResultMessage.Error;
        }
        logger.info("Entering method updateMember with memberId:{} Name:{} {} and role:{}",
                updateMember.getMemberId(), updateMember.getFirstName(), updateMember.getLastName(),updateMember.getRole());

        var member=this.membershipStore.getMember(updateMember.getMemberId());
        if(member==null){
            logger.error("Unable to find member with id:{}", updateMember.getMemberId());
            return MemberShipResultMessage.NotFound;
        }
        this.membershipStore.updateMember(updateMember);

        logger.info("Sucessfully updated member with id:{} Name:{} {} ", member.getMemberId(),member.getFirstName(),member.getLastName());
        return MemberShipResultMessage.Ok;
    }
    public Member getMemberById(int memberId){
        logger.info("Entering method getMemberById with memberId:{} ",
                memberId);
        logger.info("calling method getMemberById with memberId:{} ", memberId);
        return this.membershipStore.getMember(memberId);
    }
    public ArrayList<Member> searchMembers(int memberIdWildCard){
        logger.info("Entering method searchMembers with searchPattern:{} ", memberIdWildCard);
        return this.membershipStore.searchMembers(memberIdWildCard);
    }
    public MemberShipResultMessage removeMember(int memberId){
        logger.info("Entering method removeMember with memberId:{} ", memberId);
        var member = this.membershipStore.getMember(memberId);

        if(member==null) {
            logger.error("Unable to find member with memberId:{} ", memberId);
            return MemberShipResultMessage.NotFound;
        }
        var result= this.membershipStore.deleteMember(member);
        if(result==0) {
            logger.info("Sucesfully deleted member with memberId:{} ", memberId);
            return MemberShipResultMessage.Ok;
        }
        else {
            logger.info("Error while deleting member with memberId:{} ", memberId);
            return MemberShipResultMessage.Error;
        }

    }

    public MemberShipResultMessage login(int memberId, String password){
        logger.info("Entering method login with memberid:{} and password:{}", memberId, password);
        var member = this.membershipStore.getMember(memberId);
        if(member==null)
            return MemberShipResultMessage.NotFound;

        if(memberId==member.getMemberId() && password.equals(member.getPassword())) {
            if(member.getRole()==MemberRole.Admin)
                return MemberShipResultMessage.AdminOk;
            else
                return MemberShipResultMessage.Ok;
        }
        return MemberShipResultMessage.Error;

    }
    public MemberShipResultMessage suspendMember(int memberId) {
        logger.info("Entering method suspendMember with memberid:{}", memberId);
        var member = this.membershipStore.getMember(memberId);
        if (member == null) {
            logger.error("Unable to find memberid:{}", memberId);
            return MemberShipResultMessage.NotFound;
        }
        this.membershipStore.suspendUser(memberId);
        logger.info("Sucesfully suspended member with memberid:{}", memberId);
        return MemberShipResultMessage.Ok;
    }
    public MemberShipResultMessage reActiveUser(int memberId){
        logger.info("Entering method reActiveUser with memberid:{}", memberId);
        logger.info("Get member with memberid:{}",memberId);
        var member = this.membershipStore.getMember(memberId);
        if(member==null) {
            logger.error("not found member->Get member with memberid:{} ", memberId);
            return MemberShipResultMessage.NotFound;
        }
        else {
            logger.info("Member found!");
            this.membershipStore.reActivateUser(memberId);
            logger.info("Member with id:{} Activated!", memberId);
            return MemberShipResultMessage.Ok;
        }
    }
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void suspendRegistrationHandler(OnMemberSuspended memberSuspended){
        logger.info("Received Message OnMemberSuspended with memberid:{}", memberSuspended.getMemberId());
        logger.info("Get member with memberid:{}", memberSuspended.getMemberId());
        var member = this.membershipStore.getMember(memberSuspended.getMemberId());
        if(member!=null) {
            logger.info("Member found!");
            if(member.getMemberStatus()!=MemberStatus.Suspended) {
                this.membershipStore.suspendUser(memberSuspended.getMemberId());
                logger.info("Member with id:{} suspended!", memberSuspended.getMemberId());
            }
        }
        else
            logger.error("not found member->Get member with memberid:{} ", memberSuspended.getMemberId());
    }
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void removeMemberDueToRegulationBreach(OnMemberBreachedRegulation memberToDelete) {
        logger.info("Received Message OnMemberBreachedRegulation with memberid:{}", memberToDelete.getMemberId());
        logger.info("Get member with memberid:{}", memberToDelete.getMemberId());
        var member = this.membershipStore.getMember(memberToDelete.getMemberId());
        if(member!=null) {
            this.membershipStore.deleteMember(member);
            logger.info("Member with id:{} deleted due to regulation breach!", memberToDelete.getMemberId());
        }
    }
}
