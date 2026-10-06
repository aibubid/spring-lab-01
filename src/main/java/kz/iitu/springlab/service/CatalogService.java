package kz.iitu.springlab.service;

import kz.iitu.springlab.audit.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    // ссылка на прокси этого же бина; @Lazy убирает ошибку циклической зависимости
    @Autowired
    @Lazy
    private CatalogService self;

    public String findById(long id) {
        sleep(50); // имитация запроса к БД
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300); // намеренно «медленный» метод
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    // ВЕРСИЯ ПОСЛЕ ИСПРАВЛЕНИЯ: вызовы через self, то есть через прокси
    public String removeTwice(long id) {
        String first = self.remove(id);
        String second = self.remove(id + 1);
        return first + "; " + second;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}