package scraper;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.ConfigInterface;
import filehandler.FileHandler;

public class UserFinder implements DataFetcherStrategy {
    private ConfigInterface config;
    private int maxUsers;
    private String hashtagsFilePath;

    public UserFinder(ConfigInterface config) {
        this.config = config;
        this.maxUsers = config.getMaxUsers();;
        this.hashtagsFilePath = config.getHashTagsFilePath();
    }

    public void findKOLs(WebDriver driver, String hashtag, String outputFilepath) {
        searchHashtag(driver, hashtag);
        Set<String> links = collectUserLinks(driver, hashtag, maxUsers);
        FileHandler.writeListToFile(outputFilepath, links);
    }

    public void runWithHashtagsFromFile(WebDriver driver, String filepath, String outputFilepath) {
        Set<String> hashtags = FileHandler.readElementsFromFile(filepath);

        if (hashtags.isEmpty()) {
            System.out.println("Không tìm thấy hashtag nào trong file.");
            return;
        }

        for (String hashtag : hashtags) {
                System.out.println("Đang xử lý hashtag: " + hashtag);
                findKOLs(driver, hashtag, outputFilepath);
        }
    }

    public void searchHashtag(WebDriver driver, String hashtag) {
        driver.get(config.getExploreUrl());

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(config.getSearchQuerryCSS()))
            );
            searchBox.sendKeys(hashtag);
            searchBox.submit();

            WebElement peopleTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(config.getPeopleTabXpath()))
            );
            peopleTab.click();

            wait.until(ExpectedConditions.urlContains("f=user"));
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình tìm kiếm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Set<String> collectUserLinks(WebDriver driver, String hashtag, int maxUsers) {
        Set<String> collectedLinks = FileHandler.readElementsFromFile(config.getUsersFoundFilePath());
        Set<String> recordedLinks = new HashSet<>();
        long lastWriteTime = Instant.now().getEpochSecond();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        int count = 0;

        while (count < maxUsers) {
            List<WebElement> retryElements = driver.findElements(By.xpath(config.getRetryButtonXpath()));
            List<WebElement> users = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(config.getUserCellCSS()))
            );

            if (!users.isEmpty() && Instant.now().getEpochSecond() - lastWriteTime > 300) {
                System.out.println("Không có dữ liệu mới trong 5 phút. Chương trình tự động dừng.");
                break;
            } else if (users.isEmpty() || (Instant.now().getEpochSecond() - lastWriteTime > 30)) {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    System.err.println("Lỗi trong quá trình chờ: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }

                if (!retryElements.isEmpty()) {
                    try {
                        retryElements.get(0).click();
                    } catch (Exception e) {
                        System.err.println("Lỗi khi nhấn nút Retry: " + e.getMessage());
                    }
                }
            }

            for (WebElement user : users) {
                if (count >= maxUsers) break;

                try {
                    List<WebElement> links = user.findElements(By.cssSelector(config.getLinksCSS()));
                    if (!links.isEmpty()) {
                        String userProfileUrl = links.get(0).getAttribute(config.getLinksAttributeCSS());

                        if (!collectedLinks.contains(userProfileUrl) && !recordedLinks.contains(userProfileUrl)) {
                            recordedLinks.add(userProfileUrl);
                            collectedLinks.add(userProfileUrl);
                            count++;
                            lastWriteTime = Instant.now().getEpochSecond();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi xử lý thông tin người dùng: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1100)");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.err.println("Lỗi trong quá trình chờ tải trang: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        return recordedLinks;
    }

    @Override
    public void fetchUserByHashtagsMultiThreads(int threadCount) {
        String filepath = this.hashtagsFilePath;
        try {
            List<String> subFilePaths = FileHandler.splitFile(filepath, threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                String subFilePath = subFilePaths.get(i);
                executor.submit(() -> {
                    try {
                        String loginInfoPath = threadIndex + "_logininfo.txt";
                        Map<String, String> credentials = FileHandler.getCredentialsFromFile(loginInfoPath);
                        String username = credentials.get("username");
                        String password = credentials.get("password");
                        String email = credentials.get("email");

                        LoginService loginService = new LoginService(username, password, email, config);
                        WebDriver driver = new ChromeDriver();
                        loginService.login(driver);

                        String subfilepath = threadIndex + "_UsersFromHashtags.txt";
                        runWithHashtagsFromFile(driver, subFilePath, subfilepath);

                        driver.quit();
                    } catch (Exception e) {
                        System.err.println("Thread " + threadIndex + " gặp lỗi: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                System.err.println("Không phải tất cả các threads hoàn thành trong thời gian quy định.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetchProfileMultiThreads(int threadCount) {
        // TODO: Implement method
    }

    @Override
    public void fetchFollowersMultiThreads(int threadCount) {
        // TODO: Implement method
    }

    @Override
    public void fetchTweetsMultiThreads(int threadCount) {
        // TODO: Implement method
    }
}
