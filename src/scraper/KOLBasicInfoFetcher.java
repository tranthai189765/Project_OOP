package scraper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.ConfigInterface;
import converter.TextToIntegerConverter;
import entities.User;
import filehandler.FileHandler;
import manager.DataManagerInterface;
import synchronization.DataSyncManager;


public class KOLBasicInfoFetcher implements DataFetcherStrategy {
    private DataManagerInterface localManager;
    private ConfigInterface config;

    public KOLBasicInfoFetcher(ConfigInterface config) {
    	
    	this.config = config;

        this.localManager = config.getLocalManager();
       
    }


	public void fetchProfile(WebDriver driver, User kol, DataManagerInterface remoteManager, String outputFilepath) {
	    // TODO Auto-generated method stub
	    System.out.println("Fetching KOL profile...");
	    //remoteManager.addUserToDataBase(kol);
	    driver.get(kol.getUrl());
	    try {
	        Thread.sleep(10000); // Tạm dừng 3 giây
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
	    
	    try {
	        // Lấy số lượng bài đăng
	        WebElement postCountElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(config.getPostCountCSS())));
	        String postCountText = postCountElement.getText();
	        //System.out.println("Số lượng bài đăng: " + postCountText);
	        kol.setTweetCount(postCountText);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy số lượng bài đăng.");
	    }

	    try {
	        // Tìm phần tử mô tả
	        WebElement introElement = driver.findElement(By.cssSelector(config.getUserDescriptionCSS()));

	        // Lấy toàn bộ nội dung của phần mô tả (bao gồm cả văn bản và liên kết)
	        String introText = introElement.getText().replace("\n", " ");
	        kol.setDescription(introText);
	    } catch (Exception e) {
	        System.out.println("Có lỗi xảy ra khi xử lý phần mô tả.");
	    }


	    try {
	        // Lấy công ty của user
	        WebElement companyElement = driver.findElement(By.cssSelector(config.getUserJobCategoryCSS()));
	        String companyText = companyElement.getText();
	        //System.out.println("Công ty của user: " + companyText);
	        kol.setProfessionalCategory(companyText);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy công ty của user.");
	    }

	    try {
	        // Lấy vị trí của user
	        WebElement locationElement = driver.findElement(By.cssSelector(config.getLocationElementCSS()));
	        String locationText = locationElement.getText();
	        //System.out.println("Vị trí của user: " + locationText);
	        kol.setLocation(locationText);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy vị trí của user.");
	    }

	    try {
	        // Lấy website của user
	        WebElement websiteElement = driver.findElement(By.cssSelector(config.getUserURLCSS()));
	        String websiteUrl = websiteElement.getAttribute("href");
	        //System.out.println("Website của user: " + websiteUrl);
	        kol.setWebsite(websiteUrl);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy website của user.");
	    }

	    try {
	        // Lấy ngày tham gia của user
	        WebElement joinDateElement = driver.findElement(By.cssSelector(config.getUserJoinDate()));
	        String joinDate = joinDateElement.getText();
	        //System.out.println("Ngày tham gia của user: " + joinDate);
	        kol.setJoinDate(joinDate);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy ngày tham gia của user.");
	    }

	    try {
	        // Lấy số lượng following của user
	        WebElement followingCountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getFollowingCountElementCSS())));
	        String followingCountText  = followingCountElement.getText();
	        int followingCount = TextToIntegerConverter.convertTextToInteger(followingCountText);
	        kol.setFollowingCount(followingCount);
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy số lượng following của user.");
	        
	    }

	    try {
	        // Lấy số lượng followers của user
	    	WebElement followersCountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getFollowersCountElementCSS())));
	        String followersCountText = followersCountElement.getText();
	        int followersCount = TextToIntegerConverter.convertTextToInteger(followersCountText);
	        kol.setFollowersCount(followersCount);
	        if(followersCount >= 5000) {
	        	
	        	FileHandler.writeStringToFile(outputFilepath, kol.getUrl());
	        	System.out.println("Đã ghi link: "+ kol.getUrl() + "vào " + outputFilepath);
	        	
	        }else {
	        	System.out.println("Số lượng following không đủ. "+ followersCount + " Dừng xử lý user này: " + kol.getUrl());
	        	return;
	
	        }
	    } catch (Exception e) {
	        System.out.println("Không tìm thấy số lượng followers của user.");
	    }
	    kol.setKolType();
	    if (!kol.getKolType().equals("Non-KOL")) {
	    remoteManager.addUserToDataBase(kol);
	    remoteManager.saveToDatabase();
	    }
	    else {
	    	System.out.println("Không thực hiện cho user vào DataBase : " + kol.getUrl() );
	    }
	}



	public void fetchProfileFromKOLFile(String filepath, String outputFilepath, WebDriver driver, DataManagerInterface remoteManager) {
		// TODO Auto-generated method stub
        System.out.println("Đọc các liên kết từ file: " + filepath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("https")) { // Kiểm tra nếu là đường dẫn hợp lệ
                    System.out.println("Xử lý URL: " + line);
                    User user = new User(line); // Tạo đối tượng User từ URL
                    if(this.localManager.hasUser(user.getId())==true) {
                    	user = this.localManager.getUserById(user.getId());
                    	if(user.getTweetCount() == null || user.getTweetCount().isEmpty()) {
                    		// Gọi fetchProfile để xử lý User
                        	fetchProfile(driver, user, remoteManager, outputFilepath); 
                        }else {
                        	System.out.println("Thông tin cơ bản về User "+ user.getId() + " đã có trong local database");
                        }
                    }else {
                    	// Gọi fetchProfile để xử lý User
                    	System.out.println("Chưa có thông tin gì về User " + user.getId());
                    	fetchProfile(driver, user, remoteManager, outputFilepath); 
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
	public void fetchProfileMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		String filepath = config.getUsersFoundFilePath();
		String outputFilepath = config.getKolFilePath();
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
                        fetchProfileFromKOLFile(subFilePath, outputFilepath, driver, remoteManager);
                        DataSyncManager.syncFromRemote(this.localManager, remoteManager);
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
	public void fetchFollowersMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fetchTweetsMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}




}
