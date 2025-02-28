package entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tweet extends Node {
    @JsonProperty("author_id")
    private String authorId;
    private Set<String> commentedBy;
    private String url;
    private String likeCount;
    private String viewCount;
    private String content;
    private String postedDate;
    private String repostCount;
    private String commentCount;

    // Default Constructor
    public Tweet() {
        super();
        this.commentedBy = new HashSet<>();
        this.authorId = null;
    }

    // Constructor with ID and authorId
    public Tweet(String id, String authorId) {
        super(id);
        this.authorId = authorId;
        this.commentedBy = new HashSet<>();
    }

    // Constructor using URL
    public Tweet(String url) {
        this(generateId(url), extractAuthorId(url)); // Call constructor with generated ID
        this.url = url;
    }

    // Generate ID for the tweet from the URL
    private static String generateId(String linkUrl) {
        String username = linkUrl.substring(
            linkUrl.indexOf("https://x.com/") + "https://x.com/".length(),
            linkUrl.lastIndexOf("/status")
        );
        String tweetId = linkUrl.substring(linkUrl.lastIndexOf("/") + 1);
        return "tweet_" + username + "_" + tweetId;
    }

    // Extract authorId from the URL
    private static String extractAuthorId(String linkUrl) {
        if (linkUrl != null && linkUrl.contains("https://x.com/")) {
            return "user_" + linkUrl.substring(
                linkUrl.indexOf("https://x.com/") + "https://x.com/".length(),
                linkUrl.lastIndexOf("/status")
            );
        }
        return null;
    }

    // Getters and Setters
    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Set<String> getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(Set<String> commentedBy) {
        this.commentedBy = commentedBy;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(String repostCount) {
        this.repostCount = repostCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Add a userId to the commentedBy list
    public void addCommentedUser(String userId) {
        commentedBy.add(userId);
    }

    // Check if a userId exists in the commentedBy list
    public boolean hasCommented(String userId) {
        return commentedBy != null && commentedBy.contains(userId);
    }

    // Convert object to string representation
    @Override
    public String toString() {
        return "Tweet{" +
               "id='" + getId() + '\'' +
               ", authorId='" + authorId + '\'' +
               ", likeCount='" + likeCount + '\'' +
               ", viewCount='" + viewCount + '\'' +
               ", content='" + content + '\'' +
               ", postedDate='" + postedDate + '\'' +
               ", repostCount='" + repostCount + '\'' +
               ", commentCount='" + commentCount + '\'' +
               ", commentedBy=" + commentedBy.size() + " commented" +
               '}';
    }
}
