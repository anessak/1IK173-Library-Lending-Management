package com.llm.booksmanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class BookStore implements IBookStore {
    private Logger logger;
    String connectionString ;

    public BookStore(Logger logger) {
        this.logger =logger;
        logger.info("Entering constructor");

        this.connectionString="jdbc:sqlite:BooksManagement/resources/BooksDB.db";
        this.createDatabaseAndTables();
    }
    public BookStore(String connStr) {
        this.connectionString=connStr;
    }

    private void createDatabaseAndTables() {
        logger.info("Creating database and tables");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try (Connection conn = DriverManager.getConnection(this.connectionString,config.toProperties())){

            Statement createDbStatement = conn.createStatement();
            // create a connection to the database
            String sqlBookTitle = "CREATE TABLE IF NOT EXISTS BookTitles " +
                    "(isbn text PRIMARY KEY, title text NOT NULL, author text, releasedate text NOT NULL);";
            String sqlBookItems = "CREATE TABLE IF NOT EXISTS BookItems (id text PRIMARY KEY,  " +
                    "itemtype text, dateadded text, itemstate text, bookisbn text, " +
                    "FOREIGN KEY(bookisbn) REFERENCES BookTitles(isbn) );";
            createDbStatement.execute(sqlBookTitle);
            createDbStatement.execute(sqlBookItems);

            createDbStatement.closeOnCompletion();

            logger.info("Book database succesfully created");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        }
    }
    public void insertNewBookTitle(BookTitle bokTitle) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement bookTitleInsertSql =
                    conn.prepareStatement("INSERT INTO BookTitles(isbn, title, author, releasedate) VALUES(?,?,?,?)");
            bookTitleInsertSql.setString(1, bokTitle.getIsbn());
            bookTitleInsertSql.setString(2, bokTitle.getTitle());
            bookTitleInsertSql.setString(3, bokTitle.getAuthor());
            bookTitleInsertSql.setString(4, bokTitle.getReleaseDate().toString());
            bookTitleInsertSql.executeUpdate();

            PreparedStatement bookItemInsertSql =
                    conn.prepareStatement("INSERT INTO BookItems(id, itemtype, dateadded, itemstate, bookisbn) VALUES(?,?,?,?,?)");
            for (BookItem bookItem: bokTitle.getAvailableBookItems()) {
                bookItemInsertSql.setString(1,bookItem.getId().toString());
                bookItemInsertSql.setString(2,bookItem.getItemType().name());
                bookItemInsertSql.setString(3,bookItem.getDateAdded().toString());
                bookItemInsertSql.setString(4,bookItem.getItemState().name());
                bookItemInsertSql.setString(5,bokTitle.getIsbn());
                bookItemInsertSql.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void insertNewBookItem(BookItem bookItem, String bookIsbn) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            PreparedStatement bookItemInsertSql =
                    conn.prepareStatement("INSERT INTO BookItems(id, itemtype, dateadded, itemstate, bookisbn) VALUES(?,?,?,?,?)");
            bookItemInsertSql.setString(1, bookItem.getId().toString());
            bookItemInsertSql.setString(2, bookItem.getItemType().name());
            bookItemInsertSql.setString(3, bookItem.getDateAdded().toString());
            bookItemInsertSql.setString(4, bookItem.getItemState().name());
            bookItemInsertSql.setString(5, bookIsbn);
            bookItemInsertSql.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public BookTitle getBookTitle(String isbn){

        BookTitle bookTitle=new BookTitle();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            //PreparedStatement pstmt  = conn.prepareStatement("SELECT * FROM BookTitles WHERE isbn = ?");
            //pstmt.setString(1,isbn);
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM BookTitles WHERE UPPER(isbn) LIKE ?");
            pstmt.setString(1, isbn.toUpperCase() + "%");

            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                bookTitle= new BookTitle(result.getString("isbn"),
                        result.getString("title"),
                        result.getString("author"),
                        LocalDateTime.parse(result.getString("releasedate")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return bookTitle;
    }
    public BookTitle getBookTitleWithItems(String isbn){

        BookTitle bookTitle=new BookTitle();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            //PreparedStatement pstmt  = conn.prepareStatement("SELECT * FROM BookTitles WHERE isbn = ?");
            //pstmt.setString(1,isbn);
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM BookTitles WHERE UPPER(isbn) LIKE ?");
            pstmt.setString(1, isbn.toUpperCase() + "%");
            ResultSet bookResult = pstmt.executeQuery();
            while (bookResult.next()) {
                bookTitle= new BookTitle(bookResult.getString("isbn"),
                        bookResult.getString("title"),
                        bookResult.getString("author"),
                        LocalDateTime.parse(bookResult.getString("releasedate")));
            }

            PreparedStatement itemsStm  = conn.prepareStatement("SELECT * FROM BookItems WHERE bookisbn = ?");
            itemsStm.setString(1,bookTitle.getIsbn());
            ResultSet bookItemsResult = itemsStm.executeQuery();
            while (bookItemsResult.next()) {
                bookTitle.addBookItem(new BookItem(UUID.fromString(bookItemsResult.getString("id")),
                        ItemType.valueOf(bookItemsResult.getString("itemtype")),
                        BookItemState.valueOf(bookItemsResult.getString("itemstate")),
                        LocalDateTime.parse(bookItemsResult.getString("dateadded")),bookTitle));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return bookTitle;
    }
}
