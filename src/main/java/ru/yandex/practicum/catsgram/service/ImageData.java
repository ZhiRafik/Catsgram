package ru.yandex.practicum.catsgram.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageData {
    private final byte[] data;
    private final String name;
}