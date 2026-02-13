package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public Category create(String name, String description) {

        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }


    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category update(Long id, String newName, String newDescription) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Проверяем, не занято ли новое имя другой категорией
        if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            throw new RuntimeException("Category with this name already exists");
        }

        category.setName(newName);
        category.setDescription(newDescription);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // 1. Сначала проверяем, нет ли книг вообще (даже скрытых)
        // Если ты хочешь разрешить удаление, если книги "скрыты" - пропусти этот шаг.
        // Но лучше сначала очистить связи:

        List<Book> allBooks = bookRepository.findAllByCategoryIdIncludingHidden(id);

        if (!allBooks.isEmpty()) {
            // Лог для дебага:
            System.out.println("Найдено книг в категории: " + allBooks.size());

            for (Book book : allBooks) {
                book.setCategory(null);
            }
            bookRepository.saveAll(allBooks);
            bookRepository.flush(); // Принудительно отправляем в базу ПРЯМО СЕЙЧАС
        }

        // 2. Теперь удаляем категорию
        categoryRepository.delete(category);
    }
}

