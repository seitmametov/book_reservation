package com.example.library.service;

import com.example.library.entity.Category;
import com.example.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(String name, String description) {

        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category already exists");
        }

        return categoryRepository.save(
                new Category(null, name, description)
        );
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }
}

