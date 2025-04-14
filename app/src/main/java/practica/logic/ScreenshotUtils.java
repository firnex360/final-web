package practica.logic;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.TakesScreenshot;
import java.util.Base64;

public class ScreenshotUtils {

    public static String generateBase64Preview(String url) {
        // Set up headless Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // new headless mode
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        ChromeDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            driver.get(url);

            // Wait a bit for page to load (you can add smarter waits)
            Thread.sleep(2000);

            // Capture screenshot and convert to base64
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return Base64.getEncoder().encodeToString(screenshot);
        } catch (Exception e) {
            System.err.println("Error generating screenshot: " + e.getMessage());
            return "";
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
