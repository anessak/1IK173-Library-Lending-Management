package com.llm.membershiplending;

import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class MemberLendingStore implements IMemberLendingStore {
    private Logger logger;
    String connectionString ;

    public MemberLendingStore(Logger logger) {
        this.logger =logger;
        logger.info("Entering constructor");

        this.connectionString="jdbc:sqlite:MembershipLending/resources/MemberShipLendingDB.db";
        this.createDatabaseAndTables();
    }
    public MemberLendingStore(String connStr) {

        this.connectionString=connStr;
    }

    private void createDatabaseAndTables() {
        logger.info("Creating database and tables");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try (Connection conn = DriverManager.getConnection(this.connectionString,config.toProperties())){
            Statement createDbStatement = conn.createStatement();
            // create a connection to the database
            String sqlMember = "CREATE TABLE IF NOT EXISTS Member " +
                    "(memberId integer PRIMARY KEY, " +
                    "delayedReturnBorrowedBooksCounter integer, " +
                    "suspendedTimesCounter integer, " +
                    "maximumNumberOfItemsOneCanBorrow integer);";

            String sqlMemberLendings = "CREATE TABLE IF NOT EXISTS MemberCurrentLendings " +
                    "(memberid integer, bookitemid text NOT NULL, lendingdate text, PRIMARY KEY (memberid ,bookitemid) );";

            String sqlMemberReturnedItems = "CREATE TABLE IF NOT EXISTS MemberReturnedLendings " +
                    "(memberid integer, bookitemid text,  returndate text);";

            createDbStatement.execute(sqlMember);
            createDbStatement.execute(sqlMemberLendings);
            createDbStatement.execute(sqlMemberReturnedItems);

            createDbStatement.closeOnCompletion();

            logger.info("Member database succesfully created");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void addNewLendingBasketForMember(MemberLending memberLending) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {

            PreparedStatement lendingInsertSql =
                    conn.prepareStatement("INSERT INTO MemberCurrentLendings" +
                            "(memberid, bookitemid, lendingdate) " +
                            "VALUES(?,?,?)");


            for (LendingBasketEntity bookItemId:memberLending.getBookItemIds()) {
                lendingInsertSql.setInt(1, memberLending.getMemberId());
                lendingInsertSql.setString(2, bookItemId.getBookItemId().toString());
                lendingInsertSql.setString(3, bookItemId.getLendingDate().toString());
                lendingInsertSql.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void removeBookItemFromLending(ReturnLendBasket returnItems) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            conn.setAutoCommit(false);
            PreparedStatement bookItemReturnSql =
                    conn.prepareStatement("INSERT INTO MemberReturnedLendings" +
                            "(memberid, bookitemid, returndate) VALUES(?, ?, ?)");
            PreparedStatement bookMemberItemReturnSql =
                    conn.prepareStatement("DELETE FROM MemberCurrentLendings" +
                            "WHERE bookitemid = ?");
            for (UUID returnBookId:returnItems.getBookItemIds()) {
                bookItemReturnSql.setInt(1, returnItems.getMemberId());
                bookItemReturnSql.setString(2, returnBookId.toString());
                bookItemReturnSql.setString(3, returnItems.getReturnDate().toString());
                bookMemberItemReturnSql.setString(1, returnBookId.toString());
                bookMemberItemReturnSql.executeUpdate();
                bookItemReturnSql.executeUpdate();
            }
            conn.commit();


        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public MemberLending getMemberBorrowedBookItems(int memberId){

        MemberLending memberBorrowedItems=null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){

            PreparedStatement getMemberLendingsSql =
                    conn.prepareStatement("SELECT * FROM MemberCurrentLendings WHERE memberid = ?");
            getMemberLendingsSql.setInt(1,memberId);

            ResultSet result = getMemberLendingsSql.executeQuery();

            ArrayList<LendingBasketEntity> memberBorrowedItemsEntity=new ArrayList<>();
            while (result.next()) {
                memberBorrowedItemsEntity.add(new LendingBasketEntity(result.getInt("memberid"),
                        UUID.fromString(result.getString("bookitemid")),
                        LocalDateTime.parse(result.getString("lendingdate"))));
            }

            memberBorrowedItems= new MemberLending(memberId);

            for (LendingBasketEntity le:memberBorrowedItemsEntity) {
                memberBorrowedItems.addBookItem(le);
            }
            Collections.sort(memberBorrowedItems.getBookItemIds());
           /* Map<LocalDateTime, List<LendingBasketEntity>> entities = memberBorrowedItemsEntity.stream()
                    .collect(groupingBy(LendingBasketEntity::getLendingDate));

            for (Map.Entry<LocalDateTime, List<LendingBasketEntity>> entry : entities.entrySet()) {

                var ls= new MemberLending(memberId,
                        null,entry.getValue().get(0).getLendingDate());

                entry.getValue().forEach(tr->ls.addBookItem(tr.getBookItemId()));
                memberBorrowedItems=
            }*/

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return memberBorrowedItems;
        //return  memberBorrowedItems.stream().sorted(Comparator.comparing(MemberLending::getLendingDate))
          //      .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Member getMember(int memberId) {
        Member member = null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {

            PreparedStatement getMemberSql =
                    conn.prepareStatement("SELECT * FROM Member WHERE memberid = ?");
            getMemberSql.setInt(1, memberId);

            ResultSet result = getMemberSql.executeQuery();
            member = new Member(result.getInt("memberId"),
                    result.getInt("delayedReturnBorrowedBooksCounter"),
                    result.getInt("suspendedTimesCounter"),
                    result.getInt("maximumNumberOfItemsOneCanBorrow"));
        
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return member;
    }
    @Override
    public void addNewMember(Member member) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement memberInsertSql =
                    conn.prepareStatement("INSERT INTO Member" +
                            "(memberId, delayedReturnBorrowedBooksCounter, " +
                            "suspendedTimesCounter,maximumNumberOfItemsOneCanBorrow) " +
                            "VALUES(?,?,?,?)");

            memberInsertSql.setInt(1, member.getMemberId());
            memberInsertSql.setInt(2, member.getDelayedReturnBorrowedBooksCounter());
            memberInsertSql.setInt(3, member.getSuspendedTimesCounter());
            memberInsertSql.setInt(4, member.getMaximumNumberOfItemsOneCanBorrow());
            memberInsertSql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void updateDelayedReturnCounter(int memberId, int delayedReturnNr) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement memberUpdateSql =
                    conn.prepareStatement("UPDATE Member " +
                            "SET delayedReturnBorrowedBooksCounter = ? " +
                            "WHERE memberId=?");

            memberUpdateSql.setInt(1, memberId);
            memberUpdateSql.setInt(2, delayedReturnNr);
            memberUpdateSql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void updateSuspendedCounter(int memberId, int suspendedTimesNr) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement memberUpdateSql =
                    conn.prepareStatement("UPDATE Member " +
                            "SET suspendedTimesCounter = ? " +
                            "WHERE memberId=?");

            memberUpdateSql.setInt(1, memberId);
            memberUpdateSql.setInt(2, suspendedTimesNr);
            memberUpdateSql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}
