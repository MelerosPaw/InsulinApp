package julioverne.insulinapp.utils;

import android.animation.LayoutTransition;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.annotation.Nullable;
import julioverne.insulinapp.animation.ResizeAnimation;

public class AnimationUtil {

    public static final int MATCH_PARENT = 0;
    public static final int WRAP_CONTENT = 1;

    private AnimationUtil() {
    }

    public static void enableAnimateLayoutChanges(ViewGroup... views) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (ViewGroup view : views) {
                view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            }
        }
    }

    public static void resizeHeight(View view, int mode, boolean isVisible) {
        resizeHeight(view, mode, isVisible, null);
    }

    /**
     * Starts an animation to set the view's height to {@code wrap_content} or {@code match_parent}
     * depending on the {@code mode}.
     *
     * @param view The view whose height will be modified.
     * @param mode The way the new height will be calculated. Must be one of MATCH_PARENT or
     * WEAP_CONTENT.
     * @param isVisible If the view is visible at the moment. This is necessary to know since
     * invisible views cannot be animated
     */
    public static void resizeHeight(View view, int mode, boolean isVisible,
        @Nullable Animation.AnimationListener listener) {

        int currentHeight = view.getHeight();
        int targetHeight;

        if (!isVisible && currentHeight == 0) {
            view.getLayoutParams().height = 1;
            //            currentHeight = 1;
            view.setVisibility(View.VISIBLE);
        }

        if (mode == MATCH_PARENT) {
            targetHeight = getMatchParentHeight(view);
        } else if (mode == WRAP_CONTENT) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            targetHeight = view.getMeasuredHeight();
        } else {
            throw new IllegalStateException("You're trying to resize a view's height, but the " +
                "mode must be either MATH_PARENT or WRAP_CONTENT.");
        }

        // Este tamaño, aunque se obtenga midiendo para MATCH_PARENT o WRAP_CONTENT, será un valor
        // fijo y no match_parent o wrap_content. Para eso habría que asignarle este valor tras
        // la animación.
        int type = targetHeight > currentHeight ?
            ResizeAnimation.TYPE_EXPAND : ResizeAnimation.TYPE_COLLAPSE;

        ResizeAnimation animation;

        if (currentHeight == 0) {
            animation = new ResizeAnimation(view, type, targetHeight, currentHeight, 1000);
        } else {
            animation = new ResizeAnimation(view, type, targetHeight, currentHeight);
        }

        if (listener != null) {
            animation.setAnimationListener(listener);
        }

        view.startAnimation(animation);
    }

    private static int getMatchParentHeight(View view) {

        final int[] viewLocationOnScreen = new int[2];
        final int parentHeight;
        final int[] parentLocationOnScreen = new int[2];
        final int marginBottomDimensions;

        if (!(view.getParent() instanceof View)) {
            throw new IllegalStateException("You're trying to change the size of a view, to match" +
                " parent, but the parent view is not a View.");
        } else {
            // Gets the parent's height
            View parent = (View) view.getParent();
            parent.getLocationOnScreen(parentLocationOnScreen);
            parentHeight = parent.getHeight();

            // Gets the parent's bottom location
            int parentTopLocation = parentLocationOnScreen[1];
            int parentBottom = parentTopLocation + parentHeight;

            // Gets the view's top location
            view.getLocationOnScreen(viewLocationOnScreen);
            int viewTopLocation = viewLocationOnScreen[1];

            // The view's new height is the one that goes from the view's top to the parent's bottom
            int viewTargetHeight = parentBottom - viewTopLocation;

            // Since the view may have a bottom margin, we have to subtract that margin from the
            // view's new height or else no margin will be kept. We don't have to care about the top
            // margin since the view's top location returned by getLocationOnScreen does not take it
            // into account.
            ViewGroup.MarginLayoutParams viewMarginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginBottomDimensions = viewMarginParams.bottomMargin;

            return viewTargetHeight - marginBottomDimensions;
        }
    }
}
