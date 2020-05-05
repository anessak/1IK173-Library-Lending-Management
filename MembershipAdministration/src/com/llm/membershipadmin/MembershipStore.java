package com.llm.membershipadmin;

import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class MembershipStore implements IMembershipStore {
    private Logger logger;
    String connectionString ;

    public MembershipStore(Logger logger) {
        this.logger =logger;
        logger.info("Entering constructor");

        this.connectionString="jdbc:sqlite:MembershipAdministration/resources/MembersDB.db";
        this.createDatabaseAndTables();
    }
    public MembershipStore(String connStr) {
        this.connectionString=connStr;
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
                    "lastname text, role text, password text NOT NULL, datecreated text);";
            String sqlRole = "CREATE TABLE IF NOT EXISTS MemberRoles (rolename text PRIMARY KEY,  " +
                    "description text);";

            createDbStatement.execute(sqlMember);
            createDbStatement.execute(sqlRole);

            createDbStatement.closeOnCompletion();

            logger.info("Member database succesfully created");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void insertNewMember(Member member) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement memberInsertSql =
                    conn.prepareStatement("INSERT INTO Members(memberid, ssn, firstname, lastname, role," +
                            " password, datecreated) VALUES(?,?,?,?,?,?,?)");
            memberInsertSql.setInt(1, member.getMemberId());
            memberInsertSql.setString(2, member.getSsn());
            memberInsertSql.setString(3, member.getFirstName());
            memberInsertSql.setString(4, member.getLastName());
            memberInsertSql.setString(5, member.getRole().name());
            memberInsertSql.setString(6, "");
            memberInsertSql.setString(7, member.getDateCreated().toString());
            memberInsertSql.executeUpdate();


        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public void updateMemberRole(int memberId, MemberRole role) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement bookItemInsertSql =
                    conn.prepareStatement("UPDATE Members SET role = ? WHERE memberid = ?");
            bookItemInsertSql.setInt(1, memberId);
            bookItemInsertSql.setString(2, role.name());
            bookItemInsertSql.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @Override
    public Member getMember(int memberId){

        Member member=new Member();
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
                        result.getString("password"),
                        LocalDateTime.parse(result.getString("datecreated")));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return member;
    }
}
