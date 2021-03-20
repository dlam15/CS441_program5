package com.example.myapi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


public class VisualizeDesign extends View {

    private byte[] bytes;
    private Rect viewSize = new Rect();
    private Paint paint = new Paint();

    /* Code based on
        http://android-er.blogspot.com/2015/02/create-audio-visualizer-for-mediaplayer.html
    */

    public VisualizeDesign(Context context) {
        super(context);
        init();
    }

    public VisualizeDesign(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizeDesign(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        bytes = null;
    }

    public void updateVisualizer(byte[] bytesIn){
        bytes = bytesIn;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null) {
            return;
        }

        float split = (bytes.length - 1)/5;
        viewSize.set(0, 0, getWidth(), getHeight());
        for (int i = 0; i < bytes.length - 1; i++) {
            float left = viewSize.width() * i / (bytes.length - 1);
            float top = viewSize.height() / 2
                    + ((byte) (bytes[i] + 128)) * (viewSize.height() / 2) / 128;
            float right = viewSize.width() * (i + 1) / (bytes.length - 1);
            float bottom = viewSize.height() / 2
                    - ((byte) (bytes[i] + 128)) * (viewSize.height() / 2) / 128;

            float j =left%split;
            if(left < split){
                //purple (143,0,242)
                paint.setARGB(180, (int)(j*(-111/(split))+143),(int)(j*(53/(split))+0),(int)(j*(-8/(split))+242));
            }else if(left < split*2){
                //blue (32,53,234)
                paint.setARGB(180, (int)(j*(-32/(split))+32),(int)(j*(154/(split))+53),(int)(j*(17/(split))+234));
            }else if(left < split*3){
                //light blue (0,207,251)
                paint.setARGB(180, (int)(j*(92/(split))+0),(int)(j*(48/(split))+207),(int)(j*(-251/(split))+251));
            }else if(left < split*4) {
                //light green (92,255,0)
                paint.setARGB(180, (int)(j*(161/(split))+92), (int)(j*(-4/(split))+255), (int)(j*(0/(split))+0));
            }else if(left < split*5){
                //yellow (253,251,0)
                paint.setARGB(180, (int)(j*(2/(split))+253),(int)(j*(-239/(split))+251),(int)(j*(18/(split))+0));
            }else{
                //red (255,12,18)
                paint.setARGB(180, (int)(j*(-112/(split))+255),(int)(j*(-12/(split))+12),(int)(j*(224/(split))+18));
            }
            canvas.drawRect(left,top,right,bottom,paint);
        }
    }

}
