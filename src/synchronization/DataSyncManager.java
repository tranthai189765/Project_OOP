package synchronization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import config.ConfigInterface;
import entities.User;
import filehandler.FileHandler;
import manager.DataManagerInterface;

public class DataSyncManager {
    public static void syncFromRemote(DataManagerInterface localManager, DataManagerInterface remoteManager) {
    	remoteManager.loadFromDatabase();
    	localManager.loadFromDatabase();
    	List<String> userIds = remoteManager.getUserIds(); 
    	for(String id : userIds) {
    		if(localManager.hasUser(id) == true) {
    	
    			User userLocal = localManager.getUserById(id);
    			User userRemote = remoteManager.getUserById(id);
    			
    			if (userRemote.getTweets() != null && !userRemote.getTweets().isEmpty()) {
    			    userLocal.setTweets(userRemote.getTweets());
    			}
    			if (userRemote.getFollowers() != null && !userRemote.getFollowers().isEmpty()){
    				userLocal.setFollowers(userRemote.getFollowers());
    			}
    			if (userRemote.getDescription() != null) {
    				userLocal.setDescription(userRemote.getDescription());
    			}
    			if (userRemote.getFollowersCount() != 0) {
    				userLocal.setFollowingCount(userRemote.getFollowingCount());
    			}
    			if (userRemote.getFollowingCount() != 0) {
    				userLocal.setFollowingCount(userRemote.getFollowingCount());
    			}
    			if (userRemote.getFollowing() != null && !userRemote.getFollowing().isEmpty()) {
    				userLocal.setFollowing(userRemote.getFollowing());
    			}
    			if (userRemote.getJoinDate() != null) {
    				userLocal.setJoinDate(userRemote.getJoinDate());
    			}
    			if (userRemote.getKolType() != null) {
    				userLocal.setKolType();
    			}
    			if (userRemote.getDescription() != null) {
    				userLocal.setDescription(userRemote.getDescription());
    			}
    			if (userRemote.getLocation() != null) {
    				userLocal.setLocation(userRemote.getLocation());
    			}
    			if (userRemote.getProfessionalCategory()!= null) {
    				userLocal.setProfessionalCategory(userRemote.getProfessionalCategory());
    			}
    			if (userRemote.getTweetCount() != null) {
    				userLocal.setTweetCount(userRemote.getTweetCount());
    			}
    			if (userRemote.getUrl()!= null) {
    				userLocal.setUrl(userRemote.getUrl());
    			}
    			if (userRemote.getWebsite() != null) {
    				userLocal.setWebsite(userRemote.getWebsite());
    			}
    		}
    		else {
    			User userRemote = remoteManager.getUserById(id);
    			localManager.addUserToDataBase(userRemote);
    		}
    	}
    	localManager.saveToDatabase();
    	System.out.println("Đồng bộ dữ liệu từ remote database hoàn tất.");
    }

    public static void updateLocalDatabaseFromThreads(ConfigInterface config, int threadCount) {
        DataManagerInterface localManager = config.getLocalManager();

        for (int index = 0; index < threadCount; index++) {
            String filePathThreads = index + "_database.json";
            DataManagerInterface remoteManager = config.newManager(filePathThreads);
            remoteManager.loadFromDatabase();
            syncFromRemote(localManager, remoteManager);
        }
    }

    public static void updateLocalFileDatabase(String sourceFilePath, String targetFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(targetFilePath, true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void updateLocalFileDatabaseFromThreads(String targetFilePath, int threadCount) throws IOException {
        for (int index = 0; index < threadCount; index++) {
            String filePath = index + "_UsersFromHashtags.txt";
            updateLocalFileDatabase(filePath, targetFilePath);
        }

        FileHandler.removeDuplicatesInPlace(targetFilePath);
    }
}
