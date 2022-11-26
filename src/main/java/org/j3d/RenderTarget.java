package org.j3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class RenderTarget extends Resource {

    private final Texture[] textures;
    private final int framebuffer;
    private final int renderbuffer;
    private final int[] viewport = new int[4];

    public RenderTarget(int width, int height, PixelFormat ... formats) throws Exception {
        textures = new Texture[formats.length];
        for(int i = 0; i != textures.length; i++) {
            textures[i] = new Texture(null, width, height, formats[i], null);
        }
        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        renderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER,
                renderbuffer);
        int[] drawBuffers = new int[textures.length];
        for (int i = 0; i != drawBuffers.length; i++) {
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, drawBuffers[i], GL11.GL_TEXTURE_2D, textures[i].getTexture(), 0);
        }
        GL30.glDrawBuffers(drawBuffers);

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("failed to allocate render target");
        }
    }

    public int getTextureCount() {
        return textures.length;
    }

    public Texture getTexture(int i) {
        return textures[i];
    }

    public void begin() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL11.glViewport(0, 0, textures[0].width, textures[0].height);
    }

    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    @Override
    public void destroy() throws Exception {
        GL30.glDeleteFramebuffers(framebuffer);
        GL30.glDeleteRenderbuffers(renderbuffer);
        for(Texture texture : textures) {
            texture.destroy();
        }
        super.destroy();
    }
}
