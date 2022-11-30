package org.j3d.lm.demo1;

import java.io.File;

import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.Resource;
import org.j3d.Utils;
import org.j3d.lm.DualTextureMaterial;
import org.j3d.lm.DualTextureRenderer;
import org.j3d.lm.LightMapper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {
    
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(1000, 700, true);

            DualTextureRenderer renderer = new DualTextureRenderer();
            Mesh mesh = renderer.getMesh();
            DualTextureMaterial material =  game.getAssets().getResources().manage(new DualTextureMaterial());
            MeshPart part = new MeshPart(mesh, 7);
            Font font = game.getAssets().load(IO.file("assets/pics/font.fnt"));
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f offset = new Vector3f(100, 100, 100);
            Vector3f zero = new Vector3f();
            Matrix4f projection = new Matrix4f();
            Matrix4f view = new Matrix4f();

            material.texture = game.getAssets().load(IO.file("assets/pics/tile1.png"));
            
            part.material = material;
            mesh.addMeshPart(part);

            renderer.push(0, -128, 0, -128, 0, 0, 0, 0);
            renderer.push(0, -128, 0, +128, 0, 4, 0, 0);
            renderer.push(0, +128, 0, +128, 4, 4, 0, 0);
            renderer.push(0, +128, 0, -128, 4, 0, 0, 0);

            renderer.push(0, -64, 25, -64, 0, 0, 0, 0);
            renderer.push(0, -64, 25, +64, 0, 2, 0, 0);
            renderer.push(0, +64, 25, +64, 2, 2, 0, 0);
            renderer.push(0, +64, 25, -64, 2, 0, 0, 0);

            renderer.push(0, -64, 25, -64, 0, 0, 0, 0);
            renderer.push(0, -64, 25, +64, 0, 2, 0, 0);
            renderer.push(0, +64, 25, +64, 2, 2, 0, 0);
            renderer.push(0, +64, 25, -64, 2, 0, 0, 0);

            part.pushFace(0, 1, 2, 3);
            part.pushFace(4, 5, 6, 7);
            part.pushFace(11, 10, 9, 8);

            part.trim();
            part.bufferIndices();
            part.bufferVertices(false);
            part.calcBounds();

            mesh.calcBounds();

            File file = IO.file("assets/lightmaps/demo1.png");

            if(file.exists()) {
                file.delete();
            }

            if(new LightMapper().map(file, 64, 64, 128, 3, renderer, 2)) {
                material.texture2 = game.getAssets().load(IO.file("assets/lightmaps/demo1.png"));
                material.texture2.bind();
                material.texture2.toLinear(true);
                material.texture2.unBind();
            } else {
                System.out.println("failed to allocate light map");
                material.texture2 = null;
            }

            GLFW.glfwSwapInterval(1);

            while(game.run()) {
                projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
                view.identity().lookAt(offset, zero, up);

                game.beginRenderTarget();

                Utils.clear(0, 0, 0, 1);
                mesh.render(projection, view);

                game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
                game.getSpritePipeline().beginSprite(font);
                game.getSpritePipeline().push(font, "FPS=" + game.getFPS() + ", RES=" + Resource.getInstances(), 5, 10, 10, 1, 1, 1, 1);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();

                game.nextFrame();

                if(game.isButtonDown(0)) {
                    Utils.rotateOffsetAndUp(offset, up, game);
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
