package dcs;

import static spark.Spark.*;

public class Main {
    // the main entry point for this hot mess of an application
    public static void main(String[] args) {
        // tell the Spark framework where to find static files
        staticFiles.location("/static");

        // for convenience so you don't have to create new
        // accounts all the time with VolatileDB
        Database db = LoginController.getDatabase();
        DCSUser defaultUser = new DCSUser("intern@wondoughbank.com");
        defaultUser.setPassword("password");
        db.addUser(defaultUser);

        // map routes to controllers
        get("/", IndexController.serveIndexPage);
        get("/register/", LoginController.serveRegisterPage);
        get("/login/", LoginController.serveLoginPage);
        get("/2fa/", LoginController.serve2FAPage);
        post("/register/", LoginController.handleRegistration);
        post("/login/", LoginController.handleLoginPost);
        post("/logout/", LoginController.handleLogoutPost);
        post("/2fa/", LoginController.handle2FA);
    }
}
