package com.extra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.webview.Log;
import android.view.View;

public class MyProgressBar extends View {

	private Bitmap bitmap;
	private Paint paint;
	
	private int _progress;
	private int _max;
	
	public MyProgressBar(Context context) {
		super(context);
		//bitmap = BitmapFactory.decodeResource(getResources(),);
		//init();
	}
	
	public void init(){
		paint = new Paint();
		paint.setColor(Color.BLUE);
		//���������С
		paint.setTextSize(100);
		
		//�û�����ͼ���ǿ��ĵ�
		paint.setStyle(Paint.Style.STROKE);
		//���û������ߵ� ��ϸ�̶�
		paint.setStrokeWidth(5);
	}
	
	public void setProgress(int progress){
		_progress = progress;
	}
	public void setMax(int max){
		_max = max;
	}
	
	/**
	 * Ҫ��ͼ�Σ�������Ҫ���������
	 * 1.��ɫ���� Color
	 * 2.���ʶ��� Paint
	 * 3.�������� Canvas
	 */
	
	@Override
	public void onDraw(Canvas canvas) {
		
		//����һ����
		//canvas.drawLine(0, 0, 200, 200, paint);
		
		//������
		//canvas.drawRect(200, 500, 300, 300, paint);
		
		//��Բ
		//canvas.drawCircle(200, 200, 100, paint);
		//�����ַ� drawText(String text, float x, float y, Paint paint) 
		// y �� ��׼�� ������ �ַ�� �ײ�
		//canvas.drawText("apple", 60, 60, paint);
		//canvas.drawLine(0, 60, 500, 60, paint);
		
		//����ͼƬ
		
		/**
		 * ������Σ���ָͼƬ����Ҫ��ȡ�Ĳ���
		 * @param left:��ʾ����ߵ�x�����ؿ�ʼ
		 * @param top:�Ӷ������µ�x�����ؿ�ʼ
		 * @param right:����������ұ߿�
		 * @param bottom:��������ĵױ߿�
		 * */

		/**
		 * �������ָ���ǣ���src�ľ��Σ����ڵ�ǰָ���ľ��ο��С�
		 * ���src��des���δ�С��һ�£���Ὣsrc�е����ݽ�����Ӧ�ķŴ���С��
		 * ����ͬsrc����
		 * */
		
//		_progress = _progress<_max?_progress:_max;
		int w = _progress *100/ bitmap.getWidth();
//		if(w>0){
//			Rect des = new Rect(0,0,w,bitmap.getHeight());
//			canvas.drawBitmap(bitmap, des, des, paint);
//			Log.d("Unity", w+"-"+_progress+"-"+_max);
//		}
		Log.d("Unity", w+"-"+_progress+"-"+_max);

	}

}
