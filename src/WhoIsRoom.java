import java.util.ArrayList;
import java.util.List;

public class WhoIsRoom {
    private List<String> nicksInRoom = new ArrayList<>();

    synchronized public void resetList() {
        nicksInRoom.clear();
    }

    synchronized public List<String> getNicksInRoom() {
        return nicksInRoom;
    }

    synchronized public void addToList(String nick) {
        nicksInRoom.add(nick);
    }
}
