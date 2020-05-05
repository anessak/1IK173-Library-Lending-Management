package com.llm.membershiplending;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class LendingManager {
    private Logger logger;

    public LendingManager(Logger logger){

        this.logger =logger;
        logger.info("Finished constructor of MembershipManager");
    }

    public String[] searchMemberBorrowedItems(int memberId){

        return null;
    }
    public void returnBorrowedItem(ReturnLendBasket rlb){

    }
    public void lendBookItems(LendingBasket bookItems){
        //set book items to member

        //Update number of book items available in library
        //message [bookItemIds] borrowed
    }

    public void memberCreatedHandler(){
        //set max items for member to be able to borrow.
    }

}
