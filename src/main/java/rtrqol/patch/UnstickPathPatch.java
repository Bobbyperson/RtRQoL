package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Fix for PathFinder.findUnstickPath():
 *
 * The original code calls getCoordinatesInRangeMultithreaded(checkX, checkY, 16, false, g)
 * for every candidate node opened by the A* search, a full Dijkstra flood-fill (up to
 * ~256 nodes) nested inside the outer search loop.  On a 256×256 map with many stuck
 * mobs, this gets very expensive.
 *
 * Fix: reduce the range from 16 to 5.  The connectivity semantics are preserved, it's
 * still a real Dijkstra reachability check, just with a tighter budget.  At movement
 * cost ~1000/tile, a range-5 budget reaches ~20–50 tiles in open terrain, which is
 * more than the "> 16" threshold requires.  The only behavior change is that very
 * high-cost terrain tiles (e.g. deep forest) may not yield enough reachable tiles to
 * satisfy the threshold; in practice this means stuck mobs may roam slightly further
 * before finding an acceptable open area.
 */
public class UnstickPathPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod findUnstickPath = cc.getDeclaredMethod("findUnstickPath");

        findUnstickPath.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if ("getCoordinatesInRangeMultithreaded".equals(m.getMethodName())) {
                    // $1=startX, $2=startY, $3=range(16→5), $4=ignoreMovementCost, $5=g
                    m.replace("$_ = getCoordinatesInRangeMultithreaded($1, $2, 5, $4, $5);");
                }
            }
        });
    }
}
