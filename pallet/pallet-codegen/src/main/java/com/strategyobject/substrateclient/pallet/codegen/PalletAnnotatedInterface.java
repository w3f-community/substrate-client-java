package com.strategyobject.substrateclient.pallet.codegen;

import com.google.common.base.Strings;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.strategyobject.substrateclient.common.codegen.ProcessingException;
import com.strategyobject.substrateclient.common.codegen.ProcessorContext;
import com.strategyobject.substrateclient.pallet.annotation.Pallet;
import com.strategyobject.substrateclient.rpc.api.section.State;
import lombok.val;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

import static com.strategyobject.substrateclient.pallet.codegen.Constants.CLASS_NAME_TEMPLATE;
import static com.strategyobject.substrateclient.pallet.codegen.Constants.STATE;

public class PalletAnnotatedInterface {
    private final TypeElement interfaceElement;
    private final String name;
    private final PalletMethodProcessor methodProcessor;

    public PalletAnnotatedInterface(TypeElement interfaceElement, PalletMethodProcessor methodProcessor) throws ProcessingException {
        this.interfaceElement = interfaceElement;
        val annotation = interfaceElement.getAnnotation(Pallet.class);

        if (!interfaceElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(
                    interfaceElement,
                    "`%s` is not public. That is not allowed.",
                    interfaceElement.getQualifiedName().toString());
        }

        if (Strings.isNullOrEmpty(name = annotation.value())) {
            throw new ProcessingException(
                    interfaceElement,
                    "`@%s` of `%s` contains null or empty `value`.",
                    annotation.getClass().getSimpleName(),
                    interfaceElement.getQualifiedName().toString());
        }

        this.methodProcessor = methodProcessor;
    }

    public void generateClass(ProcessorContext context) throws ProcessingException, IOException {
        val interfaceName = interfaceElement.getSimpleName().toString();
        val className = String.format(CLASS_NAME_TEMPLATE, interfaceName);
        val packageName = context.getPackageName(interfaceElement);

        val typeSpecBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(TypeName.get(interfaceElement.asType()))
                .addField(State.class, STATE, Modifier.FINAL, Modifier.PRIVATE);

        val constructorBuilder = createConstructorBuilder();

        for (Element method : interfaceElement.getEnclosedElements()) {
            this.methodProcessor.process(name,
                    (ExecutableElement) method,
                    typeSpecBuilder,
                    constructorBuilder,
                    context);
        }

        typeSpecBuilder.addMethod(constructorBuilder.build());

        JavaFile.builder(packageName, typeSpecBuilder.build()).build().writeTo(context.getFiler());
    }

    private MethodSpec.Builder createConstructorBuilder() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(State.class, STATE)
                .beginControlFlow("if ($L == null)", STATE)
                .addStatement("throw new $T(\"$L can't be null.\")", IllegalArgumentException.class, STATE)
                .endControlFlow()
                .addStatement("this.$1L = $1L", STATE);
    }
}