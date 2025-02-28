package entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User extends Node {

    private String url;
    private String location;
    private String professionalCategory;
    private String joinDate;
    private String website;
    private String tweetCount;
    private int followingCount;
    private int followersCount;
    private String description;
    private String kolType;

    private Set<String> followers;
    private Set<String> following;
    private Set<Tweet> tweets;

    // Constructor mặc định
    public User() {
        super();
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.tweets = new HashSet<>();
    }
    
    // Constructor với id và url
    public User(String id, String url) {
        super(id);
        this.url = url;
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.tweets = new HashSet<>();
    }
    
    // Constructor với url
    public User(String url) {
        this(generateId(url), url);
    }
    
    // Tạo ID từ URL
    private static String generateId(String url) {
        return "user_" + url.substring(url.lastIndexOf("/") + 1); // Tạo id từ phần cuối URL
    }

    // Getter và Setter cho URL
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Getter và Setter cho Location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter và Setter cho Professional Category
    public String getProfessionalCategory() {
        return professionalCategory;
    }

    public void setProfessionalCategory(String professionalCategory) {
        this.professionalCategory = professionalCategory;
    }

    // Getter và Setter cho Join Date
    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    // Getter và Setter cho Website
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    // Getter và Setter cho Tweet Count
    public String getTweetCount() {
        return tweetCount;
    }

    public void setTweetCount(String tweetCount) {
        this.tweetCount = tweetCount;
    }

    // Getter và Setter cho Following Count
    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    // Getter và Setter cho Followers Count
    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    // Getter và Setter cho Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter và Setter cho Followers
    public Set<String> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<String> followers) {
        this.followers = followers;
    }

    // Getter và Setter cho Following
    public Set<String> getFollowing() {
        return following;
    }

    public void setFollowing(Set<String> following) {
        this.following = following;
    }

    // Getter và Setter cho Tweets
    public Set<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(Set<Tweet> tweets) {
        this.tweets = tweets;
    }

    // Phương thức thêm follower
    public void addFollower(String followerId) {
        followers.add(followerId);
    }
    
    public boolean hasFollower(String followerId) {
        return followers.contains(followerId);
    }
    
    public void addTweet(Tweet tweet) {
        tweets.add(tweet);
    }
    
    public boolean hasTweet(Tweet tweet) {
        return tweets.contains(tweet);
    }

    // Phương thức thêm following
    public void addFollowing(String followeeId) {
        following.add(followeeId);
    }

    // Phương thức kiểm tra xem người dùng có theo dõi người khác không
    public boolean isFollowing(User user) {
        return following.contains(user.getId());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(getId(), user.getId()); // So sánh theo ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId()); // Tạo hash từ ID
    }
    
    @Override
    public String toString() {
        return "User{" +
               "id='" + getId() + '\'' +
               ", url='" + url + '\'' +
               ", location='" + location + '\'' +
               ", professionalCategory='" + professionalCategory + '\'' +
               ", joinDate='" + joinDate + '\'' +
               ", website='" + website + '\'' +
               ", tweetCount='" + tweetCount + '\'' +
               ", followingCount='" + followingCount + '\'' +
               ", followersCount='" + followersCount + '\'' +
               ", description='" + description + '\'' +
               ", followers=" + followers.size() + " followers" +
               ", following=" + following.size() + " following" +
               ", tweets=" + tweets.size() + " tweets" +
               '}';
    }

    // Getter và Setter cho Kol Type
    public String getKolType() {
        return kolType;
    }

    public void setKolType() {
        if (this.followersCount >= 1000000) {
            this.kolType = "Mega KOL (Celebrity)";
        } else if (this.followersCount >= 100000) {
            this.kolType = "Macro KOL";
        } else if (this.followersCount >= 10000) {
            this.kolType = "Micro KOL";
        } else if (this.followersCount >= 1000) {
            this.kolType = "Nano KOL";
        } else {
            this.kolType = "Non-KOL";  
        }
    }
}
