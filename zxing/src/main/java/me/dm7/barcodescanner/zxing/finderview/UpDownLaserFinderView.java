package me.dm7.barcodescanner.zxing.finderview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import me.dm7.barcodescanner.core.ViewFinderView;

/**
 * Created by vectorzeng on 2018/5/4.
 */

public class UpDownLaserFinderView extends ViewFinderView {
    private static final String TG = "UpDownLaserFinderView";
    //每次draw的时间间隔
    private static final long ANIMATION_DELAY = 20;
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
    private float speed = 0f;
    private int dynamicBottom = 0;

    public void initAnimFactor(Rect framingRect){
        if (heightTargetBmp == 0) {
            heightTargetBmp = (int) (laserBmp.getHeight() * ((float) getFramingRect().width() / laserBmp.getWidth()));
        }

        if (targetRect == null){
            targetRect = new Rect(framingRect.left, framingRect.top, framingRect.right,0);
        }

        if (speed == 0f){
            speed = (framingRect.height() - heightTargetBmp)/ DURATION_ONCE_LASER;
        }
    }


    private float dy = 0;
    private long lastTime = 0L;
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
        initAnimFactor(framingRect);

        if(targetRect == null || speed < 0){
            Log.e(TG, "drawLaser error " + targetRect + ", " + speed);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if(lastTime > 0){
            float yOffset = speed * (currentTime - lastTime);
            /**
             * 动态改变targetRect高度，实现第一帧从扫描框顶部开始动画效果
             * 高度确定后再进行移动扫码线
             */
            if(targetRect.height() <= heightTargetBmp) {
                dynamicBottom += yOffset;
                targetRect.set(framingRect.left, framingRect.top, framingRect.right, framingRect.top + dynamicBottom);
            } else {
                dy += yOffset;
            }
        }
        lastTime = currentTime;

        if(dy > (framingRect.height() - heightTargetBmp)){
            dy = 0;
            dynamicBottom = 0;
            targetRect.setEmpty();
        }
        Log.e(TG, "Anim Y : " + dy);

        canvas.save();
        canvas.translate(0,dy);
        canvas.drawBitmap(bmp, null, targetRect, PAINT);
        canvas.restore();

        postInvalidateDelayed(ANIMATION_DELAY,
                framingRect.left - POINT_SIZE,
                framingRect.top - POINT_SIZE,
                framingRect.right + POINT_SIZE,
                framingRect.bottom + POINT_SIZE);
    }

    private Bitmap laserBmp;
    public Bitmap getLaserBmp(){
        return laserBmp;
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
