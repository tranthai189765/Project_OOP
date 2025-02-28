package config;

import java.util.List;
import manager.DataManagerInterface;

public interface ConfigInterface {
    
    // General URL and Paths
    String getBaseName();
    String getBaseURL();
    String getLoginUrl();
    String getExploreUrl();
    String getHashTagsFilePath();
    String getKolFilePath();
    String getUsersFoundFilePath();
    String getGraphFilePath();
    String getResultFilePath();

    // User Information Retrieval
    List<String> getPathToFollowers(String url);
    List<String> getPathToFollowees(String url);
    String getUserURLCSS();
    String getUserJoinDate();
    String getUserDescriptionCSS();
    String getUserJobCategoryCSS();
    String getLocationElementCSS();
    String getPostCountCSS();
    String getFollowingCountElementCSS();
    String getFollowersCountElementCSS();
    String getNameElementCSS();

    // Tweet and Interaction Elements
    String getTweetsElementCSS();
    String getTweetListElementCSS();
    String getTweetElementCSS();
    String getRepliersCSS();
    String getTweetTextCSS();
    String getTimeXpath();
    String getDateTimeAttribute();
    String getReplyButtonCSS();
    String getReplyCountCSS();
    String getRetweetButtonCSS();
    String getRetweetCountCSS();
    String getLikeButtonCSS();
    String getLikeCountCSS();
    String getViewCountCSS();
    
    // UI Elements and XPaths for Navigation
    String getPeopleTabXpath();
    String getRetryButtonXpath();
    String getLoginButtonXpath();
    String getNextButtonXpath();
    String getSearchQuerryCSS();
    String getUserCellCSS();
    String getLinksCSS();
    String getLinksAttributeCSS();
    
    // Actions for Configurations and Settings
    DataManagerInterface newManager(String databasefilepath);
    DataManagerInterface getLocalManager();
    
    // Methods to Set Limits
    int getMaxUsers();
    int getMaxFollowers();
    int getMaxTweets();
    int getMaxComments();
    void setMaxUsers(int maxUsers);
    void setMaxFollowers(int maxFollowers);
    void setMaxTweets(int maxTweets);
    void setMaxComments(int maxComments);
    
    // File and Path Configurations
    void setResultFilePath(String resultFilePath);

    // Additional Methods for Handling Specific Cases
    String getShowButtonXpath();
    String getShowSpamCommentsButtonXpath();
    String getDiscoverMoreElementXpath();
    String getArticleXpath();
    String getArticleBoundOfDiscoverMoreXpath();
    String getAdvertisementXpath();
    String getArticleInfoXpath();
}

