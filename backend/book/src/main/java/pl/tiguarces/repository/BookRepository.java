package pl.tiguarces.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.tiguarces.model.Book;
import pl.tiguarces.model.BookCover;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT new pl.tiguarces.model.Book(b.bookId, b.title, b.price, b.originalPrice, b.numberOfStars, b.mainPicture)
            FROM Book b
            JOIN b.category c
            WHERE (:category IS NULL OR c.index = :category) AND
                  (:priceFrom IS NULL OR b.price >= :priceFrom) AND
                  (:priceTo IS NULL OR b.price <= :priceTo) AND
                  (:numberOfPagesFrom IS NULL OR b.numberOfPages >= :numberOfPagesFrom) AND
                  (:numberOfPagesTo IS NULL OR b.numberOfPages <= :numberOfPagesTo) AND
                  (:publicationYearFrom IS NULL OR b.publicationYear >= :publicationYearFrom) AND
                  (:publicationYearTo IS NULL OR b.publicationYear <= :publicationYearTo) AND
                  (:cover IS NULL OR b.cover = :cover)
            ORDER BY b.bookId DESC
           """)
    Page<Book> findAllByRequest(String category, Double priceFrom, Double priceTo, Integer numberOfPagesFrom,
                                Integer numberOfPagesTo, Integer publicationYearFrom, Integer publicationYearTo,
                                BookCover cover, Pageable pageable);

}
