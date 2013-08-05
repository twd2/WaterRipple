package org.twd2.Ripple;

public class RippleEngine {

	double[] buf1,buf2;
	int PoolWidth,PoolHeight;
	
	public RippleEngine(int PoolWidth, int PoolHeight) {
		this.PoolWidth=PoolWidth;
		this.PoolHeight=PoolHeight;
		buf1=new double[PoolWidth * PoolHeight];
		buf2=new double[PoolWidth * PoolHeight];
	}
	
	public void reset() {
		for(int i=0;i<buf1.length;++i)
			buf1[i]=0;
		for(int i=0;i<buf2.length;++i)
			buf2[i]=0;
	}
	
	public void next() {
		 for(int i=PoolWidth+1; i<PoolWidth*PoolHeight-PoolWidth-1; i++)
		 {
			 //Wave energy spread
			 buf2[i] = ((buf1[i-1]+
			             buf1[i+1]+
			             buf1[i-PoolWidth]+
			             buf1[i+PoolWidth]+
			             buf1[i-1-PoolWidth]+
			             buf1[i+1-PoolWidth]+
			             buf1[i-1+PoolWidth]+
			             buf1[i+1+PoolWidth])
			            /4.0/*>>2*/)
			           - (buf2[i]);
			 
			 //Wave energy attenuation
			 //buf2[i] -= buf2[i]/32;//(/64.0*2.3333);//>>5;
			 buf2[i] *= 1.0 - ( 1/32.0*2.3333);
			 if(Math.abs(buf2[i])<0.0000001) buf2[i]=0.0;
			 //buf2[i] -= buf2[i]*0.0618;//(/64.0*2.3333);//>>5;
			 //buf2[i] -=  Math.log(Math.sin(buf2[i]) +1) * (buf2[i]>0?1:-1);
		 }
		 
		 //Swap buffer
		 double[] tmp;
		 tmp=buf1;
		 buf1=buf2;
		 buf2=tmp;
	}
	
	public void render(byte[] source_image, byte[] target_image) {
		//target_image=source_image.clone();
		//Render image
	    int xoff, yoff;
	    int k = PoolWidth;
	    for (int i=1; i<PoolHeight-1; i++)
	    {
	        for (int j=0; j<PoolWidth; j++)
	        {
	            //Calc offset
	        	xoff=yoff=0;
	        	
	        	/* Old slow algorithm
	        	 * if(!Quick_refraction_processing) {
		        	//if(Math.abs(buf1[k-1]-buf1[k+1])>=0.000)
		        	{
			        	double tan_x=(buf1[k-1]-buf1[k+1])/2*5;
			        	double sin_x=tan_x/Math.sqrt(tan_x*tan_x+1);
			        	double sin_r=sin_x/(4.0/3.0);
			        	double tan_r=sin_r/Math.sqrt(1-sin_r*sin_r);
			        	xoff=(int)(tan_x*(tan_x-tan_r)/(1+tan_x*tan_r));
		        	}
		        	//if(Math.abs(buf1[k-PoolWidth]-buf1[k+PoolWidth])>=0.000)
		        	{
			        	double tan_x=(buf1[k-PoolWidth]-buf1[k+PoolWidth])/2*5;
			        	double sin_x=tan_x/Math.sqrt(tan_x*tan_x+1);
			        	double sin_r=sin_x/(4.0/3.0);
			        	double tan_r=sin_r/Math.sqrt(1-sin_r*sin_r);
			        	yoff=(int)(tan_x*(tan_x-tan_r)/(1+tan_x*tan_r));
		        	}
	        	} else {*/
	        	
	        		//0.44 is a experience value. 
	        		xoff = (int)((buf1[k-1]-buf1[k+1])*0.44);
	        		yoff = (int)((buf1[k-PoolWidth]-buf1[k+PoolWidth])*0.44);
	        		
	        	//}
	           

	            //判断坐标是否在窗口范围内
	            //if ((i+yoff )< 0 ) {k++; continue;}
	            //if ((i+yoff )> PoolHeight) {k++; continue;}
	            //if ((j+xoff )< 0 ) {k++; continue;}
	            //if ((j+xoff )> PoolWidth ) {k++; continue;}

	            //Calc buffer offset
	            int pos1, pos2;
	            pos1=PoolWidth*4*(i+yoff) + 4*(j+xoff);
	            pos2=PoolWidth*4*i + 4*j;

	            //Copy pixels
	            for (int d=0; d<4; d++) 
	            	if(pos1>0 && pos1<source_image.length && pos2>0 && pos2<target_image.length) {
	            		if(d==3) {
	            			//Skip the alpha
	            			pos1++;
	            			target_image[pos2++]=(byte)255;
	            		} else {
	            			
	            			//Calc light
	            			double x = buf1[k]/5;
	            			if (x > 255) x = 255;
	            			if(x < 0) x = 0;
	            			double c = (int)source_image[pos1++] & 0xff; //Must AND 255, or it might be negative
	            			
	            			//Adjust the brightness
	            			double brightness=-(x/255); 
	            			c=(255-c)*brightness +c;
	            			if (c > 255) c = 255;
	            			if(c < 0) c = 0;
	            			
	            			target_image[pos2++]=(byte)(c);
	            		}
	            	}
	            k++;
	        }
	    }
	}
	
	public void renderGrayscale(byte[] target_image) {
		//target_image=source_image.clone();
		//Render image
	    int k = 0;
	    for (int i=0; i<PoolHeight; i++)
	    {
	        for (int j=0; j<PoolWidth; j++)
	        {
	            //Calc buffer offset
	            int pos;
	            pos=PoolWidth*4*i + 4*j;

	            //Copy pixels
	            for (int d=0; d<4; d++) 
	            	if(pos>0 && pos<target_image.length) {
	            		if(d==3) {
	            			target_image[pos++]=(byte)255;
	            		} else {
	            			//Gray value
	            			double x = buf1[k]/10 + 128;
	            			if (x > 255) x = 255;
	            			if(x < 0) x = 0;
	            			
	            			target_image[pos++]=(byte)(x);
	            		}
	            	}
	            k++;
	        }
	    }
	}
	
	public void dropStone(int x, int y, int size, int weight) {
		if((x+size)>PoolWidth ||(y+size)>PoolHeight||(x-size)<0||(y-size)<0)
			return;
	    for(int posx=Math.max(0,x-size);posx<Math.min(x+size, PoolWidth);++posx)
	        for(int posy=Math.max(0,y-size);posy<Math.min(y+size, PoolHeight);++posy)
	            if((posx-x)*(posx-x) + (posy-y)*(posy-y) < size*size)
	                buf1[PoolWidth*posy+posx] -= weight;
	}
	
}
