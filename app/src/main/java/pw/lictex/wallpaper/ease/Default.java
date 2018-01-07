package pw.lictex.wallpaper.ease;

/**
 * Created by kpx on 12.31-2017.
 */

public class Default implements Ease {
    private double ppf = 0;
    private double a;

    public Default(float a) {
        this.a = a;
    }

    @Override
    public double nextDraw(double target, double draw, float delta) {
        if (a == 0) return target;
        ppf = (target - draw) / a;
        double r = draw + ppf * (delta / (1d / 60d * 1000d));
        double r2 = r;
        if (draw < target) {
            if (r2 > target) {
                r = target;
                ppf = 0;
            }
        }
        if (draw > target) {
            if (r2 < target) {
                r = target;
                ppf = 0;
            }
        }
        if (draw == target) ppf = 0;
        return r;
    }
}
