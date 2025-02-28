package graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.gephi.graph.api.*;
import org.gephi.project.api.*;
import org.gephi.io.exporter.api.*;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.openide.util.Lookup;

import entities.User;
import entities.Tweet;

public class GraphManager {
    private GraphModel graphModel;
    private Graph graph;

    public GraphManager() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.getCurrentWorkspace();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        graph = graphModel.getDirectedGraph();
    }

    public void addNode(entities.Node node) {
        addNode(node.getId());
    }

    public void addNode(String id) {
        org.gephi.graph.api.Node graphNode = graph.getNode(id);
        if (graphNode != null) {
            System.out.println("Node với id " + id + " đã tồn tại.");
            return;
        }

        graphNode = graphModel.factory().newNode(id);
        graphNode.setLabel(id);

        if (id.startsWith("user_")) {
            graphNode.setColor(Color.RED);
        } else if (id.startsWith("tweet_")) {
            graphNode.setColor(Color.BLUE);
        } else {
            return;
        }
        graph.addNode(graphNode);
    }

    public void addEdge(entities.Node source, entities.Node sink) {
        graph.writeLock();
        try {
            org.gephi.graph.api.Node sourceNode = ensureNodeExists(source);
            org.gephi.graph.api.Node sinkNode = ensureNodeExists(sink);

            Edge edge = graphModel.factory().newEdge(sourceNode, sinkNode, true);
            setEdgeLabel(edge, source, sink);

            if (isEdgeDuplicate(sourceNode, sinkNode, edge.getLabel())) {
                System.out.println("Cạnh giữa " + source.getId() + " và " + sink.getId() + " với label '" + edge.getLabel() + "' đã tồn tại.");
                return;
            }

            graph.addEdge(edge);
            System.out.println("Cạnh giữa " + source.getId() + " và " + sink.getId() + " với label '" + edge.getLabel() + "' đã được thêm.");
        } finally {
            graph.writeUnlock();
        }
    }

    private org.gephi.graph.api.Node ensureNodeExists(entities.Node node) {
        org.gephi.graph.api.Node graphNode = graph.getNode(node.getId());
        if (graphNode == null) {
            graphNode = graphModel.factory().newNode(node.getId());
            graphNode.setLabel(node.getId());
            graphNode.setColor(node.getId().startsWith("user_") ? Color.RED : Color.BLUE);
            graph.addNode(graphNode);
        }
        return graphNode;
    }

    private void setEdgeLabel(Edge edge, entities.Node source, entities.Node sink) {
        if (source instanceof User && sink instanceof User) {
            edge.setLabel("follow");
        } else if (source instanceof User && sink instanceof Tweet) {
            Tweet tweetSink = (Tweet) sink;
            User userSource = (User) source;
            edge.setLabel(tweetSink.getCommentedBy().contains(userSource.getId()) ? "comment" : "retweet");
        } else if (source instanceof Tweet && sink instanceof User) {
            Tweet tweetSource = (Tweet) source;
            User userSink = (User) sink;
            edge.setLabel(tweetSource.getAuthorId().equals(userSink.getId()) ? "posted" : null);
        }
    }

    private boolean isEdgeDuplicate(org.gephi.graph.api.Node sourceNode, org.gephi.graph.api.Node sinkNode, String label) {
        for (Edge existingEdge : graph.getEdges(sourceNode)) {
            if (existingEdge.getTarget().equals(sinkNode) && label.equals(existingEdge.getLabel())) {
                return true;
            }
        }
        return false;
    }

    public List<org.gephi.graph.api.Node> getInNodes(String nodeId) {
        List<org.gephi.graph.api.Node> inNodes = new ArrayList<>();
        org.gephi.graph.api.Node targetNode = graph.getNode(nodeId);
        if (targetNode != null) {
            for (Edge edge : graph.getEdges()) {
                if (edge.getTarget().equals(targetNode)) {
                    inNodes.add(edge.getSource());
                }
            }
        } else {
            System.out.println("Node với ID " + nodeId + " không tồn tại.");
        }
        return inNodes;
    }

    public List<org.gephi.graph.api.Node> getOutNodes(String nodeId) {
        List<org.gephi.graph.api.Node> outNodes = new ArrayList<>();
        org.gephi.graph.api.Node sourceNode = graph.getNode(nodeId);
        if (sourceNode != null) {
            for (Edge edge : graph.getEdges()) {
                if (edge.getSource().equals(sourceNode)) {
                    outNodes.add(edge.getTarget());
                }
            }
        } else {
            System.out.println("Node với ID " + nodeId + " không tồn tại.");
        }
        return outNodes;
    }

    public List<org.gephi.graph.api.Node> getNodes() {
        List<org.gephi.graph.api.Node> nodes = new ArrayList<>();
        for (org.gephi.graph.api.Node node : graph.getNodes()) {
            nodes.add(node);
        }
        return nodes;
    }

    public List<org.gephi.graph.api.Edge> getEdges() {
        List<org.gephi.graph.api.Edge> edges = new ArrayList<>();
        for (org.gephi.graph.api.Edge edge : graph.getEdges()) {
            edges.add(edge);
        }
        return edges;
    }

    public void loadGraphFromFile(String filepath) {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        try {
            org.gephi.io.importer.api.Container container = importController.importFile(new java.io.File(filepath));
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();
            importController.process(container, new DefaultProcessor(), workspace);
            graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
            graph = graphModel.getDirectedGraph();
            System.out.println("Đã tải đồ thị từ file " + filepath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tải đồ thị từ file: " + filepath);
        }
    }

    public void saveGraphToFile(String filepath) {
        ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        try {
            Exporter exporter = exportController.getExporter("gexf");
            exportController.exportFile(new java.io.File(filepath), exporter);
            System.out.println("Đã xuất đồ thị ra file " + filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Graph getGraph() {
        if (graph == null) {
            System.out.println("Lỗi: Đồ thị không được khởi tạo.");
        }
        return graph;
    }
	public GraphModel getGraphModel() {
		// TODO Auto-generated method stub
		return graphModel;
	}
}
