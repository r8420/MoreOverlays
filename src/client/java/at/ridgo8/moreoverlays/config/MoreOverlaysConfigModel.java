package at.ridgo8.moreoverlays.config;
import at.ridgo8.moreoverlays.MoreOverlays;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;
import io.wispforest.owo.ui.core.Color;


@Modmenu(modId = MoreOverlays.MOD_ID)
@Config(name = MoreOverlays.MOD_ID + "-config-fabric", wrapperName = "MoreOverlaysConfig")
public class MoreOverlaysConfigModel {

    @SectionHeader("lightoverlay")
    @RangeConstraint(min = 0, max = 255)
    public int light_UpRange = 4;
    @RangeConstraint(min = 0, max = 255)
    public int light_DownRange = 16;
    @RangeConstraint(min = 0, max = 255)
    public int light_HRange = 16;
    public boolean light_IgnoreLayer = false;
    public boolean light_IgnoreSpawnList = false;
    public boolean light_SimpleEntityCheck = false;
    @RangeConstraint(min = 0, max = 16)
    public int light_SaveLevel = 1;

    @SectionHeader("chunkbounds")
    @RangeConstraint(min = 0, max = 255)
    public int chunk_EdgeRadius = 1;
    public boolean chunk_ShowMiddle = true;

    @SectionHeader("rendersettings")
    public Color render_chunkEdgeColor = Color.ofRgb(0xFF0000);
    public Color render_chunkGridColor = Color.ofRgb(0x00FF00);
    public Color render_chunkMiddleColor = Color.ofRgb(0xFFFF00);
    @RangeConstraint(min = 0d, max = 16d)
    public double render_chunkLineWidth = 1.5;
    public Color render_spawnAColor = Color.ofRgb(0xFF0000);
    public Color render_spawnNColor = Color.ofRgb(0xFFFF00);
    @RangeConstraint(min = 0d, max = 16d)
    public double render_spawnLineWidth = 2;

    @SectionHeader("itemsearch")
    public boolean search_enabled = true;
    public boolean search_searchCustom = true;
    public Color search_searchBoxColor = Color.ofRgb(0xFFFF00);
    public Color search_filteredSlotColor = Color.ofRgb(0x000000);
    @RangeConstraint(min = 0d, max = 1d)
    public double search_filteredSlotTransparancy = 0.5d;
}