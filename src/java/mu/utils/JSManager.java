package mu.utils;

import rhino.*;
import mindustry.mod.*;
import mindustry.Vars;

import static mu.EditorVars.*;

public class JSManager{
    public static Scripts scripts;
    public static Scriptable scope;

    public JSManager(Scripts scripts){
        this.scripts = scripts;
        this.scope = (ImporterTopLevel)scripts.scope;
    }

    public void importPackages(){
        // Original source: https://github.com/SMOLKEYS/new-console-hardline/blob/master/src/newconsole/js/NCJSLink.java
        packageNames.each(name -> {
            NativeJavaPackage pkg = new NativeJavaPackage(name, Vars.mods.mainLoader());
            pkg.setParentScope(scope);
            ((ImporterTopLevel)scope).importPackage(pkg);
        });
    }

    public void setVar(String name, Object value){
        scope.put(name, scope, value);
    }

    public void run(String script){
        scripts.runConsole(script);
    }
}