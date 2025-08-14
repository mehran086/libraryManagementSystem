package library.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation to include certain fields in the admin reflection report.
 * If a class has any @ReportField annotations, only those fields will be listed.
 * If no fields have this annotation, all declared fields will be shown.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportField {
}
