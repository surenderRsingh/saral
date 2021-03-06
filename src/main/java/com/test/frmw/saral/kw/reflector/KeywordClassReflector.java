package com.test.frmw.saral.kw.reflector;

import com.test.frmw.saral.annotations.FrameworkKeyword;
import com.test.frmw.saral.annotations.Param;
import com.test.frmw.saral.exceptions.ClassNotOfAutomationKeywordTypeException;
import com.test.frmw.saral.exceptions.DuplicateParameterException;
import com.test.frmw.saral.kw.AutomationKeyword;
import com.test.frmw.saral.kw.KeywordDefinition;
import com.test.frmw.saral.kw.parameters.ParameterDefinition;
import com.test.frmw.saral.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * KeywordClassReflector is used to reflect on the automation keyword class to generate Keyword Definition class
 */
public class KeywordClassReflector {

    private static final Logger logger = LoggerFactory.getLogger(KeywordClassReflector.class);

    private Class<AutomationKeyword> clazz;
    private FrameworkKeyword annotation;

    public KeywordClassReflector(final Class<? extends AutomationKeyword> classToProcess) {
        this.clazz = (Class<AutomationKeyword>) classToProcess;
        this.annotation = clazz.getAnnotation(FrameworkKeyword.class);
    }

    private String getKeywordName() {
        String className = clazz.getSimpleName();

        return annotation == null ?
                className :
                StringUtil.getNonEmptyValue(annotation.name(), className);
    }

    private String getKeywordDescription() {
        String className = clazz.getSimpleName();

        return annotation == null ?
                className :
                StringUtil.getNonEmptyValue(annotation.description(), className);
    }

    private Field[] getParamAnnotatedFields() {

        Stream<Field> fieldStream = Arrays.stream(this.clazz.getDeclaredFields()).filter(field ->
                field.getAnnotation(Param.class) != null);

        return fieldStream.toArray(Field[]::new);

    }

    private Set<ParameterDefinition> getParameterDefinitions() throws DuplicateParameterException {

        HashSet<ParameterDefinition> parameters = new HashSet<>();

        for (Field field : getParamAnnotatedFields()) {
            ParameterFieldReflector fieldReflector = new ParameterFieldReflector(field);
            if (!parameters.add(fieldReflector.getParameterDefinition())) {
                throw new DuplicateParameterException(
                        String.format("Duplicate parameters -- {%s} of class {%s} ", field.getName(), clazz.getName())
                );
            }
        }

        return parameters;


    }

    public KeywordDefinition getKeywordDefinition() throws DuplicateParameterException {

        logger.info("Processing class --  " + this.clazz.getName());

        KeywordDefinition kwDefinition = new KeywordDefinition();

        kwDefinition.setName(this.getKeywordName());
        kwDefinition.setDescription(this.getKeywordDescription());
        kwDefinition.setImplementingClass(this.clazz);
        kwDefinition.setParameterDetails(getParameterDefinitions());
        return kwDefinition;
    }


}
