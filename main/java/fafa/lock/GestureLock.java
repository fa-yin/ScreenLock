package fafa.lock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GestureLock extends View {

    private Point[][] points = new Point[3][3];
    private boolean inited = false;//是否初始化过

    private boolean isDraw = false; //是否在绘制状态

    private ArrayList<Point> pointList = new ArrayList<Point>();
    private ArrayList<Integer> passList = new ArrayList<Integer>();
    private Bitmap bitmapPointError;
    private Bitmap bitmapPointPress;
    private Bitmap bitmapPointNormal;

    private float bitmapR;

    private OnDrawFinishedListener listener;
    private float mouseX,mouseY;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Paint pressPaint = new Paint();
    Paint errorPaint = new Paint();
    public GestureLock(Context context) {
        super(context);
    }

    public GestureLock(Context context, AttributeSet attrs) { super(context, attrs);}
    public GestureLock(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}
    @Override
    public boolean onTouchEvent(MotionEvent event){
        mouseX = event.getX();
        mouseY = event.getY();
        int [] ij;
        int i,j;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                resetPoints();
                ij=getSelectedPoint();
                if(ij!=null){
                    isDraw = true;
                    i=ij[0];
                    j=ij[1];
                    points[i][j].state = Point.STATE_PRESS;
                    pointList.add(points[i][j]);
                    passList.add(i*3+j);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isDraw){
                    ij=getSelectedPoint();
                    if(ij!=null){
                        i=ij[0];
                        j=ij[1];
                        if(!pointList.contains(points[i][j])){
                            points[i][j].state = Point.STATE_PRESS;
                            pointList.add(points[i][j]);
                            passList.add(i*3+j);
                        }
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                boolean valid = false;
                if(listener !=null && isDraw){
                    valid = listener.OnDrawFinished(passList);
                }
                if(!valid){
                    for(Point p:pointList){
                        p.state = Point.STATE_ERROR;
                    }
                }
                isDraw = false; //停止绘制
                break;
        }
        this.postInvalidate();
        return true;
    }

    private int[] getSelectedPoint(){//获取选中的点
        Point pMouse = new Point(mouseX,mouseY);    //将手指按下的位置生成一个点
        for(int i=0;i<points.length;i++){
            for(int j=0;j<points[i].length;j++){
                if(points[i][j].distance(pMouse)<bitmapR){
                    int[] result = new int[2];
                    result[0]=i;
                    result[1]=j;
                    return result;
                }
            }
        }
        return null;
    }
    @Override
    protected void onDraw(Canvas canvas) {  //会调用多次
        super.onDraw(canvas);
        if(!inited) //九个点的初始化
            init();
        drawPoints(canvas);
        if(pointList.size()>0){
            Point a = pointList.get(0);
            for(int i=1;i<pointList.size();i++){
                Point b = pointList.get(i);
                drawLine(canvas,a,b);
                a=b;
            }
            if(isDraw){
                drawLine(canvas,a,new Point(mouseX,mouseY));
            }
        }
    }

    private void drawLine(Canvas canvas,Point a,Point b){//绘制连线
        if(a.state ==Point.STATE_PRESS){
            canvas.drawLine(a.x,a.y,b.x,b.y,pressPaint);
        }
        else if(a.state ==Point.STATE_ERROR){
            canvas.drawLine(a.x,a.y,b.x,b.y,errorPaint);
        }
    }
    private void drawPoints(Canvas canvas){  //绘制九个点
        for(int i=0;i<points.length;i++){
            for(int j=0;j<points[i].length;j++){
                if(points[i][j].state == Point.STATE_NORMAL){
                    canvas.drawBitmap(bitmapPointNormal,points[i][j].x-bitmapR,points[i][j].y-bitmapR,paint);
                }
                else if(points[i][j].state == Point.STATE_PRESS){
                    canvas.drawBitmap(bitmapPointPress,points[i][j].x-bitmapR,points[i][j].y-bitmapR,paint);
                }
                else{
                    canvas.drawBitmap(bitmapPointError,points[i][j].x-bitmapR,points[i][j].y-bitmapR,paint);
                }
            }
        }
    }
    private void init(){
        pressPaint.setColor(Color.YELLOW);
        pressPaint.setStrokeWidth(7);
        errorPaint.setColor(Color.RED);
        errorPaint.setStrokeWidth(7);
        bitmapPointError= BitmapFactory.decodeResource(getResources(),R.drawable.error);
        bitmapPointNormal= BitmapFactory.decodeResource(getResources(),R.drawable.normal);
        bitmapPointPress= BitmapFactory.decodeResource(getResources(),R.drawable.press);

        bitmapR = bitmapPointError.getHeight()/2;
        int width = getWidth();
        int height = getHeight();
        int offset=Math.abs(width-height)/2;
        int offsetX,offsetY;
        int space;
        if(width>height){   //横屏
            space = height/4;
            offsetX = offset;
            offsetY = 0;
        }
        else{
            offsetX = 0;
            offsetY = offset;
            space = width/4;
        }
        points[0][0]=new Point(offsetX+space,offsetY+space);
        points[0][1]=new Point(offsetX+2*space,offsetY+space);
        points[0][2]=new Point(offsetX+3*space,offsetY+space);

        points[1][0]=new Point(offsetX+space,offsetY+space*2);
        points[1][1]=new Point(offsetX+2*space,offsetY+space*2);
        points[1][2]=new Point(offsetX+3*space,offsetY+space*2);

        points[2][0]=new Point(offsetX+space,offsetY+space*3);
        points[2][1]=new Point(offsetX+2*space,offsetY+space*3);
        points[2][2]=new Point(offsetX+3*space,offsetY+space*3);

        inited= true;
    }
    public void resetPoints(){
        pointList.clear();
        passList.clear();
        for(int i=0;i<points.length;i++){
            for(int j=0;j<points[i].length;j++){
                points[i][j].state=Point.STATE_NORMAL;
            }
        }
        this.postInvalidate();
    }
    public interface  OnDrawFinishedListener{
        boolean OnDrawFinished(List<Integer> passList);
    }
    public void setOnDrawFinishedListener(OnDrawFinishedListener listener){
        this.listener = listener;
    }
}
