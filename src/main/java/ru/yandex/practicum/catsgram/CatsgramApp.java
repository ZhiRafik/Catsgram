package ru.yandex.practicum.catsgram;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class CatsgramApp {
    public static void main(final String[] args) {
        final Gson gson = new Gson();
        final Scanner scanner = new Scanner(System.in);
        SpringApplication.run(CatsgramApp.class, args);

    }
}
