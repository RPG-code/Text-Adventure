package adventureGame;

public class Exit {
	String exit = "";	// this is the name of the room that this exit leads to. The room must be one of the unique names in roomList
	boolean isDoor = false;
	boolean isOpen = true;
	boolean isLocked = false; //any locked door requires a key.
	String keyName = "key"; //if name == "key" then any generic key will do
	String doorDescr = "";
	
	Exit(String roomName) {
		exit = roomName;
	}
	
	void setDoor(boolean isOpen, boolean isLocked, String key, String descr){
		this.isDoor = true;
		this.isOpen = isOpen;
		this.isLocked = isLocked;
		this.keyName = key;
		this.doorDescr = descr;
	}
	
	
}
