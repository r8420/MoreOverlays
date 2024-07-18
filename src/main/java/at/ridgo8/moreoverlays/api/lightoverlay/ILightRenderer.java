package at.ridgo8.moreoverlays.api.lightoverlay;

import org.joml.Matrix4f;

public interface ILightRenderer {

    void renderOverlays(ILightScanner scanner, Matrix4f matrix4f);
}
