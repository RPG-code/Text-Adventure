package adventureGame;

import java.util.ArrayList;
import java.util.HashMap;

class Item {
	
	//TODO: need synonyms for items.
	//String synonym = "water, lake";
	//No, use the hashmap property ...
	//TODO: need another paper with suggestions for getting out of the maze.
	
	String descrRoom = ""; //description of item to meld with room description. 
	//if this is empty. then the item does not show up in the list of items in the room description (perhaps because it is already there - eg. black lake)	
	String descrLook = ""; //description of item when you look at it.
	
	String descrRead = "";	//what is displayed when you read the item. If empty, then there is nothing that you can read on it.
	/* food related properties */
	private int foodpoints = 0;		//how many food points the food has
	
	/* tool related properties */
	private boolean isActivated = false;
	String descrActive = ""; //the "description" changes to this when it is activated
	String activatedMethod = ""; //method to be run upon activation. begins with "a_"
	
	/* moving related properties */
	boolean isCarryable = true;
	String moveDesc = "";
	String moveMethod = ""; //method to be run upon moving. begins with "m_"
	String revealItem = ""; //something is revealed when it is moved.	
	
	/* container related properties */
	boolean isContainer = false;
	String itemContained = "";
	String openMethod = ""; //method to be run upon opening
	boolean isOpen = false;  //used for containers (and windows)
	String openRequires = "";	//object needed in order to open this item
	
	//basic constructor
	Item(String descr) {
		this.descrLook = descr;		
	}
	
	/* all getter and setter methods here */
	int getFoodPoints() {
		return this.foodpoints;
	}
	
	void setActivate(boolean b) {
		this.isActivated = b;
	}
	
	boolean isActivated() {
		return this.isActivated;
	}
	
	/*
	 Make each item in the game and add it to the list.
	 Also set the original room here, then once rooms are made, 
	 (That way a room never contains an object that is not in this list.)
	 This static method precludes having subclasses of items for Container and Doors etc.
	 since all objects added are items.
	*/
	
	static void setUpItems(HashMap<String,Item> itemList, HashMap<String,Room> roomList) {
		Item z = new Item("a ham sandwich with mustard");
		z.descrRoom = "You smell a sandwich nearby.";
		z.foodpoints = 10;
		itemList.put("sandwich",z);
		roomList.get("path1").items.add("sandwich");
		
		z = new Item("a sharp knife with a bone handle");
		z.descrRoom = "There is a knife embedded in the tree trunk.";
		itemList.put("knife",z);
		roomList.get("tree1").items.add("knife");
		
		z = new Item("A carefully folded piece of paper with writing on it.");
		z.descrRoom= "Some pieces of paper have blown under a bush.";
		z.descrRead = "To return to your world, you need to put the emerald into the silver bell"
				+ " and then ring it inside the crystal cave.";
		itemList.put("paper",z);
		roomList.get("maze2").items.add("paper");
		
		z = new Item("boring large rocks");
		z.descrRoom = "There are some loose rocks here.";
		z.isCarryable = false;
		itemList.put("rock",z);
		roomList.get("cave1").items.add("rock");
		roomList.get("cave2").items.add("rock");
//		or should this be called rocks?		
		
//		THIS DOES NOT WORK!
//		it cannot be called "rock2" because who will type that?
//		There must be a special method that check to see if the rock is in room X. If it is, then treat it as rock2		
		z = new Item("This rock seems hollow. Hmmm... is it even a real rock? If only you had a hammer.");
		z.descrRoom = "There are some loose rocks here.";
		z.activatedMethod = "hit rock with hammer to open it";
		z.isCarryable = false;
		z.isActivated = false;
		z.activatedMethod = "a_smashRock";
		itemList.put("\"rock\"",z);
		roomList.get("black_lake").items.add("\"rock\"");				
		
		z = new Item("a very useful flashlight: waterproof, knurled aluminum, trilithium batteries");
		z.descrActive = "the flashlight is glowing brightly";
		z.descrRoom = "Someone dropped a flashlight at the side of the path.";
		itemList.put("flashlight",z);
		roomList.get("clearing").items.add("flashlight");
		
		z = new Item("a securely locked heavy metal chest");
		z.descrRoom = "A chest sits on a newly revealed shelf.";
		z.isCarryable = false;
		z.isContainer = true;
		z.isOpen = false;
		z.itemContained = "emerald";
		z.openRequires = "key";
		z.openMethod = "";
		itemList.put("chest",z);
		//add to a room
		
		z = new Item("a package of lembas. Someone from Middle Earth was here.");
		z.foodpoints = 20;
		z.descrRoom = "A dirty package wrapped in leaves is wedged under the door.";
		z.activatedMethod = "a_openLembas";
		itemList.put("package",z);
		itemList.put("lembas",z);
		
		z = new Item("You can't reach the glowing/shiny crystals, and,\n unfortuntately, you couldn't do anything with them even if you could.");
		z.isCarryable = false;
		itemList.put("crystals",z);
		roomList.get("treasury").items.add("crystals");
		roomList.get("cave2").items.add("crystals");
		
		z = new Item("The lake is black and wet"); //FIXME : you can OPEN LAKE!!!
		z.isCarryable = false;
		z.isContainer = true;
		z.descrRoom = "";		
		z.activatedMethod = "a_getFromLake";
		z.descrActive = "The lake is black and wet";
		z.revealItem = "key"; 
		itemList.put("lake",z);
		itemList.put("water",z); //as long as there is no other water in the game
		roomList.get("black_lake").items.add("lake");
		
		//make the key!
		z = new Item("The silver key has a impatient multidimensional appearance.");
		z.descrRoom = "A shiny key lies nearby.";
		itemList.put("key",z);		
		
		z = new Item("a heavy metal door with debris behind it");
		z.descrRoom = "The heavy door is blocking most of the western exit, but you can still squeeze past.";
		z.isCarryable = false;
		z.moveDesc = "With great effort you move the door a few inches.\n "
				+ "You see a small package under the door.";
		z.revealItem = "package";
		itemList.put("door",z);
		roomList.get("treasury").items.add("door");
		
		//or hide the hammer here - kicking it injures you - lose health
		z = new Item("a pile of dusty leaves");
		z.descrRoom = "Piles of deciduous leaves lie on the ground.";
		z.isCarryable = false;
		z.moveDesc = "You kick the leaves around seriously hurting your foot and raising some dust."
				+ " \nAfter you finish coughing, you see a hammer in the leaves.";
		z.revealItem = "hammer";
		z.moveMethod = "m_moveLeaves"; //use this to reduce health.
		itemList.put("leaves", z);
		roomList.get("forest2").items.add("leaves");
		
		z = new Item("a hammer from Canadian Tire! It has a wooden handle.");
		z.descrRoom = "A hammer lies on the ground.";
		z.activatedMethod = "a_useHammer";
		itemList.put("hammer", z);
				
		z = new Item("A beautiful emerald!");
		z.descrRoom = "You see a green gemstone.";		
		itemList.put("emerald",z); //this is in the chest 
		
		z = new Item("This lever has never been used. It gives off a menacing air.");
		z.descrRoom = "A black metal lever on the wall has a plaque under it.";
		z.isCarryable = false;
		z.moveDesc = "Feeling foolhardy, you yank the lever down.";
		z.moveMethod = "m_moveLever";
		z.revealItem = "chest";
		itemList.put("lever", z);
		roomList.get("secret_room").items.add("lever");
		
		z = new Item("The plaque says: \"Warning: Earthquake Generator\n\t\tAuthorized use only (for terraforming)\"");
		z.isCarryable = false;
		z.descrRead = z.descrLook;
		itemList.put("plaque", z);
		roomList.get("secret_room").items.add("plaque");
			
		z = new Item("A golden bell. It is missing a jeweler clapper.");
		z.descrActive = "A golden bell with an emerald clapper.";
		z.descrRoom = "A golden bell lies on the floor.";
		itemList.put("bell", z);
		
	}
	
}
