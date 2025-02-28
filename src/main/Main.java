package main;

import config.ConfigInterface;
import config.TwitterConfig;
import graph.GraphBuilder;
import graph.GraphManager;
import scraper.DataFetcherStrategy;
import scraper.SeleniumScraper;
import view.DataSyncGUI;
import view.DataViewer;

import java.awt.event.WindowEvent;

import javax.swing.*;

import algorithms.TwitterPageRankAlgorithm;

import java.awt.event.WindowAdapter;
public class Main {
    public static void main(String[] args) {
    	 // Tạo config và scraper
    	 ConfigInterface config = new TwitterConfig();
         DataFetcherStrategy scraper = new SeleniumScraper(config);
         
         SwingUtilities.invokeLater(() -> {
             DataSyncGUI dataSyncGUI = new DataSyncGUI(config, scraper);
             JFrame frame = dataSyncGUI.getFrame();
             frame.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosed(WindowEvent e) {
                     // Khi cửa sổ đầu tiên chuẩn bị đóng, khởi chạy giao diện thứ hai
                     SwingUtilities.invokeLater(() -> {
                         GraphManager graphManager = new GraphManager();
                         GraphBuilder graphBuilder = new GraphBuilder(graphManager, config);
                         graphBuilder.buildGraph(config);
                         graphManager.loadGraphFromFile(config.getGraphFilePath());

                         TwitterPageRankAlgorithm alg = new TwitterPageRankAlgorithm(graphManager, config);
                         alg.run(100);

                         // Hiển thị giao diện thứ hai
                         new DataViewer().createAndShowGUI(config);
                     });
                 }
             });
         });
     }
 }