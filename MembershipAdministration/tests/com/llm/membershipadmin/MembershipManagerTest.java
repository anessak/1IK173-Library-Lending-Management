package com.llm.membershipadmin;

import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MembershipManagerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerNewLibraryMember_Test() {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IMembershipStore.class);
        var mgr= new MembershipManager(db,logger,bus);
        //Create member
        var nowDate= LocalDateTime.now();
        var member1=new Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);
        var member2=new Member(2000,"ssn",
                "fname","ename", MemberRole.PhD,
                MemberStatus.Active,"pwd", nowDate);

        when(mgr.getMemberById(member1.getMemberId())).thenReturn(member1);
        when(db.insertNewMember(member2)).thenReturn(0);

        assertEquals(MemberShipResultMessage.Ok, mgr.registerNewLibraryMember(member2));
    }
    @Test
    void registerNewLibraryMember_DUplicateMemberReturnConflict() {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IMembershipStore.class);
        var mgr= new MembershipManager(db,logger,bus);
        //Create member
        var nowDate= LocalDateTime.now();
        var member1=new Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);
        var member2=new Member(2000,"ssn",
                "fname","ename", MemberRole.PhD,
                MemberStatus.Active,"pwd", nowDate);

        when(mgr.getMemberById(member1.getMemberId())).thenReturn(member1);
        when(db.insertNewMember(member1)).thenReturn(-1);

        assertEquals(MemberShipResultMessage.Conflict, mgr.registerNewLibraryMember(member1));

    }
    @Test
    void registerNewLibraryMember_SuspendedMemberReturnSuspend() {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IMembershipStore.class);
        var mgr= new MembershipManager(db,logger,bus);
        //Create member
        var nowDate= LocalDateTime.now();
        var member1=new Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Suspended,"pwd", nowDate);

        when(mgr.getMemberById(member1.getMemberId())).thenReturn(member1);
        when(db.insertNewMember(member1)).thenReturn(0);

        assertEquals(MemberShipResultMessage.Suspended, mgr.registerNewLibraryMember(member1));

    }
    @Test
    void registerNewLibraryMember_SuspendedMemberReturnError() {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IMembershipStore.class);
        var mgr= new MembershipManager(db,logger,bus);
        //Create member
        var nowDate= LocalDateTime.now();
        var member1=new Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);

        var member2=new Member(2000,"ssn",
                "fname","ename", MemberRole.Postgraduate,
                MemberStatus.Active,"pwd", nowDate);

        when(mgr.getMemberById(member1.getMemberId())).thenReturn(member1);
        when(db.insertNewMember(member2)).thenReturn(-1);

        assertEquals(MemberShipResultMessage.Error, mgr.registerNewLibraryMember(member2));

    }
}