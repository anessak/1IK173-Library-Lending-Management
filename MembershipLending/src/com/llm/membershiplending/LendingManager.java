package com.llm.membershiplending;

import com.librarylendingmanagement.infrastructure.events.OnBookItemLended;
import com.librarylendingmanagement.infrastructure.events.OnBookItemReturned;
import com.librarylendingmanagement.infrastructure.events.OnMemberCreated;
import com.librarylendingmanagement.infrastructure.events.OnNewBookItemAdded;
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
        var member=this.database.getMember(memberId);
        if(member!=null) {
            var borrowedItems = this.database.getMemberBorrowedBookItems(memberId);
            member.setMembersLendings(borrowedItems);
        }
        return member;
    }
    public MemberLending searchMemberBorrowedItems(int memberId){
        return this.database.getMemberBorrowedBookItems(memberId);
    }
    public void returnBorrowedItem(ReturnLendBasket rlb)  {
        var findMember= this.database.getMember(rlb.getMemberId());
        if(findMember==null) {
            logger.error("Member with id {} was not found", rlb.getMemberId());
        }
         int result=   this.database.removeBookItemFromLending(rlb);
        if(result!=-1) {
            for (UUID bookItem : rlb.getBookItemIds()) {
                //Skicka meddelande till BookManagement så att
                //man ändrar antal bookitems i "lager"
                this.bus.post(new OnBookItemReturned(bookItem));
            }
        }
    }
    public String lendBookItems(MemberLending lending)  {

        var lendingMember= this.database.getMember(lending.getMemberId());
        if(lendingMember==null) {
            logger.error("Member with id {} was not found", lending.getMemberId());
            return "No Member Was found";
        }
        //Check how many bookitems member has already borrowed.
        //return memberid,bookitemId[],dateBorrowed,
        var memberLending=this.database.getMemberBorrowedBookItems(lending.getMemberId());

        int availableItemsOneCanBorrow=
                (lendingMember.getMaximumNumberOfItemsOneCanBorrow()-memberLending.getBookItemsIdWithDate().size());

        if(availableItemsOneCanBorrow<lending.getBookItemsIdWithDate().size())
            return String.format("du har försökt låna för många böcker ta bort %d stycken",
                    (lending.getBookItemsIdWithDate().size()-availableItemsOneCanBorrow));

        this.database.addNewLendingBasketForMember(lending);

        for (LendingBasketEntity lendingBookItem:lending.getBookItemsIdWithDate()) {
                //Skicka meddelande till BookManagement så att
                //man ändrar antal bookitems i "lager"
            LendingScheduler
                    .StartTimerForWhenLendingShouldReturn(
                            lendingBookItem.getMemberId()
                            ,lendingBookItem.getBookItemId());
            this.bus.post(new OnBookItemLended(lendingBookItem.getBookItemId()));
            }

        return "OK";
    }

    @Subscribe
    public void memberCreatedHandler(OnMemberCreated memberCreated){

        this.database.addNewMember(new Member(memberCreated.getMemberId(),
                0,0,memberCreated.getMaxItemsToBorrow()) );
    }
}
