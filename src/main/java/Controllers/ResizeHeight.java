package Controllers;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

//Credit goes to RTERP - https://rterp.wordpress.com/2015/09/01/creating-custom-animated-transitions-with-javafx/
public class ResizeHeight extends Transition {

    protected Region region;
    protected double startHeight;
    protected double newHeight;
    protected double heightDiff;

    public ResizeHeight( Duration duration, Region region, double newHeight ) {
        setCycleDuration(duration);
        this.region = region;
        this.newHeight = newHeight;
        this.startHeight = region.getHeight();
        this.heightDiff = newHeight - startHeight;
    }

    @Override
    protected void interpolate(double fraction) {
        region.setMinHeight( startHeight + ( heightDiff * fraction ) );
    }
}