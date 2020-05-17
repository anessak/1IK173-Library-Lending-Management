package com.llm.membershipadmin;

import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MembershipStore implements IMembershipStore {
    private Logger logger;
    String connectionString ;

    public MembershipStore(Logger logger) {
        this.logger =logger;
        logger.info("Entering constructor");

        this.connectionString="jdbc:sqlite:MembershipAdministration/resources/MembersDB.db";
        this.createDatabaseAndTables();
    }

    private void createDatabaseAndTables() {
        logger.info("Creating database and tables");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try (Connection conn = DriverManager.getConnection(this.connectionString,config.toProperties())){

            Statement createDbStatement = conn.createStatement();
            // create a connection to the database
            String sqlMember = "CREATE TABLE IF NOT EXISTS Members " +
                    "(memberid integer PRIMARY KEY, ssn text NOT NULL, firstname text, " +
                    "lastname text, role text, status text, password text NOT NULL, " +
                    "datecreated text, datesuspended);";

            String sqlDeletedMember = "CREATE TABLE IF NOT EXISTS DeletedMembers " +
                    "(memberid integer, ssn text NOT NULL, firstname text, " +
                    "lastname text, role text, datedeleted,  " +
                    "PRIMARY KEY (memberid,datedeleted));";

            createDbStatement.execute(sqlMember);
            createDbStatement.execute(sqlDeletedMember);

            createDbStatement.closeOnCompletion();

            logger.info("Member database succesfully created");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public int insertNewMember(Member member) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement memberInsertSql =
                    conn.prepareStatement("INSERT INTO Members(memberid, ssn, firstname, lastname, role," +
                            "status, password, datecreated) VALUES(?,?,?,?,?,?,?,?)");
            memberInsertSql.setInt(1, member.getMemberId());
            memberInsertSql.setString(2, member.getSsn());
            memberInsertSql.setString(3, member.getFirstName());
            memberInsertSql.setString(4, member.getLastName());
            memberInsertSql.setString(5, member.getRole().name());
            memberInsertSql.setString(6, member.getMemberStatus().name());
            memberInsertSql.setString(7, "");
            memberInsertSql.setString(8, member.getDateCreated().toString());
            memberInsertSql.executeUpdate();


        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    @Override
    public void suspendUser(int memberId){
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement sql =
                    conn.prepareStatement("UPDATE Members SET status = ?, datesuspended = ?" +
                            " WHERE memberid = ?");
            sql.setString(1, MemberStatus.Suspended.name());
            sql.setString(2, LocalDateTime.now().toString());
            sql.setInt(3, memberId);
            sql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void reActivateUser(int memberId){
        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement sql =
                    conn.prepareStatement("UPDATE Members SET status = ?, datesuspended = ? WHERE memberid = ?");
            sql.setString(1, MemberStatus.Active.name());
            sql.setString(2, "");
            sql.setInt(3, memberId);
            sql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public Member getMember(int memberId){

        Member member=null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            PreparedStatement pstmt  = conn.prepareStatement("SELECT * FROM Members WHERE memberid = ?");
            pstmt.setInt(1,memberId);

            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                member= new Member(result.getInt("memberid"),
                        result.getString("ssn"),
                        result.getString("firstname"),
                        result.getString("lastname"),
                        MemberRole.valueOf(result.getString("role")),
                        MemberStatus.valueOf(result.getString("status")),
                        result.getString("password"),
                        LocalDateTime.parse(result.getString("datecreated")));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return member;
    }
    @Override
    public int deleteMember(Member memberToDelete){
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt  = conn.prepareStatement(
                    "DELETE FROM Members WHERE memberid = ?");
            pstmt.setInt(1,memberToDelete.getMemberId());

            PreparedStatement sql =
                    conn.prepareStatement(
                            "INSERT INTO DeletedMembers(memberid, ssn, firstname, lastname, role," +
                            "datedeleted) VALUES(?,?,?,?,?,?)");
            sql.setInt(1, memberToDelete.getMemberId());
            sql.setString(2, memberToDelete.getSsn());
            sql.setString(3, memberToDelete.getFirstName());
            sql.setString(4, memberToDelete.getLastName());
            sql.setString(5, memberToDelete.getRole().name());
            sql.setString(6, LocalDateTime.now().toString());

            pstmt.executeUpdate();
            sql.executeUpdate();

            conn.commit();
            }
        catch (SQLException throwables) {
            logger.error(throwables.getMessage());
            throwables.printStackTrace();
            return -1;
        }
        return 0;
    }
    @Override
    public void updateMember(Member memberToUpdate){
        try (Connection conn = DriverManager.getConnection(this.connectionString)){

            PreparedStatement sql  = conn.prepareStatement(
                    "UPDATE Members SET ssn = ?, firstname = ?, " +
                            "lastname = ?, role = ?, status = ?, " +
                            "password = ? WHERE memberid = ?");
            sql.setString(1,memberToUpdate.getSsn());
            sql.setString(2,memberToUpdate.getFirstName());
            sql.setString(3,memberToUpdate.getLastName());
            sql.setString(4,memberToUpdate.getRole().name());
            sql.setString(5,memberToUpdate.getMemberStatus().name());
            sql.setString(6,memberToUpdate.getPassword());

            sql.executeUpdate();

        }
        catch (SQLException throwables) {
            logger.error(throwables.getMessage());
            throwables.printStackTrace();
        }
    }

    @Override
    public ArrayList<Member> searchMembers(int memberIdWildCard) {
        ArrayList<Member> members=null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            PreparedStatement pstmt  = conn.prepareStatement(
                    "SELECT * FROM Members WHERE CAST(memberId AS text) LIKE ?");
            pstmt.setString(1,memberIdWildCard+"%");

            ResultSet result = pstmt.executeQuery();
            members=new ArrayList<>();
            while (result.next()) {
                members.add(new Member(result.getInt("memberid"),
                        result.getString("ssn"),
                        result.getString("firstname"),
                        result.getString("lastname"),
                        MemberRole.valueOf(result.getString("role")),
                        MemberStatus.valueOf(result.getString("status")),
                        result.getString("password"),
                        LocalDateTime.parse(result.getString("datecreated"))));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return members;
    }
}
