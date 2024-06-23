package de.mrjulsen.blockbeats.util;

import java.time.Duration;
import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;

public class Utils {
    
    public static MutableComponent trans(String cat, String key) {
        return TextUtils.translate("gui." + BlockBeats.MOD_ID + "." + cat + "." + key);
    }
    
    public static MutableComponent trans(String cat, String key, Object... args) {
        return TextUtils.translate("gui." + BlockBeats.MOD_ID + "." + cat + "." + key, args);
    }

    public static final String formatDurationMs(long s) {
        Duration duration = Duration.ofMillis(s);
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        if (HH <= 0) {            
            return String.format("%02d:%02d", MM, SS);
        }        
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

}
