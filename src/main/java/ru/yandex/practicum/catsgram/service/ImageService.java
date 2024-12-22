package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.catsgram.model.Image;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ImageFileException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final PostService postService;
    private final Map<Long, Image> images = new HashMap<>();
    private final String imageDirectory = "/Users/nadejda/Desktop/хзчо/catsgramImages";

    public List<Image> getPostImages(long postId) {
        return images.values()
                .stream()
                .filter(image -> image.getPostId() == postId)
                .collect(Collectors.toList());
    }

    public List<Image> saveImages(long postId, List<MultipartFile> files) {
        return files.stream().map(file -> saveImage(postId, file)).collect(Collectors.toList());
    }

    public ImageData getImageData(long imageId) {
        if (!images.containsKey(imageId)) {
            throw new NotFoundException("Изображение с id = " + imageId + " не найдено");
        }
        Image image = images.get(imageId);
        // загрузка файла с диска
        byte[] data = loadFile(image);

        return new ImageData(data, image.getOriginalFileName());
    }

    private byte[] loadFile(Image image) {
        Path path = Paths.get(image.getFilePath());
        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new ImageFileException("Ошибка чтения файла.  Id: " + image.getId()
                        + ", name: " + image.getOriginalFileName() + ", " + e);
            }
        } else {
            throw new ImageFileException("Файл не найден. Id: " + image.getId()
                    + ", name: " + image.getOriginalFileName());
        }
    }

    private Image saveImage(long postId, MultipartFile file) {
        Post post = postService.getPostById(postId)
                .orElseThrow(() -> new ConditionsNotMetException("Указанный пост не найден"));

        Path filePath = saveFile(file, post);
        long imageId = getNextId();

        Image image = new Image();
        image.setId(imageId);
        image.setFilePath(filePath.toString());
        image.setPostId(postId);
        image.setOriginalFileName(file.getOriginalFilename());

        images.put(imageId, image);

        return image;
    }


    private Path saveFile(MultipartFile file, Post post) {
        try {
            String uniqueFileName = String.format("%d.%s", Instant.now().toEpochMilli(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename()));

            Path uploadPath = Paths.get(imageDirectory, String.valueOf(post.getAuthorId()), post.getId().toString());
            Path filePath = uploadPath.resolve(uniqueFileName);
            // imageDirectory/authorId/postId/uniqueFileName

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            file.transferTo(filePath);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getNextId() {
        long currentMaxId = images.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
