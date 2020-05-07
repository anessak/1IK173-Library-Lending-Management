package com.llm.membershiplending;

import com.librarylendingmanagement.infrastructure.events.OnMemberBreachedRegulation;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LendingScheduler {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private static Logger logger;
    private static IMemberLendingStore memberLendingStore;
    static EventBus Bus;
    public static void Init(IMemberLendingStore db, Logger l, EventBus bus)
    {
        memberLendingStore=db;
        logger =l;
        Bus= bus;
        logger.info("Finished constructor of LendingScheduler");
    }
    public static void StartTimerForWhenLendingShouldReturn(int memberId, UUID bookItemId) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

        Runnable task = () -> BookItemLendingTimeHasExpired(memberId,bookItemId);
        //run this task after 15 seconds
        ses.schedule(task, 15, TimeUnit.SECONDS);
        ses.shutdown();
    }
    public static void BookItemLendingTimeHasExpired(int memberId,UUID bookItemId){
        try {
            var memberLendings = memberLendingStore.getMemberBorrowedBookItems(memberId);
            boolean foundLendedItem=false;
            for(LendingBasketEntity lendingBookItemId:memberLendings.getBookItemsIdWithDate()){
                if(lendingBookItemId.getBookItemId().equals(bookItemId) && lendingBookItemId.getMemberId()==memberId)
                {
                    foundLendedItem=true;
                    break;
                }
            }
            if (foundLendedItem) {
                var member = memberLendingStore.getMember(memberId);
                if (member != null) {
                    member.setMembersLendings(memberLendings);
                    member.increaseDelayedReturnBorrowedBooksCounter();
                    memberLendingStore.updateMemberCounters(member.getMemberId(),
                            member.getDelayedReturnBorrowedBooksCounter(),
                            member.getSuspendedTimesCounter());

                    if (member.getSuspendedTimesCounter() >= 2)
                        Bus.post(new OnMemberBreachedRegulation(member.getMemberId()));
                }
            }
            else
                logger.info("Member {} has returned bookItem in time {}", memberId, bookItemId);
        }catch (Exception e)
        {
         logger.error(e.getMessage());
        }
    }
}
