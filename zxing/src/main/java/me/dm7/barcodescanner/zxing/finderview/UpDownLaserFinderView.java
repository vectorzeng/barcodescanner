package me.dm7.barcodescanner.zxing.finderview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;

import me.dm7.barcodescanner.core.ViewFinderView;

/**
 * Created by vectorzeng on 2018/5/4.
 */

public class UpDownLaserFinderView extends ViewFinderView {
    private static final String TG = "UpDownLaserFinderView";
    //每次draw的时间间隔
    private long animDelay = 10;
    private static float DURATION_ONCE_LASER = 2000.0f; //激光一次扫苗动画需要的时长
    private static final int POINT_SIZE = 10;

    public final Paint PAINT = new Paint();
    public UpDownLaserFinderView(Context context) {
        super(context);
        init();
    }

    public UpDownLaserFinderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init(){
        PAINT.setColor(Color.WHITE);
        PAINT.setAntiAlias(true);
        setSquareViewFinder(true);
        setLaserEnabled(true);
    }

    private int heightTargetBmp = 0;
    private Rect targetRect = null;
    private float speed;

    public void computeAnimation(Bitmap bmp, Rect framingRect){
        if(targetRect == null) {
            heightTargetBmp = (int) (bmp.getHeight() * ((float) framingRect.width() / bmp.getWidth()));
            int top = framingRect.top - heightTargetBmp;
            targetRect = new Rect(framingRect.left + 2, top, framingRect.right + 2, framingRect.top);
            speed = (framingRect.height() - targetRect.height())/ DURATION_ONCE_LASER;
        }
    }


    private float dy = 0;
    private long lastTime;
    @Override
    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();
        if(framingRect == null){
            return;
        }
        Bitmap bmp = getLaserBmp();
        if(bmp == null){
            return;
        }
        computeAnimation(bmp, framingRect);
        if(targetRect == null || speed < 0){
            Log.e(TG, "drawLaser error " + targetRect + ", " + speed);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if(lastTime > 0){
            dy += speed*(currentTime - lastTime) ;
        }
        lastTime = currentTime;
        if(dy > framingRect.height()){
            dy = 0;
        }

        canvas.clipRect(framingRect, Region.Op.INTERSECT);
        canvas.save();
        canvas.translate(0,dy);
        canvas.drawBitmap(bmp, null, targetRect, PAINT);
        canvas.restore();

        postInvalidateDelayed(animDelay,
                framingRect.left - POINT_SIZE,
                framingRect.top - POINT_SIZE,
                framingRect.right + POINT_SIZE,
                framingRect.bottom + POINT_SIZE);
    }

    private Bitmap laserBmp;
    public Bitmap getLaserBmp(){
        return laserBmp;
    }

    public void setDrawInterval(long interval){
        animDelay = interval;
    }

    /**
     * 激光图片
     * @param laserBmp
     */
    public void setLaserBmp(Bitmap laserBmp) {
        this.laserBmp = laserBmp;
    }

    /**
     * 激光一次上下扫描时长
     * @param durationOnceLaser
     */
    public static void setDurationOnceLaser(float durationOnceLaser) {
        DURATION_ONCE_LASER = durationOnceLaser;
    }
}
