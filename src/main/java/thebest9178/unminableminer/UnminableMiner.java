package thebest9178.unminableminer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.commands.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnminableMiner extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();

	@Override
	public void onInitialize() {
		LOG.info("Initializing UnminableMiner");

        Modules.get().add(new thebest9178.unminableminer.modules.UnminableMiner());

        Commands.add(new thebest9178.unminableminer.commands.UnminableHelp());

	}

    @Override
    public String getPackage() {
        return "thebest9178.unminableminer";
    }
}
