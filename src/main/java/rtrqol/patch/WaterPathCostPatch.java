package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Scales the A* movement cost in PathFinder.findPath() for water tiles only.
 *
 * Vanilla uses the raw tile cost directly as the A* edge weight, but actual mob speed is
 * baseVelocity × (1 − tileCost/1000). The pathfinder therefore underestimates water:
 * deep water (tileCost=1000) nearly stops mobs (speed clamped to 0.15 in MobBase) yet A*
 * only charges 2× a normal tile. Mobs then cut through water when going around is faster.
 *
 * Fix: for tiles where any layer has the water flag, replace rawM with 1000 / effectiveSpeed,
 * where effectiveSpeed = max(0.15, (2000 − rawM) / 1000), matching the 0.15 floor in MobBase.
 * Non-water tiles are left at their vanilla cost.
 * Corrected costs: shallow water=2000, deep water=6667.
 */
public class WaterPathCostPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod findPath = cc.getDeclaredMethod("findPath");
        findPath.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if ("getM".equals(m.getMethodName())
                        && "rtr.mobs.pathfinder.PathNode".equals(m.getClassName())) {
                    m.replace("{"
                        + "int rawM = $proceed($$);"
                        + "boolean isWaterTile = false;"
                        + "int nx = $0.getX(), ny = $0.getY();"
                        + "for (int l2 = 0; l2 < 12; l2++) {"
                        + "    int tid = this.map.getTileId(nx, ny, l2);"
                        + "    if (tid != 0 && this.map.getMapTileLoader().isTileWater(tid)) {"
                        + "        isWaterTile = true; break;"
                        + "    }"
                        + "}"
                        + "if (isWaterTile) {"
                        + "    float speedFactor = java.lang.Math.max(0.15f, (2000.0f - rawM) / 1000.0f);"
                        + "    $_ = (int)(1000.0f / speedFactor);"
                        + "} else {"
                        + "    $_ = rawM;"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
