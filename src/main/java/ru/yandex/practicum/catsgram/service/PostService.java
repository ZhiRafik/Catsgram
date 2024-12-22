package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.enums.SortOrder;
import ru.yandex.practicum.catsgram.model.Post;

import java.util.Optional;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {

    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(int size, Long from, String sort) {
        if (size < 0 || from < 0 || from + size > posts.size()) {
            throw new ConditionsNotMetException("Requested range is out of bounds.");
        }
        SortOrder sortOrder = SortOrder.from(sort);
        if (sortOrder == null) {
            throw new ConditionsNotMetException("Sort must be ascending (asc) or descending (desc).");
        }
        LinkedList<Post> foundPosts = new LinkedList<>(); // использую LinkedList, т.к. неизвестно количество постов
        // ArrayList может использовать много лишней памяти. Если использовать Stream API, чтобы сначала отсортировать,
        // а потом уже выбрать нужные посты, то я создаю огрмоный лишний дубликат (постов может быть 1.000.000.000)
        if (sortOrder == SortOrder.ASCENDING) {
            for (int i = 0; i < size; i++) { // посты хранятся в posts и так по возрастанию, т.к. - первый добавленный
                foundPosts.add(posts.get(from + i));
            }
        } else {
            for (int i = 0; i < size; i++) {
                foundPosts.addFirst(posts.get(from + i)); // добавляем в начало списка
            }
        }
        return foundPosts;
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        if (userService.findUserById(post.getAuthorId()).isEmpty()) {
            throw new ConditionsNotMetException("«Автор с id = " + post.getAuthorId() + " не найден»");
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    public Optional<Post> getPostById(long id) {
        return Optional.of(posts.get(id));
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}