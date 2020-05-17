package com.llm.booksmanagement;

import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public void insertNewBookTitle(BookTitle bokTitle) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            conn.setAutoCommit(false);
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
            conn.commit();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public void insertNewBookItem(BookItem bookItem, String bookIsbn) {

        try (Connection conn = DriverManager.getConnection(this.connectionString)) {
            conn.setAutoCommit(false);
            PreparedStatement bookItemInsertSql =
                    conn.prepareStatement("INSERT INTO BookItems(id, itemtype, dateadded, itemstate, bookisbn) VALUES(?,?,?,?,?)");
            bookItemInsertSql.setString(1, bookItem.getId().toString());
            bookItemInsertSql.setString(2, bookItem.getItemType().name());
            bookItemInsertSql.setString(3, bookItem.getDateAdded().toString());
            bookItemInsertSql.setString(4, bookItem.getItemState().name());
            bookItemInsertSql.setString(5, bookIsbn);
            bookItemInsertSql.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public BookTitle getBookTitle(String isbn){

        BookTitle bookTitle=null;
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
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return bookTitle;
    }
    @Override
    public BookItem getBookItem(UUID bookItemId){

        BookItem book = null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM BookItems WHERE id= ?");
            pstmt.setString(1, bookItemId.toString());

            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                book= new BookItem(UUID.fromString(result.getString("id")),
                        ItemType.valueOf(result.getString("itemtype")),
                        BookItemState.valueOf(result.getString("itemstate")),
                        LocalDateTime.parse(result.getString("dateadded")),null);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return book;
    }
    @Override
    public ArrayList<BookTitle> searchBooksByIsbn(String isbn){
        ArrayList<BookTitle> bookTitles=new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM BookTitles WHERE UPPER(isbn) LIKE ?");
            pstmt.setString(1, isbn.toUpperCase() + "%");
            ResultSet bookResult = pstmt.executeQuery();
            while (bookResult.next()) {
                bookTitles.add(new BookTitle(bookResult.getString("isbn"),
                        bookResult.getString("title"),
                        bookResult.getString("author"),
                        LocalDateTime.parse(bookResult.getString("releasedate"))));
            }
            for (BookTitle t:bookTitles) {
                PreparedStatement itemsStm  = conn.prepareStatement("SELECT * FROM BookItems WHERE bookisbn = ?");
                itemsStm.setString(1,t.getIsbn());
                ResultSet bookItemsResult = itemsStm.executeQuery();
                while (bookItemsResult.next()) {
                    t.addBookItem(new BookItem(UUID.fromString(bookItemsResult.getString("id")),
                            ItemType.valueOf(bookItemsResult.getString("itemtype")),
                            BookItemState.valueOf(bookItemsResult.getString("itemstate")),
                            LocalDateTime.parse(bookItemsResult.getString("dateadded")),t));
                }

            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return bookTitles;
    }
    public BookTitle getBookTitleWithItems(String isbn){

        BookTitle bookTitle=null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM BookTitles WHERE isbn= ?");
            pstmt.setString(1, isbn);
            ResultSet bookResult = pstmt.executeQuery();
            while (bookResult.next()) {
                bookTitle= new BookTitle(bookResult.getString("isbn"),
                        bookResult.getString("title"),
                        bookResult.getString("author"),
                        LocalDateTime.parse(bookResult.getString("releasedate")));
            }
            if(bookTitle!=null) {
                PreparedStatement itemsStm = conn.prepareStatement("SELECT * FROM BookItems WHERE bookisbn = ?");
                itemsStm.setString(1, bookTitle.getIsbn());
                ResultSet bookItemsResult = itemsStm.executeQuery();
                while (bookItemsResult.next()) {
                    bookTitle.addBookItem(new BookItem(UUID.fromString(bookItemsResult.getString("id")),
                            ItemType.valueOf(bookItemsResult.getString("itemtype")),
                            BookItemState.valueOf(bookItemsResult.getString("itemstate")),
                            LocalDateTime.parse(bookItemsResult.getString("dateadded")), bookTitle));
                }
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return bookTitle;
    }
    @Override
    public void updateBookState(UUID bookItemId, BookItemState itemState){

        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE BookItems SET itemstate = ? WHERE id = ?");
            pstmt.setString(1, itemState.name());
            pstmt.setString(2, bookItemId.toString());
            pstmt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void updateBookTitle(BookTitle bookTitle){
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE BookTitles SET title = ?, author = ?, releasedate = ? WHERE isbn = ?");
            pstmt.setString(1, bookTitle.getTitle());
            pstmt.setString(2, bookTitle.getAuthor());
            pstmt.setString(3, bookTitle.getReleaseDate().toString());
            pstmt.setString(4, bookTitle.getIsbn());
            pstmt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void updateBookItem(UUID bookIsbn, BookItem bookItem){
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE BookItems SET itemtype = ?, dateadded = ?," +
                            "itemstate = ?, bookisbn = ? WHERE id = ?");
            pstmt.setString(1, bookItem.getItemType().name());
            pstmt.setString(2, bookItem.getDateAdded().toString());
            pstmt.setString(3, bookItem.getItemState().name());
            pstmt.setString(4, bookIsbn.toString());
            pstmt.setString(5, bookItem.getId().toString());
            pstmt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public int deleteBook(String isbn) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt1 = conn.prepareStatement(
                    "DELETE FROM BookItems WHERE bookisbn = ?");
            pstmt1.setString(1, isbn);
            pstmt1.executeUpdate();

            PreparedStatement pstmt2 = conn.prepareStatement(
                    "DELETE FROM BookTitles WHERE isbn = ?");
            pstmt2.setString(1, isbn);
            pstmt2.executeUpdate();

            conn.commit();
            return 0;

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int deleteBookItem(UUID itemId) {
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            conn.setAutoCommit(false);
            PreparedStatement pstmt1 = conn.prepareStatement(
                    "DELETE FROM BookItems WHERE id = ?");
            pstmt1.setString(1, itemId.toString());
            pstmt1.executeUpdate();
            conn.commit();
            return 0;

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public BookTitle getBookTitleByItem(UUID bookItemId) {

        BookTitle bookTitle=null;
        try (Connection conn = DriverManager.getConnection(this.connectionString)){
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT BookTitles.*, BookItems.* FROM BookTitles " +
                            "INNER JOIN BookItems ON BookTitles.isbn=BookItems.bookisbn " +
                            "WHERE BookItems.id= ?");
            pstmt.setString(1, bookItemId.toString());
            ResultSet bookResult = pstmt.executeQuery();
            while (bookResult.next()) {
                bookTitle= new BookTitle(bookResult.getString("isbn"),
                        bookResult.getString("title"),
                        bookResult.getString("author"),
                        LocalDateTime.parse(bookResult.getString("releasedate")));
                bookTitle.addBookItem(new BookItem(UUID.fromString(bookResult.getString("id"))
                        ,ItemType.valueOf(bookResult.getString("itemtype"))));
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return bookTitle;
    }
}
