package com.wyx.tableviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * @author winney E-mail:542111388@qq.com
 * @version 创建时间: 2015/12/07 下午5:51
 */
public class TableBorderLayout extends ViewGroup {
  private int rowCount = 0;
  private int colCount = 0;

  public static short TOP = 1 << 1;
  public static short BOTTOM = 1 << 2;
  public static short LEFT = 1 << 3;
  public static short RIGHT = 1 << 4;
  private List<Integer> rowHeight;
  private List<Integer> colWidth;
  private short[][] eraseBorder;
  private int[] colWeight;
  private int[] rowWeight;
  private boolean fastDrawBorder = true;
  private int strokeWidth = 2;

  private int borderColor = Color.LTGRAY;
  private Paint borderPaint;

  /**
   * 使用说明:如果是宽度是match_parent需要设置weight,否则默认均分,
   * 如果是wrap_content,则会根据子View自动调节大小
   */
  public TableBorderLayout(Context context) {
    this(context, null);
  }

  public TableBorderLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TableBorderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TableBorderLayout);
    borderColor = array.getColor(R.styleable.TableBorderLayout_tableborderColor, Color.LTGRAY);
    strokeWidth = (int) array.getDimension(R.styleable.TableBorderLayout_tableborderWidth, 2);
    rowCount = array.getInteger(R.styleable.TableBorderLayout_tablerowcount, 0);
    colCount = array.getInteger(R.styleable.TableBorderLayout_tablecolcount, 0);
    array.recycle();

    init();
  }

  @Override public void requestLayout() {
    initPaint();
    super.requestLayout();
  }

  private void init() {
    initPaint();
    this.colWidth = new ArrayList<>();
    this.rowHeight = new ArrayList<>();
    setRowColCount(rowCount, colCount);
  }

  private void initPaint() {
    if (borderPaint == null) {
      borderPaint = new Paint();
    }
    borderPaint.setColor(borderColor);
    borderPaint.setStrokeWidth(strokeWidth);
    borderPaint.setAntiAlias(true);
  }

  public void setRowColCount(int rowCount, int colCount) {
    this.rowCount = rowCount;
    this.colCount = colCount;
    this.eraseBorder = new short[rowCount][colCount];
    this.colWeight = new int[colCount];
    this.rowWeight = new int[rowCount];
    initWeight(colWeight);
    initWeight(rowWeight);
    requestLayout();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

    rowHeight.clear();
    colWidth.clear();
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
      int widthSum = getSum(colWeight);
      int fullWidth = measuredWidth - (colCount + 1) * strokeWidth;
      for (int i = 0; i < colCount; i++) {
        colWidth.add(fullWidth / widthSum * colWeight[i]);
      }
    }

    if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {

      int heightSum = getSum(rowWeight);
      int fullHeight = measuredHeight - (rowCount + 1) * strokeWidth;

      for (int i = 0; i < rowCount; i++) {
        rowHeight.add(fullHeight / heightSum * rowWeight[i]);
      }
    }
    int rowHighest = 0;
    for (int i = 0; i < Math.min(getChildCount(), rowCount * colCount); i++) {
      View mItem = getChildAt(i);
      LayoutParams params = mItem.getLayoutParams();
      if (mItem.getVisibility() == View.GONE) {
        mItem.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
      } else if (params != null && params.height >= 0) {
        mItem.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
        rowHighest = Math.max(getChildAt(i).getMeasuredHeight(), rowHighest);
      } else {
        mItem.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        rowHighest = Math.max(getChildAt(i).getMeasuredHeight(), rowHighest);
      }
      int colIndex = i % colCount;
      if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
        if (colIndex == colCount - 1) {
          rowHeight.add(rowHighest);
          rowHighest = 0;
        }
      }
      if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
        if (colWidth.size() <= colIndex || colWidth.get(colIndex) == null) {
          colWidth.add(mItem.getMeasuredWidth());
        } else if (colWidth.get(colIndex) < mItem.getMeasuredWidth()) {
          colWidth.set(colIndex, mItem.getMeasuredWidth());
        }
      }
    }

    int rowIndex = 0;
    for (int i = 0; i < Math.min(getChildCount(), rowCount * colCount); i++) {
      View mItem = getChildAt(i);
      if (i % colCount == 0 && i != 0) {
        rowIndex++;
      }
      mItem.measure(MeasureSpec.makeMeasureSpec(colWidth.get(i % colCount), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(rowHeight.get(rowIndex), MeasureSpec.EXACTLY));
    }

    setMeasuredDimension(getTableWidth(), getTableHeight());
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int startX = strokeWidth;
    int startY = strokeWidth;
    int colIndex = 0;
    for (int i = 0; i < Math.min(getChildCount(), rowCount * colCount); i++) {
      View mItem = getChildAt(i);
      if (i % colCount == 0 && i != 0) {
        startY += rowHeight.get(colIndex++);
        startY += strokeWidth;
        startX = strokeWidth;
      }
      mItem.layout(startX, startY, startX + colWidth.get(i % colCount), startY + rowHeight.get(colIndex));
      startX += colWidth.get(i % colCount);
      startX += strokeWidth;
    }
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (getChildCount() == 0) {
      return;
    }
    drawBorder(canvas);
  }

  public void setColWeight(int colIndex, int weight) {
    colWeight[colIndex] = weight;
  }

  public void setRowWeight(int rowIndex, int weight) {
    rowWeight[rowIndex] = weight;
  }

  public void eraseBorder(int x, int y, short border) {
    fastDrawBorder = false;
    eraseBorder[x][y] |= border;
  }

  private void drawBorder(Canvas canvas) {
    canvas.save();
    if (fastDrawBorder) {
      fastDrawBorder(canvas);
    } else {
      drawEachBorder(canvas);
    }
    canvas.restore();
  }

  private void drawEachBorder(Canvas canvas) {
    int halfStrWid = strokeWidth / 2;
    int startX = halfStrWid;
    int startY = halfStrWid;
    int endX = startX;
    int endY = startY;
    for (int i = 0; i <= rowCount; i++) {
      if (i != rowCount) {
        endY += rowHeight.get(i);
        endY += strokeWidth;
      }
      for (int j = 0; j <= colCount; j++) {
        if (j != colCount) {
          endX += colWidth.get(j);
          endX += strokeWidth;
        }
        //draw vertical line
        if (!erasedLR(i, j)) {
          canvas.drawLine(startX, startY - halfStrWid, startX, endY + halfStrWid, borderPaint);
        }
        // draw horizontal line
        if (!erasedTB(i, j)) {
          canvas.drawLine(startX - halfStrWid, startY, endX + halfStrWid, startY, borderPaint);
        }
        if (j != colCount) {
          startX += colWidth.get(j);
          startX += strokeWidth;
        }
      }
      if (i != rowCount) {
        startY += rowHeight.get(i);
        startY += strokeWidth;
      }
      endX = strokeWidth / 2;
      startX = strokeWidth / 2;
    }
  }

  private int getSum(int[] weights) {
    int res = 0;
    for (int weight : weights) {
      res += weight;
    }
    if (res == 0) {
      initWeight(weights);
    }
    return res == 0 ? weights.length : res;
  }

  /**
   * 默认Weight初始化为1,也就是平分
   */
  private void initWeight(int[] weights) {
    for (int i = 0; i < weights.length; i++) {
      weights[i] = 1;
    }
  }

  private boolean erasedLR(int i, int j) {
    if (i < 0 || j < 0 || i >= rowCount || j >= colCount) {
      return false;
    }
    if ((eraseBorder[i][j] & LEFT) == LEFT) {
      return true;
    }
    if (i > 0 && j > 0 && (eraseBorder[i][j - 1] & RIGHT) == RIGHT) {
      return true;
    }
    return false;
  }

  private boolean erasedTB(int i, int j) {
    if (i < 0 || j < 0 || i >= rowCount || j >= colCount) {
      return false;
    }
    if ((eraseBorder[i][j] & TOP) == TOP) {
      return true;
    }
    if (i > 0 && j > 0 && (eraseBorder[i - 1][j] & BOTTOM) == BOTTOM) {
      return true;
    }
    return false;
  }

  public void fastDrawBorder(Canvas canvas) {
    int startX = strokeWidth / 2;
    int startY = strokeWidth / 2;
    //draw vertical line
    for (int i = 0; i <= colCount; i++) {
      canvas.drawLine(startX, strokeWidth, startX, getMeasuredHeight(), borderPaint);
      if (i != colCount) {
        startX += colWidth.get(i);
        startX += strokeWidth;
      }
    }

    //draw horizontal line
    for (int i = 0; i <= rowCount; i++) {
      canvas.drawLine(strokeWidth, startY, getTableWidth(), startY, borderPaint);
      if (i != rowCount) {
        startY += rowHeight.get(i);
        startY += strokeWidth;
      }
    }
  }

  public int getTableWidth() {
    int res = 0;
    for (Integer itemWidth : colWidth) {
      res += itemWidth;
    }
    res += (colCount + 1) * strokeWidth;
    return res;
  }

  public int getTableHeight() {
    int res = 0;
    for (Integer itemHeight : rowHeight) {
      res += itemHeight;
    }
    res += (rowCount + 1) * strokeWidth;
    return res;
  }

  public int getStrokeWidth() {
    return strokeWidth;
  }

  public void setStrokeWidth(int strokeWidth) {
    this.strokeWidth = strokeWidth;
    requestLayout();
  }

  public int getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(int borderColor) {
    this.borderColor = borderColor;
    requestLayout();
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getColCount() {
    return colCount;
  }
}

