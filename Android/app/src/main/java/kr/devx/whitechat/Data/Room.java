package kr.devx.whitechat.Data;

import java.util.ArrayList;

public class Room {
    public int room_index;
    public String room_name;
    public int room_notice;
    public int room_count;

    public ArrayList<User> participants;
    public ArrayList<Message> messages;

    public Room(int _index, String _name, int _notice) {
        this.room_index = _index;
        this.room_name = _name;
        this.room_notice = _notice;
        participants = new ArrayList<>();
        messages = new ArrayList<>();
    }
}
