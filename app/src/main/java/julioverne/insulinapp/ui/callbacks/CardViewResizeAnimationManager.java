package julioverne.insulinapp.ui.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import julioverne.insulinapp.animation.CustomTransitionSet;
import julioverne.insulinapp.widgets.ScrollControlLayoutManager;

public class CardViewResizeAnimationManager implements Transition.TransitionListener {

    @NonNull
    private final ScrollControlLayoutManager layoutManager;
    @NonNull
    private final RecyclerView recyclerView;
    @NonNull
    private TransitionSet transitionSet;
    private boolean isAnimationOnGoing = false;

    public CardViewResizeAnimationManager(@NonNull final RecyclerView recyclerView,
        @NonNull final ScrollControlLayoutManager layoutManager,
        @NonNull final TransitionSet transitionSet) {
        this.recyclerView = recyclerView;
        this.layoutManager = layoutManager;
        this.transitionSet = transitionSet;
        setUpListener();
    }

    public CardViewResizeAnimationManager(@NonNull final RecyclerView recyclerView,
        @NonNull final ScrollControlLayoutManager layoutManager) {
        this.recyclerView = recyclerView;
        this.layoutManager = layoutManager;
        this.transitionSet = new CustomTransitionSet();
        setUpListener();
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {
        layoutManager.setScrollEnabled(false);
    }

    @Override
    public void onTransitionEnd(@NonNull Transition transition) {
        enableAnimation();
    }

    @Override
    public void onTransitionCancel(@NonNull Transition transition) {
        enableAnimation();
    }

    @Override
    public void onTransitionPause(@NonNull Transition transition) {
        layoutManager.setScrollEnabled(true);
    }

    @Override
    public void onTransitionResume(@NonNull Transition transition) {
        layoutManager.setScrollEnabled(false);
    }

    /**
     * Prepares the RecyclerView for a boundary change.
     *
     * @return True if the resizing can be performed, or false if there is an ongoing animation,
     * in which case, you shouldn't let other resizing animations happen to avoid display issues.
     */
    public boolean resize() {
        if (!isAnimationOnGoing) {
            isAnimationOnGoing = true;
            TransitionManager.beginDelayedTransition(recyclerView, transitionSet);
            return true;
        } else {
            return false;
        }
    }

    public void setTransitionSet(@NonNull final TransitionSet transitionSet) {
        this.transitionSet = transitionSet;
        setUpListener();
    }

    public boolean isAnimationOnGoing() {
        return isAnimationOnGoing;
    }

    public void setAnimationOnGoing(boolean animationOnGoing) {
        isAnimationOnGoing = animationOnGoing;
    }

    public void enableAnimation() {
        enableScroll(true);
        isAnimationOnGoing = false;
    }

    public void enableScroll(boolean enable) {
        layoutManager.setScrollEnabled(enable);
    }

    private void setUpListener() {
        transitionSet.addListener(this);
    }
}
