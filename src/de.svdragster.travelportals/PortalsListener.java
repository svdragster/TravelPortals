package de.svdragster.travelportals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;








import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.potion.PotionEffectType;
import net.canarymod.api.world.DimensionType;
import net.canarymod.api.world.World;
import net.canarymod.api.world.WorldManager;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.effects.SoundEffect;
import net.canarymod.api.world.effects.SoundEffect.Type;
import net.canarymod.api.world.position.Direction;
import net.canarymod.api.world.position.Location;
import net.canarymod.chat.Colors;
import net.canarymod.chat.TextFormat;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.PlayerMoveHook;
import net.canarymod.plugin.PluginListener;
import net.visualillusionsent.utils.PropertiesFile;

public class PortalsListener implements PluginListener {

	public static final String DIR = "config/travelportals/";
	public static final String PROPERTIES = DIR + "travelportals.properties";
	public static final String PORTALS = DIR + "portals.txt";
	public static final String PERMISSION_ADMIN = "portals.admin.";
	public static final String PERMISSION_ADMIN_EDIT = "edit"; // If the player is allowed to change portals of others
	public static final String PERMISSION_PLAYER = "portals.player.";
	public static final String PERMISSION_USE = "use";
	public static final String PERMISSION_DESTROY = "destroy";
	public static final String PERMISSION_CREATE =  "create";
	public static final String PERMISSION_NAME =  "name";
	public static final String PERMISSION_DESTINATION =  "destination";
	public static final String PERMISSION_CHECKFORUPDATES =  "checkforupdates";
	private static final String USER_AGENT = "canary_minecraft";
	public static final String VERSION = new TravelPortals().getVersion();
	
	public static boolean CheckForUpdates = true; //default is true
	public static boolean Potioneffects = true; //default is true
	public static boolean Soundeffects = true; //default is true
	
	public static ArrayList<Link> Portals = new ArrayList<Link>();

	public static int BlockId = 49;
	
	static TravelPortals travelPortals = new TravelPortals();
	
	public static void StorePortals() {
		StringBuilder builder = new StringBuilder();
		File file = new File(PORTALS);
		File dir = new File(DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (int i=0; i<Portals.size(); i++) {
			Link link = Portals.get(i);
			String name = link.getName();
			String x = link.getX() + "";
			String y = link.getY() + "";
			String z = link.getZ() + "";
			String world = link.getWorld();
			String owner = link.getOwner();
			String destination = link.getDestination();
			String str = name + "=" + x + ";" + y + ";" + z + ";" + world + ";" + owner + ";" + destination;
			builder.append(str + "\n");
		}
		
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(builder.toString());
			bw.close();
			Portals.clear();
			travelPortals.LogInfo("Saved Portals.");
		} catch (IOException e) {
			travelPortals.LogException("There was an error saving the portals.");
			e.printStackTrace();
		}
		
	}
	
	public static void LoadBlocks() {
		File file = new File(PORTALS);
		File dir = new File(DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Read through the file and put the contents into an arraylist
		 */
		BufferedReader br = null; 
		ArrayList<String> lines = new ArrayList<String>();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(PORTALS));
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		for (int i=0; i<lines.size(); i++) {
			String name = lines.get(i).split("=")[0];
			String value = lines.get(i).split("=")[1];
			int x = Integer.parseInt(value.split(";")[0]);
			int y = Integer.parseInt(value.split(";")[1]);
			int z = Integer.parseInt(value.split(";")[2]);
			String world = value.split(";")[3];
			String owner = value.split(";")[4];
			String destination = value.split(";")[5];
			Link link = new Link(x, y, z, world);
			link.setName(name);
			link.setOwner(owner);
			link.setDestination(destination);
			Portals.add(link);
		}
		
	}
	
	public static void LoadProperties() {
		File file = new File(PROPERTIES);
		File dir = new File(DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		PropertiesFile prop = new PropertiesFile(PROPERTIES);
		if (!prop.containsKey("CheckForUpdates")) {
			prop.setBoolean("CheckForUpdates", true);
		}
		if (!prop.getBoolean("CheckForUpdates")) {
			CheckForUpdates = false;
		}
		if (!prop.containsKey("SoundEffects")) {
			prop.setBoolean("SoundEffects", true);
		}
		if (!prop.getBoolean("SoundEffects")) {
			Soundeffects = false;
		}
		if (!prop.containsKey("PotionEffects")) {
			prop.setBoolean("PotionEffects", true);
		}
		if (!prop.getBoolean("PotionEffects")) {
			Potioneffects = false;
		}
		prop.save();
	}
	
	public Link getLinkFromName(String name) {
		for (int i=0; i<Portals.size(); i++) { // Test all Links if they have such a name
			Link link = Portals.get(i);
			if (link.hasName()) {
				if (link.getName().equals(name)) {
					return link;
				}
			}
		}
		return null;
	}
	
	public Link getLink(String world, Location loc) {
		for (int i=0; i<Portals.size(); i++) { // Iterate through all existing portals
			Link link = Portals.get(i);
			if (link.getY() == loc.getY()) { // If the location has the same height as the link
				if (link.getWorld().equals(world)) { // Same world?
					if (Math.abs(link.getX() - loc.getX()) <= 1 && Math.abs(link.getZ() - loc.getZ()) <= 1) { // Check if the distance is smaller than 1
						return link;
					}
				}
			}
		}
		return null; // If there is no Zelda* close to the location return null.      *Link
	}
	
	@HookHandler
	public void onPlayerMove(PlayerMoveHook hook) {
		Player player = hook.getPlayer();
		if (player.getWorld().getBlockAt(player.getPosition()).getType().equals(BlockType.Water)) { // Check if the player is in a portal (here: water)
			if (player.hasPermission(PERMISSION_PLAYER + PERMISSION_USE) || player.hasPermission(PERMISSION_ADMIN + PERMISSION_USE)) { // Permission check
				Link link = getLink(player.getWorld().getName(), player.getLocation()); // hook.getTo() is the same as player.getLocation()
				if (link != null) {
					if (link.hasDestination()) {
						Link destination = getLinkFromName(link.getDestination());
						if (destination != null) {
							// This will spawn the player in the middle of the block
							double toaddx = 0.5;
							double toaddz = 0.5;
							if (destination.getX() < 0) {
								toaddx = 0.5;
							}
							if (destination.getZ() < 0) {
								toaddz = 0.5;
							}
							Direction direction = getDirection(new Location(destination.getX(), destination.getY(), destination.getZ()));
							int dir = 180; // north is 180
							if (direction != null) {
								if (direction.equals(Direction.NORTH)) {
									dir = 180;
								} else if (direction.equals(Direction.EAST)) {
									dir = -90;
								} else if (direction.equals(Direction.SOUTH)) {
									dir = 0;
								} else if (direction.equals(Direction.WEST)) {
									dir = 90;
								}
							}
							String worldname = destination.getWorld();
							WorldManager manager = Canary.getServer().getWorldManager();
							//if (manager.worldExists(worldname)) {
								World world = null;
								//Canary.getServer().broadcastMessage(manager.worldExists(worldname) + ", " + manager.worldIsLoaded(worldname, DimensionType.NORMAL));
								if (manager.worldIsLoaded(worldname, DimensionType.NORMAL)) {
									world = manager.getWorld(worldname, false);
								} else {
									player.message(Colors.GRAY + "Loading world, please wait...");
									world = manager.loadWorld(worldname, DimensionType.NORMAL);
								}
								if (world != null) { // Basically it shouldn't be null, but who knows. Just making sure.
									if (Potioneffects) {
										player.addPotionEffect(PotionEffectType.CONFUSION, 140, 1);
									}
									player.teleportTo(destination.getX()+toaddx, destination.getY(), destination.getZ()+toaddz, 0, dir, world); // destination.getWorld() will actually teleport the player to the world
									if (Soundeffects) {
										SoundEffect effect = new SoundEffect(Type.ENDERMAN_PORTAL, player.getX(), player.getY(), player.getZ(), 1.0f, 0.6f);
										player.getWorld().playSound(effect);
									}
								} else {
									player.notice("There was an error. (world '" + worldname + "' is null)");
									travelPortals.LogException(player.getName() + " tried using a portal that is null ('" + worldname + "')!!!");
								}
							/*} else {
								player.notice("This portal is pointing to a world(called '" + worldname + "') that does not exist!");
								new TravelPortals().LogException(player.getName() + " tried using a portal leading to a nonexistent world called " + worldname + "!");
							}*/
							
						}
					}
				}
			}
		}
	}
	
	public Direction getDirection(Location loc) {
		World world = loc.getWorld(); //Get the world
		Direction direction = null;
		
		//Checking the blocks around the location
		//right side
		Block right = world.getBlockAt((int) loc.getX()+1, (int) loc.getY(), (int) loc.getZ());
		if (right.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.EAST;
		}
		Block righttop = world.getBlockAt((int) loc.getX()+1, (int) loc.getY()+1, (int) loc.getZ());
		if (righttop.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.EAST;
		}
		//left side
		Block left = world.getBlockAt((int) loc.getX()-1, (int) loc.getY(), (int) loc.getZ());
		if (left.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.WEST;
		}
		Block lefttop = world.getBlockAt((int) loc.getX()-1, (int) loc.getY()+1, (int) loc.getZ());
		if (lefttop.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.WEST;
		}
		//front side
		Block front = world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()+1);
		if (front.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.SOUTH;
		}
		Block fronttop = world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()+1);
		if (fronttop.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.SOUTH;
		}
		//back side
		Block back = world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()-1);
		if (back.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.NORTH;
		}
		Block backtop = world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()-1);
		if (backtop.getType().equals(BlockType.WoodenDoor)) {
			direction = Direction.NORTH;
		}
		//return the direction. If there is no door it will be null
		return direction;
	}
	
	public ArrayList<Block> GetBlocks(Location loc) {
		if (loc == null) {
			return null;
		}
		World world = loc.getWorld(); //Get the world
		int BlockAmount = 0;
		boolean door = false;
		boolean doortop = false;
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		//Checking the blocks around the location
		//right side
		Block right = world.getBlockAt((int) loc.getX()+1, (int) loc.getY(), (int) loc.getZ());
		if (right.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(right);
		} else if (right.getType().equals(BlockType.WoodenDoor)) {
			door = true;
			blocks.add(right);
		}
		Block righttop = world.getBlockAt((int) loc.getX()+1, (int) loc.getY()+1, (int) loc.getZ());
		if (righttop.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(righttop);
		} else if (righttop.getType().equals(BlockType.WoodenDoor)) {
			doortop = true;
			blocks.add(righttop);
		}
		//left side
		Block left = world.getBlockAt((int) loc.getX()-1, (int) loc.getY(), (int) loc.getZ());
		if (left.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(left);
		} else if (left.getType().equals(BlockType.WoodenDoor)) {
			door = true;
			blocks.add(left);
		}
		Block lefttop = world.getBlockAt((int) loc.getX()-1, (int) loc.getY()+1, (int) loc.getZ());
		if (lefttop.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(lefttop);
		} else if (lefttop.getType().equals(BlockType.WoodenDoor)) {
			doortop = true;
			blocks.add(lefttop);
		}
		//front side
		Block front = world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()+1);
		if (front.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(front);
			
		} else if (front.getType().equals(BlockType.WoodenDoor)) {
			door = true;
			blocks.add(front);
		}
		Block fronttop = world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()+1);
		if (fronttop.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(fronttop);
		} else if (fronttop.getType().equals(BlockType.WoodenDoor)) {
			doortop = true;
			blocks.add(fronttop);
		}
		//back side
		Block back = world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()-1);
		if (back.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(back);
		} else if (back.getType().equals(BlockType.WoodenDoor)) {
			door = true;
			blocks.add(back);
		}
		Block backtop = world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()-1);
		if (backtop.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(backtop);
		} else if (backtop.getType().equals(BlockType.WoodenDoor)) {
			doortop = true;
			blocks.add(backtop);
		}
		//top
		Block top = world.getBlockAt((int) loc.getX(), (int) loc.getY()+2, (int) loc.getZ());
		if (top.getType().equals(BlockType.Obsidian)) {
			BlockAmount++;
			blocks.add(top);
		}
		//If the location is surrounded by obsidian BlockAmount will be 7
		//new TravelPortals().LogInfo("Debug: BlockAmount = " + BlockAmount);
		if (BlockAmount >= 7) {
			if (door && doortop) {
				if (world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()).getType().equals(BlockType.Water)) { // Add the water(if there isn't already) inside the portal to the arraylist.
					blocks.add(world.getBlockAt((int) loc.getX(), (int) loc.getY(), (int) loc.getZ()));	
				}
				if (world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()).getType().equals(BlockType.Water)) {
					blocks.add(world.getBlockAt((int) loc.getX(), (int) loc.getY()+1, (int) loc.getZ()));
				}
				return blocks;
			}
		}
		return null;
	}
	
	@HookHandler
	public void onBlockPlace(BlockPlaceHook hook) {
		Player player = hook.getPlayer();
		Block newBlock = hook.getBlockPlaced();
		Block block = newBlock.getWorld().getBlockAt(newBlock.getPosition());
		//Check if the player is destroying a portal
		if (block.getType().equals(BlockType.Water)) {
			for (int i=0; i<Portals.size(); i++) { // Look through every portal existing
				Link link = Portals.get(i);
				Location loc = new Location(link.getX(), link.getY(), link.getZ());
				ArrayList<Block> blocks = GetBlocks(loc);
				if (block != null) {
					if (blocks != null) {
						if (blocks.contains(block)) { // If the player destroyed a block which is part of a portal
							if (player.hasPermission(PERMISSION_ADMIN + PERMISSION_DESTROY) || player.hasPermission(PERMISSION_PLAYER + PERMISSION_DESTROY)) {
								if ((link.hasOwner() && link.getOwner().equals(player.getName())) || player.hasPermission(PERMISSION_ADMIN + PERMISSION_ADMIN_EDIT)) {
									for (int water=0; water<blocks.size(); water++) {
										Block waterBlock = blocks.get(water);
										if (waterBlock.getType().equals(BlockType.Water)) {
											waterBlock.getWorld().setBlockAt(waterBlock.getPosition(), (short) 0); // Remove the water from the portal
										}
									}
									player.notice("Portal '" + link.getName() + "' destroyed!");
								} else {
									hook.setCanceled();
								}
							} else {
								hook.setCanceled();
								player.notice("You do not have permission to destroy a portal.");
							}
						}
					} else {
						travelPortals.LogException("blocks == null, cant create portal.");
					}
				}
			}
		}
		//Check if the player placed a redstone torch to create a new portal
		if (newBlock.getTypeId() == 76) {
			Block torch = newBlock;
			ArrayList<Block> blocks = GetBlocks(torch.getLocation());
			if (blocks != null) {
				if (blocks.size() < 9) {
					player.notice("The portal is not finished yet!");
				} else if (blocks.size() == 9) {
					if (player.hasPermission(PERMISSION_PLAYER + PERMISSION_CREATE) || player.hasPermission(PERMISSION_ADMIN + PERMISSION_CREATE)) {
						torch.getWorld().setBlockAt(torch.getX(), torch.getY(), torch.getZ(), (short) 9);
						torch.getWorld().setBlockAt(torch.getX(), torch.getY()+1, torch.getZ(), (short) 9);
						Link link = new Link(torch.getX(), torch.getY(), torch.getZ(), torch.getWorld().getName());
						link.setOwner(player.getName());
						Portals.add(link);
						player.message(Colors.LIGHT_GREEN + "Created new portal! Use " + Colors.GREEN + "/travelportals " + Colors.LIGHT_GREEN + " for more info");
						if (Soundeffects) {
							SoundEffect effect = new SoundEffect(Type.BLAZE_HIT, player.getX(), player.getY(), player.getZ(), 1.0f, 0.6f);
							player.getWorld().playSound(effect);
						}
					}
				} else {
					player.notice("This portal has already been created.");
				}
			}
		}
	}
	
	@HookHandler
	public void onBlockDestroy(BlockDestroyHook hook) {
		Player player = hook.getPlayer();
		Block block = hook.getBlock();
		if (block.getType().equals(BlockType.Obsidian) || block.getType().equals(BlockType.WoodenDoor)) { // Is it even a block used for portals?
			for (int i=0; i<Portals.size(); i++) { // Look through every portal existing
				Link link = Portals.get(i);
				if (link != null) {
					Location loc = new Location(link.getX(), link.getY(), link.getZ());
					if (block != null) {
						ArrayList<Block> blocks = GetBlocks(loc);
						if (blocks != null) {
							if (!blocks.isEmpty()) {
								if (blocks.contains(block)) { // If the player destroyed a block which is part of a portal
									if (player.hasPermission(PERMISSION_ADMIN + PERMISSION_DESTROY) || player.hasPermission(PERMISSION_PLAYER + PERMISSION_DESTROY)) {
										if ((link.hasOwner() && link.getOwner().equals(player.getName())) || player.hasPermission(PERMISSION_ADMIN + PERMISSION_ADMIN_EDIT)) {
											for (int water=0; water<blocks.size(); water++) {
												Block waterBlock = blocks.get(water);
												if (waterBlock.getType().equals(BlockType.Water)) {
													waterBlock.getWorld().setBlockAt(waterBlock.getPosition(), (short) 0); // Remove the water from the portal
												}
											}
											Portals.remove(link);
											player.notice("Portal '" + link.getName() + "' destroyed!");
										} else {
											hook.setCanceled();
										}
									} else {
										hook.setCanceled();
										player.notice("You do not have permission to destroy a portal.");
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	@HookHandler
	public void onLogin(ConnectionHook hook) {
		if (hook.getPlayer().hasPermission(PERMISSION_ADMIN + PERMISSION_CHECKFORUPDATES)) {
			try {
				String result = sendGet();
				if (result != null) {
					if (!result.isEmpty()) {
						hook.getPlayer().message(result);
						hook.getPlayer().message("Or you can check the forum post.");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String sendGet() throws Exception {
		String MYIDSTART = "svdragster>";
		String MYIDEND = "<svdragster";
		String url = "http://svdragster.dtdns.net/checkupdate.php?version=" + VERSION + "&plugin=travelportals";
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		String result = response.toString();
		if (result.contains(MYIDSTART) && result.contains(MYIDEND)) {
			int endPos = result.indexOf(MYIDEND);
			result = Colors.ORANGE + "<TravelPortals> " + Colors.GREEN + "Update available at: " + Colors.WHITE + result.substring(MYIDSTART.length(), endPos);
		}
		return result;
	}
}
