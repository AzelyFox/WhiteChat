package kr.devx.whitechat.Data;

public class Message {
    public int message_index;
    public int message_owner;
    public String message_content;
    public String message_created;

    public Message(int _index, int _owner, String _content, String _created) {
        this.message_index = _index;
        this.message_owner = _owner;
        this.message_content = _content;
        this.message_created = _created;
    }
}
