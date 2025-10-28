package com.vizja.swp.lab2;


import com.vizja.swp.lab2.controller.HomeController;
import com.vizja.swp.lab2.lib.FrontController;
import com.vizja.swp.lab2.lib.Server;

public class Application {
    public static void main(String[] args) {

        try (final var server = new Server()) {
            FrontController.addRoute("/", new HomeController());

            server.start(8090);
        }
    }
}