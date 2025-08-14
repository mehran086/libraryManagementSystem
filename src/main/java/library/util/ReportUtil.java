package library.util;

import library.domain.*;
import library.service.LibraryService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ReportUtil {

    public static String generateReport(LibraryService library) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 📊 ADMIN REFLECTION REPORT ===\n\n");

        // 1. Instance counts
        sb.append("Instance Counts:\n");
        long bookCount = library.listAllBooks().size();
        long studentCount = library.getAllUsers().stream().filter(u -> u instanceof Student).count();
        long librarianCount = library.getAllUsers().stream().filter(u -> u instanceof Librarian).count();
        sb.append(String.format("  Books     : %d\n", bookCount));
        sb.append(String.format("  Students  : %d\n", studentCount));
        sb.append(String.format("  Librarians: %d\n", librarianCount));
        sb.append("\n");

        // 2. Fields per domain class using reflection
        sb.append("Class Fields:\n");

        // We already have live instances via LibraryService
        Set<Class<?>> domainClasses = new LinkedHashSet<>();
        library.listAllBooks().forEach(b -> domainClasses.add(b.getClass()));
        library.getAllUsers().forEach(u -> domainClasses.add(u.getClass()));

        for (Class<?> clazz : domainClasses) {
            sb.append(" - ").append(clazz.getSimpleName()).append(":\n");

            Field[] fields = clazz.getDeclaredFields();
            List<Field> fieldsToReport;

            // If any fields are annotated, use only those; otherwise, use all
            List<Field> annotatedFields = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(ReportField.class))
                    .collect(Collectors.toList());

            if (!annotatedFields.isEmpty()) {
                fieldsToReport = annotatedFields;
            } else {
                fieldsToReport = Arrays.asList(fields);
            }

            for (Field f : fieldsToReport) {
                f.setAccessible(true);
                sb.append(String.format("    %s : %s\n",
                        f.getName(),
                        f.getType().getSimpleName()));
            }
        }

        sb.append("\n=== End of Report ===");
        return sb.toString();
    }
}
