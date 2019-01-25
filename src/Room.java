public class Room {
    private String room;

    public Room(String room) {
        this.room = room;
    }


    synchronized public void setRoom(String room) {
        this.room = room;
    }

    synchronized public String getRoom() {
        return room;
    }
}
