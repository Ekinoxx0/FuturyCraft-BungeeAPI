package api.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by loucass003 on 15/12/16.
 */
public class Utils {

    public static String readFile(File f)
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                line = br.readLine();
            }

            String everything = sb.toString();
            br.close();
            return everything;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer isNumeric(String str)
    {
        try
        {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }
}
