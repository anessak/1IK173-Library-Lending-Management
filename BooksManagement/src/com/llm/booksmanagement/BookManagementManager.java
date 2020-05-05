package com.llm.booksmanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class BookManagementManager {
    //use cases for book
    IBookStore bookStore;
    private Logger logger;
    public BookManagementManager(IBookStore bokStore, Logger logger){

        this.bookStore=bokStore;
        this.logger =logger;
        logger.info("Finished constructor of BookManagementManager");
    }

    public void addBookTitleToLibrary(BookTitle bokToSave){
        //search for book
        logger.info("Searching för bok with isbn: {}",bokToSave.getIsbn());
        var bok=this.searchBookTitlebyIsbn(bokToSave.getIsbn());
        if(bok!=null) {
            logger.info("Book not found now we can add it to database");
            //add book
            this.bookStore.insertNewBookTitle(bokToSave);
            logger.info("Adding bok ISBN: {}; Title: {}; Author: {}; Release Date: {}",
                    bokToSave.getIsbn(),bokToSave.getTitle(),bokToSave.getAuthor(), bokToSave.getReleaseDate());
        }

    }
    public BookTitle searchBookTitlebyIsbn(String isbn){
        logger.info("Searching för bok with isbn: {}",isbn);
        return this.bookStore.getBookTitleWithItems(isbn);
    }
    public ArrayList<BookTitle> searchBookTitlebyTitle(String title){
        return null;
    }

}
