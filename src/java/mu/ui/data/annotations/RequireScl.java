package mu.ui.data.annotations;

import java.lang.annotation.*;

/** Indicates that a field requires Scl.scl() before assignment through reflect in copyFields() method. */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireScl{}
