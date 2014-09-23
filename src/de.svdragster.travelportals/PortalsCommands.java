package de.svdragster.travelportals;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;

public class PortalsCommands implements CommandListener {

	public static final String[] CHANGELOG = {"-1.25 Fixed breaking obsidian throwing an error. ","-1.24 Multiworld should finally be fixed; /travelportals list; Sound effects and Potioneffect", "-1.13 Player heads will rotate into doors direction; /travelportals info; Fixed owner", "-1.02 Fixed Multiworld; Fixed/Added permissions and commands", "-1.01 Fixed checkforupdates notification"};
	
	@Command(aliases = { "travelportals", "travelportal", "travelp", "tportals" },
            description = "Travelportals commands",
            permissions = { "travelportals" },
            toolTip = "/travelportals")
    public void TravelPortalsCommand(MessageReceiver caller, String[] parameters) {
		Player player = Canary.getServer().getPlayer(caller.getName());
		if (parameters.length == 1) {
			player.message(Colors.LIGHT_GRAY + "/travelportals name <name> -- Sets portal name of portal you are standing in");
			player.message(Colors.WHITE + "/travelportals destination <name> -- Sets portal destination of portal you are standing in");
			if (player.hasPermission(PortalsListener.PERMISSION_ADMIN)) {
				player.message(Colors.LIGHT_GRAY + "/travelportals changelog");
				player.message(Colors.WHITE + "/travelportals version");
				player.message(Colors.LIGHT_GRAY + "/travelportals info -- Displays info about portal you are standing in");
			}
			return;
		}
		if (parameters.length > 2) {
			if (parameters[1].equalsIgnoreCase("name")) {
				if (player.hasPermission(PortalsListener.PERMISSION_PLAYER + PortalsListener.PERMISSION_NAME) || player.hasPermission(PortalsListener.PERMISSION_ADMIN)) {
					Link link = new PortalsListener().getLink(player.getWorld().getName(), player.getLocation());
					if (link != null) {
						if ((link.hasOwner() && link.getOwner().equals(player.getName()) || player.hasPermission(PortalsListener.PERMISSION_ADMIN + PortalsListener.PERMISSION_ADMIN_EDIT))) {
							String name = parameters[2];
							if (new PortalsListener().getLinkFromName(name) == null) {						
								link.setName(parameters[2]);
								player.message(Colors.LIGHT_GREEN + "Set name of this portal to " + Colors.GREEN + parameters[2] + Colors.LIGHT_GREEN + "!");
							} else {
								player.notice("There is already a portal with that name. Please choose another!");
							}
						} else {
							player.notice("You are not the owner of this portal.");
						}
					} else {
						player.notice("No portal found! (You have to stay in the portal)");
					}
				}
			}
			if (parameters[1].equalsIgnoreCase("destination")) {
				if (player.hasPermission(PortalsListener.PERMISSION_PLAYER + PortalsListener.PERMISSION_DESTINATION) || player.hasPermission(PortalsListener.PERMISSION_ADMIN + PortalsListener.PERMISSION_DESTINATION)) {
					Link link = new PortalsListener().getLink(player.getWorld().getName(), player.getLocation());
					if (link != null) {
						if ((link.hasOwner() && link.getOwner().equals(player.getName()) || player.hasPermission(PortalsListener.PERMISSION_ADMIN + PortalsListener.PERMISSION_ADMIN_EDIT))) {
							String strDestination = parameters[2];
							Link destination = new PortalsListener().getLinkFromName(strDestination);
							if (destination != null) {
								link.setDestination(strDestination);
								player.message(Colors.LIGHT_GREEN + "Set the destination of the portal " + Colors.GREEN + link.getName() + Colors.LIGHT_GREEN + " to " + Colors.GREEN + strDestination + Colors.LIGHT_GREEN + "!");
							} else {
								player.notice("There is no portal called like this!");
							}
						} else {
							player.notice("You are not the owner of this portal.");
						}
					} else {
						player.notice("No portal found! (You have to stay in the portal)");
					}
				}
			}
		}
		if (parameters.length == 2) {
			if (player.hasPermission(PortalsListener.PERMISSION_ADMIN)) {
				if (parameters[1].equalsIgnoreCase("changelog")) {
					for (int i=0; i<CHANGELOG.length; i++) {
						player.message(CHANGELOG[i]);
					}
				}
				if (parameters[1].equalsIgnoreCase("version")) {
					player.message("TravelPortals version: " + PortalsListener.VERSION + " created by svdragster.");
					String result = null;
					try {
						result = new PortalsListener().sendGet();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (result != null) {
						if (!result.isEmpty()) {
							player.message(result);
						} else {
							player.message("You have the latest version of TravelPortals.");
						}
					} else {
						player.message("You have the latest version of TravelPortals.");
					}
				}
				if (parameters[1].equalsIgnoreCase("list")) {
					int color = 0;
					for (int i=0; i<PortalsListener.Portals.size(); i++) {
						Link link = PortalsListener.Portals.get(i);
						String str;
						if (color == 0) {
							str = Colors.LIGHT_GRAY;
							color = 1;
						} else {
							str = Colors.WHITE;
							color = 0;
						}
						player.message(str + link.getName() + ": x=" + link.getX() + ", y=" + link.getY() + ", z=" + link.getZ() + ", world=" + link.getWorld() + ", owner=" + link.getOwner() + ", destination=" + link.getDestination());
					}
				}
				if (parameters[1].equalsIgnoreCase("info")) {
					Link link = new PortalsListener().getLink(player.getWorld().getName(), player.getLocation());
					if (link.hasName()) {
						player.message(Colors.LIGHT_GRAY + link.getName() + ":");
					}
					if (link.hasOwner()) {
						player.message(Colors.GRAY + "Owner: " + link.getOwner());
					}
					if (link.hasDestination()) {
						player.message(Colors.GRAY + "Destination: " + link.getDestination());
					}
				}
			}
		}
	}
}
