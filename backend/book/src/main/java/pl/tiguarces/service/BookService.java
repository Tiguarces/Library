package pl.tiguarces.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tiguarces.controller.request.NewAuthorRequest;
import pl.tiguarces.controller.request.NewBookRequest;
import pl.tiguarces.controller.request.SearchBookRequest;
import pl.tiguarces.controller.response.BookDetailsResponse;
import pl.tiguarces.model.Author;
import pl.tiguarces.model.Book;
import pl.tiguarces.model.Picture;
import pl.tiguarces.model.Publisher;
import pl.tiguarces.repository.AuthorRepository;
import pl.tiguarces.repository.BookRepository;
import pl.tiguarces.repository.CategoryRepository;
import pl.tiguarces.repository.PublisherRepository;

import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    private static final String SEMICOLON = ";";
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(SEMICOLON);

    public Page<Book> findAll(final SearchBookRequest request) {
        return repository.findAllByRequest(request.getCategory(), request.getPriceFrom(), request.getPriceTo(),
                                           request.getNumberOfPagesFrom(), request.getNumberOfPagesTo(), request.getPublicationYearFrom(),
                                           request.getPublicationYearTo(), request.getCover(), PageRequest.of(request.getPage(), request.getSize()));
    }

    @Transactional(readOnly = true)
    public Optional<BookDetailsResponse> findById(final long bookId) {
        var possibleBook = repository.findById(bookId);

        if(possibleBook.isPresent()) {
            var book = possibleBook.get();
            var authors = book.getAuthors()
                              .stream()
                              .map(author -> new BookDetailsResponse.Author(author.getAuthorId(), author.getName(),
                                                                            author.getDescription(), author.getPicture()))
                              .toList();

            var pictures = new LinkedList<String>() {{
                add(book.getMainPicture());
                addAll(book.getPictures()
                           .stream()
                           .map(Picture::getPath)
                           .toList());
            }};

            var bookCategory = book.getCategory();
            String categoryIndex = bookCategory.getIndex();
            String category;
            String subCategory = null;

            if(!categoryIndex.endsWith(SEMICOLON)) {
                String[] splitCategoryIndex = SEMICOLON_PATTERN.split(categoryIndex);

                category = categoryRepository.findNameByIndex(splitCategoryIndex[0]);
                subCategory = categoryRepository.findNameByIndex(splitCategoryIndex[1]);

            } else {
                category = bookCategory.getName();
            }

            return Optional.of(new BookDetailsResponse(bookId, book.getTitle(), book.getPrice(), book.getOriginalPrice(),
                                                               book.getQuantity(), book.getPublisher(), authors, book.getNumberOfPages(),
                                                               book.getEdition(), book.getNumberOfStars(), book.getPublicationYear(), book.getDescription(),
                                                               category, subCategory, pictures, book.getCover().toString()));

        } else return Optional.empty();
    }

    public void save(final NewBookRequest request) {
        repository.save(mapToNewBook(request));
    }

    private Book mapToNewBook(final NewBookRequest request) {
        Book book = new Book();
             book.setTitle(request.title());
             book.setPrice(request.price());
             book.setNumberOfPages(request.numberOfPages());
             book.setEdition(request.edition());
             book.setPublicationYear(request.publicationYear());    // TODO: set as required field
             book.setDescription(request.description());
             book.setMainPicture(request.mainPicture());
             book.setCover(request.cover());
//             book.setCategory(request.category());
             book.setQuantity(request.quantity());

        // TODO: set name as unique fields
        publisherRepository.findByName(request.publisher())
                           .ifPresentOrElse(book::setPublisher, () -> book.setPublisher(new Publisher(request.publisher(), book)));

        book.setAuthors(request.authors()
                               .stream()
                               .map(authorRequest -> mapToAuthor(authorRequest, book))
                               .toList());

        book.setPictures(request.pictures()
                                .stream()
                                .map(picture -> new Picture(picture, book))
                                .toList());

        return book;
    }

    private Author mapToAuthor(final NewAuthorRequest request, final Book book) {
        return authorRepository.findByName(request.name())
                               .orElse(new Author(request, book));
    }
}
