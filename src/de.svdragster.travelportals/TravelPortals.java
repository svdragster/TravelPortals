package de.svdragster.travelportals;


import org.apache.logging.log4j.Level;

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;

public class TravelPortals extends Plugin {

	public void LogInfo(String str) {
		getLogman().log(Level.INFO, str);
	}
	
	public void LogException(String str) {
		getLogman().error(str);
	}
	
	@Override
	public void disable() {
		//PortalsListener listener = new PortalsListener();
		PortalsListener.StorePortals();
	}

	@Override
	public boolean enable() {
		try {
			Canary.commands().registerCommands(new PortalsCommands(), this, false);
		} catch (CommandDependencyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new PortalsListener();
		PortalsListener.LoadBlocks();
		new PortalsListener();
		PortalsListener.LoadProperties();
		Canary.hooks().registerListener(new PortalsListener(), this);
		return true;
	}

}
