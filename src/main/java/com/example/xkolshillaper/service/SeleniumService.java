package com.example.xkolshillaper.service;

import jakarta.annotation.PostConstruct;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v123.backgroundservice.BackgroundService;
import org.openqa.selenium.devtools.v123.backgroundservice.model.ServiceName;
import org.openqa.selenium.devtools.v123.network.Network;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SeleniumService {

    @PostConstruct
    public void launchDriverLoginAndListenForNotifications(){
        DriverSetup driverSetup = getDriverSetup();
        loginInX(driverSetup.driver());
        printNewPosts(driverSetup.devTools());
    }

    private static DriverSetup getDriverSetup() {
        Map<String, Object> prefs = new HashMap<>();

        prefs.put("profile.default_content_setting_values.notifications", 1);

        ChromeOptions options = new ChromeOptions();

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-cache");

        ChromeDriver driver = new ChromeDriver(options);

        DevTools devTools = driver.getDevTools();
        devTools.createSession();

        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        return new DriverSetup(driver, devTools);
    }

    private record DriverSetup(ChromeDriver driver, DevTools devTools) {
    }

    private static void printNewPosts(DevTools devTools) {
        devTools.send(BackgroundService.startObserving(ServiceName.NOTIFICATIONS));
        devTools.send(BackgroundService.setRecording(true, ServiceName.NOTIFICATIONS));

        devTools.addListener(BackgroundService.backgroundServiceEventReceived(), event -> {

            String tweetStatusId = event.getInstanceId().replaceAll("tweet-", "");

            System.out.println(event.getInstanceId());

            String tweetMadeByName = event.getEventMetadata().get(0).getValue();
            String tweetContent = event.getEventMetadata().get(1).getValue();

            System.out.println(tweetMadeByName);
            System.out.println(tweetContent);

            /*Use the post info as you want*/
        });
    }

    private static void loginInX(ChromeDriver driver) {
        String url = "https://twitter.com/i/flow/login";
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='text'][type='text']")));
        usernameField.sendKeys(System.getenv("X_USERNAME"));

        WebElement nextButton =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='button' and contains(@class, 'css-175oi2r')]//span[text()='Next']")));
        nextButton.click();

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='password'][type='password']")));
        passwordField.sendKeys(System.getenv("X_PASSWORD"));

        WebElement loginButton =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-testid='LoginForm_Login_Button']")));
        loginButton.click();
    }
}
