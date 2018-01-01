package pw.lictex.wallpaper.ease;

/**
 * Created by kpx on 12.31-2017.
 */

public class Default implements Ease {
    private float ppf = 0;
    private float a;

    public Default(float a) {
        this.a = a;
    }

    @Override
    public float nextDraw(float target, float draw) {
        if (a == 0) return target;
        ppf = (target - draw) / a;
        float r = draw + ppf;
        if (draw < target) {
            if (draw + ppf > target) {
                r = target;
                ppf = 0;
            }
        }
        if (draw > target) {
            if (draw + ppf < target) {
                r = target;
                ppf = 0;
            }
        }
        if (draw == target) ppf = 0;
        return r;
    }
}
