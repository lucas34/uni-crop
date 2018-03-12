package fr.nelaupe.unicrop;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yalantis.ucrop.util.RectUtils;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class CropImageView extends ImageViewTouchBase {

    private final LayerDrawable mHighlight;

    private int mAspectX;

    private int mAspectY;

    private Bitmap mImageBitmapResetBase;

    private float mLastX;

    private float mLastY;

    private HighlightView mMotionHighlightView = null;

    private int mMotionEdge;

    private final List<HighlightView> mHighlightViews = new ArrayList<>();

    private HighlightView mCrop;

    private boolean mWaitingToPick;

    private RectF mUserRect;

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // load layer list for HighlightView
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, R.attr.cropKitStyle, R.style.CropKit);

        Drawable dHighlight = a.getDrawable(R.styleable.CropImageView_cropKitHighlight);
        if (!(dHighlight instanceof LayerDrawable)) {
            a.recycle();
            throw new IllegalStateException("cropKitHightlight must be a layer-list");
        }

        mHighlight = (LayerDrawable) dHighlight;
        int[] idCheck = {R.id.cropkit_highlight_diagonal, R.id.cropkit_highlight_horizontal, R.id.cropkit_highlight_vertical};

        // validate layer list member
        for (int id : idCheck) {
            if (mHighlight.findDrawableByLayerId(id) == null) {
                a.recycle();
                throw new IllegalStateException("@id/" + getResources().getResourceEntryName(id) + " is not included in cropKitHightlight layer-list");
            }
        }

        mAspectX = a.getInteger(R.styleable.CropImageView_cropKitAspectX, 0);
        mAspectY = a.getInteger(R.styleable.CropImageView_cropKitAspectY, 0);

        a.recycle();
    }

    public void initWith(CropKitParams params) {
        setAspect(params.aspectX, params.aspectY);
        if (params.defaultCropPosition != null && RectUtils.trapToRect(params.defaultCropPosition).width() > 10 && RectUtils.trapToRect(params.defaultCropPosition).height() > 10) {
            setUserRect(RectUtils.trapToRect(params.defaultCropPosition));
        }
    }

    public void setAspect(int x, int y) {
        mAspectX = x;
        mAspectY = y;
    }

    public void setUserRect(RectF rect) {
        if (rect != null) {
            mUserRect = rect;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        mImageBitmapResetBase = bitmap;
        super.setImageBitmap(bitmap);
        setupView();
    }

    public Bitmap getBaseBitmap() {
        return mImageBitmapResetBase;
    }

    public RectF getSelectedCropArea() {
        return mCrop.getCropRect();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (HighlightView hv : mHighlightViews) {
            hv.setMatrix(getImageMatrix());
            hv.invalidate();
            if (hv.mIsFocused) {
                centerBasedOnHighlightView(hv);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupView();
    }

    private void setupView() {
        if (mImageBitmapResetBase == null) return;

        if (mHighlightViews.size() == 0) {
            if (mUserRect != null) {
                _add(createHighlight(mUserRect));
            } else {
                _add(createDefaultHighlight());
            }
        }

        invalidate();
        if (mHighlightViews.size() == 1) {
            mCrop = mHighlightViews.get(0);
            mCrop.setFocus(true);
        }
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : mHighlightViews) {
            hv.setMatrix(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (HighlightView hv : mHighlightViews) {
            hv.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private void recomputeFocus(MotionEvent event) {
        for (HighlightView hv : mHighlightViews) {
            hv.setFocus(false);
            hv.invalidate();
        }

        for (HighlightView hv : mHighlightViews) {
            int edge = hv.getHit(event.getX(), event.getY());
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true);
                    hv.invalidate();
                }
                break;
            }
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mWaitingToPick) {
                    recomputeFocus(event);
                } else {
                    for (HighlightView hv : mHighlightViews) {
                        int edge = hv.getHit(event.getX(), event.getY());
                        if (edge != HighlightView.GROW_NONE && hv.hasFocus()) {
                            mMotionEdge = edge;
                            mMotionHighlightView = hv;
                            mLastX = event.getX();
                            mLastY = event.getY();
                            mMotionHighlightView.setMode(
                                    (edge == HighlightView.MOVE)
                                            ? HighlightView.ModifyMode.Move
                                            : HighlightView.ModifyMode.Grow
                            );
                            break;
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mWaitingToPick) {
                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        if (hv.hasFocus()) {
                            mCrop = hv;
                            for (int j = 0; j < mHighlightViews.size(); j++) {
                                if (j == i) {
                                    continue;
                                }
                                mHighlightViews.get(j).setHidden(true);
                            }

                            centerBasedOnHighlightView(hv);
                            mWaitingToPick = false;
                            return true;
                        }
                    }
                } else if (mMotionHighlightView != null) {
                    centerBasedOnHighlightView(mMotionHighlightView);
                    mMotionHighlightView.setMode(
                            HighlightView.ModifyMode.None);
                }
                mMotionHighlightView = null;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mWaitingToPick) {
                    recomputeFocus(event);
                } else if (mMotionHighlightView != null) {
                    mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
                    mLastX = event.getX();
                    mLastY = event.getY();

                    // if (true)
                    {
                        // This section of code is optional. It has some user
                        // benefit in that moving the crop rectangle against
                        // the edge of the screen causes scrolling but it means
                        // that the crop rectangle is no longer fixed under
                        // the user's finger.
                        ensureVisible(mMotionHighlightView);
                    }
                }
                break;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: {
                center(true, true);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around.  This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    center(true, true);
                }
                break;
            }
        }

        return true;
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .5F;
        float z2 = thisHeight / height * .5F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);

        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[]{hv.mCropRect.centerX(), hv.mCropRect.centerY()};

            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300L);
        }

        ensureVisible(hv);
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, this.getLeft() - r.left);
        int panDeltaX2 = Math.min(0, this.getRight() - r.right);

        int panDeltaY1 = Math.max(0, this.getTop() - r.top);
        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (HighlightView hv : mHighlightViews) {
            hv.draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        mHighlightViews.add(hv);
        invalidate();
    }

    private void _add(HighlightView hv) {
        mHighlightViews.add(hv);
    }

    public void clearHighlights() {
        mHighlightViews.clear();
    }

    // Create a default HightlightView if we found no face in the picture.
    private HighlightView createDefaultHighlight() {
        int width = mImageBitmapResetBase.getWidth();
        int height = mImageBitmapResetBase.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);

        // make the default size about 4/5 of the width or height
        int cropWidth = width; // Math.min(width, height) * 4 / 5;

        //noinspection SuspiciousNameCombination
        int cropHeight = height; // cropWidth;

        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }

        int x = (width - cropWidth) / 2;
        int y = (height - cropHeight) / 2;

        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);

        HighlightView hv = new HighlightView(this, mHighlight);
        hv.setup(getImageViewMatrix(), imageRect, cropRect, mAspectX != 0 && mAspectY != 0);

        return hv;
    }

    // Create a default HightlightView if we found no face in the picture.
    private HighlightView createHighlight(RectF rect) {
        int width = mImageBitmapResetBase.getWidth();
        int height = mImageBitmapResetBase.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);

        RectF cropRect = new RectF(rect);

        HighlightView hv = new HighlightView(this, mHighlight);
        hv.setup(getImageViewMatrix(), imageRect, cropRect, mAspectX != 0 && mAspectY != 0);

        return hv;
    }

}