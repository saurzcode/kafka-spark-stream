package in.saurzcode.stream.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private long id;
    private String name;

    @SerializedName("screen_name")
    private String screenName;
    @SerializedName("followers_count")
    private int followersCount;
    @SerializedName("friends_count")
    private int followingCount;
    @SerializedName("statuses_count")
    private int postCount;

    public User(long id, String name, String screenName, int followersCount, int followingCount, int postCount) {
        this.id = id;
        this.name = name;
        this.screenName = screenName;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.postCount = postCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}

