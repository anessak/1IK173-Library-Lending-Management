package com.llm.membershiplending;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

public class LendingManager {
    private Logger logger;
    private IMemberLendingStore database;
    public LendingManager(IMemberLendingStore db, Logger logger){
        this.database=db;
        this.logger =logger;
        logger.info("Finished constructor of MembershipManager");
    }

    public MemberLending searchMemberBorrowedItems(int memberId){
        return this.database.getMemberBorrowedBookItems(memberId);
    }
    public void returnBorrowedItem(ReturnLendBasket rlb) throws Exception {
        var findMember= this.database.getMember(rlb.getMemberId());
        if(findMember==null) {
            logger.error("Member with id {} was not found", rlb.getMemberId());
            throw new Exception("MEmber Not Found");
        }
            this.database.removeBookItemFromLending(rlb);
        for (UUID bookItem:rlb.getBookItemIds()) {
            //Skicka meddelande till BookManagement så att
            //man ändrar antal bookitems i "lager"

        }
    }
    public String lendBookItems(MemberLending lending) throws Exception {

        var lendingMember= this.database.getMember(lending.getMemberId());
        if(lendingMember==null) {
            logger.error("Member with id {} was not found", lending.getMemberId());
            throw new Exception("MEmber Not Found");
        }
        //Check how many bookitems member has already borrowed.
        //return memberid,bookitemId[],dateBorrowed,
        var memberLending=this.database.getMemberBorrowedBookItems(lending.getMemberId());

        int availableItemsOneCanBorrow=
                (lendingMember.getMaximumNumberOfItemsOneCanBorrow()-memberLending.getBookItemIds().size());

        if(availableItemsOneCanBorrow<lending.getBookItemIds().size())
            return String.format("du har försökt låna för många böcker ta bort %d stycken",
                    (lending.getBookItemIds().size()-availableItemsOneCanBorrow));

        this.database.addNewLendingBasketForMember(lending);

        for (LendingBasketEntity lendingBookItem:lending.getBookItemIds()) {
                //Skicka meddelande till BookManagement så att
                //man ändrar antal bookitems i "lager"
            }

        return "OK";
    }

    //Metod bör ta in ett Event från MemberShipAdministration
    public void memberCreatedHandler(){
        int memberId=0;
        int maxNrOfItems=0;
        this.database.addNewMember(new Member(memberId,0,0,maxNrOfItems) );
    }

}
