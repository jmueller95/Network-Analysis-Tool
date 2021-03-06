package main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * <h1>This defines the UserSettjngs class</h1>
 * <p>
 * The userSettings hashmap contains all userSettings at all times. Every setting that is supposed to be included into the
 * UserSettings should be defined here and also added to the addUserSettings.
 * This makes sure that when a dummy file is required that all settings are always present.
 * </p>
 *
 * @see util.SaveAndLoadOptions
 *
 */
public class UserSettings {

    //Strings
    private static File defaultFileChooserLocation = new File(System.getProperty("user.home"));
    private static String font;
    private static String nodesColor;

    //booleans
    private static boolean isDarkTheme = false;
    private static boolean isDefaultDirectoryLocation = true;
    private static boolean isUnconnectedEdgesHiddenOnStartup = false;

    //contains all userSettings
    public static HashMap<String, Object> userSettings = new HashMap<>();

    /**
     * adds the user settings to the userSettings.txt file
     * creates a dummy file if there's no file yet
     */
    public static void addUserSettings(){
        File dummyFile = new File(GlobalConstants.USER_SETTINGS_FILEPATH);
        boolean isFileCreated = false;
        try {
            isFileCreated = dummyFile.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        //add Strings
        userSettings.put("defaultFileChooserLocation", defaultFileChooserLocation.getAbsolutePath());
        userSettings.put("font", font);
        userSettings.put("nodesColor", nodesColor);

        //add Booleans
        userSettings.put("theme", isDarkTheme);
        userSettings.put("isDefaultFileChooserLocation", isDefaultDirectoryLocation);
        userSettings.put("isUnconnectedEdgesHiddenOnStartup", isUnconnectedEdgesHiddenOnStartup);
    }

    /**
     * toggles between the light and the dark theme
     *
     * @param theme
     * @return
     */
    public static String determineLightOrDarkTheme(boolean theme){
        if (theme){
            return GlobalConstants.DARKTHEME;
        } else {
            return GlobalConstants.LIGHTTHEME;
        }
    }

}
