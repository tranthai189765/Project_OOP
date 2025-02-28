package scraper;

import config.ConfigInterface;

public class SeleniumScraper implements DataFetcherStrategy {
	
	private UserFinder userFinder;
	private KOLBasicInfoFetcher basicInfoFetcher;
	private KOLFollowerFetcher followerFetcher;
	private KOLTweetFetcher tweetFetcher;
	public SeleniumScraper(ConfigInterface config) {
		this.userFinder = new UserFinder(config);
		this.basicInfoFetcher = new KOLBasicInfoFetcher(config);
		this.followerFetcher = new KOLFollowerFetcher(config);
		this.tweetFetcher = new KOLTweetFetcher(config);
	}

	@Override
	public void fetchUserByHashtagsMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		userFinder.fetchUserByHashtagsMultiThreads(threadCount);
	}

	@Override
	public void fetchProfileMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		basicInfoFetcher.fetchProfileMultiThreads(threadCount);
	}

	@Override
	public void fetchFollowersMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		followerFetcher.fetchFollowersMultiThreads(threadCount);
		
	}

	@Override
	public void fetchTweetsMultiThreads(int threadCount) {
		// TODO Auto-generated method stub
		tweetFetcher.fetchTweetsMultiThreads(threadCount);
	}

}
