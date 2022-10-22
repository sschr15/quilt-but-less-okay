package sschr15.tools.qblo.annotations;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Used to indicate a class.
 */
@Language(value = "JAVA", prefix = "import ", suffix = ";")
@Retention(RetentionPolicy.CLASS)
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT})
public @interface JvmClass {
	/**
	 * A class name, as {@link Class#forName(String)} would expect.
	 */
	@Language(value = "JAVA", prefix = "class X { Class<?> x = Class.forName(\"", suffix = "\"); }")
	@Retention(RetentionPolicy.CLASS)
	@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT})
	@interface ForName {}
}
