package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * In the new-building description panel (RightObjectDescriptionPanel), colors
 * resource costs and building requirements green when the requirement is met:
 *
 * - "Requires X for Y" lines (class-file line 129): green when the building
 *   that produces resource Y is currently built.
 * - Resource amounts like "8 Wood" (class-file line 140): green when the
 *   player already has enough of that resource in total.
 * - Required support building names (class-file line 175): green when any
 *   building in the required group is currently built.
 */
public class BuildDescriptionColorPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        cc.addMethod(CtNewMethod.make(
            "private boolean qolHasRefiningBuilding(rtr.resources.ResourceModule.ResourceType type) {" +
            "    java.util.HashMap flagsList = this.object.getObjectFlagFactory().getObjectFlagsList();" +
            "    java.util.Iterator iter = flagsList.entrySet().iterator();" +
            "    while (iter.hasNext()) {" +
            "        java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();" +
            "        rtr.map.MapTilesLoader.TileSet ts = (rtr.map.MapTilesLoader.TileSet) entry.getKey();" +
            "        java.util.HashMap sub = (java.util.HashMap) entry.getValue();" +
            "        rtr.objects.objectflags.ObjectFlags f = (rtr.objects.objectflags.ObjectFlags)" +
            "            sub.get(rtr.objects.ObjectBase.ObjectSubType.BUILT);" +
            "        if (f != null && f.getWorkerJobType().getJob().canWorkerRefine(type)" +
            "                && f.getCanBuildingStore().contains(type)" +
            "                && this.object.objectCount(ts, rtr.objects.ObjectBase.ObjectSubType.BUILT) > 0)" +
            "            return true;" +
            "    }" +
            "    return false;" +
            "}",
            cc));

        cc.addMethod(CtNewMethod.make(
            "private boolean qolHasSupportBuilding(rtr.map.MapTilesLoader.TileSet[] group) {" +
            "    for (int i = 0; i < group.length; i++) {" +
            "        if (this.object.objectCount(group[i], rtr.objects.ObjectBase.ObjectSubType.BUILT) > 0)" +
            "            return true;" +
            "    }" +
            "    return false;" +
            "}",
            cc));

        CtMethod render = cc.getDeclaredMethod("render");
        render.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (!"drawString".equals(m.getMethodName())) return;

                // Line numbers below are bytecode (LineNumberTable) values, not decompiled source lines.
                // ../rtr/rtr/gui/states/shared/RightObjectDescriptionPanel.java (decompiled lines in parens)
                if (m.getLineNumber() == 129) { // decompiled line 147: drawString for "Requires X for Y"
                    // "Requires X for Y" green if the refining building is built
                    m.replace(
                        "{ String _t = qolHasRefiningBuilding(type)" +
                        "    ? $3.replace(\"$RED1\", \"$GRE1\") : $3;" +
                        "  $0.drawString($1, $2, _t, $4, $5); }"
                    );
                } else if (m.getLineNumber() == 140) { // decompiled line 163: drawString for "$RED1" + getResourceValueBaseCount(type) + ...
                    // Resource amount green if player has enough
                    m.replace(
                        "{ String _c = this.resource.getResourceTotal(type, false) >= this.highlightedObjectFlags.getResourceValueBaseCount(type)" +
                        "    ? \"$GRE1\" : \"$RED1\";" +
                        "  $0.drawString($1, $2, $3.replace(\"$RED1\", _c), $4, $5); }"
                    );
                } else if (m.getLineNumber() == 175) { // decompiled line 192: drawString for support building out
                    // Required support building green if any building in group is built
                    m.replace(
                        "{ String _t = qolHasSupportBuilding(a)" +
                        "    ? $3.replace(\"$RED0\", \"$GRE0\") : $3;" +
                        "  $0.drawString($1, $2, _t, $4, $5); }"
                    );
                }
            }
        });
    }
}
