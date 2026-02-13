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

    @Transactional // ОБЯЗАТЕЛЬНО добавь это, так как у нас два действия с БД
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 1. Проверяем наличие АКТИВНЫХ книг (не удаленных мягко)
        // Допустим, мы разрешаем удалять категорию, если в ней нет ЖИВЫХ книг
        boolean hasActiveBooks = bookRepository.existsByCategoryIdAndDeletedFalse(id);

        if (hasActiveBooks) {
            throw new RuntimeException("Нельзя удалить категорию: в ней всё еще есть активные книги!");
        }

        // 2. РЕШАЕМ ПРОБЛЕМУ FOREIGN KEY:
        // Находим все архивные (soft-deleted) книги этой категории и убираем у них привязку
        List<Book> archivedBooks = bookRepository.findAllByCategoryId(id);
        for (Book book : archivedBooks) {
            book.setCategory(null); // или перекинь в категорию "Без категории"
        }
        bookRepository.saveAll(archivedBooks);

        // 3. Теперь база позволит удалить категорию
        categoryRepository.delete(category);
    }
}

