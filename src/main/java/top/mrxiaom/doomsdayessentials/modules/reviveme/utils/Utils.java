package top.mrxiaom.doomsdayessentials.modules.reviveme.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Utils {
    public static String getIp() {
        URL myIP;
        try {
            myIP = new URL("http://api.externalip.net/ip/");
            BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
            return in.readLine();
        } catch (Exception var6) {
            try {
                myIP = new URL("http://myip.dnsomatic.com/");
                BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
                return in.readLine();
            } catch (Exception var5) {
                try {
                    myIP = new URL("http://icanhazip.com/");
                    BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
                    return in.readLine();
                } catch (Exception var4) {
                    var4.printStackTrace();
                    return null;
                }
            }
        }
    }
}
