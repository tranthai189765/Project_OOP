package manager;

import entities.Tweet;
import entities.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataManagerInterface {
    void addUserToDataBase(User user);
    public User getUserById(String userId);
    public boolean hasUser(String userId);
    void updatePostsForUser(String userId, Set<Tweet> tweets);
    void updateFollowersForUser(String userId, Set<String> followers);
    void updateBasicInfoForUser(String userId, User updatedUser);
    void saveToDatabase();
    void loadFromDatabase();
    public Map<String, List<User>> getData();
    public List<String> getUserIds();
	String getDatabasefilepath();
}

