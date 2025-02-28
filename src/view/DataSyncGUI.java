package view;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import config.ConfigInterface;
import synchronization.DataSyncManager;
import scraper.DataFetcherStrategy;

public class DataSyncGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
    private JPanel mainPanel;
    private JPanel processPanel;
    private JTextArea textArea;
    private JButton viewDataButton;
    private JButton continueButton;
    private JLabel statusLabel;

    private ConfigInterface config;
    private DataFetcherStrategy scraper;

    private int currentStep = 0; // Theo dõi bước hiện tại

    public DataSyncGUI (ConfigInterface config, DataFetcherStrategy scraper) {
        this.config = config;
        this.scraper = scraper;
        initializeMainUI();
    }

    private void initializeMainUI() {
        // Tạo JFrame chính
        frame = new JFrame("Analyze Twitter KOL Data");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new CardLayout());

        // Panel màn hình chính
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Analyze Twitter KOL Data", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.PLAIN, 16));
        mainPanel.add(startButton, BorderLayout.SOUTH);

        // Action khi nhấn Start
        startButton.addActionListener(e -> {
                switchToProcessUI();
           
        });

        frame.add(mainPanel, "main");
        initializeProcessUI();
        frame.setVisible(true);
    }

    private void initializeProcessUI() {
        // Panel giao diện xử lý
        processPanel = new JPanel();
        processPanel.setLayout(new BorderLayout());

        // JLabel trạng thái
        statusLabel = new JLabel("Chương trình bắt đầu...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        processPanel.add(statusLabel, BorderLayout.NORTH);

        // JTextArea hiển thị nội dung file
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        processPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel();
        viewDataButton = new JButton("Refesh");
        continueButton = new JButton("Tiếp tục");
        JButton skipButton = new JButton("Skip"); // Nút Skip

        viewDataButton.setEnabled(false);
        continueButton.setEnabled(false);

        buttonPanel.add(viewDataButton);
        buttonPanel.add(continueButton);
        buttonPanel.add(skipButton); // Thêm nút Skip
        processPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action cho nút "Xem dữ liệu đã cập nhật"
        viewDataButton.addActionListener(e -> displayFileContent());

        // Action cho nút "Tiếp tục"
        continueButton.addActionListener(e -> {
            viewDataButton.setEnabled(false);
            continueButton.setEnabled(false);

            new Thread(() -> {
                try {
                    switch (currentStep) {
                        case 0:
                            int numberOfThreadsUsers = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số luồng cho bước này:"));
                            if(numberOfThreadsUsers == 0) {
                            	break;
                            }
                            int maxUsers = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số lượng users tối đa cho mỗi hashtags:"));
                            config.setMaxUsers(maxUsers);
                            scraper.fetchUserByHashtagsMultiThreads(numberOfThreadsUsers);
                            DataSyncManager.updateLocalFileDatabaseFromThreads(config.getUsersFoundFilePath(), numberOfThreadsUsers);
                            break;
                        case 1:
                            int numberOfThreadsProfile = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số luồng cho bước này:"));
                            if(numberOfThreadsProfile == 0) {
                            	break;
                            }
                            scraper.fetchProfileMultiThreads(numberOfThreadsProfile);
                            DataSyncManager.updateLocalDatabaseFromThreads(config, numberOfThreadsProfile);
                            break;
                        case 2:
                            int numberOfThreadsFollowers = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số luồng cho bước này:"));
                            if(numberOfThreadsFollowers == 0) {
                            	break;
                            }
                            int maxFollowers = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số lượng followers tối đa:"));
                            config.setMaxFollowers(maxFollowers);
                            scraper.fetchFollowersMultiThreads(numberOfThreadsFollowers);
                            DataSyncManager.updateLocalDatabaseFromThreads(config, numberOfThreadsFollowers);
                            break;
                        case 3:
                            int numberOfThreadsTweets = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số luồng cho bước này:"));
                            if(numberOfThreadsTweets == 0) {
                            	break;
                            }
                            int maxTweets = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số lượng tweets tối đa:"));
                            config.setMaxTweets(maxTweets);
                            int maxComments = Integer.parseInt(JOptionPane.showInputDialog(frame, "Nhập số lượng comments tối đa:"));
                            config.setMaxComments(maxComments);
                            scraper.fetchTweetsMultiThreads(numberOfThreadsTweets);
                            DataSyncManager.updateLocalDatabaseFromThreads(config, numberOfThreadsTweets);
                            break;
                    }

                    SwingUtilities.invokeLater(() -> {
                    	if(currentStep==0) {
                    		statusLabel.setText("Thu thập dữ liệu về users bằng hashtags đã hoàn thành" );
                    	}else if(currentStep==1) {
                    		statusLabel.setText("Thu thập dữ liệu các users đã hoàn thành" );
                    	}else if(currentStep==2) {
                    		statusLabel.setText("Thu thập dữ liệu về followers của KOL đã hoàn thành" );
                    	}else if(currentStep==3) {
                    		statusLabel.setText("Thu thập dữ liệu về tweets của KOL đã hoàn thành" );
                    	}
                        currentStep++;
                        executeNextStep();
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Lỗi khi thực hiện bước " + (currentStep + 1) + ": " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        // Action cho nút "Skip"
        skipButton.addActionListener(e -> {
            currentStep++; // Tăng bước hiện tại
            executeNextStep(); // Gọi bước tiếp theo
        });

        frame.add(processPanel, "process");
    }
    
    
    private void switchToProcessUI() {
        // Chuyển từ màn hình chính sang màn hình xử lý
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "process");
        executeNextStep();
    }
    private void displayFileContent() {
        try {
            // Tạo panel để hiển thị 3 cột
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(1, 3, 10, 10)); // Chia thành 3 cột, với khoảng cách 10px

            // Đọc nội dung từ file `UsersFoundFilePath`
            String usersContent = new String(Files.readAllBytes(Paths.get(config.getUsersFoundFilePath())));
            JTextArea usersTextArea = new JTextArea(usersContent);
            usersTextArea.setEditable(false);
            JScrollPane usersScrollPane = new JScrollPane(usersTextArea);
            usersScrollPane.setBorder(BorderFactory.createTitledBorder("Users Found"));

            // Đọc nội dung từ file `HashTagsFilePath`
            String hashtagsContent = new String(Files.readAllBytes(Paths.get(config.getHashTagsFilePath())));
            JTextArea hashtagsTextArea = new JTextArea(hashtagsContent);
            hashtagsTextArea.setEditable(false);
            JScrollPane hashtagsScrollPane = new JScrollPane(hashtagsTextArea);
            hashtagsScrollPane.setBorder(BorderFactory.createTitledBorder("HashTags"));

            // Đọc nội dung từ file `DatabaseFilePath`
            String databaseContent = new String(Files.readAllBytes(Paths.get(config.getLocalManager().getDatabasefilepath())));
            JTextArea databaseTextArea = new JTextArea(databaseContent);
            databaseTextArea.setEditable(false);
            JScrollPane databaseScrollPane = new JScrollPane(databaseTextArea);
            databaseScrollPane.setBorder(BorderFactory.createTitledBorder("Database"));

            // Thêm các cột vào contentPanel
            contentPanel.add(usersScrollPane);
            contentPanel.add(hashtagsScrollPane);
            contentPanel.add(databaseScrollPane);

            // Thay thế nội dung cũ của processPanel
            SwingUtilities.invokeLater(() -> {
                processPanel.removeAll(); // Xóa tất cả nội dung hiện tại
                processPanel.setLayout(new BorderLayout()); // Đảm bảo đúng bố cục BorderLayout
                processPanel.add(contentPanel, BorderLayout.CENTER); // Thêm contentPanel vào giữa
                processPanel.add(createButtonPanel(), BorderLayout.SOUTH); // Giữ lại các nút ở dưới
                processPanel.add(statusLabel, BorderLayout.NORTH);
                processPanel.revalidate(); // Làm mới giao diện
                processPanel.repaint(); // Vẽ lại giao diện
                
            });

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Lỗi khi đọc dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewDataButton);
        buttonPanel.add(continueButton);
       // buttonPanel.add(new JButton("Skip")); // Tạo lại nút Skip nếu cần
        return buttonPanel;
    }




    private void executeNextStep() {
        viewDataButton.setEnabled(false);
        continueButton.setEnabled(false);
        textArea.setText("");

        if (currentStep > 3) { // Nếu đã hoàn thành tất cả các bước
            statusLabel.setText("Hoàn thành tất cả các bước.");
            JOptionPane.showMessageDialog(frame, "Quá trình đã hoàn tất. Giao diện sẽ đóng.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose(); // Tắt giao diện
            return;
        }

        // Hiển thị file tương ứng với bước hiện tại
        SwingUtilities.invokeLater(() -> displayFileContent());
        System.out.println(currentStep);

        // Chờ người dùng chọn hành động
        SwingUtilities.invokeLater(() -> {
        	if (currentStep == 0) {
        	    statusLabel.setText("Nhấn Tiếp tục. Bạn có muốn thu thập dữ liệu về users bằng hashtags không?\n"
        	            + " Nếu Có, chọn 0. Nếu Không, chọn số luồng bạn muốn.");
        	} else if (currentStep == 1) {
        	    statusLabel.setText("Bạn có muốn thu thập dữ liệu về Profile của KOL không?" + " Nếu Có, chọn 0. Nếu không, chọn số luồng bạn muốn.");
        	} else if (currentStep == 2) {
        	    statusLabel.setText("Bạn có muốn thu thập dữ liệu về followers của KOL không?\n"
        	            +" Nếu Có, chọn 0. Nếu Không, chọn số luồng bạn muốn.");
        	} else if (currentStep == 3) {
        	    statusLabel.setText("Để thu thập dữ liệu về tweets của KOL không?\n"
        	            + " Nếu Có, chọn 0. Nếu không, chọn số luồng bạn muốn.");
        	}
            viewDataButton.setEnabled(true);
            continueButton.setEnabled(true);
        });
    }

	public JFrame getFrame() {
		// TODO Auto-generated method stub
		return frame;
	}


}