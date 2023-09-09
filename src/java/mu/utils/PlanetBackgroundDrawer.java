package mu.utils;

import arc.graphics.*;
import arc.graphics.gl.FrameBuffer;
import arc.util.Nullable;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.graphics.g3d.PlanetRenderer;

import static mindustry.Vars.*;
import static arc.Core.*;

public class PlanetBackgroundDrawer{
    static private @Nullable FrameBuffer backgroundBuffer;
    static private final PlanetRenderer planets = new PlanetRenderer();

    static public int size = Math.max(graphics.getWidth(), graphics.getHeight());
    static public float drawSize = Math.max(camera.width, camera.height);
    static public Boolean changed = true;

    static public void update(){
        changed = true;
    }

    static public Texture draw(PlanetParams params){
        if (params == null) {
            return new Texture(0, 0);
        }

        size = Math.max(graphics.getWidth(), graphics.getHeight());

        boolean resized = false;
        if(backgroundBuffer == null){
            resized = true;
            backgroundBuffer = new FrameBuffer(size, size);
        }

        if(changed || resized || backgroundBuffer.resizeCheck(size, size)){
            changed = false;

            backgroundBuffer.begin(Color.clear);

            //override some values
            params.viewW = size;
            params.viewH = size;
            params.alwaysDrawAtmosphere = true;
            params.drawUi = false;


            planets.render(params);

            backgroundBuffer.end();
        }

        drawSize = Math.max(camera.width, camera.height);
        return backgroundBuffer.getTexture();
    }
}