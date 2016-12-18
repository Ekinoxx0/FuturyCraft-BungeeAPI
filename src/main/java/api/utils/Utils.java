package api.utils;

import java.io.*;

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

    public static void delete(File f) throws IOException
    {
        if (f.isDirectory())
        {
            File[] files = f.listFiles();
            if (files == null)
                throw new NullPointerException("Failed to delete file: " + f);
            for (File c : files)
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
