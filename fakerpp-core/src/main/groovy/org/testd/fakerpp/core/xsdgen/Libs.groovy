package org.testd.fakerpp.core.xsdgen

import com.google.common.reflect.ClassPath
import groovy.xml.MarkupBuilder
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LocalVariableAttribute
import javassist.bytecode.MethodInfo
import lombok.extern.slf4j.Slf4j
import org.javatuples.Triplet
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString
import org.testd.fakerpp.core.util.MyReflectUtil

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Slf4j
abstract class Libs extends Script {

    enum SupportParamType {
        INT("xs:int"), LONG("xs:long"), STRING("xs:token"), BOOLEAN("xs:boolean"),
        LIST("")

        String xsdType

        SupportParamType(String xsdType) {
            this.xsdType = xsdType
        }
    }

    def typeMap = [(int.class)    : SupportParamType.INT,
                   (Integer.class): SupportParamType.INT,
                   (boolean.class): SupportParamType.BOOLEAN,
                   (Boolean.class): SupportParamType.BOOLEAN,
                   (long.class)   : SupportParamType.LONG,
                   (Long.class)   : SupportParamType.LONG,
                   (String.class) : SupportParamType.STRING,
                   (List.class)   : SupportParamType.LIST]

    // jar classes
    def classesFromPkg(ClassPool cp, String pkg, Closure<Boolean> filter) {
        return ClassPath.from(Thread.currentThread().getContextClassLoader())
                .getTopLevelClasses(pkg)
                .collect { it.load() }
                .findAll(filter)
                .collect({ [cp.get(it.name), it] })
    }

    def methodInfo(Method m, CtClass cc) {
        if (cc.isFrozen()) {
            cc.defrost()
        }
        def name = m.getName()
        def paramTypes = m.getParameterTypes()
        def ctParams = paramTypes.collect { paramT -> ClassPool.getDefault().getCtClass(paramT.getName()) }
        def ctMethod = cc.getDeclaredMethod(name, ctParams.toArray(new CtClass[0]))
        MethodInfo methodInfo = ctMethod.getMethodInfo()
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute()
        LocalVariableAttribute attr =
                (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag)
        List<Triplet<String, Class, Object>> paramInfo = []
        paramTypes.eachWithIndex { paramType, i ->
            paramInfo << new Triplet<>(attr.variableName(i + 1), paramType, null)
        }

        return [name, paramInfo]
    }

    def getMethods(CtClass cc, Class c) {
        return MyReflectUtil.getSortedDeclaredMethods(c)
                .toUnique({ it.name })
                .findAll { m ->
            !Modifier.isStatic(m.getModifiers()) &&
                    !Modifier.isPrivate(m.getModifiers())
        }
        .collect { m -> methodInfo(m, cc) }
    }

    def camelToDelimit(String str) {
        if (str.charAt(0).isUpperCase()) {
            def chars = str.toCharArray()
            chars[0] = chars[0].toLowerCase()
            str = new String(chars)
        }

        return str.replaceAll('[A-Z]', '-$0').toLowerCase()
    }

    static class ParamInfo {
        String name
        SupportParamType type
        Object defaultValue

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof ParamInfo)) return false

            ParamInfo paramInfo = (ParamInfo) o

            if (defaultValue != paramInfo.defaultValue) return false
            if (name != paramInfo.name) return false
            if (type != paramInfo.type) return false

            return true
        }

        int hashCode() {
            int result
            result = (name != null ? name.hashCode() : 0)
            result = 31 * result + (type != null ? type.hashCode() : 0)
            result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0)
            return result
        }


        @Override
        String toString() {
            return "ParamInfo{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }

    static class Generator {
        String name
        List<ParamInfo> paramInfos


        @Override
        String toString() {
            return "Generator{" +
                    "name='" + name + '\'' +
                    ", paramInfos=" + paramInfos +
                    '}'
        }
    }

    static class FakerField {
        String name
        List<Generator> gens

        @Override
        String toString() {
            return "FakerField{" +
                    "name='" + name + '\'' +
                    ", gens=" + gens +
                    '}'
        }
    }

    /**
     *
     * @param camelName
     * @param paramInfos
     *            Triplet:
     *                value0: param name
     *                value1: param class
     *                value2: param default value
     * @param supportList
     * @return a list with single Generator object or with no element
     */
    def toGenerator(String camelName, List<Triplet<String, Class, Object>> paramInfos,
                    boolean supportList = false) {
        List<ParamInfo> infoObjs = []
        for (Triplet<String, Class, Object> entry : paramInfos) {
            def pname = entry.value0
            def ptype = entry.value1
            if (ptype == List.class && !supportList) {
                return []
            }

            if (!typeMap.containsKey(ptype)) {
                return []
            }
            infoObjs << new ParamInfo(name: camelToDelimit(pname),
                    type: typeMap[ptype], defaultValue: entry.value2)
        }

        return [new Generator(name: camelToDelimit(camelName), paramInfos: infoObjs)]
    }

    def fakerMethod2Field(ClassPool cp, Method method) {
        return [method.name, cp.get(method.getReturnType().name), method.getReturnType()]
    }

    def fieldsInFakerJar(ClassPool cp) {
        return MyReflectUtil.getFakerFieldMethod()
                .collect { fakerMethod2Field(cp, it) }
                .collect { String mName, CtClass cc, Class c ->
            new FakerField(name: camelToDelimit(mName),
                    gens: getMethods(cc, c)
                            .collectMany { name, paramInfo -> toGenerator(name, paramInfo) })
        }
        .findAll { !it.gens.empty }
    }

    def class2Generator(Class c) {
        /*
           Triplet:
               value0: param name
               value1: param class
               value2: param default value
         */
        List<Triplet<String, Class, Object>> attrInfo = c.getDeclaredFields()
                .findAll { Modifier.isPublic(it.getModifiers()) }
                .collect {
            List<Annotation> annos = it.getAnnotations().toList()
            annos.retainAll { it instanceof DefaultString || it instanceof DefaultNumber }
            if (annos.size() != 1) {
                return new Triplet<>(it.name, it.getType(), null)
            }
            return new Triplet<>(it.name, it.getType(), annos[0].value())
        }

        def simpleName = c.simpleName
        return toGenerator(simpleName.substring(0, simpleName.length() - 3),
                attrInfo, true)
    }

    def buildInField(ClassPool cp) {
        return new FakerField(name: "built-in",
                gens: classesFromPkg(cp, "org.testd.fakerpp.core.engine.generator.builtin",
                        { Class c ->
                            c.getInterfaces().
                                    contains(org.testd.fakerpp.core.engine.generator.Generator.class) &&
                                    c.simpleName.endsWith("Gen")
                        }
                ).collectMany { ignore, c -> class2Generator(c) }
        )
    }

    List<FakerField> allFields(ClassPool cp) {
        return fieldsInFakerJar(cp) << buildInField(cp)
    }

    MarkupBuilder getXsdBuilder(Writer writer) {
        def xsd = new MarkupBuilder(writer)
        xsd.setDoubleQuotes(true)
        return xsd
    }

    // construct xsd with groovy markup template
    def formatField(FakerField field) {
        def writer = new StringWriter()
        def xsd = getXsdBuilder(writer)
        def type = "${field.name}Type".toString()


        xsd."xs:complexType"(name: type) {
            mkp.comment("${field.name} generators")
            // lazy xs:choice
            def choice = {
                "xs:choice" {
                    field.gens.each {
                        gen ->
                            if (gen.paramInfos == null || gen.paramInfos.empty) {
                                "xs:element"(type: "baseGenType", name: gen.name)
                                mkp.comment("${gen.name} generator")
                                return
                            }
                            "xs:element"(name: gen.name) {
                                mkp.comment("${gen.name} generator")
                                "xs:complexType" {
                                    "xs:complexContent" {
                                        "xs:extension"(base: "baseGenType") {
                                            gen.paramInfos.each {
                                                ParamInfo pInfo ->
                                                    switch (pInfo.type) {
                                                        case SupportParamType.LIST:
                                                            "xs:sequence" {
                                                                "xs:element"(name: pInfo.name, type: "optionsType")
                                                            }
                                                            break
                                                        default:
                                                            def attr = [type: pInfo.type.xsdType, name: pInfo.name]
                                                            if (pInfo.defaultValue == null) {
                                                                attr["use"] = "required"
                                                            } else {
                                                                attr["default"] = pInfo.defaultValue.toString()
                                                            }
                                                            "xs:attribute"(attr)
                                                    }
                                            }
                                        }
                                    }
                                }
                            }

                    }
                }
            }

            field.name == "built-in" ?
                    choice() :
                    "xs:complexContent"() {
                        "xs:extension"(base: "baseFakerFieldType") {
                            choice()
                        }
                    }

        }

        return writer.toString()
    }

    def registerFields(List<FakerField> fields) {
        def writer = new StringWriter()
        def xsd = getXsdBuilder(writer)
        xsd."xs:group"(name: "anyOneGenerator") {
            mkp.comment("register all generators")
            "xs:choice" {
                fields.each {
                    "xs:element"(name: it.name, type: "${it.name}Type")
                }
            }
        }

        return writer.toString()
    }
}
