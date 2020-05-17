package com.llm.booksmanagement;

import com.librarylendingmanagement.infrastructure.events.*;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookManagementManager {
    //use cases for book
    IBookStore bookStore;
    private Logger logger;

    public BookManagementManager(IBookStore bokStore, Logger logger, EventBus bus){

        this.bookStore=bokStore;
        this.logger =logger;
        bus.register(this);

        logger.info("Finished constructor of BookManagementManager");
    }

    public ResultMessage addBookTitleToLibrary(BookTitle bokToSave){
        if (bokToSave == null)
            return ResultMessage.Error;
        logger.info("Searching för bok with isbn: {}", bokToSave.getIsbn());

        var bok = this.getBookTitlebyIsbn(bokToSave.getIsbn());

        if (bok != null) {
            logger.error("Bookwith same isbn found {}", bokToSave.getIsbn());
            return ResultMessage.Conflict;
        }
        logger.info("Book not found now we can add it to database");
        //add book
        this.bookStore.insertNewBookTitle(bokToSave);
        logger.info("Added bok ISBN: {}; Title: {}; Author: {}; Release Date: {}",
                bokToSave.getIsbn(), bokToSave.getTitle(), bokToSave.getAuthor(), bokToSave.getReleaseDate());

        return ResultMessage.Ok;
    }
    public ResultMessage addNewBookItemsForBok(BookTitle book){
        if(book==null) {
            logger.error("Callaed addNewBookItemsForBok with NULL argument");
            return ResultMessage.Error;
        }
        logger.info("Searching för bok with isbn: {}", book.getIsbn());
        var bok = this.getBookTitlebyIsbn(book.getIsbn());
        if (bok == null) {
            logger.info("Unable to find book to add new BookItem for book SIBN:{}", book.getIsbn());
            return ResultMessage.NotFound;
        }

        for (BookItem b : book.getAvailableBookItems()) {
            this.bookStore.insertNewBookItem(b, book.getIsbn());
            logger.info("Added ny bok item of type {} for book isbn: {}; ",
                    b.getItemType().name(), book.getIsbn());
        }
        return ResultMessage.Ok;
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
    public ResultMessage updateBookTitle(BookTitle bookTitle, String isbn){
        logger.info("Entering method updateBooktitle isbn: {}",isbn);
        if(bookTitle==null)
            return ResultMessage.Error;
        var book = bookStore.getBookTitleWithItems(isbn);
        if (book == null) {
            logger.error("Unable to find book with isbn: {}",isbn);
            return ResultMessage.NotFound;
        }
        this.bookStore.updateBookTitle(bookTitle);

        return ResultMessage.Ok;
    }
    public ResultMessage removeBookFromRegistry(BookTitle book)
    {
        if(book==null) {
            logger.info("Entering method removeBookFromRegistry with NULL argument");
            return ResultMessage.Error;
        }
        logger.info("Entering method removeBookFromRegistry isbn: {}",book.getIsbn());
        logger.info("Searching för bok with isbn: {}",book.getIsbn());
        var bok=this.getBookTitlebyIsbn(book.getIsbn());
        if(bok == null) {
            logger.error("Not found bok with isbn: {}",book.getIsbn());
            return ResultMessage.NotFound;
        }
        logger.info("Deleting bok with isbn: {}",book.getIsbn());
        var resultfromDB = this.bookStore.deleteBook(book.getIsbn());
        if(resultfromDB==0) {
            logger.info("Bok with isbn: {} deleted!", book.getIsbn());
            return ResultMessage.Ok;
        }
        return ResultMessage.Error;
    }
    public ResultMessage removeBookFromRegistryWithIsbn(String isbn)
    {
        logger.info("Searching för bok with isbn: {}",isbn);
        var bok=this.getBookTitlebyIsbn(isbn);
        if(bok == null)
            return ResultMessage.NotFound;

        this.bookStore.deleteBook(isbn);
        return ResultMessage.Ok;
    }
    public ResultMessage removeBookItemFromRegistry(UUID id)
    {
        logger.info("Entered method removeBookItemFromRegistry id: {}",id);
        var bookItem=bookStore.getBookItem(id);
        if(bookItem==null) {
            logger.error("Unable to find item with id: {}",id);
            return ResultMessage.NotFound;
        }

        logger.info("Searching för bok via bok item id with id: {}",id);
        var bok=this.getBookTitleByBookItemId(id);

        if(bok==null) {
            logger.error("Unable to find bok item with id: {}",id);
            return ResultMessage.NotFound;
        }

        logger.info("Deleting book ite with id: {}",id);
        var resultFromDb= this.bookStore.deleteBookItem(bookItem.getId());
        if(resultFromDb==0) {
            logger.info("Deleted book ite with id: {}", id);
            return ResultMessage.Ok;
        }
        return ResultMessage.Error;
    }
    public BookTitle getBookTitleByBookItemId(UUID bookItemId){
        return this.bookStore.getBookTitleByItem(bookItemId);
    }
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
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
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
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

}
