package com.llm.membershiplending;

import com.librarylendingmanagement.infrastructure.events.OnMemberBreachedRegulation;
import com.librarylendingmanagement.infrastructure.events.OnMemberSuspended;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LendingScheduler {
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
            var matchFound=memberLendings.getBookItemsIdWithDate()
                    .stream()
                    .filter(lendingBookItemId->
                            (lendingBookItemId.getBookItemId().equals(bookItemId)
                                    && lendingBookItemId.getMemberId()==memberId))
                    .findAny()
                    .orElse(null);

            if (matchFound!=null) {
                var member = memberLendingStore.getMember(memberId);
                if (member != null) {
                    member.setMembersLendings(memberLendings);
                    member.increaseDelayedReturnBorrowedBooksCounter();
                    memberLendingStore.updateMemberCounters(member.getMemberId(),
                            member.getDelayedReturnBorrowedBooksCounter(),
                            member.getSuspendedTimesCounter());

                    logger.info("Scheduler memberId:{}, delays:{}, suspend:{}",memberId,member.getDelayedReturnBorrowedBooksCounter(),member.getSuspendedTimesCounter());
                    if(member.getDelayedReturnBorrowedBooksCounter()>=2 && member.getDelayedReturnBorrowedBooksCounter()%2==0)
                        Bus.post(new OnMemberSuspended(member.getMemberId()));
                    if (member.getSuspendedTimesCounter() >= 2)
                        Bus.post(new OnMemberBreachedRegulation(member.getMemberId()));
                }
            }
            else
                logger.info("Member {} has returned bookItem in time {}", memberId, bookItemId);
        }catch (Exception e) {
         logger.error(e.getMessage());
         e.printStackTrace();
        }
    }
}
