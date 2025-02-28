package algorithms;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import config.ConfigInterface;
import converter.TextToIntegerConverter;
import entities.Tweet;
import entities.User;
import graph.GraphManager;
import manager.DataManagerInterface;

public class TwitterPageRankAlgorithm {
	private final Graph graph;
    private final GraphModel graphModel;
    private final double[] outEdgeWeights;
    private final boolean[] deadend;
    private final DataManagerInterface manager;
    private final ConfigInterface config;

    public TwitterPageRankAlgorithm(GraphManager graphManager, ConfigInterface config) {
        this.graph = graphManager.getGraph();
        this.graphModel = graphManager.getGraphModel();
        int nodeCount = graph.getNodeCount();
        this.outEdgeWeights = new double[nodeCount];
        this.deadend = new boolean[nodeCount];
        this.manager = config.getLocalManager();
        this.config = config;
    }

    // Gán chỉ số (index) cho các node trong đồ thị
    public void assignIndexToNodes() {
        Table nodeTable = graphModel.getNodeTable();
        if (nodeTable.getColumn("index") == null) {
            nodeTable.addColumn("index", Integer.class);
        }
        int index = 0;
        for (Node node : graph.getNodes()) {
            node.setAttribute("index", index++);
        }
    }

    // Thêm trọng số cho tất cả các cạnh
    public void addWeightToAllEdges() {
        for (Edge edge : graph.getEdges()) {
            switch (edge.getLabel()) {
                case "retweet":
                    edge.setWeight(1.0);
                    break;
                case "follow":
                    edge.setWeight(0.8);
                    break;
                case "comment":
                case "posted":
                    edge.setWeight(0.6);
                    break;
                default:
                    break;
            }
        }
    }

    // Tính tổng trọng số các cạnh đi ra (outEdges) cho từng node
    public void computeOutEdgeWeights() {
        for (Node node : graph.getNodes()) {
            int index = (int) node.getAttribute("index");
            double totalWeight = 0.0;
            for (Edge edge : graph.getEdges(node)) {
                if (edge.getSource().equals(node)) {
                    totalWeight += edge.getWeight();
                }
            }
            outEdgeWeights[index] = totalWeight;
            deadend[index] = totalWeight == 0;
        }
    }

    // Thêm trọng số cho tất cả các node
    public void addWeightToAllNodes() {
        if (graphModel.getNodeTable().getColumn("weight") == null) {
            graphModel.getNodeTable().addColumn("weight", Double.class);
        }
        manager.loadFromDatabase();
        double totalWeight = 0.0;

        for (Node node : graph.getNodes()) {
            String nodeLabel = node.getLabel();
            double weight = 0.0;

            if (nodeLabel.startsWith("user_")) {
                User user = manager.getUserById(nodeLabel);
                weight = user != null ? Math.log(Math.max(user.getFollowersCount(), 1)) : 1.0;

                if (user != null && !user.getTweets().isEmpty()) {
                    for (Tweet tweet : user.getTweets()) {
                        Node tweetNode = graph.getNode(tweet.getId());
                        if (tweetNode != null) {
                            int numLikes = TextToIntegerConverter.convertTextToInteger(tweet.getLikeCount());
                            int numReposts = TextToIntegerConverter.convertTextToInteger(tweet.getRepostCount());
                            tweetNode.setAttribute("weight", Math.log(Math.max(numLikes + numReposts, 1)));
                        }
                    }
                }
            }
            node.setAttribute("weight", weight);
            totalWeight += weight;
        }

        // Chuẩn hóa trọng số các node
        for (Node node : graph.getNodes()) {
            double currentWeight = (double) node.getAttribute("weight");
            node.setAttribute("weight", currentWeight / totalWeight);
        }
    }

    // Thực hiện thuật toán PageRank
    public void run(int maxIterations) {
        assignIndexToNodes();
        addWeightToAllEdges();
        addWeightToAllNodes();
        computeOutEdgeWeights();

        double[] pageRank = new double[graph.getNodeCount()];
        for (Node node : graph.getNodes()) {
            pageRank[(int) node.getAttribute("index")] = (double) node.getAttribute("weight");
        }

        for (int i = 0; i < maxIterations; i++) {
            double[] newPageRank = new double[graph.getNodeCount()];

            for (Node node : graph.getNodes()) {
                int index = (int) node.getAttribute("index");
                double newValue = 0.0;

                for (Edge edge : graph.getEdges(node)) {
                    Node source = edge.getSource();
                    if (!source.equals(node)) {
                        int sourceIndex = (int) source.getAttribute("index");
                        newValue += pageRank[sourceIndex] * edge.getWeight() / outEdgeWeights[sourceIndex];
                    }
                }
                newPageRank[index] = newValue + (deadend[index] ? pageRank[index] : 0.0);
            }

            pageRank = newPageRank;

            // Debug: In nội dung PageRank sau mỗi vòng lặp
            System.out.println("PageRank after iteration " + (i + 1) + ":");
            for (Node node : graph.getNodes()) {
                int index = (int) node.getAttribute("index");
                System.out.println("Node ID: " + node.getId() + ", Score: " + pageRank[index]);
            }
        }

        // Gán điểm PageRank vào node và tính rank
        Table nodeTable = graphModel.getNodeTable();
        if (nodeTable.getColumn("score") == null) {
            nodeTable.addColumn("score", Double.class); // Khai báo cột 'score' là kiểu Double
        }
        if (nodeTable.getColumn("rank") == null) {
            nodeTable.addColumn("rank", Integer.class); // Khai báo cột 'rank' là kiểu Integer
        }

        // Lấy các điểm số (scores) từ các node
        manager.loadFromDatabase();
        List<NodeScore> nodeScores = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            String nodeId = (String) node.getId();
            if (manager.hasUser(nodeId)) { // Kiểm tra nếu node tồn tại trong database
                double score = pageRank[(int) node.getAttribute("index")]; // Lấy score từ mảng pageRank
                node.setAttribute("score", score); // Gán score vào node
                nodeScores.add(new NodeScore(node, score));
            }
        }

        // Sắp xếp các node theo điểm số giảm dần
        nodeScores.sort((n1, n2) -> Double.compare(n2.score, n1.score));

        // Gán rank cho mỗi node
        for (int rank = 0; rank < nodeScores.size(); rank++) {
            Node node = nodeScores.get(rank).node;
            node.setAttribute("rank", rank + 1); // Rank bắt đầu từ 1
        }

        // In ra rank và score của các node
        for (Node node : graph.getNodes()) {
            if (manager.hasUser(node.getId().toString())) { // Kiểm tra nếu node có trong database
                System.out.println("Node ID: " + node.getId() + ", Rank: " + node.getAttribute("rank")
                        + ", Score: " + (double) node.getAttribute("score") * 100);
            }
        }

        // Ghi kết quả ra file
        writeResultsToFile(nodeScores, config.getResultFilePath());
    }

    private void writeResultsToFile(List<NodeScore> nodeScores, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Ghi tiêu đề với định dạng cột
            String header = String.format("%-70s %-10s %-20s%n", "NodeID", "Rank", "Score");
            writer.write(header);

            // Ghi dữ liệu từng dòng
            for (NodeScore nodeScore : nodeScores) {
                Node node = nodeScore.node;
                int rank = (int) node.getAttribute("rank");
                double score = (double) node.getAttribute("score");

                // Định dạng mỗi dòng với độ rộng cố định
                String row = String.format("%-70s %-10d %-20.12f%n", node.getId(), rank, score);
                writer.write(row);
            }

            System.out.println("Kết quả đã được ghi vào file: " + fileName);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    // Lớp phụ trợ để lưu trữ node và điểm số của nó
    private static class NodeScore {
        Node node;
        double score;

        NodeScore(Node node, double score) {
            this.node = node;
            this.score = score;
        }
    }
}