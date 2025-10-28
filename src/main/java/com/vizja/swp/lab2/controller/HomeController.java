package com.vizja.swp.lab2.controller;

import com.vizja.swp.lab2.http.Cookie;
import com.vizja.swp.lab2.dto.Todo;
import com.vizja.swp.lab2.lib.BaseController;
import com.vizja.swp.lab2.lib.http.HttpRequest;
import com.vizja.swp.lab2.lib.http.HttpResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController extends BaseController {

    private static final String INDEX_HTML = "src/main/resources/templates/index.html";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public final List<Todo> todos = new ArrayList<>(
            List.of(
                    new Todo(1, "Learn Java", false),
                    new Todo(2, "Build a web app", false),
                    new Todo(3, "Deploy the app", false)
            )
    );

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        try {
            Path path = Path.of(INDEX_HTML);

            if (!Files.exists(path)) {
                response.setStatus(404, "Not Found");
                response.getWriter().println("HTML file not found: " + INDEX_HTML);
                return;
            }

            response.setStatus(200, "OK");
            response.setHeader("Content-Type", "text/html; charset=UTF-8");

            String cookieHeader = String.valueOf(request.getHeader("Cookie"));
            if (cookieHeader != null && !cookieHeader.isEmpty()) {
                response.getWriter().println("<!-- Cookies: " + cookieHeader + " -->");
            }

            try (PrintWriter writer = response.getWriter()) {
                Files.lines(path, StandardCharsets.UTF_8).forEach(writer::println);
            }
        } catch (IOException e) {
            response.setStatus(500, "Internal Server Error");
            response.getWriter().println("Error reading HTML: " + e.getMessage());
        }
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        try {
            String body = request.getBody();
            Todo newTodo = objectMapper.readValue(body, Todo.class);

            int newId = todos.size() + 1;
            Todo todoWithId = new Todo(newId, newTodo.text(), newTodo.completed());
            todos.add(todoWithId);

            Cookie cookie = new Cookie("lastTodo", newTodo.text())
                    .setPath("/")
                    .setHttpOnly(true)
                    .setMaxAge(3600); 

            response.setHeader("Set-Cookie", cookie.toString());

            response.setStatus(201, "Created");
            response.setHeader("Content-Type", "application/json");
            String jsonTodo = objectMapper.writeValueAsString(todoWithId);
            response.getWriter().println(jsonTodo);
        } catch (Exception e) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Invalid JSON format: " + e.getMessage());
        }
    }

    @Override
    public void doPut(HttpRequest request, HttpResponse response) {
        try {
            String body = request.getBody();
            Todo updatedTodo = objectMapper.readValue(body, Todo.class);

            for (Todo todo : new ArrayList<>(todos)) {
                if (todo.id().equals(updatedTodo.id())) {
                    todos.remove(todo);
                    todos.add(updatedTodo);
                    response.setStatus(200, "OK");
                    response.setHeader("Content-Type", "application/json");

                    Cookie cookie = new Cookie("lastUpdatedTodo", updatedTodo.text())
                            .setPath("/")
                            .setHttpOnly(true)
                            .setMaxAge(1800); // 30 minutes
                    response.setHeader("Set-Cookie", cookie.toString());

                    String jsonTodo = objectMapper.writeValueAsString(updatedTodo);
                    response.getWriter().println(jsonTodo);
                    return;
                }
            }

            response.setStatus(404, "Not Found");
            response.getWriter().println("Todo with ID " + updatedTodo.id() + " not found.");
        } catch (Exception e) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Invalid JSON format: " + e.getMessage());
        }
    }

    @Override
    public void doDelete(HttpRequest request, HttpResponse response) {
        try {
            String body = request.getBody();
            Todo todoToDelete = objectMapper.readValue(body, Todo.class);

            for (Todo todo : new ArrayList<>(todos)) {
                if (todo.id().equals(todoToDelete.id())) {
                    todos.remove(todo);

                    Cookie deleteCookie = new Cookie("lastTodo", "")
                            .setPath("/")
                            .setMaxAge(0);
                    response.setHeader("Set-Cookie", deleteCookie.toString());

                    response.setStatus(200, "OK");
                    response.getWriter().println("Todo with ID " + todoToDelete.id() + " deleted.");
                    return;
                }
            }

            response.setStatus(404, "Not Found");
            response.getWriter().println("Todo with ID " + todoToDelete.id() + " not found.");
        } catch (Exception e) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Invalid JSON format: " + e.getMessage());
        }
    }
}
