package org.twd2.Ripple;

import java.nio.IntBuffer;
import java.util.ArrayList;

import java.io.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import static org.lwjgl.openal.AL10.*;
import org.lwjgl.util.WaveData;

public class Sound {
	
	public static Sound i; 
	
	private ArrayList<Integer> sources = new ArrayList<Integer>();
	
	public Sound() {
		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		alGetError();
		i=this;
	}
	
	public void destory() {
		for(int i=0;i<sources.size();++i) {
			remove(sources.get(i));
		}
		AL.destroy();
	}
	
	public int source(String file) {
		try {
			return source(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int source(InputStream file) {
		int lastError;
		
		//create 1 buffer and 1 source
        IntBuffer buffers = BufferUtils.createIntBuffer(1);
        IntBuffer sources = BufferUtils.createIntBuffer(1);
         
        // al generate buffers and sources
        buffers.position(0).limit(1);
        alGenBuffers(buffers);
		if((lastError = alGetError()) != AL_NO_ERROR) {
            return lastError;
        }
 
        sources.position(0).limit(1);
        alGenSources(sources);
        if((lastError = alGetError()) != AL_NO_ERROR) {
        	return lastError;
        }
        
        // load wave data from buffer
        WaveData wavefile = WaveData.create(new BufferedInputStream(file));
        
        //copy to buffers
        alBufferData(buffers.get(0), wavefile.format, wavefile.data, wavefile.samplerate);
         
        //unload file again
        wavefile.dispose();  
        
        if((lastError = alGetError()) != AL_NO_ERROR) {
        	return lastError;
        }        
 
        //set up source input
        alSourcei(sources.get(0), AL_BUFFER, buffers.get(0));
        if((lastError = alGetError()) != AL_NO_ERROR) {
        	return lastError;
        }        
         
        //lets loop the sound
        //alSourcei(sources.get(0), AL_LOOPING, AL_FALSE);
        alSourcei(sources.get(0), AL_LOOPING, AL_TRUE);
        if((lastError = alGetError()) != AL_NO_ERROR) {
        	return lastError;
        }        
         
        //play source 0
        //alSourcePlay(sources.get(0));
        //if((lastError = alGetError()) != AL_NO_ERROR) {
        //	return lastError;
        //}
        this.sources.add(sources.get(0));
        return sources.get(0);
        //return AL_NO_ERROR;
	}
	
	public void play(int sourceID) {
		alSourcePlay(sourceID);
	}
	
	public void stop(int sourceID) {
		alSourceStop(sourceID);
	}
	
	public void remove(int sourceID) {
		alDeleteSources(sourceID);
	}
	
}
