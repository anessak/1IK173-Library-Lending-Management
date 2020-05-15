package com.llm.ui;

import com.llm.booksmanagement.*;
import com.llm.membershipadmin.*;
import com.llm.membershipadmin.Member;
import com.llm.membershiplending.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static UUID randomUUID1=UUID.randomUUID();
    private static UUID randomUUID2=UUID.randomUUID();
    private static UUID randomUUID3=UUID.randomUUID();
    private static UUID randomUUID4=UUID.randomUUID();
    private static UUID randomUUID5=UUID.randomUUID();
    private static UUID randomUUID6=UUID.randomUUID();
    private static UUID randomUUID7=UUID.randomUUID();
    private static UUID randomUUID8=UUID.randomUUID();
    private static UUID randomUUID9=UUID.randomUUID();
    private static UUID randomUUID10=UUID.randomUUID();
    private static UUID randomUUID11=UUID.randomUUID();

    public static void main(String[] args) {
        setUp();
       // if(login())
            visaHuvudMenu();
    }

    private static boolean login() {
        System.out.println("-----------");
        System.out.println("1. Logga in");
        System.out.println("9. Avsluta");
        System.out.println("-----------");
        System.out.print("Gör ditt val -->");
        var scan = new Scanner(System.in);

        int menuVal=scan.nextInt();
        if (menuVal != 1)
            System.exit(0);



        System.out.println("==============");
        System.out.println("Användarid:");
        int memberId = scan.nextInt();
        scan.nextLine();
        System.out.println("Lösenord:");
        String password = scan.nextLine();

        return medlemsRegister.login(memberId, password);


    }

    private static void visaHuvudMenu() {
        while(true) {
            System.out.println("Library Management System");
            System.out.println("=========================");
            System.out.println("1. Medlemshantering register");
            System.out.println("2. Bok register");
            System.out.println("3. Hantering av lån (lån/återlämning/sök)");
            System.out.println("4. Avsluta");
            System.out.print("Gör ditt val -->");

            var scan = new Scanner(System.in);
            int menuVal=scan.nextInt();
            if (menuVal == 4)
                System.exit(0);

            switch (menuVal) {
                case 1 -> visaMedlemskapMenu();
                case 2 -> visaMenuForBocker();
                case 3 -> visaMenuForUtlaning();
                default -> System.out.println("Felaktig val");
            }
            System.out.println();
        }
    }
    private static void visaMenuForUtlaning() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Låna böcker");
        System.out.println("1. Sök tillgänlig bok");
        System.out.println("2. Låna böcker");
        System.out.println("3. Återlämna böcker");
        System.out.println("0. Huvud Menu");
        System.out.print("==>");
        int valAvOperation = scan.nextInt();

        if (valAvOperation == 1)
            sokTillgangligBok();
        else if (valAvOperation == 2)
            lanaBok();
        else if (valAvOperation == 3)
            aterlamnaBok();
        else
            visaHuvudMenu();
    }

    private static void sokTillgangligBok() {
        Scanner scan = new Scanner(System.in);
        System.out.println("----------------");
        System.out.println("Skriv book isbn:");
        String isbn = scan.nextLine();

        var books=bokRegister.searchBookItemsbyIsbnAvailableToBorrow(isbn);
        System.out.println("------------------");
        if(books.size()==0) {
            System.out.println("Ingen book kunde hittas med detta isbn");
        }
        else {
            System.out.format("Det finns %d tillgängliga exemplar att låna%n",books.size());
            for (BookItem bookItem : books) {
                System.out.format(" book with ISBN:%s%n ITEM_ID:%s%n Title:%s%n Item-Type: %s%n",
                        bookItem.getReferencedBook().getIsbn(),
                        bookItem.getId().toString(),
                        bookItem.getReferencedBook().getTitle(),
                        bookItem.getItemType().name());
                System.out.println("-----------------------------------");
            }
        }
    }
    private static void lanaBok()  {
        Scanner scan = new Scanner(System.in);
        System.out.println("Låna bok");
        System.out.println("Ange ditt medlemsid:");
        int medlemsId=scan.nextInt();
        scan.nextLine();
        var medlem=medlemsRegister.getMemberById(medlemsId);
        if(medlem==null) {
            System.out.format("Medlem med detta id (%d) finns inte", medlemsId);
            return;
        }
        var utlaningsDatum=LocalDateTime.now();
        var booksToLend=new ArrayList<LendingBasketEntity>();
        do {
            System.out.println("Skriv book item ID:");
            UUID bookId = UUID.fromString(scan.nextLine());
            System.out.println("Lägg till fler böcker (j)");
            booksToLend.add(new LendingBasketEntity(medlemsId, bookId, utlaningsDatum));
        } while(scan.nextLine().equals("j"));

        utlaningsRegister.lendBookItems(new MemberLending(medlemsId,booksToLend ));

    }
    private static void aterlamnaBok() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Återlämna bok");
        System.out.println("Ange ditt medlemsid:");
        int medlemsId=scan.nextInt();
        scan.nextLine();

        var medlem=medlemsRegister.getMemberById(medlemsId);
        if(medlem==null) {
            System.out.format("Medlem med detta id (%d) finns inte", medlemsId);
            return;
        }
        var booksToReturn=new ArrayList<UUID>();
        do {
            System.out.println("Skriv book item ID:");
            UUID bookId = UUID.fromString(scan.nextLine());
            System.out.println("Lägg till fler böcker (j)");
            booksToReturn.add(bookId);
        } while(scan.nextLine().equals("j"));

        utlaningsRegister.returnBorrowedItem(new ReturnLendBasket(medlemsId,booksToReturn,LocalDateTime.now()));

    }

    private static void visaMenuForBocker() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Böckers hantering och register?");
        System.out.println("1. Sök bok");
        System.out.println("2. Lägg till ny bok");
        System.out.println("3. Lägg till ny bok item");
        System.out.println("4. Ta bort bok");
        System.out.println("5. Ta bort bok item");
        System.out.println("0. Huvud Menu");
        System.out.print("==>");
        int valAvOperation = scan.nextInt();

        if (valAvOperation == 1)
            sokBok();
        else if (valAvOperation == 2)
            registreraNyBok();
        else if (valAvOperation == 3)
            registreraNyBokItem();
        else if (valAvOperation == 4)
            taBortBok();
        else if (valAvOperation == 5)
            taBortBokItem();
        else
            visaHuvudMenu();
    }
    private static void sokBok() {
        Scanner scan = new Scanner(System.in);
        System.out.println("==============");
        System.out.println("Skriv book isbn:");
        String isbn = scan.nextLine();

        var books=bokRegister.searchBookTitlesbyIsbn(isbn);
        System.out.println("==============");
        if(books.size()==0) {
            System.out.println("Ingen book kunde hittas med detta isbn");
        }
        else {
            System.out.println("Sök resultat");
            for (BookTitle book : books) {
                System.out.format(" ISBN:%s%n Title:%s%n Authr: %s%n Release date:%s%n Nr of items:%d%n",
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getReleaseDate().toLocalDate().toString(),
                        book.getAvailableBookItems().size());
            }
        }
        visaMenuForBocker();
    }
    private static void registreraNyBok() {
        System.out.println("***Registrera ny bok***");
        Scanner scan = new Scanner(System.in);
        System.out.print("book isbn:");
        String isbn = scan.nextLine();

        var book=bokRegister.getBookTitlebyIsbn(isbn);
        if(book==null) {

            System.out.print("Titel:");
            String titel = scan.nextLine();

            System.out.print("Autor:");
            String author = scan.nextLine();

            System.out.print("Release datum:");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            var str=scan.nextLine();
            LocalDateTime releaseDate = LocalDate.parse(str,formatter).atStartOfDay();

            book=new BookTitle(isbn,titel,author,releaseDate);

            System.out.print("Lägg till ny bok item (j/n)?");
            String valAvItem = scan.nextLine();

            if(valAvItem.equals("j"))
                registreraNyBokItem(book);

            bokRegister.addBookTitleToLibrary(book);
            System.out.println("Boken och dess items inlaga i registret");
        }
        else {
            System.out.format("Book med samma isbn finns i registret");
            System.out.format("Titel:%s Autor %s%n ", book.getTitle(),book.getAuthor() );
        }
        visaMenuForBocker();
    }
    private static void registreraNyBokItem(BookTitle bokTitle) {
        System.out.println("***Registrera ny bok item***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Bok-item typ p(aper) (a)udio (v)ideo (ebok) ");
        var val = scan.nextLine();
        ItemType itemType = switch (val) {
            case "p" -> ItemType.Paper;
            case "a" -> ItemType.Audio;
            case "v" -> ItemType.Video;
            case "ebok" -> ItemType.EBook;
            default -> null;
        };

        bokTitle.addBookItem(new BookItem(UUID.randomUUID(), itemType));
        System.out.print("Lägg till ny bok item (j/n)?");
        String flerBi = scan.nextLine();
        if (flerBi.equals("j"))
            registreraNyBokItem(bokTitle);

    }
    private static void registreraNyBokItem() {
        System.out.println("***Registrera ny bok item***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Bok titelns isbn:");
        String isbn = scan.nextLine();

        var bokTitle=bokRegister.getBookTitlebyIsbn(isbn);
        if(bokTitle==null) {
            System.out.print("Kunde inte hitta boken med denna isbn");
            visaMenuForBocker();
        }
        else {
            System.out.print("Bok-item typ p(aper) (a)udio (v)ideo (ebok) ");
            var val = scan.nextLine();
            ItemType itemType = switch (val) {
                case "p" -> ItemType.Paper;
                case "a" -> ItemType.Audio;
                case "v" -> ItemType.Video;
                case "ebok" -> ItemType.EBook;
                default -> null;
            };

            bokTitle.addBookItem(new BookItem(UUID.randomUUID(), itemType));
            System.out.print("Lägg till ny bok item (j/n)?");
            String flerBi = scan.nextLine();
            if (flerBi.equals("j"))
                registreraNyBokItem(bokTitle);
            else
                bokRegister.addNewBookItemsForBok(bokTitle);
        }
    }
    private static void taBortBok() {
        System.out.println("***Ta bort bok***");
        Scanner scan = new Scanner(System.in);
        System.out.print("book isbn:");
        String isbn = scan.nextLine();

        var book=bokRegister.getBookTitlebyIsbn(isbn);
        if(book==null) {
            System.out.print("Kunde inte hitta boken med denna isbn");
        }
        else{
            bokRegister.removeBookFromRegistry(book);
            System.out.print("Boken borttagen ur registret med alla dessa items");
        }
        visaMenuForBocker();
    }
    private static void taBortBokItem() {
        System.out.println("***Ta bort bok item***");
        Scanner scan = new Scanner(System.in);
        System.out.print("book isbn:");
        String isbn = scan.nextLine();

        var book=bokRegister.getBookTitlebyIsbn(isbn);
        if(book==null) {
            System.out.print("Kunde inte hitta boken med denna isbn");
        }
        else{
            System.out.print("book item id:");
            UUID bookitemid = UUID.fromString(scan.nextLine());
            bokRegister.removeBookItemFromRegistry(new BookItem(bookitemid,ItemType.Paper),isbn);
        }
        visaMenuForBocker();
    }

    private static void visaMedlemskapMenu() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Medlems hantering och register?");
        System.out.println("1. Sök medlem");
        System.out.println("2. Sök medlems utlånade böcker");
        System.out.println("3. Registrera medlem");
        System.out.println("4. Ta bort medlem");
        System.out.println("0. Huvud Menu");
        System.out.print("==>");
        int valAvOperation = scan.nextInt();

        switch (valAvOperation) {
            case 1 -> sokMedlem();
            case 2 -> sokMedlemUtlaningsinformation();
            case 3 -> registreraMedlem();
            case 4 -> taBortMedlem();
            default -> visaHuvudMenu();
        }

    }

    private static void sokMedlemUtlaningsinformation() {
        Scanner scan = new Scanner(System.in);
        System.out.println("==============");
        System.out.println("Skriv medlems id:");
        int medlemsId = scan.nextInt();

        var medlems=medlemsRegister.searchMembers(medlemsId);
        System.out.println("==============");
        if(medlems.size()==0) {
            System.out.println("Igen medlem kunde hittas med detta id");
        }
        else {

            var utlanadeBocker = utlaningsRegister.searchMemberBorrowedItems(medlemsId);
            var medlemsSocialaKontrakt = utlaningsRegister.getMemberAndCurrentBorrowedBookItems(medlemsId);
            System.out.println("Sök resultat");
            System.out.println("------------");
            for (LendingBasketEntity bookItem : utlanadeBocker.getBookItemsIdWithDate()) {
                var bookTitle = bokRegister.getBookTitleByBookItemId(bookItem.getBookItemId());
                System.out.format("Utlånad titeln   '%s'%n" +
                                  "med itemId       '%s'%n"+
                                  "av typen         '%s'%n" +
                                  "Utlånad den      '%s' bör lämnas den '%s'%n",
                        bookTitle.getTitle(),
                        bookTitle.getAvailableBookItems().get(0).getId(),
                        bookTitle.getAvailableBookItems().get(0).getItemType().name(),
                        bookItem.getLendingDate().toLocalDate().toString(),
                        bookItem.getLendingDate().toLocalDate().plusDays(15).toString());
            }
            for (Member medlem : medlems) {
                System.out.format("SSN:        %s%n" +
                                "Namn:       %s%n" +
                                "Role:       %s%n" +
                                "Status:     %s%n",
                        medlem.getSsn(),
                        medlem.getFirstName() + " " +
                                medlem.getLastName(),
                        medlem.getRole().name(),
                        medlem.getMemberStatus().name());
            }
            System.out.format("Antal Utlånade Böcker: %d%n" +
                            "Max möjligautlåningar: %d%n" +
                            "Antalet Förseningar:   %d%n" +
                            "Antalet Suspenderingar:%d%n",
                    medlemsSocialaKontrakt.getMemberLendings().getBookItemsIdWithDate().size(),
                    medlemsSocialaKontrakt.getMaximumNumberOfItemsOneCanBorrow(),
                    medlemsSocialaKontrakt.getDelayedReturnBorrowedBooksCounter(),
                    medlemsSocialaKontrakt.getSuspendedTimesCounter()
                    );
            System.out.println("------------------");
        }
        visaMedlemskapMenu();
    }

    private static void sokMedlem(){
        Scanner scan = new Scanner(System.in);
        System.out.println("==============");
        System.out.println("Skriv medlems id:");
        int medlemsId = scan.nextInt();

        var medlems=medlemsRegister.searchMembers(medlemsId);
        System.out.println("==============");
        if(medlems.size()==0) {
            System.out.println("Igen medlem kunde hittas med detta id");
        }
        else {
            System.out.println("Sök resultat");
            for (Member medlem : medlems) {
                System.out.format("Id:%d%n SSN:%s%n Namn: %s %s%n Role:%s%n Status:%s%n Medlem sedan:%s%n",
                        medlem.getMemberId(),
                        medlem.getSsn(),
                        medlem.getFirstName(),
                        medlem.getLastName(),
                        medlem.getRole().name(),
                        medlem.getMemberStatus().name(),
                        medlem.getDateCreated().toString());
            }
        }
        visaMedlemskapMenu();
    }
    private static void registreraMedlem() {
        System.out.println("***Registrera ny medlem***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Medlems id:");
        int medlemsId = scan.nextInt();
        scan.nextLine();

        var medlem=medlemsRegister.getMemberById(medlemsId);
        if(medlem==null) {

            System.out.print("Medlems personnummer:");
            String ssn = scan.nextLine();

            System.out.print("Förnamn:");
            String forNamn = scan.nextLine();

            System.out.print("Efternamn:");
            String efterNamn = scan.nextLine();

            System.out.print("Roll (U)ndergraduate, (P)ostgraduate, (PhD), (T)eacher): ");
            String role = scan.nextLine();
            MemberRole mRole = switch (role) {
                case "U" -> MemberRole.Undergraduate;
                case "P" -> MemberRole.Postgraduate;
                case "PhD" -> MemberRole.PhD;
                case "T" -> MemberRole.Teacher;
                default -> null;
            };

            medlemsRegister.registerNewLibraryMember(new Member(medlemsId, ssn, forNamn, efterNamn, mRole));
            System.out.println("Medlem inlagt i registret");
        }
        else {
            if (medlem.getMemberStatus() == MemberStatus.Suspended) {
                System.out.format("Medlem är suspenderat och kan inte registreras");
            }
            System.out.format("'%s' medlem med samma id:%s finns i registret%n", medlem.getMemberStatus().name(), medlemsId);
            System.out.format("Namn:%s %s Skapad %s ", medlem.getFirstName(), medlem.getLastName(), medlem.getDateCreated().toLocalDate().toString());
            System.out.println();
        }
        visaMedlemskapMenu();
    }
    private static void taBortMedlem(){
        Scanner scan = new Scanner(System.in);
        System.out.println("***Borttagning av medlem***");
        System.out.println("Skriv medlems id:");
        System.out.print("==>");
        int medlemsId = scan.nextInt();
        var medlem = medlemsRegister.getMemberById(medlemsId);
        if (medlem == null) {
            System.out.println("------------------------");
            System.out.println("Kunde inte hitta medlem");
        }
        else {
            medlemsRegister.removeMember(medlemsId);
            System.out.println("Medlem borttagen!!");

        }
        visaMedlemskapMenu();
    }

    private static MembershipManager medlemsRegister;
    private static LendingManager utlaningsRegister;
    private static BookManagementManager bokRegister;

    private static void setUp() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        EventBus eventBus = EventBus.getDefault();

        Logger logger = LogManager.getLogger("MembershipRolFileAppndr");
        IMembershipStore db= new MembershipStore(logger);
        medlemsRegister = new MembershipManager(db,logger,eventBus);


        Logger logger1 = LogManager.getLogger("MembershipLending");
        IMemberLendingStore db1= new MemberLendingStore(logger1);
        utlaningsRegister= new LendingManager(db1,logger1,eventBus);
        LendingScheduler.Init(db1,logger1,eventBus);

        Logger logger2 = LogManager.getLogger("BooksRolFileAppndr");
        IBookStore db2=new BookStore(logger2);
        bokRegister = new BookManagementManager(db2,logger2);

    //    addBooks();
      //  addMember(mgr);
        //addMemberLendings(mgr1);
    }

    public static void addMember(MembershipManager mgr)
    {
        mgr.registerNewLibraryMember(new Member(1000,"19990404",
                "Anessa","Kurtagic",MemberRole.Undergraduate,MemberStatus.Active, "lösenord",LocalDateTime.now()));

    }
    public static void addMemberLendings(LendingManager mgr) {

        //Medlem med id 1000 får ha max 6 böcker utlånade

        ArrayList<LendingBasketEntity> liu1= new ArrayList<>();
        liu1.add(new LendingBasketEntity(1000,
                randomUUID1,
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                randomUUID2,
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                randomUUID3,
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                randomUUID4,
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        var li1=new MemberLending(1000,liu1);

        ArrayList<LendingBasketEntity> liu2= new ArrayList<>();
        liu2.add(new LendingBasketEntity(1000, randomUUID5,
                LocalDateTime.of(2019, Month.MARCH, 20, 6, 30)));
        liu2.add(new LendingBasketEntity(1000, randomUUID6,
                LocalDateTime.of(2019, Month.MARCH, 20, 6, 30)));

        var li2=new MemberLending(1000,liu2);

        ArrayList<LendingBasketEntity> liu3= new ArrayList<>();
        liu3.add(new LendingBasketEntity(1000, randomUUID7,
                LocalDateTime.of(2019, Month.DECEMBER, 20, 6, 30)));
        liu3.add(new LendingBasketEntity(1000, randomUUID8,
                LocalDateTime.of(2019, Month.DECEMBER, 20, 6, 30)));

        var li3=new MemberLending(1000,liu3);

        ArrayList<LendingBasketEntity> liu4= new ArrayList<>();
        liu4.add(new LendingBasketEntity(1000, randomUUID9,
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));
        liu4.add(new LendingBasketEntity(1000, randomUUID10,
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));

        liu4.add(new LendingBasketEntity(1000, randomUUID11,
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));

        var li4= new MemberLending(1000,liu4);

        ArrayList<LendingBasketEntity> liu5= new ArrayList<>();
        liu5.add(new LendingBasketEntity(1000, randomUUID1,
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        var li5= new MemberLending(1000,liu5);

        ArrayList<LendingBasketEntity> liu6= new ArrayList<>();
        liu6.add(new LendingBasketEntity(2000, randomUUID1,
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        liu6.add(new LendingBasketEntity(2000, randomUUID2,
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var li6=new MemberLending(1000,liu6);

        //mgr.lendBookItems(li1);
        mgr.lendBookItems(li2);
        //mgr.lendBookItems(li3);
        //mgr.lendBookItems(li4);
        //mgr.lendBookItems(li5);
        //mgr.lendBookItems(li6);

        var result= mgr.searchMemberBorrowedItems(1000);

    }
    public static void addBooks()
    {
        var bookTitleA=new BookTitle("1022-2321-33123",
                "bästa boken","Anessa Kurtagic",
                LocalDateTime.of(2015, Month.FEBRUARY, 20, 6, 30));

        bookTitleA.addBookItem(new BookItem(randomUUID1, ItemType.Paper));
        bookTitleA.addBookItem(new BookItem(randomUUID2, ItemType.Paper));
        bookTitleA.addBookItem(new BookItem(randomUUID3, ItemType.Paper));
        bookTitleA.addBookItem(new BookItem(randomUUID4, ItemType.Audio));

        var bookTitleB= new BookTitle("9992-2321-31230",
                "Äntligen Monarki igen!","Kungen är bäst!",
                LocalDateTime.of(2020, Month.FEBRUARY, 21, 2, 10));
        bookTitleB.addBookItem(new BookItem(randomUUID5, ItemType.Paper));
        bookTitleB.addBookItem(new BookItem(randomUUID6, ItemType.Paper));
        bookTitleB.addBookItem(new BookItem(randomUUID7, ItemType.Video));

        bokRegister.addBookTitleToLibrary(bookTitleA);
        bokRegister.addBookTitleToLibrary(bookTitleB);

        var res1=bokRegister.searchBookTitlesbyIsbn("1022-");
        var res2=bokRegister.searchBookTitlesbyIsbn("9992-2321-31230");
    }
}
