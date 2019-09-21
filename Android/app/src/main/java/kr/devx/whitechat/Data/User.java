package kr.devx.whitechat.Data;

import android.util.ArrayMap;

import java.util.ArrayList;

public class User {
    public int user_index;
    public String user_id;
    public String user_nickname;
    public int user_rank;

    public String user_created;
    public String user_thumbnail;
    public int user_cash;

    public String user_key;

    public ArrayList<User> friends;
    public ArrayList<Room> rooms;

    public User(int _index, String _id, String _nickname, int _rank) {
        this.user_index = _index;
        this.user_id = _id;
        this.user_nickname = _nickname;
        this.user_rank = _rank;
        friends = new ArrayList<>();
        rooms = new ArrayList<>();
    }
}
