package config;

import java.util.ArrayList;
import java.util.List;

import manager.DataManagerInterface;
import manager.TwitterDataManager;

public class TwitterConfig implements ConfigInterface {
	
    private final String baseName = "Twitter";
    private final String baseURL = "https://x.com/";
    private final String loginURL = "https://x.com/i/flow/login";
    private final String exploreURL = "https://x.com/explore";
    
    // XPaths
    private final String peopleTabXpath = "//*[@id='react-root']/div/div/div[2]/main/div/div/div/div[1]/div/div[1]/div[1]/div[2]/nav/div/div[2]/div/div[3]/a/div/div/span";
    private final String retryButtonXpath = "//span[contains(text(),'Retry') and contains(@class, 'css-1jxf684')]";
    private final String loginButtonXpath = "//span[text()='Log in']";
    private final String nextButtonXpath = "//span[text()='Next']";
    private final String timeXpath = "//div[contains(@class, 'css-146c3p1')]//a//time";
    private final String showButtonXpath = "//button[contains(@class, 'css-175oi2r')]//span[normalize-space(text())='Show']";
    private final String showSpamCommentsButtonXpath = "//button[contains(@class, 'css-175oi2r') and contains(., 'Show probable spam')]";
    private final String discoverMoreElementXpath = "//h2[contains(., 'Discover more')]";
    private final String articleXpath = "//article[starts-with(@aria-labelledby, '')]";
    private final String articleBoundOfDiscoverMoreXpath = "//h2[contains(., 'Discover more')]/preceding::article";
    private final String advertisementXpath = ".//*[contains(text(), 'Ad ')]";
    private final String articleInfoXpath = ".//*[@data-testid='Tweet-User-Avatar']";
    
    // CSS Selectors
    private final String postCountCSS = "//div[contains(text(),'posts')]";
    private final String followingCountElementCSS = "a[href$='/following'] span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String followersCountElementCSS = "a[href$='/verified_followers'] span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String searchQuerryCSS = "input[aria-label='Search query']";
    private final String userCellCSS = "button[data-testid='UserCell']";
    private final String linksCSS = "a[href*='/']";
    private final String linksAttributeCSS = "href";
    private final String nameElementCSS = "span[dir='ltr']";
    private final String userDescriptionCSS = "div[data-testid='UserDescription']";
    private final String userJobCategoryCSS = "span[data-testid='UserProfessionalCategory'] button span";
    private final String locationElementCSS = "span[data-testid='UserLocation'] span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String userURLCSS = "a[data-testid='UserUrl']";
    private final String userJoinDate = "span[data-testid='UserJoinDate']";
    private final String tweetsElementCSS = "article[data-testid='tweet']";
    private final String tweetListElementCSS = "a[href*='/status/']";
    private final String tweetElementCSS = "a[href*='/status/']";
    private final String repliersCSS = "article[data-testid='tweet']";
    private final String replyButtonCSS = "button[data-testid='reply']";
    private final String replyCountCSS = "span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String retweetButtonCSS = "button[data-testid='retweet']";
    private final String retweetCountCSS = "span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String likeButtonCSS = "button[data-testid='like']";
    private final String likeCountCSS = "span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String viewCountCSS = "span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3 > div > span > span.css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3";
    private final String tweetTextCSS = "div[data-testid='tweetText']";

    // Attributes
    private final String dateTimeAttribute = "datetime";
    
    // Parametters   
	private int maxUsers = 1000;
	private int maxFollowers = 20;
	private int maxTweets = 30;
	private int maxComments = 30;
	private String graphFilePath =  "graph.gexf"; 
	private DataManagerInterface localManager = new TwitterDataManager("database.json");
	private String hashtagsFilePath = "hashtags.txt";
	private String kolFilePath = "kol_links.txt";
	private String usersFoundFilePath = "all_user_links.txt";
	private String resultFilePath = "result.txt";
	
	public String getSearchQuerryCSS() {
		return searchQuerryCSS;
	}

	public String getUserCellCSS() {
		return userCellCSS;
	}

	public String getLinksCSS() {
		return linksCSS;
	}

	public String getLinksAttributeCSS() {
		return linksAttributeCSS;
	}

	public String getNameElementCSS() {
		return nameElementCSS;
	}

	public String getPostCountCSS() {
		return postCountCSS;
	}

	public String getUserDescriptionCSS() {
		return userDescriptionCSS;
	}

	public String getUserJobCategoryCSS() {
		return userJobCategoryCSS;
	}

	public String getLocationElementCSS() {
		return locationElementCSS;
	}

	public String getUserURLCSS() {
		return userURLCSS;
	}

	public String getUserJoinDate() {
		return userJoinDate;
	}

	public String getFollowingCountElementCSS() {
		return followingCountElementCSS;
	}

	public String getFollowersCountElementCSS() {
		return followersCountElementCSS;
	}

	public String getTweetsElementCSS() {
		return tweetsElementCSS;
	}

	public String getTweetListElementCSS() {
		return tweetListElementCSS;
	}

	public String getTweetElementCSS() {
		return tweetElementCSS;
	}

	public String getRepliersCSS() {
		return repliersCSS;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public String getReplyButtonCSS() {
		return replyButtonCSS;
	}

	public String getReplyCountCSS() {
		return replyCountCSS;
	}

	public String getRetweetButtonCSS() {
		return retweetButtonCSS;
	}

	public String getRetweetCountCSS() {
		return retweetCountCSS;
	}

	public String getLikeButtonCSS() {
		return likeButtonCSS;
	}

	public String getLikeCountCSS() {
		return likeCountCSS;
	}

	public String getViewCountCSS() {
		return viewCountCSS;
	}

	public String getTweetTextCSS() {
		return tweetTextCSS;
	}

	public String getTimeXpath() {
		return timeXpath;
	}

	public String getDateTimeAttribute() {
		return dateTimeAttribute;
	}

	public String getShowButtonXpath() {
		return showButtonXpath;
	}

	public String getShowSpamCommentsButtonXpath() {
		return showSpamCommentsButtonXpath;
	}

	public String getDiscoverMoreElementXpath() {
		return discoverMoreElementXpath;
	}

	public String getArticleXpath() {
		return articleXpath;
	}

	public String getArticleBoundOfDiscoverMoreXpath() {
		return articleBoundOfDiscoverMoreXpath;
	}

	public String getAdvertisementXpath() {
		return advertisementXpath;
	}

	public String getArticleInfoXpath() {
		return articleInfoXpath;
	}

	public String getLoginURL() {
		return loginURL;
	}

	public String getExploreURL() {
		return exploreURL;
	}


	public String getHashtagsFilePath() {
		return hashtagsFilePath;
	}
	
	@Override
	public String getBaseName() {
		// TODO Auto-generated method stub
		return this.baseName;
	}

	@Override
	public String getLoginUrl() {
		// TODO Auto-generated method stub
		return this.loginURL;
	}

	@Override
	public String getExploreUrl() {
		// TODO Auto-generated method stub
		return this.exploreURL;
	}

	@Override
	public String getPeopleTabXpath() {
		// TODO Auto-generated method stub
		return this.peopleTabXpath;
	}

	@Override
	public String getRetryButtonXpath() {
		// TODO Auto-generated method stub
		return this.retryButtonXpath;
	}

	@Override
	public String getLoginButtonXpath() {
		// TODO Auto-generated method stub
		return this.loginButtonXpath;
	}

	@Override
	public String getNextButtonXpath() {
		// TODO Auto-generated method stub
		return this.nextButtonXpath;
	}

	@Override
	public List<String> getPathToFollowers(String url) {
		List<String> paths = new ArrayList<String>();
		String firstPath = url + "/verified_followers";
		String secondPath = url + "/followers";
		paths.add(firstPath);
		paths.add(secondPath);
		return paths;
	}

	@Override
	public List<String> getPathToFollowees(String url) {
		List<String> paths = new ArrayList<String>();
		String firstPath = url + "/following";
		paths.add(firstPath);
		return paths;
	}

	@Override
	public DataManagerInterface newManager(String databasefilepath) {
		// TODO Auto-generated method stub
		return new TwitterDataManager(databasefilepath);
	}

	@Override
	public String getUsersFoundFilePath() {
		// TODO Auto-generated method stub
		return this.usersFoundFilePath;
	}

	@Override
	public int getMaxUsers() {
		// TODO Auto-generated method stub
		return this.maxUsers;
	}

	@Override
	public int getMaxFollowers() {
		// TODO Auto-generated method stub
		return this.maxFollowers;
	}

	@Override
	public int getMaxTweets() {
		// TODO Auto-generated method stub
		return this.maxTweets;
	}

	@Override
	public int getMaxComments() {
		// TODO Auto-generated method stub
		return this.maxComments;
	}

	public void setMaxFollowers(int maxFollowers) {
		this.maxFollowers = maxFollowers;
	}

	public void setMaxTweets(int maxTweets) {
		this.maxTweets = maxTweets;
	}

	public void setMaxComments(int maxComments) {
		this.maxComments = maxComments;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	@Override
	public String getGraphFilePath() {
		// TODO Auto-generated method stub
		return this.graphFilePath;
	}

	@Override
	public DataManagerInterface getLocalManager() {
		// TODO Auto-generated method stub
		return this.localManager;
	}

	public void setGraphFilePath(String graphFilePath) {
		this.graphFilePath = graphFilePath;
	}

	public void setLocalManager(DataManagerInterface localManager) {
		this.localManager = localManager;
	}

	@Override
	public String getHashTagsFilePath() {
		// TODO Auto-generated method stub
		return hashtagsFilePath;
	}

	public void setHashtagsFilePath(String hashtagsFilePath) {
		this.hashtagsFilePath = hashtagsFilePath;
	}

	public String getKolFilePath() {
		return kolFilePath;
	}

	public void setKolFilePath(String kolFilePath) {
		this.kolFilePath = kolFilePath;
	}

	public void setUsersFoundFilePath(String usersFoundFilePath) {
		this.usersFoundFilePath = usersFoundFilePath;
	}

	public String getResultFilePath() {
		return resultFilePath;
	}

	public void setResultFilePath(String resultFilePath) {
		this.resultFilePath = resultFilePath;
	}

}
