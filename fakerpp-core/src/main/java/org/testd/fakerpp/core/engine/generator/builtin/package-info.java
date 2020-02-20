/**
 * org.testd.fakerpp.core.engine.generator.builtin if the package for built-in generators
 *
 * Specifications for xml tag about  built-in generator:
 *  1. Class name is tag name (camel-cased) suffixed with "Gen"
 *  2. a public field of basic type (int long String e.t.c)  = an attribute of tag
 *  3. `List<List<String>> options` = options sub tag
 *  4. field default value is the tag default value
 *  5. must have a no arg constructor
 */
package org.testd.fakerpp.core.engine.generator.builtin;