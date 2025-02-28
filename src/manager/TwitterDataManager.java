package manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import entities.Tweet;
import entities.User;


public class TwitterDataManager implements DataManagerInterface {
	private Map<String, List<User>> data;

	private final String databasefilepath;
	public TwitterDataManager(String databasefilepath) {
        this.data = new HashMap<>();
        this.databasefilepath = databasefilepath;
    }

	@Override
	public void addUserToDataBase(User user) {
		// TODO Auto-generated method stub
		String dict = user.getId();
        data.computeIfAbsent(dict, k -> new ArrayList<>());
        
        // Kiểm tra nếu User đã có trong danh sách
        if (!data.get(dict).contains(user)) {
            data.get(dict).add(user);
            System.out.println("Đã thêm User: " + user.getUrl());
        } else {
            System.out.println("User đã tồn tại: " + user.getUrl() +  " trong " + this.databasefilepath);
        }
	}

	@Override
	public void updatePostsForUser(String userId, Set<Tweet> tweets) {
		// TODO Auto-generated method stub
		// Tìm User trong database và cập nhật danh sách Tweet
		String dict = userId;
        if (data.containsKey(dict)) {
            Optional<User> userOpt = data.get(dict).stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();

            if (userOpt.isPresent()) {
                userOpt.get().setTweets(tweets);
                System.out.println("Danh sách Tweet đã được cập nhật cho User: " + userId);
            } else {
                System.out.println("Không tìm thấy User: " + userId);
            }
        } else {
            System.out.println("Không tìm thấy từ điển: " + dict);
        }
	}
	
	public void updateFollowersForUser(String userId, Set<String> followers) {
		// TODO Auto-generated method stub
		// Tìm User trong database và cập nhật danh sách Tweet
		String dict = userId;
        if (data.containsKey(dict)) {
            Optional<User> userOpt = data.get(dict).stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();

            if (userOpt.isPresent()) {
                userOpt.get().setFollowers(followers);
                
                System.out.println("Danh sách Followers đã được cập nhật cho User: " + userId);
            } else {
                System.out.println("Không tìm thấy User: " + userId);
            }
        } else {
            System.out.println("Không tìm thấy từ điển: " + dict);
        }
	}

	@Override
	public void saveToDatabase() {
		// TODO Auto-generated method stub
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(new File(this.databasefilepath), data);
            System.out.println("Dữ liệu đã được lưu vào file JSON: " + this.databasefilepath);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file JSON: " + e.getMessage());
        }
    }

	@Override
	public void loadFromDatabase() {
		// TODO Auto-generated method stub
        ObjectMapper mapper = new ObjectMapper();

        try {
            File file = new File(this.databasefilepath);
            if (file.exists()) {
                data = mapper.readValue(file, new TypeReference<Map<String, List<User>>>() {});
                System.out.println("Dữ liệu đã được tải từ file JSON " + this.databasefilepath);
            } else {
                System.out.println("File JSON "+ this.databasefilepath  + " không tồn tại, tạo mới.");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi tải file JSON: " + e.getMessage());
        }
	}


	public String getDatabasefilepath() {
		return databasefilepath;
	}

	@Override
	public void updateBasicInfoForUser(String userId, User updatedUser) {
		// TODO Auto-generated method stub
		// Tìm User trong database và cập nhật thông tin cơ bản
	    String dict = userId;
	    if (data.containsKey(dict)) {
	        Optional<User> userOpt = data.get(dict).stream()
	            .filter(user -> user.getId().equals(userId))
	            .findFirst();

	        if (userOpt.isPresent()) {
	            User existingUser = userOpt.get();
	            // Cập nhật thông tin cơ bản
	            existingUser.setDescription(updatedUser.getDescription());
	            existingUser.setFollowersCount(updatedUser.getFollowersCount());
	            existingUser.setFollowingCount(updatedUser.getFollowingCount());
	            existingUser.setId(updatedUser.getId());
	            existingUser.setJoinDate(updatedUser.getJoinDate());
	            existingUser.setLocation(updatedUser.getLocation());
	            existingUser.setProfessionalCategory(updatedUser.getProfessionalCategory());
	            existingUser.setTweetCount(updatedUser.getTweetCount());
	            existingUser.setUrl(updatedUser.getUrl());
	            existingUser.setWebsite(updatedUser.getWebsite());
	            // Thêm các trường cần cập nhật khác nếu cần thiết

	            System.out.println("Thông tin cơ bản đã được cập nhật cho User: " + userId);
	        } else {
	            System.out.println("Không tìm thấy User: " + userId);
	        }
	    } else {
	        System.out.println("Không tìm thấy từ điển: " + dict);
	    }
		
	}

	@Override
	public User getUserById(String userId) {
		// TODO Auto-generated method stub
		// Tìm User trong database theo id
	    String dict = userId;
	    if (data.containsKey(dict)) {
	        Optional<User> userOpt = data.get(dict).stream()
	            .filter(user -> user.getId().equals(userId))
	            .findFirst();

	        if (userOpt.isPresent()) {
	            return userOpt.get(); // Trả về User nếu tìm thấy
	        } else {
	            System.out.println("Không tìm thấy User: " + userId);
	            return null; // Hoặc ném ngoại lệ tùy theo yêu cầu
	        }
	    } else {
	        System.out.println("Không tìm thấy từ điển: " + dict);
	        return null; // Hoặc ném ngoại lệ tùy theo yêu cầu
	    }
	}

	@Override
	public boolean hasUser(String userId) {
		// TODO Auto-generated method stub
		String dict = userId;
	    if (data.containsKey(dict)) {
	        Optional<User> userOpt = data.get(dict).stream()
	            .filter(user -> user.getId().equals(userId))
	            .findFirst();

	        if (userOpt.isPresent()) {
	            return true; // Trả về User nếu tìm thấy
	        } else {
	            System.out.println("Không tìm thấy User: " + userId);
	            return false; // Hoặc ném ngoại lệ tùy theo yêu cầu
	        }
	    } else {
	        System.out.println("Không tìm thấy từ điển: " + dict);
	        return false; // Hoặc ném ngoại lệ tùy theo yêu cầu
	    }
	}

	@Override
	public Map<String, List<User>> getData() {
		// TODO Auto-generated method stub
		return this.data;
	}

	@Override
	public List<String> getUserIds() {
        return data.values().stream()
                   .flatMap(List::stream)
                   .map(User::getId)
                   .collect(Collectors.toList());
    }
		

}
