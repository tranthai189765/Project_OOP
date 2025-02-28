package scraper;

public interface DataFetcherStrategy {
	void fetchUserByHashtagsMultiThreads(int threadCount);
    void fetchProfileMultiThreads(int threadCount);
    void fetchFollowersMultiThreads(int threadCount);
    void fetchTweetsMultiThreads(int threadCount);
}
