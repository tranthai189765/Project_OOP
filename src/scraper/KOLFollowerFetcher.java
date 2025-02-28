package scraper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.ConfigInterface;
import entities.User;
import filehandler.FileHandler;
import manager.DataManagerInterface;

public class KOLFollowerFetcher implements DataFetcherStrategy{
	private final DataManagerInterface localManager;
	private final int maxFollowers;
	private final ConfigInterface config;

    public KOLFollowerFetcher(ConfigInterface config) {
        this.config = config;
        this.localManager = config.getLocalManager();
        this.maxFollowers = config.getMaxFollowers();
    }
    

	
	public void fetchFollowers(WebDriver driver, User kol, DataManagerInterface remoteManager) {
	    System.out.println("Fetching KOL followers...");
	    remoteManager.addUserToDataBase(kol);
	    List<String> paths = config.getPathToFollowers(kol.getUrl());
	    if (paths.isEmpty()) {
	        System.out.println("Không tìm thấy đường dẫn tới followers.");
	        return;
	    }

	    for (String path : paths) {
	        try {
	            driver.get(path);
	            Thread.sleep(2000); // Chờ tải trang

	            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	            int count = 0;
	            int retryCount = 0; // Đếm số lần retry liên tiếp
	            long startTime = Instant.now().getEpochSecond(); // Thời gian bắt đầu
	            long timeout = 30; // Thời gian tối đa cho vòng lặp (30 giây)

	            while (count < maxFollowers) {
	                try {
	                    List<WebElement> retryElements = driver.findElements(By.xpath(config.getRetryButtonXpath()));
	                    List<WebElement> followers = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(config.getUserCellCSS())));

	                    if (followers.isEmpty()) {
	                        retryCount++;
	                        if (retryCount >= 3) {
	                            System.out.println("Không có dữ liệu mới sau 3 lần retry. Dừng lại.");
	                            break;
	                        }
	                        handleRetry(retryElements);
	                        continue;
	                    } else {
	                        retryCount = 0; // Reset retry nếu tìm thấy dữ liệu mới
	                    }

	                    // Xử lý danh sách followers
	                    for (WebElement follower : followers) {
	                        if (count >= maxFollowers) break;
	                        processFollower(follower, kol);
	                        count++;
	                    }

	                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 800)");
	                    Thread.sleep(2000); // Chờ tải thêm dữ liệu

	                    // Kiểm tra timeout
	                    if (Instant.now().getEpochSecond() - startTime > timeout) {
	                        System.out.println("Quá thời gian cho phép. Dừng vòng lặp.");
	                        break;
	                    }
	                } catch (Exception e) {
	                    System.out.println("Lỗi trong vòng lặp xử lý followers: " + e.getMessage());
	                    e.printStackTrace();
	                    break;
	                }
	            }

	            remoteManager.updateFollowersForUser(kol.getId(), kol.getFollowers());
	            remoteManager.saveToDatabase();
	        } catch (Exception e) {
	            System.out.println("Lỗi trong phương thức fetchFollowers: " + e.getMessage());
	            e.printStackTrace();
	        }

	        try {
	            Thread.sleep(8000); // Chờ trước khi xử lý đường dẫn tiếp theo
	        } catch (InterruptedException e) {
	            System.out.println("Lỗi trong khi chờ: " + e.getMessage());
	            Thread.currentThread().interrupt();
	        }
	    }
	}

	// Hàm xử lý retry
	private void handleRetry(List<WebElement> retryElements) {
	    try {
	        if (!retryElements.isEmpty()) {
	            retryElements.get(0).click();
	            System.out.println("Đã nhấn nút Retry để tải thêm users.");
	        } else {
	            System.out.println("Không tìm thấy nút Retry.");
	        }
	        Thread.sleep(30000); // Đợi trước khi thử lại
	    } catch (Exception e) {
	        System.out.println("Lỗi khi nhấn nút Retry: " + e.getMessage());
	    }
	}

	// Hàm xử lý từng follower
	private void processFollower(WebElement follower, User kol) {
	    try {
	        List<WebElement> links = follower.findElements(By.cssSelector(config.getLinksCSS()));
	        if (!links.isEmpty()) {
	            String userProfileUrl = links.get(0).getAttribute("href");
	            User user = new User(userProfileUrl);

	            if (!kol.hasFollower(user.getId())) {
	                kol.addFollower(user.getId());
	                System.out.println("Thêm follower mới: " + user.getId());
	            }
	        } else {
	            WebElement nameElement = follower.findElement(By.cssSelector(config.getNameElementCSS()));
	            if (nameElement != null) {
	                System.out.println("Không tìm thấy liên kết cho người dùng: " + nameElement.getText());
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("Không thể lấy thông tin của người dùng này: " + e.getMessage());
	        e.printStackTrace();
	    }
	}


	
	public void fetchFollowersFromKOLFile(WebDriver driver, String filepath, DataManagerInterface remoteManager) {
		// TODO Auto-generated method stub
        System.out.println("Đọc các liên kết từ file: " + filepath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("https")) { // Kiểm tra nếu là đường dẫn hợp lệ
                    System.out.println("Xử lý URL: " + line);
                    User user = new User(line); // Tạo đối tượng User từ URL
                    if(localManager.hasUser(user.getId())==true) {
                    	user = localManager.getUserById(user.getId());
                    	if(user.getFollowers().isEmpty()) {
                    		// Gọi fetchProfile để xử lý User
                        	fetchFollowers(driver, user, remoteManager);
                        }else {
                        	System.out.println("Danh sách followers của User "+ user.getId() + " đã có trong database");
                        }
                    }else {
                    	// Gọi fetchProfile để xử lý User
                    	System.out.println("Chưa có thông tin gì về Followers của User " + user.getId());
                    	fetchFollowers(driver, user, remoteManager);
                    }
                    
                            
                } else {
                    System.out.println("Bỏ qua dòng không hợp lệ: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Có lỗi xảy ra khi đọc file: " + e.getMessage());
        }
		
		
	}


	@Override
	public void fetchFollowersMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		String filepath = config.getKolFilePath();
		try {
			List<String> subFilePaths = FileHandler.splitFile(filepath, threadCount);
			// Tạo thread pool với số threads xác định
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // Tạo task cho mỗi thread
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                String subFilePath = subFilePaths.get(i);
                executor.submit(() -> {
                    try {
                        // Đọc thông tin đăng nhập cho thread
                        String loginInfoPath = threadIndex + "_logininfo.txt";
                        System.out.println(loginInfoPath);
                        Map<String, String> credentials = FileHandler.getCredentialsFromFile(loginInfoPath);
                        String username = credentials.get("username");
                        String password = credentials.get("password");
                        String email = credentials.get("email");
                        LoginService loginService = new LoginService(username, password, email, config);
                        WebDriver driver = new ChromeDriver();
                        // Đăng nhập
                        loginService.login(driver);
                        String remoteManagerPath = threadIndex + "_database.json";
                        DataManagerInterface remoteManager = config.newManager(remoteManagerPath);
          
                        // Đăng nhập
   

                        // Đào tweets từ file nhỏ
                        fetchFollowersFromKOLFile(driver, subFilePath, remoteManager);
                       // DataSyncManager.syncFromRemote(this.localManager, remoteManager);
                        driver.quit();

                    } catch (Exception e) {
                        System.err.println("Thread " + threadIndex + " gặp lỗi: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

            // Đóng ExecutorService sau khi hoàn tất
            executor.shutdown();
            while (!executor.isTerminated()) {
                Thread.sleep(100); // Chờ threads hoàn thành
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}



	@Override
	public void fetchUserByHashtagsMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void fetchProfileMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void fetchTweetsMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}


}
