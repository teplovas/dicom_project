package OpenGLFeatures;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLX13;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class FBO {

	private Display display;
	private long window;
   //window size
   final int WIDTH = 640;
   final int HEIGHT = 480;
   
   //texture
   Texture box;
   Texture light;
   
   //frame buffer
   int fbo;
   int depthbuffer;
   int fb_texture;
   
   public void start(){
      setUpDisplay();
      setUpGL();
      loadTextures();
      createFBO();
      
      while(!Display.isCloseRequested()){
         render();
         Display.update();
         Display.sync(60);
      }
   }
   
   private void render(){
      //render to fbo
      glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);
      glPushAttrib(GL_VIEWPORT_BIT);
      glViewport(0, 0, WIDTH, HEIGHT);
      
      glBindTexture(GL_TEXTURE_2D, light.getTextureID());
      //drawTexture(0, 0, 64, 64);
      glBindTexture(GL_TEXTURE_2D, 0);
      
      glPopAttrib();
      
      glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
      
      //render the texture
      glBindTexture(GL_TEXTURE_2D, fb_texture);
      //drawTexture(0, 0, WIDTH, HEIGHT);
      glBindTexture(GL_TEXTURE_2D, 0);
      
   }
   
   private void drawTexture(float x, float y, int width, int height){
      glBegin(GL_QUADS);
      glTexCoord2f(0f, 0f);
      glVertex2f(x, y);
      
      glTexCoord2f(1f, 0f);
      glVertex2f(x + width, y);
      
      glTexCoord2f(1f, 1f);
      glVertex2f(x + width, y + height);
      
      glTexCoord2f(0f, 1f);
      glVertex2f(x, y + height);
      glEnd();
   }
   
   private void createFBO() {
      
      //frame buffer
      fbo = glGenFramebuffersEXT();
      glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);
      
      //depth buffer
      depthbuffer = glGenRenderbuffersEXT();
      glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthbuffer);
      
      //allocate space for the renderbuffer
      glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT, WIDTH, HEIGHT);
      
      //attach depth buffer to fbo
      glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthbuffer);
      
      //create texture to render to
      fb_texture = glGenTextures();
      glBindTexture(GL_TEXTURE_2D, fb_texture);
      glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, WIDTH, HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer)null);
      
      //attach texture to the fbo
      glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, fb_texture, 0);
      
      //check completeness
      if(glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) == GL_FRAMEBUFFER_COMPLETE_EXT){
         System.out.println("Frame buffer created sucessfully.");
      }
      else
         System.out.println("An error occured creating the frame buffer.");
      
   }
   
   
   

   private void loadTextures(){
      try {
         box = TextureLoader.getTexture("PNG", new FileInputStream(new File("res/grass.png")), GL_NEAREST);
         light = TextureLoader.getTexture("PNG", new FileInputStream(new File("res/test.png")), GL_NEAREST);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   private void setUpGL() {
      glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
      glMatrixMode(GL_MODELVIEW);
      glViewport(0, 0, Display.getWidth(), Display.getHeight());
      glEnable(GL_TEXTURE_2D);
      glEnable(GL_BLEND);
      glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
      glClearColor(0f, 0f, 0f, 0f);
      
   }


   private void setUpDisplay() {
      try {
    	  
         Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
         Display.setTitle("FBO Test");
         //Display.setParent(arg0);
         Display.create();
    	  //GLX13.glXMakeContextCurrent(display.);
      } catch (LWJGLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      glfwMakeContextCurrent(this.window);
      GL.createCapabilities();
   }


   public static void main(String[] args){
      new FBO().start();
   }
}