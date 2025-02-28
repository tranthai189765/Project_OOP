package config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverSetup {

    // Đường dẫn đến ChromeDriver
    private static final String CHROME_DRIVER_PATH = "D:\\clusterTestFile\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";

    // Thiết lập ChromeDriver cho WebDriver
    private static void setupChromeDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
    }

    // Trả về một đối tượng ChromeDriver mới
    public static WebDriver getChromeDriver() {
        setupChromeDriver();
        return new ChromeDriver();
    }
}
