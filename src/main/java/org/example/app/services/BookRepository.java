package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookRepository implements ProjectRepository<Book>, ApplicationContextAware {

  private final Logger logger = Logger.getLogger(BookRepository.class);
  //private final List<Book> repo = new ArrayList<>();
  private ApplicationContext context;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public BookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Book> retreiveAll() {
    List<Book> books = jdbcTemplate.query("SELECT * FROM books", (ResultSet rs, int rowNum) -> {
      Book book = new Book();
      book.setId(rs.getInt("id"));
      book.setAuthor(rs.getString("author"));
      book.setTitle(rs.getString("title"));
      book.setSize(rs.getInt("size"));
      return book;
    });
    return new ArrayList<>(books);
  }

  @Override
  public void store(Book book) {
    if (book.getAuthor().isEmpty() && book.getTitle().isEmpty() && book.getSize() == null)
      return;

    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("author", book.getAuthor());
    parameterSource.addValue("title", book.getTitle());
    parameterSource.addValue("size", book.getSize());
    jdbcTemplate.update("INSERT INTO books(author, title, size) VALUES(:author, :title, :size)", parameterSource);
    logger.info("store new book: " + book);
  }

  @Override
  public boolean removeItemById(Integer bookIdToRemove) {
    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("id", bookIdToRemove);
    jdbcTemplate.update("DELETE FROM books WHERE id = :id", parameterSource);
    logger.info("remove book completed");
    return true;
  }

  @Override
  public boolean removeItemByRegex(String bookRegexToRemove) {
    if (removeItemByAuthor(bookRegexToRemove)) {
      return true;
    }
    if (removeItemByTitle(bookRegexToRemove)) {
      return true;
    }
    try {
      return removeItemBySize(Integer.parseInt(bookRegexToRemove));
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

  public boolean removeItemByAuthor(String bookAuthorToRemove) {
    List<Book> booksForRemove = new ArrayList<>();
    for (Book book : retreiveAll()) {
      if (book.getAuthor().equals(bookAuthorToRemove)) {
        booksForRemove.add(book);
        logger.info("remove book completed: " + book);
      }
    }
    if (!booksForRemove.isEmpty()) {
      for (Book book : booksForRemove) {
        removeItemById(book.getId());
      }
      return true;
    }
    return false;
  }

  public boolean removeItemByTitle(String bookTitleToRemove) {
    List<Book> booksForRemove = new ArrayList<>();
    for (Book book : retreiveAll()) {
      if (book.getTitle().equals(bookTitleToRemove)) {
        booksForRemove.add(book);
        logger.info("remove book completed: " + book);
      }
    }
    if (!booksForRemove.isEmpty()) {
      for (Book book : booksForRemove) {
        removeItemById(book.getId());
      }
      return true;
    }
    return false;
  }

  public boolean removeItemBySize(Integer bookSizeToRemove) {
    List<Book> booksForRemove = new ArrayList<>();
    for (Book book : retreiveAll()) {
      if (book.getSize().equals(bookSizeToRemove)) {
        booksForRemove.add(book);
        logger.info("remove book completed: " + book);
      }
    }
    if (!booksForRemove.isEmpty()) {
      for (Book book : booksForRemove) {
        removeItemById(book.getId());
      }
      return true;
    }
    return false;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  private void defaultInit() {
    logger.info("default INIT in book repo bean");
  }

  private void defaultDestroy() {
    logger.info("default DESTROY in book repo bean");
  }
}
