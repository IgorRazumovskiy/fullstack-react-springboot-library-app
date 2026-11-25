package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dao.CheckoutRepository;
import com.luv2code.springbootlibrary.dao.HistoryRepository;
import com.luv2code.springbootlibrary.dao.PaymentRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Checkout;
import com.luv2code.springbootlibrary.entity.History;
import com.luv2code.springbootlibrary.entity.Payment;
import com.luv2code.springbootlibrary.responsemodels.ShelfCurrentLoansResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private static final int LOAN_PERIOD_DAYS = 7;
    private static final String NOT_CHECKED_OUT = "Book doesn't exist or not checked out by user";

    private final BookRepository bookRepository;

    private final CheckoutRepository checkoutRepository;

    private final HistoryRepository historyRepository;

    private final PaymentRepository paymentRepository;

    private final Clock clock;

    public BookService(BookRepository bookRepository, CheckoutRepository checkoutRepository,
                       HistoryRepository historyRepository, PaymentRepository paymentRepository,
                       Clock clock) {
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    public Book checkoutBook(String userEmail, Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (book == null || validateCheckout != null || book.getCopiesAvailable() <= 0) {
            throw new EntityNotFoundException("Book doesn't exist or already checked out by user");
        }

        boolean bookNeedsReturn = checkoutRepository.findBooksByUserEmail(userEmail).stream()
                .anyMatch(checkout -> daysUntilReturn(checkout) < 0);

        Payment userPayment = paymentRepository.findByUserEmail(userEmail);
        if (userPayment != null && (userPayment.getAmount() > 0 || bookNeedsReturn)) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Outstanding fees");
        }
        if (userPayment == null) {
            Payment payment = new Payment();
            payment.setAmount(0.0);
            payment.setUserEmail(userEmail);
            paymentRepository.save(payment);
        }

        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now(clock);
        Checkout checkout = new Checkout(
                userEmail,
                today.toString(),
                today.plusDays(LOAN_PERIOD_DAYS).toString(),
                book.getId());
        checkoutRepository.save(checkout);

        return book;
    }

    public boolean checkoutBookByUser(String userEmail, Long bookId) {
        return checkoutRepository.findByUserEmailAndBookId(userEmail, bookId) != null;
    }

    public int currentLoansCount(String userEmail) {
        return checkoutRepository.findBooksByUserEmail(userEmail).size();
    }

    public List<ShelfCurrentLoansResponse> currentLoans(String userEmail) {
        List<Checkout> checkoutList = checkoutRepository.findBooksByUserEmail(userEmail);
        Map<Long, Checkout> checkoutsByBookId = checkoutList.stream()
                .collect(Collectors.toMap(Checkout::getBookId, Function.identity(), (first, second) -> first));

        List<Book> books = bookRepository.findBooksByBookIds(new ArrayList<>(checkoutsByBookId.keySet()));

        List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses = new ArrayList<>();
        for (Book book : books) {
            Checkout checkout = checkoutsByBookId.get(book.getId());
            if (checkout != null) {
                shelfCurrentLoansResponses.add(
                        new ShelfCurrentLoansResponse(book, (int) daysUntilReturn(checkout)));
            }
        }
        return shelfCurrentLoansResponses;
    }

    public void returnBook(String userEmail, Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (book == null || validateCheckout == null) {
            throw new EntityNotFoundException(NOT_CHECKED_OUT);
        }

        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);

        long daysLeft = daysUntilReturn(validateCheckout);
        if (daysLeft < 0) {
            Payment payment = paymentRepository.findByUserEmail(userEmail);
            if (payment == null) {
                throw new EntityNotFoundException("Payment information is missing");
            }
            payment.setAmount(payment.getAmount() - daysLeft);
            paymentRepository.save(payment);
        }

        checkoutRepository.deleteById(validateCheckout.getId());

        History history = new History(userEmail, validateCheckout.getCheckoutDate(), validateCheckout.getReturnDate(),
                book.getTitle(), book.getAuthor(), book.getDescription(), book.getImg());
        historyRepository.save(history);
    }

    public void renewLoan(String userEmail, Long bookId) {
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (validateCheckout == null) {
            throw new EntityNotFoundException(NOT_CHECKED_OUT);
        }
        if (daysUntilReturn(validateCheckout) >= 0) {
            validateCheckout.setReturnDate(LocalDate.now(clock).plusDays(LOAN_PERIOD_DAYS).toString());
            checkoutRepository.save(validateCheckout);
        }
    }

    private long daysUntilReturn(Checkout checkout) {
        return ChronoUnit.DAYS.between(LocalDate.now(clock), LocalDate.parse(checkout.getReturnDate()));
    }
}
