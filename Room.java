package adventureGame;

import java.util.ArrayList;
import java.util.HashMap;

/* This class sets up the rooms (ie. all locations) for the adventure game. 
 * Items are later on placed into the rooms once the items are created (see Item class). * 
 */

public class Room{
	
	private String title;
	private String description;
	private Exit N,S,W,E,U,D; //these are exits that point to the name of other rooms.
								 //We're using a hashmap, so each name is guaranteed to be unique.
								 //If the name starts with "r_" it is a special method that must be run when moving that direction. 
	private boolean isDark=false;
	private boolean visited=false;	
	ArrayList<String> items = new ArrayList<String>(); //items in room

	//constructors
//	Room(String name, String title, String description){
	Room(String title, String description){
		//this.name = name;
		this.title = title;
		this.description = description;		
	}
	
	void setExits(String N, String S, String W, String E, String U, String D) {
		this.N = new Exit(N);
		this.S = new Exit(S);
		this.E = new Exit(E);
		this.W = new Exit(W);
		this.U = new Exit(U);
		this.D = new Exit(D);
				
	}
	Exit getExit(char c) {
		switch (c) {
		case 'n': return this.N;
		case 'e': return this.E;
		case 's': return this.S;
		case 'w': return this.W;
		case 'u': return this.U;
		case 'd': return this.D;
		default: return null;
		}
	}
	
	String getExitRoomname(char c) {
		switch (c) {
		case 'n': return this.N.exit;
		case 'e': return this.E.exit;
		case 's': return this.S.exit;
		case 'w': return this.W.exit;
		case 'u': return this.U.exit;
		case 'd': return this.D.exit;
		default: return "";
		}
	}
	
//	String getRoomID()		{ return this.name; }
	String getTitle()		{ return title; }
	String getDesc()		{ return description; }
	boolean hasVisited()	{ return visited; }
	void visit()			{ visited = true;}
	boolean getIsDark()		{return this.isDark; }

	
	//most classes need to have this. Only used for testing (to print out all rooms (below)).  
	public String toString(){
		String s = String.format("Title=%-25s\tDescription=%s",title,description);		
		return s;
	}
	
		
	static void setupRooms(HashMap<String,Room> roomList) {
		
		Room r = new Room("Forest Clearing", 
				"There is a lovely clearing in the forest here. It is very peaceful.\n"
				+ "There is a path leaning north and another winding one that heads off to the west.");
		//		   N S W E U D
		r.setExits("forest1", "",  "path1","", "","");		
		roomList.put("clearing",r);		
		
		r = new Room("Forest path", 
				"You are on a path in the forest. "
				+ "A very tall tree completely blocks the path to the north. "
				+ "There is a path to the south.");
		r.setExits("", "clearing","", "", "tree1","");
		roomList.put("forest1",r);
		
		r = new Room("Partway up a huge tree", 
				"It is getting a lot harder to climb. Perhaps you should stop now."
				+ "\nSomeone has carved your initials here.");
		r.setExits("", "","tree1b", "tree1b", "r_fallFromTree","forest1");				
		roomList.put("tree1",r);
		
		r = new Room("Partway up a huge tree", 
				"It is getting a lot harder to climb. Perhaps you should stop now.");
		r.setExits("", "","tree1", "tree1", "r_fallFromTree","maze2");				
		roomList.put("tree1b",r);
		
		r = new Room("Gloomy forest path", 
				"You are in a gloomy part of the forest.\n"
				+ "A very tall tree completely blocks the path to the south."
				+ " Unfortunately you can't climb it from this side.\n"
				+ "There are narrow windy paths in other directions, but you might get lost if you follow them.");
		r.setExits("maze2","","maze5","maze4", "","");
		roomList.put("maze2",r);
		
		r = new Room("Gloomy forest path", "You are in a gloomy part of the forest with narrow windy paths in many directions.");
		r.setExits("maze5","maze6","maze2","maze3", "","");
		roomList.put("maze3",r);
		
		r = new Room("Gloomy forest path",	"You are in a gloomy part of the forest with narrow windy paths in many directions.");
		r.setExits("", "maze2","maze5","maze6", "","");
		roomList.put("maze4",r);
		
		r = new Room("Gloomy forest path",	"You are in a gloomy part of the forest with narrow windy paths in many directions.");
		r.setExits("maze4","maze2","","maze3","","");
		roomList.put("maze5",r);
		
		r = new Room("Gloomy forest path",	"You are in a gloomy part of the forest with narrow windy paths in many directions.");
		r.setExits("maze6","maze3","forest1","maze4", "","");
		roomList.put("maze6",r);
		
		r = new Room("Forest path", 
				"There appears to be a clearing to the north. A cave is to the west. "
				+ "The path continues to the south.");
		r.setExits("clearing", "forest2","cave1", "", "","");		
		roomList.put("path1",r);
		
		r = new Room("Forest", 
				"You're in the forest. You see light to the north.");
		r.setExits("path1", "","forest2", "forest2", "","");		
		roomList.put("forest2",r);	
		
		r = new Room("Entrance to cave",
				"You're standing in the entrance to a dark cave. A path is to the east "
				+ "and the cave goes further on to the west");
		r.setExits("", "","cave2", "path1", "","");
		roomList.put("cave1",r);

	/*
		//make a standard closed Door
		Exit door = new Exit("");
		door.isDoor = true;
		door.isOpen = false;
		
		//make a standard locked door
		Exit lockedDoor = new Exit("");
		lockedDoor.isDoor = true;
		lockedDoor.isOpen = false;
		lockedDoor.isLocked= true;
	*/	
		r= new Room("Sparkling cave",
				"This is a huge cavern. The walls of the cave sparkle with shiny crystals. There is light to the east"
				+ " and passages in other directions.");	
		r.setExits("chimney", "tunnel1", "cave3", "cave1", "","tunnel1");
		r.W.setDoor(false,false,"","A heavy wooden door, reinforced with studs against axe attacks"); //YOU MUST PUT EACH DOOR IN TWICE! IN EACH ROOM THAT ADJOINS IT.
		r.isDark = true;		
		roomList.put("cave2", r);
		
		r = new Room("Fetid cave", "This cave smells stale and nauseating."); 
		r.setExits("", "","", "cave2", "","");
		r.E.setDoor(false,false,"","A heavy wooden door, reinforced with studs against axe attacks");
		r.isDark = true;		
		roomList.put("cave3", r);
		
		r = new Room("Tunnel","a slippery tunnel connecting parts of the cave system.");
		r.setExits("cave2", "tunnel2","cave2", "", "","");
		r.isDark = true;		
		roomList.put("tunnel1", r);

		r = new Room("Tunnel","a long slippery tunnel sloping slightly downwards.");
		r.setExits("tunnel1", "tunnel3","", "", "secret_room","");
		r.isDark = true;		
		//make a secret exit up top -- a single room with a hidden chest?
		roomList.put("tunnel2", r);
		
		r = new Room("Secret Room", "You found a secret room with beautiful murals. "
				+ "\nYou can exit by climbing down to the tunnel below. ");
		r.setExits("","","","","","tunnel2");
		roomList.put("secret_room",r);		
		
		r = new Room("Tunnel","a slippery tunnel."
				+ "\nAt the south end is a room whose entrance is blocked by massive iron door that is off its hinges,"
				+ "\nbut, it looks like you can squeeze past it.");
		r.setExits("tunnel2", "treasury","", "", "","");
		r.isDark = true;		
		roomList.put("tunnel3", r);
				
		r = new Room("Dwarf treasury","centuries ago, this was the treasury of the dwarfs."
				+ "\nIt has obviously been thoroughly looted, though the ceiling is still lit by glowing crystals."
				+ "\nYou entered from the tunnel to the west.");
		r.setExits("", "","tunnel3", "", "","");
		r.isDark = false;		
		roomList.put("treasury", r);
		
		r = new Room("Chimney crack",
				"A vertical crack in the rock - it is climbable since you're so fit.");
		r.setExits("", "","", "", "cave2","chimney2");
		r.isDark = true;		
		roomList.put("chimney", r);
		
		r = new Room("Chimney crack",
				"A vertical crack in the rock - you can't see the top nor the bottom.");
		r.setExits("", "","", "", "chimney","chimney3");
		r.isDark = true;		
		roomList.put("chimney2", r);
		
		r = new Room("Chimney crack",
				"A vertical crack in the rock - tight and uncomfortable.");
		r.setExits("", "","", "", "chimney2","black_lake");
		r.isDark = true;		
		roomList.put("chimney3", r);
		
		r = new Room("Black lake",
				"You have descended to a large underground cavern. A black lake laps against the shore."
				+ "\nThe only exit is a narrow crack that extends upwards out of sight.");
		r.setExits("", "","", "", "chimney3","");
		r.isDark = true;		
		roomList.put("black_lake", r);

/*
		//List all rooms	
		System.out.println("*****************************************");
		for (Room m : roomList.values()){
			System.out.println(m.toString());
		}
		System.out.println("*****************************************\n\n");
*/
	
	}

}