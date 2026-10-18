package cn.gcte.xts;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetXTSAddition implements ModInitializer {
	public static final String MOD_ID = "carpet-xts-addition";

	public static final Logger LOGGER = LoggerFactory.getLogger("Carpet XTS Addition");

	@Override
	public void onInitialize() {
        XTSExtensionServer.register();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
