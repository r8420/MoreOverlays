package at.ridgo8.moreoverlays.config;


public class Config {

    public static int light_UpRange;
    public static int light_DownRange;
    public static int light_HRange;
    public static boolean light_IgnoreLayer;
    public static boolean light_IgnoreSpawnList;
    public static boolean light_SimpleEntityCheck;
    public static int light_SaveLevel;
    public static boolean light_FinishedMigration;

    public static int chunk_EdgeRadius;
    public static boolean chunk_ShowMiddle;

    public static int render_chunkEdgeColor;
    public static int render_chunkGridColor;
    public static int render_chunkMiddleColor;
    public static double render_chunkLineWidth;
    public static int render_spawnAColor;
    public static int render_spawnNColor;
    public static double render_spawnLineWidth;

    public static boolean search_enabled;
    public static boolean search_searchCustom;
    public static int search_searchBoxColor;
    public static int search_filteredSlotColor;
    public static double search_filteredSlotTransparancy;


    public static void initialize() {
        // Settings for the light / mobspawn overlay
        light_UpRange = 4;
        light_DownRange = 16;
        light_HRange = 16;
        light_IgnoreLayer = false;
        light_IgnoreSpawnList = false;
        light_SimpleEntityCheck = false;
        light_SaveLevel = 1;
        light_FinishedMigration = true;

        // Settings for the chunk bounds overlay
        chunk_EdgeRadius = 1;
        chunk_ShowMiddle = true;

        // General render settings.\nLine thickness, Colors, ...
        render_chunkEdgeColor = 0xFF0000;
        render_chunkGridColor = 0x00FF00;
        render_chunkMiddleColor = 0xFFFF00;
        render_chunkLineWidth = 1.5;
        render_spawnAColor = 0xFF0000;
        render_spawnNColor = 0xFFFF00;
        render_spawnLineWidth = 2;

        // Settings for the search overlay
        search_enabled = true;
        search_searchCustom = true;
        search_searchBoxColor = 0xFFFF00;
        search_filteredSlotColor = 0x000000;
        search_filteredSlotTransparancy = 0.5F;
    }
}
