package cn.gcte.xts;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetXTSAddition implements ModInitializer {
	public static final String MOD_ID = "carpet-xts-addition";

	public static final Logger LOGGER = LoggerFactory.getLogger("Carpet XTS Addition");

	@Override
	public void onInitialize() {
        XTSExtensionServer.register();
	}

    public static class TempSharedConstants {
        public static final boolean selfSwapIsRelease = true;
        public static final boolean releaseCommandAlwaysDisabled = true;
        public static boolean allowControlRealPlayer = false;
        public static boolean everyTPCanBack = false;
    }
}
