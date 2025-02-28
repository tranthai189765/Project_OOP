package graph;

import manager.DataManagerInterface;
import java.util.List;
import java.util.Map;

import config.ConfigInterface;
import entities.Tweet;
import entities.User;

public class GraphBuilder {

    private final DataManagerInterface dataManager;
    private final GraphManager graphManager;

    public GraphBuilder(GraphManager graphManager, ConfigInterface config) {
        this.dataManager = config.getLocalManager();
        this.graphManager = graphManager;
    }

    public void buildGraph(ConfigInterface config) {
        String filepath = config.getGraphFilePath();

        // Tải dữ liệu từ database
        dataManager.loadFromDatabase();

        // Duyệt qua từng mục dữ liệu và xây dựng đồ thị
        for (Map.Entry<String, List<User>> entry : dataManager.getData().entrySet()) {
            for (User user : entry.getValue()) {
                if (!user.getTweets().isEmpty()) {
                    processUser(user);
                }
            }
        }

        // Lưu đồ thị vào file
        graphManager.saveGraphToFile(filepath);
        System.out.println("Đồ thị đã được xây dựng từ dữ liệu.");
    }

    private void processUser(User user) {
        // Thêm User vào đồ thị
        graphManager.addNode(user);

        // Thêm các Followers của User vào đồ thị
        addFollowersToGraph(user);

        // Thêm các Tweet của User vào đồ thị
        addTweetsToGraph(user);
    }

    private void addFollowersToGraph(User user) {
        for (String followerId : user.getFollowers()) {
            User followerUser = new User();
            followerUser.setId(followerId);
            graphManager.addNode(followerUser);
            graphManager.addEdge(followerUser, user);
        }
    }

    private void addTweetsToGraph(User user) {
        for (Tweet tweet : user.getTweets()) {
            graphManager.addNode(tweet);

            // Kết nối Tweet với tác giả
            User author = new User();
            author.setId(tweet.getAuthorId());
            graphManager.addEdge(tweet, author);

            // Nếu User không phải tác giả của Tweet, kết nối User với Tweet
            if (!user.getId().equals(author.getId())) {
                graphManager.addEdge(user, tweet);
            }

            // Thêm các người bình luận vào Tweet
            addCommentersToGraph(tweet);
        }
    }

    private void addCommentersToGraph(Tweet tweet) {
        for (String commenterId : tweet.getCommentedBy()) {
            User commenter = new User();
            commenter.setId(commenterId);
            graphManager.addEdge(commenter, tweet);
        }
    }
}
