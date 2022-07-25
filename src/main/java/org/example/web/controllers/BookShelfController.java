package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.example.web.dto.BookRegexToRemove;
import org.example.web.dto.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Controller
@RequestMapping(value = "/books")
@Scope("singleton")
public class BookShelfController {

  private Logger logger = Logger.getLogger(BookShelfController.class);
  private BookService bookService;

  @Autowired
  public BookShelfController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping("/shelf")
  public String books(Model model) {
    logger.info(this.toString());
    model.addAttribute("book", new Book());
    model.addAttribute("file", new UploadFile());
    model.addAttribute("bookIdToRemove", new BookIdToRemove());
    model.addAttribute("bookRegexToRemove", new BookRegexToRemove());
    model.addAttribute("bookList", bookService.getAllBooks());
    return "book_shelf";
  }

  @PostMapping("/save")
  public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("book", book);
      model.addAttribute("file", new UploadFile());
      model.addAttribute("bookIdToRemove", new BookIdToRemove());
      model.addAttribute("bookRegexToRemove", new BookRegexToRemove());
      model.addAttribute("bookList", bookService.getAllBooks());
      return "book_shelf";
    } else {
      bookService.saveBook(book);
      logger.info("current repository size: " + bookService.getAllBooks().size());
      return "redirect:/books/shelf";
    }
  }

  @PostMapping("/remove")
  public String removeBook(@Valid BookIdToRemove bookIdToRemove, BindingResult bindingResult, Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("book", new Book());
      model.addAttribute("file", new UploadFile());
      model.addAttribute("bookRegexToRemove", new BookRegexToRemove());
      model.addAttribute("bookList", bookService.getAllBooks());
      return "book_shelf";
    } else {
      bookService.removeBookById(bookIdToRemove.getId());
      return "redirect:/books/shelf";
    }
  }

  @PostMapping("/uploadFile")
  public String uploadFile(@Valid UploadFile file, BindingResult bindingResult, Model model) throws Exception {
    file.validate(bindingResult);
    if (bindingResult.hasErrors()) {
      model.addAttribute("book", new Book());
      model.addAttribute("bookIdToRemove", new BookIdToRemove());
      model.addAttribute("bookRegexToRemove", new BookRegexToRemove());
      model.addAttribute("bookList", bookService.getAllBooks());
      return "book_shelf";
    } else {
      String name = file.getFile().getOriginalFilename();
      byte[] bytes = file.getFile().getBytes();

      //create dir
      String rootPath = System.getProperty("catalina.home");
      File dir = new File(rootPath + File.separator + "external_uploads");
      if (!dir.exists()) {
        dir.mkdirs();
      }

      //create file
      File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
      BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
      stream.write(bytes);
      stream.close();

      logger.info("new file saved at: " + serverFile.getAbsolutePath());

      return "redirect:/books/shelf";
    }
  }

  @PostMapping("/removeByRegex")
  public String removeBookByRegex(@Valid BookRegexToRemove bookRegexToRemove, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("book", new Book());
      model.addAttribute("file", new UploadFile());
      model.addAttribute("bookIdToRemove", new BookIdToRemove());
      model.addAttribute("bookList", bookService.getAllBooks());
      return "book_shelf";
    } else {
      bookService.removeBookByRegex(bookRegexToRemove.getQueryRegex());
      return "redirect:/books/shelf";
    }
  }
}
