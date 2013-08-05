package org.twd2.Ripple;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.util.glu.GLU.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


public class Ripple {
	
	public final int WIDTH = 1024;
	public final int HEIGHT= 768;
	
	/** time at last frame */
	long lastFrame;
	
	long lastFPS=0; 
	int fps=0;
	
	int MaxFPS=-1;//60;
	
	ByteBuffer source_buf;
	byte[] source_arr;
	MyImage source_img;
	
	ByteBuffer new_buf;
	byte[] new_arr;
	
	RippleEngine engine;
	
	int texID=0;
	
	long lastRain=0; 
	
	long lastGC=0; 
	
	long lastNext=0; 
	
	Random rand = new Random();
	
	public Ripple() {
		new Sound();
	}

	public void init() {
		System.out.println("Press R to Reset.");
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			
			InputStream ins = this.getClass().getResourceAsStream("resources/favicon.png");
			
			Display.setIcon(IconLoader.load(ins));
			
			//PixelFormat pixel_format=new PixelFormat();
			//ContextAttribs attribs=new ContextAttribs(1,5);
			//Display.create(pixel_format, attribs);
			Display.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initGL();
		initEngine();
	}
	
	public void initGL() {
		// init OpenGL
		glViewport(0, 0, WIDTH, HEIGHT);
		glLoadIdentity();
		glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
		glEnable(GL_TEXTURE_2D);  
		LoadTextures();
	}
	
	public void LoadTextures() {
		texID = TextureLoader.loadPNGTexture(this.getClass().getResourceAsStream("resources/texture.png"), GL_TEXTURE0, this);
		source_img = TextureLoader.loadPNG(this.getClass().getResourceAsStream("resources/texture.png"));
		source_buf=source_img.buffer;
		source_arr=new byte[source_buf.limit()];
		source_buf.get(source_arr);
		source_buf.flip();
		new_arr=source_arr.clone();
	}
	
	public void initEngine() {
		engine=new RippleEngine(source_img.Width, source_img.Height);
		initStone();
	}
	
	public void initStone() {
		//Drop a row of stones
		for(int x=0;x<source_img.Width;++x)
			engine.dropStone(x, 11, 10, 128);
	}
	
	public void rain() {
	    if (getTime() - lastRain > 25) {
	    	engine.dropStone(rand.nextInt(source_img.Width), rand.nextInt(source_img.Height), rand.nextInt(10), rand.nextInt(512));
	    	lastRain = getTime(); //add one second
	    }
	}	
	
	public void GC() {
	    if (getTime() - lastGC > 1000) {
	    	System.gc();
	    	lastGC = getTime(); //add one second
	    }
	}
	
	
	public void clearGL() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void renderGL() {
		
		clearGL();
		
		if (getTime() - lastNext > 16) {
			engine.next();
	    	lastNext = getTime(); //add one second
	    }
		
		engine.render(source_arr, new_arr);
		new_buf = ByteBuffer.allocateDirect(new_arr.length);
		new_buf.put(new_arr, 0, new_arr.length);
		new_buf.flip();
		
		//Refresh texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, source_img.Width, source_img.Height, 0, 
				GL_RGBA, GL_UNSIGNED_BYTE, new_buf);

		glBegin(GL_POLYGON);
			glTexCoord2f(0.0f, 1.0f); glVertex2f(0f, 0f);//the lower left
			glTexCoord2f(1.0f, 1.0f); glVertex2f(WIDTH, 0f); //the lower right
			glTexCoord2f(1.0f, 0f); glVertex2f(WIDTH, HEIGHT);//the upper right
			glTexCoord2f(0.0f, 0f); glVertex2f(0f, HEIGHT);//the upper left
			glTexCoord2f(0.0f, 1.0f); glVertex2f(0f, 0f);//the lower left
			glTexCoord2f(1.0f, 1.0f); glVertex2f((float)source_img.Width, 0f); //the lower right
			glTexCoord2f(1.0f, 0f); glVertex2f((float)source_img.Width, (float)source_img.Height);//the upper right
			glTexCoord2f(0.0f, 0f); glVertex2f(0f, (float)source_img.Height);//the upper left
		glEnd();
		
		engine.renderGrayscale(new_arr);
		new_buf = ByteBuffer.allocateDirect(new_arr.length);
		new_buf.put(new_arr, 0, new_arr.length);
		new_buf.flip();
		
		//Refresh texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, source_img.Width, source_img.Height, 0, 
				GL_RGBA, GL_UNSIGNED_BYTE, new_buf);

		glBegin(GL_POLYGON);
			glTexCoord2f(0.0f, 1.0f); glVertex2f((float)source_img.Width, 0f);//the lower left
			glTexCoord2f(1.0f, 1.0f); glVertex2f((float)source_img.Width*2, 0f); //the lower right
			glTexCoord2f(1.0f, 0f); glVertex2f((float)source_img.Width*2, (float)source_img.Height);//the upper right
			glTexCoord2f(0.0f, 0f); glVertex2f((float)source_img.Width, (float)source_img.Height);//the upper left
		glEnd();
	}
		
	public void update(float delta) {
		updateInput(delta);
	}
	
	private boolean _lastMouse=false;
	public void updateInput(float delta) {
		engine.dropStone(Mouse.getX() % source_img.Width, (-Mouse.getY()+source_img.Height) % source_img.Height, 10, 16);
		if(!_lastMouse && Mouse.isButtonDown(0)){
			engine.dropStone(Mouse.getX() % source_img.Width, (-Mouse.getY()+source_img.Height) % source_img.Height, 10, 128);
		}
		_lastMouse=Mouse.isButtonDown(0);
		
		while (Keyboard.next()) {
		    if (Keyboard.getEventKeyState()) {
		        if (Keyboard.getEventKey() == Keyboard.KEY_R) {
		        	engine.reset();
		        	initStone();
		        }
		    }
		}
	}
	
	public void start() {
		init();
		getDelta();
		while(!Display.isCloseRequested())
		{
			update(getDelta());
			renderGL();
			rain();
			updateFPS();
			Display.update();
			if (MaxFPS>0) {
				Display.sync(MaxFPS);
			}
			GC();
		}
		glDeleteTextures(texID);
		Sound.i.destory();
		Display.destroy();
		System.exit(0);
	}
	
	/**
	 * Get the time in milliseconds
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
 
	    return delta;
	}
	
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		 //Display.setTitle(String.valueOf(getTime()));
	    if (getTime() - lastFPS > 1000) {
	        Display.setTitle("FPS: " + fps); 
	        fps = 0; //reset the FPS counter
	        lastFPS = getTime(); //add one second
	    }
	   fps++;
	}
	
	public void exitOnGLError(String errorMessage) {
		int errorValue = glGetError();
		
		if (errorValue != GL_NO_ERROR) {
			String errorString = gluErrorString(errorValue);
			System.err.println("ERROR - " + errorMessage + ": " + errorString);
			
			if (Display.isCreated()) Display.destroy();
			System.exit(-1);
		}
	}
}
