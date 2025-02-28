package scraper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.ConfigInterface;
import converter.TextToIntegerConverter;

import entities.User;
import entities.Tweet;

import manager.DataManagerInterface;
import filehandler.FileHandler;

public class KOLTweetFetcher implements DataFetcherStrategy {
	private final DataManagerInterface localManager;
	private final int maxTweets;
	private final int maxComments;
	private final ConfigInterface config;

    public KOLTweetFetcher(ConfigInterface config) {
        this.config = config;
        this.localManager = config.getLocalManager();
        this.maxTweets = config.getMaxTweets();
        this.maxComments = config.getMaxComments();
    }

    public void fetchTweets(WebDriver driver, User kol, DataManagerInterface remoteManager) {
        System.out.println("Fetching KOL profile...");
        remoteManager.addUserToDataBase(kol);
        try {
            driver.get(kol.getUrl());
            Thread.sleep(6000); // Đợi tải trang
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 600)");

            int count = 0;
            int stagnantScrollCount = 0;
            int previousTweetCount = 0;
            Set<String> tweetLinks = new HashSet<>();

            while (tweetLinks.size() < maxTweets) {
                try {
                    List<WebElement> tweets = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(config.getTweetsElementCSS())));

                    if (tweets.isEmpty()) {
                        System.out.println("Không có dữ liệu mới trong 15 giây. Đợi 30s để tải thêm tweets...");
                        Thread.sleep(6000); // Đợi thêm để tải tweet mới

                        driver.navigate().refresh();
                    }

                    for (WebElement tweet : tweets) {
                        if (count >= maxTweets) break;

                        try {
                            List<WebElement> links = tweet.findElements(By.cssSelector(config.getTweetElementCSS()));
                            if (!links.isEmpty()) {
                                String tweetUrl = links.get(0).getAttribute(config.getLinksAttributeCSS());
                                if(!tweetLinks.contains(tweetUrl)) {
                                	tweetLinks.add(tweetUrl);
                                	System.out.println("Đã ghi nhận: "+tweetUrl);
                                }
                                System.out.println("COUNT = " + tweetLinks.size());
                            }
                        } catch (Exception e) {
                            System.out.println("Không thể lấy tweets của người dùng này: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    if(tweetLinks.size() == previousTweetCount) {
                    	stagnantScrollCount++;
                    	System.out.println("Cuộn check lần thứ: "+stagnantScrollCount);
                    	if (stagnantScrollCount >= 50) {
                    		System.out.println("Hết dữ liệu");
                    		break;
                    	}
                    }else {
                    	stagnantScrollCount = 0;
                    }
                    
                    previousTweetCount = tweetLinks.size();

                    // Cuộn trang tiếp để tải thêm tweet
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    jsExecutor.executeScript("window.scrollBy(0, 800)");
                    Thread.sleep(6000);
                } catch (Exception e) {
                    System.out.println("Lỗi trong vòng lặp xử lý tweets: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
            for(String tweetLink : tweetLinks) {
            	System.out.println("Đang xử lý cho: " + tweetLink);
            	Tweet tweet = new Tweet(tweetLink);
            	if (!kol.hasTweet(tweet)) {
            		kol.addTweet(tweet);
            	}
            }
            proceedTweets(driver, remoteManager, kol);
            remoteManager.updatePostsForUser(kol.getId(), kol.getTweets());
            remoteManager.saveToDatabase();
        } catch (Exception e) {
            System.out.println("Lỗi trong phương thức fetchTweet: " + e.getMessage());
            e.printStackTrace();
        }
    }
	
	private void scrollUntilGetRepliers(WebDriver driver) {
		while (true) {
            try {
            	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                // Kiểm tra xem phần tử có hiển thị trên trang hay không
                WebElement repliers = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getRepliersCSS())));

                // Nếu tất cả các phần tử đã hiển thị, dừng cuộn
                if (repliers.isDisplayed()) {
                    break;
                }
            } catch (Exception e) {
                // Nếu các phần tử chưa xuất hiện, cuộn trang
            	((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 100)");
                try {
                    Thread.sleep(10000); // Đợi thêm thời gian cho trang tải các phần tử mới
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
	}
	
	private void proceedTweets(WebDriver driver, DataManagerInterface remoteManager, User kol) {
		Set<Tweet> tweets = kol.getTweets();
		int countRespawn = 1;
		for (Tweet tweet : tweets) {
			System.out.println("Đang xử lý tweet cho user: " + kol.getId());
			driver.get(tweet.getUrl());
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (countRespawn % 20 == 0) {
				driver.navigate().refresh();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				countRespawn = 1;
			}
			countRespawn++;
			System.out.println("CountRespawn: " + countRespawn);
			extractInfo(driver, tweet);
			int numComments = TextToIntegerConverter.convertTextToInteger(tweet.getCommentCount());
        	if (numComments == 0) {
        		continue;
        	}
			scrollUntilGetRepliers(driver);
			Set<String> repliers = replierURL(driver, kol, remoteManager, tweet.getUrl(), maxComments, numComments);
        	if (repliers.isEmpty()) {
        		System.out.println("Không hiển thị người trả lời");
        		tweet.addCommentedUser("Không hiển thị");
        	} else {
        		for (String replier : repliers) {
                 	if (tweet.hasCommented(replier.substring(replier.indexOf(config.getBaseURL()) + config.getBaseURL().length())) == false) {
                 		tweet.addCommentedUser("user_"+ replier.substring(replier.indexOf(config.getBaseURL()) + config.getBaseURL().length()));
                 		System.out.println("Đã thêm người comment có URL: " + replier);
                 	}
                }
        	}
        	try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String returnReply(WebDriver driver, Tweet tweet) {
		String replyCountText = "0"; // Mặc định là 0 nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        	WebElement replyButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getReplyButtonCSS())));
        	WebElement replyCountElement = replyButton.findElement(By.cssSelector(config.getReplyCountCSS()));
            replyCountText = replyCountElement.getText(); // Số lượng trả lời
            System.out.println("Số lượng trả lời: " + replyCountText);
        } catch (Exception e) {
            System.out.println("Không tìm thấy số lượng trả lời cho tweet: ");
            replyCountText = "0";
        }
        return replyCountText;
	}
	
	private String returnRepost(WebDriver driver, Tweet tweet) {
		// Lấy số lượng retweet
        String retweetCountText = "0"; // Mặc định là 0 nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        	WebElement retweetButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getRetweetButtonCSS())));
        	WebElement repostCountElement = retweetButton.findElement(By.cssSelector(config.getRetweetCountCSS()));
            retweetCountText = repostCountElement.getText(); // Số lượng retweet
            System.out.println("Số lượng retweet: " + retweetCountText);
        } catch (Exception e) {
            System.out.println("Không tìm thấy số lượng retweet cho tweet: ");
            retweetCountText = "0";
        }
        return retweetCountText;
	}
	
	private String returnLike(WebDriver driver, Tweet tweet) {
		// Lấy số lượng like
        String likeCountText = "0"; // Mặc định là 0 nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        	WebElement likeButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getLikeButtonCSS())));
        	WebElement likeCountElement = likeButton.findElement(By.cssSelector(config.getLikeCountCSS()));
            likeCountText = likeCountElement.getText(); // Số lượng like
            System.out.println("Số lượng like: " + likeCountText);
        } catch (Exception e) {
            System.out.println("Không tìm thấy số lượng like cho tweet: ");
            likeCountText = "0";
        }
		return likeCountText;
	}
	
	private String returnView(WebDriver driver, Tweet tweet) {
		// Lấy số lượt view
        String viewCountText = "0"; // Mặc định là 0 nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        	WebElement viewCountElement = wait.until(ExpectedConditions.presenceOfElementLocated(
        			By.cssSelector(config.getViewCountCSS())));
            viewCountText = viewCountElement.getText(); // Lấy số lượt view
            System.out.println("Số lượt view: " + viewCountText);
        } catch (Exception e) {
            System.out.println("Không tìm thấy số lượt view cho tweet: ");
            viewCountText = "0";
        }
        return viewCountText;
	}
	
	private String returnContent(WebDriver driver, Tweet tweet) {
		// Lấy nội dung tweet
        String content = ""; // Mặc định là chuỗi rỗng nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        	WebElement tweetElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(config.getTweetTextCSS())));
        	if (!tweetElement.isDisplayed()) {
        		driver.navigate().refresh();
        		tweetElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(config.getTweetTextCSS())));
        	}
        	content = tweetElement.getText();
            System.out.println("Nội dung tweet: " + content); 
        } catch (Exception e) {
            System.out.println("Không tìm thấy nội dung tweet cho tweet: ");
            content = "Tweet không có nội dung";
        }
        return content;
	}
	
	private String returnTime(WebDriver driver, Tweet tweet) {
		// Lấy ngày đăng tweet
        String tweetDate = ""; // Mặc định là chuỗi rỗng nếu không tìm thấy
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement timeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(config.getTimeXpath())));
            tweetDate = timeElement.getAttribute(config.getDateTimeAttribute()); // Lấy giá trị thuộc tính datetime
            System.out.println("Ngày đăng tweet: " + tweetDate);
        } catch (Exception e) {
            System.out.println("Không tìm thấy ngày đăng tweet cho tweet: ");
            tweetDate = "Error";
        } 
        return tweetDate;
	}
	
	
    private void scrollUntilElementsVisible(WebDriver driver) {
        while (true) {
            try {
            	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                // Kiểm tra xem phần tử có hiển thị trên trang hay không
                WebElement replyElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(config.getReplyButtonCSS())));

                // Nếu tất cả các phần tử đã hiển thị, dừng cuộn
                if (replyElement.isDisplayed()) {
                    break;
                }
            } catch (Exception e) {
                // Nếu các phần tử chưa xuất hiện, cuộn trang
            	((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 100)");
                try {
                    Thread.sleep(2000); // Đợi thêm thời gian cho trang tải các phần tử mới
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
	
	public void extractInfo(WebDriver driver, Tweet tweet) {
		driver.get(tweet.getUrl());
		List<WebElement> retryElements = driver.findElements(By.xpath(config.getRetryButtonXpath()));
    	
    	if (!retryElements.isEmpty()) {
            try {
                driver.navigate().refresh();
                System.out.println("Đã nhấn nút Retry để tải nội dung.");
            } catch (Exception e) {
                System.out.println("Lỗi khi nhấn nút Retry: " + e.getMessage());
            }
        }
    	
		tweet.setContent(returnContent(driver,tweet));
		scrollUntilElementsVisible(driver);
		tweet.setCommentCount(returnReply(driver,tweet));
		tweet.setLikeCount(returnLike(driver,tweet));
		tweet.setRepostCount(returnRepost(driver, tweet));
		tweet.setViewCount(returnView(driver, tweet));
		tweet.setPostedDate(returnTime(driver, tweet));
	}
	
	public Set<String> replierURL(WebDriver driver, User kol, DataManagerInterface remoteManager, String tweetUrl, int maxComments, int numComments) {
	    Set<String> replierLinks = new HashSet<>();
	    int needComments = Math.min(maxComments, numComments); // Số comment cần lấy
	    int count = 0;
	    int stagnantScrollCount = 0; // Đếm số lần cuộn không tìm thấy phần tử mới
	    int previousTweetCount = 0; // Số lượng bài viết trước lần cuộn cuối
	    boolean isFirstElementSkipped = false; // Đánh dấu nếu đã bỏ qua phần tử đầu tiên
	    WebElement firstUserElement = null; // Lưu phần tử đầu tiên
	    int numSeenDicoverMore = 0;

	    try {
	    
	        while (count < needComments) {
	            // Kiểm tra và nhấn nút "Show more" nếu có
	            // Cuộn xuống để tải thêm nội dung
	            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 800)");
	            Thread.sleep(2000);
	      
	            try { 
	                // Thay đổi XPath để tìm kiếm văn bản "Show" bên trong <span> thay vì trực tiếp trong <button>
	                List<WebElement> showButtons = driver.findElements(By.xpath(config.getShowButtonXpath()));
	                if (!showButtons.isEmpty()) {
	                    WebElement showButton = showButtons.get(0); // Lấy nút đầu tiên
	                    showButton.click();
	                    System.out.println("Đã nhấn nút 'Show' để tải thêm repliers.");
	                    Thread.sleep(3000); // Chờ nội dung tải xong
	                }
	            } catch (Exception e) {
	                System.out.println("Không thể nhấn nút 'Show': " + e.getMessage());
	            }


	            // Xử lý nút "Show probable spam"
	            try {
	                List<WebElement> showSpamButtons = driver.findElements(By.xpath(config.getShowSpamCommentsButtonXpath()));
	                if (!showSpamButtons.isEmpty()) {
	                    WebElement showSpamButton = showSpamButtons.get(0); // Lấy nút đầu tiên
	                    showSpamButton.click();
	                    System.out.println("Đã nhấn nút 'Show probable spam'.");
	                    Thread.sleep(3000); // Chờ nội dung tải xong
	                }
	            } catch (Exception e) {
	                System.out.println("Không thể nhấn nút 'Show probable spam': " + e.getMessage());
	            }

	            WebElement discoverMoreHeading = null;
	            try {
	                discoverMoreHeading = driver.findElement(By.xpath(config.getDiscoverMoreElementXpath()));
	                System.out.println("Thấy Discover More rồi nhe !!!");
	                numSeenDicoverMore ++;
	            } catch (Exception e) {
	                System.out.println("Không tìm thấy phần tử 'Discover more'. Bỏ qua bước này.");
	            }

	            List<WebElement> articles;
	            if (discoverMoreHeading != null) {
	                articles = driver.findElements(By.xpath(config.getArticleBoundOfDiscoverMoreXpath()));
	                System.out.println("Bug ở load user sau khi thấy Discover More");
	            } else {
	                articles = driver.findElements(By.xpath(config.getArticleXpath()));
	            }

	            if (articles.size() == previousTweetCount) {
	                stagnantScrollCount++;
	                if (stagnantScrollCount >= 10) {
	                    System.out.println("Không tìm thấy thêm người comment, dừng cuộn.");
	                    break;
	                }
	            } else {
	                stagnantScrollCount = 0; // Reset nếu tìm thấy phần tử mới
	            }

	            previousTweetCount = articles.size(); // Cập nhật số lượng bài viết trước

	            // Lấy URL của từng replier
	            System.out.println("Bắt đầu duyệt nhé !!!");
	            for (WebElement article : articles) {
	                if (count >= needComments) {
	                    break;
	                }
	                try {
	                	
	                	boolean isAd = !article.findElements(By.xpath(config.getAdvertisementXpath())).isEmpty();
	                	if (isAd) {
	                	    System.out.println("Bỏ qua quảng cáo.");
	                	    continue;
	                	}
	                    
	                    WebElement avatar = article.findElement(By.xpath(config.getArticleInfoXpath()));
	                    String userProfileUrl = avatar.findElement(By.tagName("a")).getAttribute(config.getLinksAttributeCSS());
	                    

	                    if (!isFirstElementSkipped) {
	                        // Lưu phần tử đầu tiên
	                        firstUserElement = article;
	                        isFirstElementSkipped = true;
	                        System.out.println("Bỏ qua người dùng đầu tiên: " + userProfileUrl);
	                        continue;
	                    }

	                    // Kiểm tra nếu phần tử hiện tại trùng với phần tử đầu tiên
	                    if (article.equals(firstUserElement)) {
	                        System.out.println("Người dùng này trùng với người đầu tiên, bỏ qua.");
	                        continue;
	                    }

	                    if (replierLinks.add(userProfileUrl)) { // Chỉ thêm nếu chưa tồn tại
	                        count++;
	                        System.out.println("Đã thêm người dùng: " + userProfileUrl);
	                    }

	                } catch (NoSuchElementException e) {
	                    System.out.println("Không thể tìm thấy avatar trong bài viết này.");
	                } catch (Exception e) {
	                    System.out.println("Lỗi khi lấy URL người comment: " + e.getMessage());
	                }
	            }
	            if (numSeenDicoverMore==3) {
	                System.out.println("Thấy rồi, dừng cuộn nhé !");
	                break;
	            }
	        }
	    } catch (InterruptedException e) {
	        System.out.println("Lỗi trong quá trình xử lý: " + e.getMessage());
	        Thread.currentThread().interrupt(); // Giữ lại trạng thái gián đoạn
	    } finally {
	        System.out.println("Kết thúc quá trình thu thập danh sách người comment.");
	    }
	    remoteManager.saveToDatabase();
	    return replierLinks;
	}


	public void fetchTweetsFromKOLFile(WebDriver driver, String filepath, DataManagerInterface remoteManager, boolean done) {
		int countUp = 0;
		// TODO Auto-generated method stub
		Set<String> kolLinks = FileHandler.readElementsFromFile(filepath);
		if (kolLinks.isEmpty()) {
    		return;
    	}else {
    		for (String kolLink : kolLinks) {
    			if(countUp == 7) {
    				System.out.println("Đã đủ 7 KOLs, đăng nhập lại.");
    				return;
    			}
    			User processKOL = new User(kolLink);
    			if (!localManager.hasUser(processKOL.getId())) {
    				if(!remoteManager.hasUser(processKOL.getId())) {
    					remoteManager.addUserToDataBase(processKOL);
    				}
    				else {
    					processKOL = remoteManager.getUserById(processKOL.getId());
    				}
 
    			}
    			else {
    				processKOL = localManager.getUserById(processKOL.getId());
    			}
    			Set<Tweet> tweets = processKOL.getTweets();
    			if (tweets.size() == 0) {
    				done = false;
    				fetchTweets(driver, processKOL, remoteManager);
    				countUp++;
    			}else {
    				continue;
    			}
    			try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    			
    		}
    	}
	}
	@Override
	public void fetchTweetsMultiThreads(int threadCount) {
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
                        String remoteManagerPath = threadIndex + "_database.json";
                        DataManagerInterface remoteManager = config.newManager(remoteManagerPath);
          
                        // Đăng nhập
   

						// Đào tweets từ file nhỏ
                        boolean done;
                        do {
                        WebDriver driver = new ChromeDriver();
                        // Đăng nhập
                         loginService.login(driver);
                         done = true;
                        fetchTweetsFromKOLFile(driver, subFilePath, remoteManager, done);
                        driver.quit();
                        }while(done == false);

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
	public void fetchFollowersMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		
	}

}