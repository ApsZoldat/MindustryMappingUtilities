package mu.utils;

import java.lang.annotation.*;

public class MUAnnotations{
    /** Indicates that a field must not be copied through reflect directly in copyFields() method. */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoCopy{}
    
    /** Indicates that a field requires Scl.scl() before assignment through reflect in copyFields() method. */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RequireScl{}

    /** Indicates that a field must not be Json serialized directly. */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoJson{}
}
