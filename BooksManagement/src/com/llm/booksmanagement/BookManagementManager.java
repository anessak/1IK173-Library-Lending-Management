package com.llm.booksmanagement;

import com.librarylendingmanagement.infrastructure.events.*;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookManagementManager {
    //use cases for book
    IBookStore bookStore;
    private Logger logger;
    private EventBus Bus;
    public BookManagementManager(IBookStore bokStore, Logger logger, EventBus b){

        this.bookStore=bokStore;
        this.logger =logger;
        this.Bus=b;
        this.Bus.register(this);

        logger.info("Finished constructor of BookManagementManager");
    }

    public void addBookTitleToLibrary(BookTitle bokToSave){
        //search for book
        logger.info("Searching för bok with isbn: {}",bokToSave.getIsbn());
        var bok=this.getBookTitlebyIsbn(bokToSave.getIsbn());
        if(bok==null) {
            logger.info("Book not found now we can add it to database");
            //add book
            this.bookStore.insertNewBookTitle(bokToSave);
            logger.info("Adding bok ISBN: {}; Title: {}; Author: {}; Release Date: {}",
                    bokToSave.getIsbn(),bokToSave.getTitle(),bokToSave.getAuthor(), bokToSave.getReleaseDate());
        }
    }
    public void addNewBookItemsForBok(BookTitle book){
        logger.info("Searching för bok with isbn: {}",book.getIsbn());
        var bok=this.getBookTitlebyIsbn(book.getIsbn());
        if(bok==null){
            for(BookItem b:book.getAvailableBookItems()) {
                this.bookStore.insertNewBookItem(b,book.getIsbn());
                logger.info("Added ny bok item of type {} for book isbn: {}; ",
                        b.getItemType().name(),book.getIsbn());
            }
        }
        else
            logger.error("Book not found unable to add bokitem");
    }
    public BookTitle getBookTitlebyIsbn(String isbn){
        logger.info("Searching för bok with isbn: {}",isbn);
        var bookTitle= this.bookStore.getBookTitleWithItems(isbn);
        if(bookTitle!=null)
            logger.info("found book with isbn:{} and title:{}",isbn,bookTitle.getTitle());
        else
            logger.info("No book for isbn {} found",isbn);
        return bookTitle;
    }
    public ArrayList<BookTitle> searchBookTitlesbyIsbn(String isbnWildcard){
        logger.info("Searching för books with isbn: {}",isbnWildcard);
        var bookTitles= this.bookStore.searchBooksByIsbn(isbnWildcard);
        if(bookTitles.size()!=0)
            logger.info("found {} books with isbn:{} ",bookTitles.size(), isbnWildcard);
        return bookTitles;
    }
    public ArrayList<BookItem> searchBookItemsbyIsbnAvailableToBorrow(String isbnWildcard){
        logger.info("Searching för books with isbn: {}",isbnWildcard);
        var bookTitles= this.bookStore.searchBooksByIsbn(isbnWildcard);
        if(bookTitles.size()!=0) {
            logger.info("found {} books with isbn:{} ", bookTitles.size(), isbnWildcard);

            return (ArrayList<BookItem>) bookTitles.get(0).getAvailableBookItems().stream()                // convert list to stream
                    .filter(bi -> bi.getItemState()==BookItemState.HomeInLibrary)     // we dont like mkyong
                    .collect(Collectors.toList());
        }
        return null;

    }
    public void removeBookFromRegistry(BookTitle book)
    {
        logger.info("Searching för bok with isbn: {}",book.getIsbn());
        var bok=this.getBookTitlebyIsbn(book.getIsbn());
        if(bok!=null)
            this.bookStore.deleteBook(book.getIsbn());
    }
    public void removeBookItemFromRegistry(BookItem bookItem,String isbn)
    {
        logger.info("Searching för bok with isbn: {}",isbn);
        var bok=this.getBookTitlebyIsbn(isbn);
        if(bok!=null)
            this.bookStore.deleteBookItem(bookItem.getId());
    }
    @Subscribe
    public void bokItemHasBeenReturned(OnBookItemReturned bookItemId) throws Exception {
        //find bookItem via bookItemId from database
        logger.info("Searching för book item with id: {}",bookItemId.getId());
        var id=this.bookStore.getBookItem(bookItemId.getId());

        if(id==null) {
            logger.error("Unable to find bookItem with id:{}",bookItemId.getId());
            throw new Exception("Unable to find bookItem with id:" + bookItemId.getId().toString());
        }
        logger.info("found title now updating bookitem:{} status to '{}'", bookItemId.getId(), BookItemState.HomeInLibrary.name());
        //Update found bookItem by updating bookItem state to HomeInLibrary
        this.bookStore.updateBookState(bookItemId.getId(), BookItemState.HomeInLibrary);


    }
    @Subscribe
    public void bokItemHasBeenBorrowed(OnBookItemLended bookItemId) throws Exception {
        //find bookItem via bookItemId from database
        logger.info("Searching för book item with id: {}",bookItemId.getId());
        var id=this.bookStore.getBookItem(bookItemId.getId());

        if(id==null) {
            logger.error("Unable to find bookItem with id:{}",bookItemId.getId());
            throw new Exception("Unable to find bookItem with id:" + bookItemId.getId().toString());
        }
        //Update found bookItem by updating bookItem state to Borrowed
        logger.info("found title now updating bookitem:{} status to '{}'", bookItemId.getId(), BookItemState.BorrowedToMember.name());
        this.bookStore.updateBookState(bookItemId.getId(), BookItemState.BorrowedToMember);

    }
    public BookTitle getBookTitleByBookItemId(UUID bookItemId){
        return this.bookStore.getBookTitleByItem(bookItemId);
    }
}
