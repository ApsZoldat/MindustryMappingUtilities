package mu.ui.data.annotations;

import java.lang.annotation.*;

/** Indicates that a field must not be copied through reflect directly in copyFields() method. */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoCopy{}
