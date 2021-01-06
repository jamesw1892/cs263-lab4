# Lab 4 of CS263: Cyber Security

My code for lab 4, implementing 2FA in an existing web service.

# What happens

When the user logs in, a key is generated for their session which is presented to them on the 2FA page. They then have to input this into the authenticator application which outputs the one-time passcode based on the key and the current time interval. They then enter this back into the server which compares it to its own calculation. If they are the same, the user is allowed in. Unlike most authenticator systems where the user only has to type in one code, in this application, they have to enter two so both need to be human-readable. This is why I convert them to 6- or 8-digit integers.

# Usage

1. In a terminal, run the webserver with `cd server` and `gradle run`
1. Navigate to the displayed URL
1. Login with default details: username=`intern@wondoughbank.com`, password=`password`
1. In another terminal, run the authenticator with `cd authenticator` and `gradle run`
1. Type in the key from the 2FA page of the website into the authenticator command line
1. Type in the one-time-passcode from the authenticator into the 2FA page of the website

If successful, you will be redirected to the homepage where it asks if you want to register or login. If unsuccessful, you will be redirected to the login page.

# Exercises

## Ex10

Implement the `intervals` method in `authenticator/src/main/java/Program.java`. I used methods in the `java.time` package to calculate the current time, find the difference between them and divide that by the time interval.

## Ex11

Implement the `otp` method in `authenticator/src/main/java/Program.java` which calls the method we completed in Ex10 to generate the one time password based on the time interval and a password. I generate a SHA-256 HMAC and convert it to a 6-digit integer so it is easy to read and remember.

## Ex12

Copy the `intervals` and `otp` methods from `authenticator/src/main/java/Program.java` to `server/src/main/java/dcs/LoginController.java`.

## Ex13

Implement the `generate2FAKey` method in `server/src/main/java/dcs/LoginController.java` to generate a specific key for the SHA_256 HMAC using a CSPRNG.

## Ex14

Complete the `handleLoginPost` method in `server/src/main/java/dcs/LoginController.java` to generate the key using the `generate2FAKey` method and store it in the session.

## Ex15

Complete the `serve2FAPage` method in `server/src/main/java/dcs/LoginController.java` to put the key from the session in the model to serve the 2FA page if it exists (and therefore they have been authenticated).

## Ex16

Complete the `handle2FA` method in `server/src/main/java/dcs/LoginController.java` to get the key, get the user to input the one-time-passcode from the authenticator and check it is valid.