package adventureGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//needed for methodMap to work.
interface Command {
	void callMe();
}

public class AdventureMain {

	static int INVSIZE = 10; //size of inventory	
	//instance variables
	//ArrayList<Room> roomList = new ArrayList<Room>();
	HashMap<String,Room> roomList = new HashMap<String,Room>();
	HashMap<String, Item> itemList = new HashMap<String,Item>(); //list of all item objects
	Map<String, Command> methodMap = new HashMap<String, Command>();
	ArrayList<String> inventory = new ArrayList<String>();
	String currentRoom;
	Player player;
	
	int turns = 0;
	enum Dark{OKAY, WARNING, DEAD};
	Dark darkWarning = Dark.OKAY;

	public static void main(String[]args){
		new AdventureMain();
	}

	AdventureMain() {

		boolean playing = true;
		String command = "";
		addMethodMap();
		Room.setupRooms(roomList);		
		Item.setUpItems(itemList, roomList);		
		System.out.print("Please type your firstname: (press enter for \"Billy\") ");
		String name = getCommand();
		if (name.equals("qwerty")) name = "Billy";
		player = new Player(name); //make a new player with given name		
		//main game loop
		//set to false if the user quits
		startingMessage();

		/*  for ANSI screen controls -- which don't work in Windows.
		char escCode = 0x1B;
		int row = 10; int column = 10;
		System.out.print(escCode + "[2J"); //clear screen
		System.out.print(String.format("%c[%d;%df",escCode,row,column));
	    */
		
		lookAtRoom(true);
		while (playing) {
			command = getCommand();

			playing = parseCommand(command);

			if (darkWarning == Dark.DEAD) {
				System.out.println("You died. How sad.");
				playing = false;
			}

			if (player.getHealth() < 0) {
				System.out.println("You died. How sad.");
				playing=false;
			}
			//handle all food points
			//TODO: fix this so that it does not use magic numbers
			{
				int fp = player.getFoodPoints();			
				if (fp > 0 && fp < 20) System.out.println("You are getting hungry.");
				if (fp > -15 && fp <= 0) System.out.println("You are starving.");
				if (fp < -15) {
					System.out.println("You starved to death. How sad.");
					playing=false;
				}
			}
			//check if player won the game.
		}
	}


	String getCommand() {
		Scanner sc = new Scanner(System.in);		
		String text = sc.nextLine();
		if (text.length() == 0) text = "qwerty"; //default command
		//sc.close();
		return text;
	}

	/* Commands that work so far:
	GO direction, direction,
	LOOK, QUIT
	?, HELP  <--- not working yet
	This will crash if the wrong number of words are entered! e.g. "look at"
	 */
	boolean parseCommand(String text) {

		text = text.toLowerCase().trim();	//complete string BEFORE parsing
		//no words entered:
		if (text.length()==0 || text.equals("qwerty")) return true;
		if (text.equals("a")) return true;
		if (text.equals("an")) return true;
		if (text.equals("the")) return true;
		if (text.equals("go")) return true;
		
		/***** PREPROCESSING *****/
		text = text.replaceAll(" into ", " in ");
		text = text.replaceAll(" rocks", " rock");
		text = text.replaceAll("pick up", "pickup");
		text = text.replaceAll("look at", "lookat");
		text = text.replaceAll("climb up", "climbup");
		

		String words[] = text.split(" ");
		//pre-parsing: remove "THE". What about "go to"
		ArrayList<String> wordlist = new ArrayList<String>(Arrays.asList(words));		//array list of words
		for(int i=0; i< wordlist.size(); i++) {
			if (wordlist.get(i).equals("the")) wordlist.remove(i--);
			if (wordlist.get(i).equals("a")) wordlist.remove(i--);
			if (wordlist.get(i).equals("an")) wordlist.remove(i--);
			if (wordlist.get(i).equals("go")) wordlist.remove(i--);
		}

		String word1 = wordlist.get(0);
		if (word1 == null || word1.length() == 0) return true;
		words = wordlist.toArray(new String[wordlist.size()]);	//array of words (reusing the "words" variable again)

		String word2,word3,word4;
		word2 = word3 = word4 = "";
		if (words.length > 1) word2 = words[1];
		if (words.length > 2) word3 = words[2]; 
		if (words.length > 3) word4 = words[3]; 
		//special case for "rock2".  Note, all rocks are called "rock".
		//TODO: explain, what does this do?
		if (word2.equals("rock") && roomList.get(currentRoom).items.contains("\"rock\"")) word2 = "\"rock\"";
		if (word3.equals("rock") && roomList.get(currentRoom).items.contains("\"rock\"")) word3 = "\"rock\"";
		
		/***** MAIN PROCESSING *****/
		switch(word1) {
		//one word commands
		case "quit":
			System.out.print("Do you really want to quit the game? ");
			String ans = getCommand().toUpperCase();
			if (ans.equals("YES") || ans.equals("Y")) {
				System.out.print("Thanks for playing. Bye.");
				return false;
			}			
		case "n": case "s": case "w": case "e": case "u": case "d":
		case "north": case "south": case "west": case "east": case "up": case "down":
			moveToRoom(word1.charAt(0));
			break;
		case "i": case "inventory":
			showInventory();
			break;
		case "sleep":
			sleep();			
			break;	
		case "look":
			lookAtRoom(true);
			break;
		case "help":
			printHelp();
			break;
			
		//two word commands
		case "climbup":
		case "climb":
			if (word2.equals("")) {
				System.out.println("What do you want to climb?");
				break;
			}
			if (word2.equals("up")) word2 = word3;
			if (word2.equals("tree")) {
				if (currentRoom.equals("forest1")) {
					System.out.println("You start climbing ...");
					moveToRoom('u');
				} else {
					System.out.println("There is no climbable tree here.");					
				}
			} else {
				System.out.println("You can't climb that.");				
			}
			break;
		case "read":
			readObject(word2);
			break;
		case "lookat":
		case "examine":
			lookAtObject(word2);
			break;
		
		
		case "pickup":
			takeObject(word2);
			break;		
		case "take":
			//take B, take B from A
			if (word3.equals("from")) {
				takeObject(word2, word4);
			} else {
				takeObject(word2);
			}
			break;
		case "drop":
			dropObject(word2);
			break;
		case "eat":
			eatItem(word2);
			break;		
				 
		case "move": //move an item. These are things you can't pick up.
			moveItem(word2);
			break;
		case "put":  
			//TODO: put A in B  (why would anyone do this?) "put hammer in chest"
			//This does not work EXCEPT for these two special commands
			if (text.startsWith("put emerald in bell")) activate("bell");
			//special lake command
			else if (currentRoom.equals("black_lake") && text.startsWith("put hand in lake")) activate("lake");
			else System.out.println ("huh?");
			break;
		case "reach":
			if (currentRoom.equals("black_lake") && text.startsWith("reach in lake")) activate("lake");
			break;
		//hit rock with hammer
		case "smash":
		case "break":
		case "hit":
			if (text.contains (" rock with hammer")) activate("hammer");
			else System.out.println("Sorry, I don't understand that command");
			break;
		//use hammer to break rock
		case "use":
			if (text.startsWith("use hammer to") && text.contains("rock")) {
				activate("hammer");
				break;
			}
			System.out.println("Sorry, I don't understand that command");
			//activate(word2);
			break;
			
		//SPECIAL COMMANDS
		//get this working for open paper and open package, maybe also open door
		case "open":
			if (word2=="") {
				System.out.println("open what?");				
			} else {
				openStuff(word2, word3, word4);
			}			
			break;			
		case "close":
			if (word2.contains("door")) closeDoor(word2);
			else closeObject(word2);
			break;	
		
			/*	turn on flashlight.  turn off flashlight
			turn flashlight on, turn flashlight off		*/
		case "turn":
			if (word3.equals("flashlight")) {
				if (word2.equals("on")) { 
					itemList.get("flashlight").setActivate(true);
					lookAtObject("flashlight");
				}
				if (word2.equals("off")) itemList.get("flashlight").setActivate(false);
			}
			else if (word2.equals("flashlight")) {
				if (word3.equals("on")) { 
					itemList.get("flashlight").setActivate(true); 
					lookAtObject("flashlight");
				}
				if (word3.equals("off")) itemList.get("flashlight").setActivate(false);
			}
			else System.out.println("Sorry, I don't understand that command");
			break;
		case "ring":
			ringBell(word2);
			break;
		default: 
			System.out.println("Sorry, I don't understand that command");
		}
		return true;
	}			
	
	void moveToRoom(char dir) {
		String newRoom = roomList.get(currentRoom).getExitRoomname(dir);
		
		if (newRoom.length()==0) {
			System.out.println("You can't go that way");
			return;
		}
		//TODO: FIXME: check to see if there is a door, if it is closed, if it is locked
		Exit exit = roomList.get(currentRoom).getExit(dir);
		if (exit.isDoor) {
			if(exit.isOpen) {
				System.out.println("There is a door here. You walk through the open door.");
			} else {
				System.out.println(exit.doorDescr);
				System.out.println("The door to that room is closed.");
				return;
			}
		}
		player.hunger(1);
		turns++;
		//what is this for? (methods for moving ... e.g. climbing the tree and falling, moving through the door)
		if (newRoom.substring(0, 2).equals("r_")) {			
			runMethod(newRoom);
			return;
		}
		currentRoom = newRoom;		
		lookAtRoom(false);		
	}

	/* lookAtRoom:
	 * function: this displays the title and description of the room.
	 * 		If you have already been in this room, it won't display the description
	 * 		It also lists all of the items in the room.
	 * 		It prints a warning about dark rooms & death.
	 * parameters: if look is true, then it will display the description.
	 */
	void lookAtRoom(boolean look) {
		Room rm = roomList.get(currentRoom);
		if (rm == null) { 
			System.out.println("ERROR: room \""+ currentRoom + "\" does not exist.");
			return;
		}		

		if (rm.getIsDark()) {
			if (inventory.contains("flashlight") && itemList.get("flashlight").isActivated()) {
				//continue
			} else {			
				if (darkWarning == Dark.WARNING) {
					//you are moving into a second dark room
					System.out.println("\n== ??? ==");
					System.out.println("It is pitch black in here. "		
							+ "You really have fallen into a pit and died. (I tried to warn you.)");
					darkWarning = Dark.DEAD;
					return;
				}
				System.out.println("\n== ??? ==");
				System.out.println("It is pitch black in here. "		
						+ "You will probably fall into a pit and die.");
				darkWarning=Dark.WARNING;
				return;
			}
		}
		darkWarning=Dark.OKAY; //you are no longer in a dark room.
		System.out.println("\n== " + rm.getTitle() + " ==");
		if (!rm.hasVisited() || look) {
			System.out.println("" + rm.getDesc());		
			for (String s : rm.items){
				//make sure you don't print out blank lines (e.g. in treasury);
				if (itemList.get(s).descrRoom.trim() != "")				
					System.out.println(itemList.get(s).descrRoom);
			}		
			rm.visit();
		}	
	}

	Item itemPresent(String itemName) {
		if ((inventory.contains(itemName))) {
			return itemList.get(itemName);
		}
		Room r = roomList.get(currentRoom);
		if (r.items.contains(itemName)) {
			return itemList.get(itemName);
		}
		return null;
		
	}
	void lookAtObject(String itemName){
		//is item in inventory
		if ((inventory.contains(itemName))) {
			Item it = itemList.get(itemName);

			if (it.isActivated()) System.out.println(it.descrActive);
			else System.out.println(it.descrLook);
			return;
		}
		//is item in current room?
		Room r = roomList.get(currentRoom);
		if (r.items.contains(itemName)) {
			Item q = itemList.get(itemName);
			if (q.isActivated()) System.out.println(q.descrActive);
			else if(q.isOpen) {
				System.out.print("The " + itemName + " is open ");
				if (q.itemContained.equals("")) System.out.println("and it is empty.");	
				else System.out.println("and it contains a " + q.itemContained);							
			}
			else System.out.println(q.descrLook);			
			return;
		}
		System.out.println("That object does not exist (here).");	
	}

	void readObject(String itemname) {
		Item z = itemPresent(itemname);
		if (z == null) {
			System.out.println("There is no " + itemname + " in this location, nor in your inventory.");
			return;			
		}
		if (z.descrRead.length() > 0)
			System.out.println("The " + itemname + " says: " + z.descrRead);
		else
			System.out.println("There is no writing on the " + itemname +".");
	}

	void eatItem(String itemname) {
		if (itemname.equals("")){
			System.out.println("eat what?");
			return;
		}
		//is item in current room? eat that item first.
		Room r = roomList.get(currentRoom);
		if (r.items.contains(itemname)) {			
			if (! player.eat(itemList.get(itemname))) return;				
			r.items.remove(itemname);
			return;				
		}
		//is item in inventory:
		for (String s : inventory) {
			if (s.equals(itemname)) {
				if (! player.eat(itemList.get(itemname))) return;				
				inventory.remove(itemname);
				return;
			}
		}
		System.out.println("There is no " + itemname + " here.");
	}

	void startingMessage() {
		currentRoom = "clearing";
		String startingText = "\n\n" + player.name + ". You wake up in a forest clearing.\n "
				+ "The birds are sinning and the sky is shining.";
		System.out.println(startingText);
	}

	void sleep() {
		if (currentRoom.equals("clearing")) {
			if (player.getHealth() < 75) {
				System.out.println("You have a much needed nap");
				player.heal(7);
			} else {
				System.out.println("You're too wide awake to sleep");
			}
		} else {
			System.out.println("It doesn't feel safe to sleep here.");			
		}

	}

	void printHelp() {
		System.out.println("\n*******************************************************************************");
		System.out.println("You're in a strange land and have to complete a quest/puzzle to get home again.\n"
				+ "Try simple commands to do things. You can move in the cardinal directions\n"
				+ " as well as vertically by typing in the appropriate word."
				+ "\nOther common adventure game commands work here too: look, inventory, move, take, drop ...");
		System.out.println("*******************************************************************************");
	}
	
	void takeAll() {
		Room rm = roomList.get(currentRoom);
		int index=0;
		while(rm.items.size() > 0 && index < rm.items.size()) {
			if (inventory.size() > INVSIZE) {
				System.out.println("Your inventory is full. Drop something first.");
				return;
			}
			String eachItem = rm.items.get(index);
			//handle items that are in room that cannot be picked up (or you'll get infinite loops)
			if (!itemList.get(eachItem).isCarryable) {
				index++;
				continue;
			}
			takeObject(eachItem);
		}
		System.out.println("Everything in the room has been added to your backpack.");
	}

	void takeObject(String itemName) {
		if (inventory.size() > INVSIZE) {
			System.out.println("Your inventory is full. Drop something first.");
			return;
		}

		//handle "take all"
		if (itemName.equals("all")) {
			takeAll();
			return;
		}
		
		//see if item is in current room.
		Room r = roomList.get(currentRoom);
		if (! r.items.contains(itemName)) {		
			System.out.println("There is no " + itemName + " here.");
			return;
		}
		//see if item is carryable
		if (! itemList.get(itemName).isCarryable) {
			System.out.println("You can't take that!");
			return;
		}
		//move item from room to inventory
		r.items.remove(itemName);
		inventory.add(itemName);
		System.out.println("You add the " + itemName + " to your backpack.");		

	}

	void takeObject(String itemName, String container) {
		Room r = roomList.get(currentRoom);
		if (! r.items.contains(container)) {		
			System.out.println("There is no " + container + " here.");
			return;
		}
		
		Item it2 = itemList.get(container);
		if (! it2.isOpen) {
			System.out.println("Sorry, the " + container + " is not open.");
			return;
		}
		if (it2.itemContained.equals("")) {
			System.out.printf("The %s is empty.%n", container);
			return;
		}
		if (it2.itemContained.equals(itemName)) {
			//itemList.get(container).itemContained = "";
			if (inventory.size() > INVSIZE) {
				System.out.println("Your inventory is full. Drop something first.");
				return;
			}
			inventory.add(itemName);
			it2.itemContained = "";	//TODO does this remove it from the item??
			System.out.println("You add the " + itemName + " to your backpack.");	

		} else {
			System.out.printf("There is no %s is in the %s.%n", itemName, container);
		}				
	}
	
	void dropObject(String item) {
		if (item.equals("all")) {
			while(inventory.size() > 0) {
				String nextItem = inventory.get(0);
				inventory.remove(nextItem);
				roomList.get(currentRoom).items.add(nextItem);				
			}
			System.out.println("You drop everything!");
			return;			
		}
		
		
		if (inventory.contains(item)) {
			inventory.remove(item);
			roomList.get(currentRoom).items.add(item);
			System.out.println("You drop the " + item);
		} else {
			System.out.println("You do not have " + item + " in your backpack.");
		}
			
	}

	void showInventory() {
		//show health, hunger, status, inventory
		System.out.println("\n*******************************************************************************");
		System.out.printf("Stats for %s: \tHealth=%s\t\tFood=%s\t\tTurns=%s%n" ,
				player.name, player.getHealth(), player.getFoodPoints(), turns);
		for (int i=0; i < inventory.size(); i++) {
			System.out.println((char)(i+97) + ") " + inventory.get(i));
		}
		if (inventory.size() == 0) System.out.println("You have nothing in your backpack");
		System.out.println("*******************************************************************************");
	}


	/* This method will move an item
	 * It must be in the current room and not be carryable.
	 * Items can reveal an object when they are moved. This only happens once.
	 * An item can also have a method that is called when it is moved.
	 */
	void moveItem(String itemName){
		//is item in current room?
		Room r = roomList.get(currentRoom);
		if (r.items.contains(itemName)) {
			Item it = itemList.get(itemName);
			if (!it.isCarryable) {	//can only move non-carryable things				
				if (! it.revealItem.isEmpty())	{ //reveal object
					System.out.println(it.moveDesc);
					//check if new revealed object is already in room:
					if (! roomList.get(currentRoom).items.contains(it.revealItem))
						roomList.get(currentRoom).items.add(it.revealItem);
					//stop object from being moved again (revealing new objects).					
					//it.revealItem = "";
					//itemList.put(itemName, it);
					itemList.get(itemName).revealItem = ""; //this should update the item in the list.
					//itemList.get(itemName).moveMethod = "";
				} else {
					System.out.printf("You move the %s.%n", itemName);					
				}
				if (it.moveMethod.startsWith("m_")) runMethod(it.moveMethod);
			}
			else System.out.println("You can't move that object.");			
			return;
		}
		System.out.println("That object does not exist (here).");	
	}
	
	void closeObject(String itemName) {
		Room r = roomList.get(currentRoom);
		if (!r.items.contains(itemName)) {
			System.out.println("That object does not exist (here).");
			return;
		}
		Item it = itemList.get(itemName);
		if (!it.isContainer) {
			System.out.println("You cannot close that item.");
			return;
		}
		if (it.isOpen) {
			System.out.println("You close the " + itemName);
			it.isOpen = false;
		} else {
			System.out.println("The " + itemName + " is already closed.");
		}
	}
	
	//TODO: what objects get activated? (by opening?)
		//rock: hit rock with hammer or open rock with hammer, or smash rock with hammer
		//flashlight: activated in switch statement
		//lake (in black_lake room)
		//hammer
		//bell
		void activate(String itemName) {
			Room r = roomList.get(currentRoom);
			//exit if it is not in the room and not in the inventory
			if (! r.items.contains(itemName)) {
				if (!(inventory.contains(itemName))) {
					System.out.println("You don't have " + itemName + " in your inventory.");
					return;
				}
			}
			Item it = itemList.get(itemName);
			if (it.activatedMethod.equals("") && it.descrActive.equals(""))	{
				System.out.println("You don't know how to use this.");
				return;
			}
			it.setActivate(true);	
			if (it.activatedMethod.startsWith("a_")) {
				runMethod(it.activatedMethod);
			}
			System.out.println(it.descrActive);
		}
	void openStuff(String w2, String w3, String w4)
	{
		//we know that w2 is not empty.
		
		//open A, open A with B			
		if (w3.equals("with") && w4.length() > 0) {
			//openObject(w2, w4);
			openObject(w2);
		} else if (w2.contains("door")) {
			openDoor(w2);
		}	
		else {
			openObject(w2);
			//else activate(word2);
		}
	}
	void openObject(String itemName) {
		//TODO: FIXME This means that you can't open things in your inventory
		
		Room r = roomList.get(currentRoom);
		if (!r.items.contains(itemName)) {
			System.out.println("That object does not exist (here).");
			return;
		}
		Item it = itemList.get(itemName);		
		if (!it.isContainer) {
			System.out.println("You cannot open that item.");
			return;
		}
		if (it.isOpen) {
			System.out.print("The " + itemName + " is already open ");
			if (it.itemContained.equals("")) {
				System.out.println("and it is empty.");	
			} else {
				System.out.println("and it contains a " + it.itemContained);
			}
			return;
		}
		String tool = it.openRequires;
		if (tool.equals("")) {
			it.isOpen = true;
			System.out.printf("You open the %s and it contains a %s.%n", itemName, it.itemContained);
		} else {
			if (inventory.contains(tool)) {
				System.out.printf("You open the %s with the %s.%n", itemName, tool);
				System.out.printf("It contains a %s.%n", it.itemContained);	
				it.isOpen = true;
			} else {
				System.out.printf("You need a %s to open this.%n", tool);
			}
		}		
	}
	
	/*void openObject(String itemName, String tool) {		
		openObject(itemName);
	}*/
	
	void openDoor(String itemName) {
		if (currentRoom.equals("cave2") || currentRoom.equals("cave3") ) {
			if (itemList.get("door2").isOpen == false) {
				System.out.println("You open the door.");
				itemList.get("door2").isOpen = true;
			} else {
				System.out.println("The door is already open.");
			}			
		}
	}
	
	void closeDoor(String itemName) {
		//FIXME
		System.out.println("You close the door.");
		System.out.println("The door is already closed.");		
	}
	
	void ringBell(String itemName) {
		
		if (! itemName.equals("bell")) {
			System.out.println("You can't ring that.");
			return;			
		}
		if (! inventory.contains("bell")) {
			System.out.println("You don't have a bell to ring.");
			return;
		}
		if (! currentRoom.equals("cave2")) {
			System.out.println("The bell doesn't work in this room/location.");
			return;			
		}
		if (! itemList.get("bell").isActivated()) {
			System.out.println("The bell will not work without an emerald clapper.");
			return;
		}
		
		System.out.println("You ring the bell and a beautiful liquid sound fills the cave.\n"
				+ "Everything shimmers and you find yourself back home - with the emerald bell still in your hand.\n"
				+ "\n\n **** Thanks for playing. ****");
		System.exit(0);
	}
	
	/**************************************************
	 * This will run the methods stored in variables
	 * for any part of the program.
	 * r_  = room method
	 * m_  = move item method
	 * a_  = activate method
	 ************************************************/
	void runMethod(String md) {		
		md = md.substring(2);
		try {
			methodMap.get(md).callMe();
		} catch(Exception e) {
			System.out.println(e.toString());
		}

		//this doesn't, but I don't know why
		//		try {
		//			AdventureMain.class.getMethod(md).invoke(null);
		//		} catch(Exception e) {
		//			System.out.println(e.toString());
		//		}
	}

	/* add all methods to map, along with key */
	void addMethodMap() {
		methodMap.put("fallFromTree", new Command(){public void callMe() { fallFromTree(); } });		
		methodMap.put("getFromLake", new Command(){public void callMe() { getFromLake(); } });
		methodMap.put("moveLever", new Command(){public void callMe() { moveLever(); } });
		methodMap.put("moveLeaves", new Command(){public void callMe() { moveLeaves(); } });
		methodMap.put("cave3Door", new Command(){public void callMe() { cave3Door(); } });
		methodMap.put("useHammer", new Command(){public void callMe() { useHammer(); } });
		//methodMap.put("moveDoor", new Command(){public void callMe() { moveDoor(); } });		
		//methodMap.put("moveRock2", new Command(){public void callMe() { moveRock2(); } });
	}

	void fallFromTree(){
		System.out.println("You fall from the tree and break a leg");
		player.injury(15);
		currentRoom = "forest1";
		lookAtRoom(false);
	}

	void getFromLake() {
		System.out.println("You feel around in the dark water and pull up a small metal object.");
		System.out.println("It's a key! The key has been added to your backpack.");
		roomList.get("black_lake").items.add("key");
		takeObject("key");
	}
	
	void moveLever() {
		System.out.println("The ground trembles. You hear a deep grinding noise."
				+ "\nThe room shakes and part of it opens."
				+ "\nYou hear falling rocks. Dust is choking you.");
		player.injury(15);
		//make lever immoveable.
		Item z = new Item("The lever looks totally worn out.");	
		z.descrRoom = "A black metal lever on the wall has a plaque under it.";
		z.isCarryable = false;
		z.moveDesc = "Nothing happens when you move the lever now.";		
		itemList.put("lever", z);		
		
		//change exits. Maybe open up a new room too?		
		Room r = new Room("[Former] Entrance to cave",
				"This was where you entered the cave. The eastern exit is totally blocked off with rock from the earthquake."
				+ "\nThe cave continues to the west");
		r.setExits("", "","cave2", "", "","");
		roomList.put("cave1",r);
	}
	
	void moveLeaves() {
		System.out.println("[cough][cough] These leaves are too dusty.");
		player.injury(10); //for choking dust
		if (roomList.get(currentRoom).items.contains("hammer")) player.injury(10);		
	}
	
	//this is the door between cave2 and cave3
	void cave3Door() {
		if (currentRoom.equals("cave2")) {
			if (itemList.get("door2").isOpen) {
				System.out.println("You walk through the doorway.");
				currentRoom = "cave3";
				lookAtRoom(false);
				return;
			} else {
				System.out.println("There is a wooden door blocking your way and it is closed.");
				return;
			}
		}
		if (currentRoom.equals("cave3")) {
			if (itemList.get("door2").isOpen) {
				System.out.println("You walk through the doorway.");
				currentRoom = "cave2";
				lookAtRoom(false);
				return;
			} else {
				System.out.println("There is a wooden door blocking your way and it is closed.");
				return;
			}
		}
	}
	
	void useHammer() {
		Room r = roomList.get(currentRoom);
		if (r.items.contains("\"rock\"")) {
			System.out.println("The rock breaks open revealing a golden bell.");
			roomList.get(currentRoom).items.remove("\"rock\""); //	remove "rock"
			roomList.get(currentRoom).items.add("bell");			
		} else {
			System.out.println("Nothing happens.");
		}
	}
	
	/*	
	void moveLeaves() {		
		System.out.println("You kick the leaves around. Hey, there's a shiny key in the leaves.");
		if (! roomList.get("clearning").items.contains("key"))
			roomList.get("clearing").items.add("key");		
		Item it = itemList.get("leaves");
		it.moveMethod = "";
		itemList.put("leaves", it);

	}	

	void moveDoor() {
		System.out.println("With great effort you move the door a few inches.\n"
				+ "You see a small package under the door.");
		//check if package is already in room:
		if (! roomList.get("treasury").items.contains("package"))
			roomList.get("treasury").items.add("package");
		//stop door from being moved again.
		Item it = itemList.get("door");
		it.moveMethod = "";
		itemList.put("door", it);
	}
	 */
}
