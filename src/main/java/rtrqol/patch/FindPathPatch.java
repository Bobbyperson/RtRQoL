package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Two fixes to PathFinder.findPath():
 *
 * Fix 1: heuristic variance: the original picks variance in [1,100] and multiplies
 * the euclidean heuristic by it.  High values (e.g. 87) produce very greedy, wandering
 * paths because the heuristic dominates the accumulated cost.  Reducing the range to
 * [1,3] keeps path variety while staying close to true A*.
 *
 * Fix 2: A* open-set update: when a shorter route to an already-open node is found,
 * the original updates the parent but silently drops the g/f update and never
 * re-inserts the node in the priority queue.  The node stays at its stale (worse)
 * priority, so A* may expand it too late or reconstruct the path via the wrong cost.
 * Fix: remove the node, update g + f, re-insert.
 */
public class FindPathPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod findPath = cc.getDeclaredMethod("findPath");

        // Fix 1: cap pathVariance at 2 (result: 1–3 after the original +1)
        final boolean[] patchedVariance = {false};
        findPath.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (!patchedVariance[0]
                        && "randomInt".equals(m.getMethodName())
                        && "rtr.utilities.Utilities".equals(m.getClassName())) {
                    patchedVariance[0] = true;
                    m.replace("$_ = rtr.utilities.Utilities.randomInt(2);");
                }
            }
        });

        // Fix 2: on the first setParent call in findPath (the one inside the isOpen()
        // branch), also update g/f and re-insert into the priority queue.
        // The second setParent call (in the else/new-node branch) is left untouched.
        final int[] setParentCount = {0};
        findPath.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if ("setParent".equals(m.getMethodName())) {
                    setParentCount[0]++;
                    if (setParentCount[0] == 1) {
                        // $0 = checkNode, $1 = this.basePathNode
                        // gValueCheck is a local variable in scope at this call site
                        m.replace("{"
                            + "this.openPathNodes.remove($0);"
                            + "$proceed($$);"
                            + "$0.setG(gValueCheck);"
                            + "$0.calcF();"
                            + "this.openPathNodes.add($0);"
                            + "}");
                    }
                }
            }
        });
    }
}
