package com.gpsolutions.selenium_demo;

import com.gpsolutions.selenium_demo.main.dto.EmailMessage;
import com.gpsolutions.selenium_demo.main.service.EmailMessageExtractor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeleniumDemoApplicationTests {
    private WebDriver driver;
    private WebDriverWait webDriverWaitForOperation;
    private WebDriverWait webDriverWaitForProject;
    private Actions clickAction;
    private EmailMessageExtractor emailMessageExtractor;
    @Value("${test.driver_property_key}")
    private String driverPropertyKey;
    @Value("${test.driver_property_value}")
    private String driverPropertyValue;
    @Value("${mail.ru.user}")
    private String user;
    @Value("${mail.ru.sender_mail}")
    private String senderEmail;
    private String projectName;
    @Value("${test.first_name}")
    private String firstName;
    @Value("${test.second_name}")
    private String secondName;
    @Value("${test.base_url}")
    private String baseUrl;
    private LocalDateTime requestForValidationCodeDate;

    @PostConstruct
    public void init() {
        System.setProperty(driverPropertyKey, driverPropertyValue);
        projectName = generateProjectName();
        driver = new ChromeDriver();
        driver.get(baseUrl);
        webDriverWaitForOperation = new WebDriverWait(driver, 3);
        webDriverWaitForProject = new WebDriverWait(driver, 10);
        clickAction = new Actions(driver);
    }

    private String generateProjectName() {
        return "Project" + new Random(1_000_000).nextInt();
    }

    @Test
    public void testCreateAccountAndProjectProject() {
        testCreateAccount();
        testCreateProject();
        testIfProjectCreated();
    }

    private void testCreateAccount() {
        final WebElement createANewProjectButton =
                webDriverWaitForOperation.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@class='caption-button' and .='Create a new project']")));
        createANewProjectButton.click();

        webDriverWaitForOperation.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@class='message-segment text']/span[.='or enter another user.']")),
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@class='message-segment text']/span[.='Please, enter your email address.']"))));

        final WebElement messageAreaDiv = driver.findElement(By.xpath("//div[@class='input-placeholder']"));
        clickAction.moveToElement(messageAreaDiv).click().perform();
        final WebElement messageArea = driver.findElement(By.xpath("//div[@class='content-inner']"));
        messageArea.sendKeys(user);
        messageArea.sendKeys(Keys.ENTER);
        requestForValidationCodeDate = LocalDateTime.now(ZoneId.systemDefault());

        final int validationCode = getValidationCode();
        messageArea.sendKeys(String.valueOf(validationCode));
        messageArea.sendKeys(Keys.ENTER);
    }

    private int getValidationCode() {
        EmailMessage newEmailMessage = null;
        do {
            for (EmailMessage emailMessage : emailMessageExtractor.getEmailMessagesBySenderEmailAddress(senderEmail)) {
                if (emailMessage.getReceivedDate().isAfter(requestForValidationCodeDate)) {
                    newEmailMessage = emailMessage;
                    break;
                }
            }
        } while (newEmailMessage == null);
        return Integer.valueOf(newEmailMessage.getMessageText().replaceAll("\\D", ""));
    }

    private void testCreateProject() {
        webDriverWaitForOperation.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='message-segment text']/span[.='Great! Please enter project name.']")));
        final WebElement messageAreaDiv = driver.findElement(By.xpath(
                "//div[contains(@class, 'input-placeholder')]"));
        clickAction.moveToElement(messageAreaDiv).click().perform();
        final WebElement messageArea = driver.findElement(By.xpath(
                "//div[contains(@class, 'content-inner')]"));
        messageArea.sendKeys(projectName);
        messageArea.sendKeys(Keys.ENTER);

        webDriverWaitForOperation.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='message-segment text']/span[.='...your first name?']")));
        messageArea.sendKeys(firstName);
        messageArea.sendKeys(Keys.ENTER);

        webDriverWaitForOperation.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='message-segment text']/span[.='...and your second name?']")));
        messageArea.sendKeys(secondName);
        messageArea.sendKeys(Keys.ENTER);
    }

    private void testIfProjectCreated() {
        Assert.assertNotNull("Project " + projectName + " was not created!",
                webDriverWaitForProject.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class,'node-name')]/span[.='" + projectName + "']"))));
    }

    @Autowired
    public void setEmailMessageExtractor(final EmailMessageExtractor emailMessageExtractor) {
        this.emailMessageExtractor = emailMessageExtractor;
    }
}

