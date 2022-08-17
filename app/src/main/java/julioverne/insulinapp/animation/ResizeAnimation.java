package julioverne.insulinapp.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {

  public static final int TYPE_EXPAND = 0;
  public static final int TYPE_COLLAPSE = 1;

  private View view;
  private int targetHeight;
  private int currentHeight;
  private int type;
  private int heightDifference;

  public ResizeAnimation(View view, int type, int targetHeight, int currentHeight, long duration) {
    this.view = view;
    this.targetHeight = targetHeight;
    this.currentHeight = currentHeight;
    this.type = type;
    this.heightDifference = getHeightDifference();
    setDuration(duration);
  }

  public ResizeAnimation(View view, int type, int targetHeight, int currentHeight) {
    this.view = view;
    this.targetHeight = targetHeight;
    this.currentHeight = currentHeight;
    this.type = type;
    this.heightDifference = getHeightDifference();
    setDuration(assignDuration(heightDifference));
  }

  /**
   * Returns the difference between the view's current height and the targetHeight to know how
   * much the animation must decrease or increase the size of the view.
   *
   * @return The difference between the view's current height and the targetHeight.
   */
  private int getHeightDifference() {

    int difference;

    if (type == TYPE_EXPAND) {
      difference = targetHeight - currentHeight;
    } else {
      difference = currentHeight - targetHeight;
    }

    return difference;
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {

    int interpolatedHeight;

    if (type == TYPE_EXPAND) {
      interpolatedHeight = (int) (currentHeight + (heightDifference * interpolatedTime));
    } else {
      interpolatedHeight = (int) (currentHeight - (heightDifference * interpolatedTime));
    }

    view.getLayoutParams().height = interpolatedHeight;
    view.requestLayout();
  }

  @Override
  public boolean willChangeBounds() {
    return true;
  }

  /**
   * Returns a duration of 1ms per dp
   *
   * @return The duration depending on the {@code heightDifference}.
   */
  private long assignDuration(int heightDifference) {
    //        return (long) (heightDifference / view.getContext().getResources().getDisplayMetrics().density);
    return 200l;
  }
}
