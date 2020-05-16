package com.llm.membershiplending;

import com.librarylendingmanagement.infrastructure.events.OnBookItemLended;
import com.librarylendingmanagement.infrastructure.events.OnBookItemReturned;
import com.librarylendingmanagement.infrastructure.events.OnMemberCreated;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.UUID;

public class LendingManager {
    private Logger logger;
    private IMemberLendingStore database;
    private EventBus bus;
    public LendingManager(IMemberLendingStore db, Logger logger, EventBus busen){
        this.database=db;
        this.logger =logger;
        this.bus= busen;
        this.bus.register(this);
        logger.info("Finished constructor of MembershipManager");
    }
    public Member getMemberAndCurrentBorrowedBookItems(int memberId){
        logger.info("Entering method getMemberAndCurrentBorrowedBookItems memberId:{}",memberId);

        var member=this.database.getMember(memberId);
        if(member!=null) {
            var borrowedItems = this.database.getMemberBorrowedBookItems(memberId);
            member.setMembersLendings(borrowedItems);
        }
        return member;
    }
    public MemberLending searchMemberBorrowedItems(int memberId){
        logger.info("Entering method searchMemberBorrowedItems memberId:{}",memberId);
        var member=this.database.getMember(memberId);
        if(member!=null) {
            return this.database.getMemberBorrowedBookItems(memberId);
        }
        return null;
    }
    public LendingResultMessage returnBorrowedItem(ReturnLendBasket rlb)  {
        logger.info("Entering method returnBorrowedItem memberId:{}",rlb.getMemberId());
        var findMember= this.database.getMember(rlb.getMemberId());
        if(findMember==null) {
            logger.error("Member with id {} was not found", rlb.getMemberId());
            return LendingResultMessage.Error;
        }
        int result = this.database.removeBookItemFromLending(rlb);

        if(result !=-1) {
            for (UUID bookItem : rlb.getBookItemIds()) {
                //Skicka meddelande till BookManagement så att
                //man ändrar antal bookitems i "lager"
                logger.info("Sending message OnBookItemReturned with itemId:{}",bookItem);
                this.bus.post(new OnBookItemReturned(bookItem));
            }
            return LendingResultMessage.Ok;
        }
        logger.error("Error when returning borrowed books memberid:{}", rlb.getMemberId());
        return LendingResultMessage.Error;
    }
    public LendingResultMessage lendBookItems(MemberLending lending)  {
        logger.info("Entering method lendBookItems");
        if(lending==null || lending.getBookItemsIdWithDate().size()==0) {
            logger.error("Object is null or no book to return");
            return LendingResultMessage.Error;
        }
        logger.info("Find member who is trying to lend memberId:{}",lending.getMemberId());
        var lendingMember= this.database.getMember(lending.getMemberId());
        if(lendingMember==null){
            logger.error("Member with id {} was not found", lending.getMemberId());
            return LendingResultMessage.Error;
        }
        //Check how many bookitems member has already borrowed.
        //return memberid,bookitemId[],dateBorrowed,
        logger.info("Find how many books member  has borrowed memberId:{}",lending.getMemberId());
        var memberLending=this.database.getMemberBorrowedBookItems(lending.getMemberId());

        int availableItemsOneCanBorrow=
                (lendingMember.getMaximumNumberOfItemsOneCanBorrow()-memberLending.getBookItemsIdWithDate().size());

        logger.info("memberId:{} can borrow {}",lending.getMemberId(),availableItemsOneCanBorrow);

        if(availableItemsOneCanBorrow<lending.getBookItemsIdWithDate().size()) {
            logger.error("Man har försökt låna för många böcker man ska ta bort {} stycken",
                    (lending.getBookItemsIdWithDate().size() - availableItemsOneCanBorrow));
            return LendingResultMessage.Conflict;
        }

        logger.info("Create new leding post for memberId:{}",lending.getMemberId());
        this.database.addNewLendingBasketForMember(lending);

        for (LendingBasketEntity lendingBookItem:lending.getBookItemsIdWithDate()) {
            //Skicka meddelande till BookManagement så att
            //man ändrar antal bookitems i "lager"
            logger.info("Start monitoring book end date, when member should return book memberId:{}, bookItemId:{}",
                    lending.getMemberId(), lendingBookItem.getBookItemId());
            LendingScheduler
                    .StartTimerForWhenLendingShouldReturn(
                            lendingBookItem.getMemberId()
                            ,lendingBookItem.getBookItemId());
            logger.info("Send message OnBookItemLended bookItemId:{}",lendingBookItem.getBookItemId());
            this.bus.post(new OnBookItemLended(lendingBookItem.getBookItemId()));
        }

        return LendingResultMessage.Ok;
    }
    public LendingResultMessage deleteMember(int memberId){
        logger.info("Trying to delete member:{}",memberId);
        var result=this.database.deleteMember(memberId);
        if (result==0) {
            logger.info("Sucessfully deleted member:{}",memberId);
            return LendingResultMessage.Ok;
        }
        else {
            logger.error("ERROR while deleting member:{}",memberId);
            return LendingResultMessage.Error;
        }
    }
    @SuppressWarnings("unused")
    @Subscribe
    public void memberCreatedHandler(OnMemberCreated memberCreated){
        logger.info("Received message OnMemberCreated memberId:{}, maxItemsToBorrow:{}",memberCreated.getMemberId(),memberCreated.getMaxItemsToBorrow());
        this.database.addNewMember(new Member(memberCreated.getMemberId(),
                0,0,memberCreated.getMaxItemsToBorrow()) );
    }
}
