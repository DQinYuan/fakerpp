/**
 * org.testany.fakerpp.core.engine.generator.builtin if the package for built-in generators
 *
 * Specifications for xml tag about  built-in generator:
 *  1. Class name is tag name (camel-cased) suffixed with "Gen"
 *  2. a public field of basic type (int long String e.t.c)  = an attribute of tag
 *  3. a public field of list<String> type  = an sub tag of tag  (for example, field "List<String> attrs",
 *  it's sub tag is "<attrs><attr>aa</attr><attr>bb</attr></attrs>")
 *  4. field default value is the tag default value
 */
package org.testany.fakerpp.core.engine.generator.builtin;